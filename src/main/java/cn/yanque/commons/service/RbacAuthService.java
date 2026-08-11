package cn.yanque.commons.service;

import cn.yanque.commons.constant.RbacConstants;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.utils.RedisUtils;
import cn.yanque.modules.permissions.mapper.SysPermissionMapper;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 用户角色和权限的查询、缓存及缓存失效服务。
 */
@Service
public class RbacAuthService {
    private final RedisUtils redisUtils;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    public RbacAuthService(RedisUtils redisUtils, SysRoleMapper roleMapper,
                           SysPermissionMapper permissionMapper) {
        this.redisUtils = redisUtils;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public boolean hasRole(Long userId, String roleCode) {
        String key = roleKey(userId);
        ensureRolesCached(userId, key);
        return Boolean.TRUE.equals(redisUtils.isSetMember(key, roleCode));
    }

    public boolean hasAnyRole(Long userId, String[] roleCodes) {
        for (String roleCode : roleCodes) {
            if (hasRole(userId, roleCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyPermission(Long userId, String[] permissionCodes) {
        String key = permissionKey(userId);
        ensurePermissionsCached(userId, key);
        for (String permissionCode : permissionCodes) {
            if (Boolean.TRUE.equals(redisUtils.isSetMember(key, permissionCode))) {
                return true;
            }
        }
        return false;
    }

    public void evictUser(Long userId) {
        redisUtils.delete(List.of(roleKey(userId), permissionKey(userId)));
    }

    /**
     * 删除用户登录会话和授权缓存，使旧 Token 立即失效。
     */
    public void invalidateLogin(Long userId) {
        redisUtils.delete(List.of(
                JwtConstants.JWT_TOKEN_KEY_PREFIX + userId,
                JwtConstants.SIGN_SECRET_KEY_PREFIX + userId,
                roleKey(userId),
                permissionKey(userId)
        ));
    }

    public void evictUsers(Collection<Long> userIds) {
        userIds.forEach(this::evictUser);
    }

    public void invalidateLogins(Collection<Long> userIds) {
        userIds.forEach(this::invalidateLogin);
    }

    private void ensureRolesCached(Long userId, String key) {
        if (!Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            cacheSet(key, roleMapper.selectRoleCodesByUserId(userId));
        }
    }

    private void ensurePermissionsCached(Long userId, String key) {
        if (!Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            cacheSet(key, permissionMapper.selectPermissionCodesByUserId(userId));
        }
    }

    private void cacheSet(String key, List<String> values) {
        String[] cacheValues = values.isEmpty()
                ? new String[]{RbacConstants.EMPTY_CACHE_VALUE}
                : values.toArray(String[]::new);
        redisUtils.addToSet(key, cacheValues);
        redisUtils.expire(key, RbacConstants.AUTH_CACHE_TTL);
    }

    private String roleKey(Long userId) {
        return RbacConstants.USER_ROLE_KEY_PREFIX + userId;
    }

    private String permissionKey(Long userId) {
        return RbacConstants.USER_PERMISSION_KEY_PREFIX + userId;
    }
}
