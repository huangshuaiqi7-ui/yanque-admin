package cn.yanque.modules.users.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWTUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.utils.RedisUtils;
import cn.yanque.commons.service.RbacAuthService;
import cn.yanque.commons.context.UserContext;
import cn.yanque.modules.users.mapper.SysUserMapper;
import cn.yanque.modules.users.pojo.entity.SysUserEntity;
import cn.yanque.modules.users.pojo.vo.reqvo.LoginReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserCreateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserPageReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserUpdateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserRoleAssignReq;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import cn.yanque.modules.users.pojo.vo.resvo.LoginRes;
import cn.yanque.modules.users.pojo.vo.resvo.UserDetailRes;
import cn.yanque.modules.users.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * @ClassName SysUserServiceImpl
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description SysUserServiceImpl.服务实现类
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    private static final int SIGN_SECRET_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RbacAuthService rbacAuthService;

    @Override
    public LoginRes login(LoginReq req) {

        // (1) 校验用户名称和密码.( 学习了springboot 校验框架后, 可以省略不写)
        String username = req.getUsername();
        String password = req.getPassword();
        if (StrUtil.hasBlank(username, password)) {
            throw BusinessException.of(CommonErrorCode.USERNAME_OR_PASSWORD_NOT_NULL);
        }
        // (2) 调用Mapper层, 根据用户名称获取用户信息.
        SysUserEntity sysUserEntity = sysUserMapper.selectByUsername(username);
        // (3) 判断用户信息是否为空.
        if (sysUserEntity == null) {
            throw BusinessException.of(CommonErrorCode.USER_NOT_EXIST);
        }

        // (4) 密码校验.
        if (!password.equals(sysUserEntity.getPassword())) {
            throw BusinessException.of(CommonErrorCode.USER_NOT_EXIST);
        }

        // (5) 判断用户是否是 启用  | 禁用
        if (!CommonStatusEnum.ACTIVE.name().equals(sysUserEntity.getStatus())) {
            throw BusinessException.of(CommonErrorCode.USER_NOT_ACTIVE);
        }

        //  (6) 生成token, 存redis
        String sessionId = IdUtil.simpleUUID();
        String token = this.createToken(sysUserEntity, sessionId);
        String signSecret = this.createSign();

        // (7) 存入redis 中. 后台支持多端登录，所以一个用户会有多个 jti 会话。
        String userId = String.valueOf(sysUserEntity.getId());
        String sessionKey = JwtConstants.JWT_SESSION_KEY_PREFIX + userId;
        Set<String> oldSessions = redisUtils.setMembers(sessionKey);
        clearExpiredSessions(userId, sessionKey, oldSessions);
        redisUtils.addToSet(sessionKey, sessionId);
        redisUtils.expire(sessionKey, JwtConstants.LOGIN_TOKEN_TTL);
        redisUtils.set(JwtConstants.JWT_TOKEN_KEY_PREFIX + userId + ":" + sessionId, token, JwtConstants.LOGIN_TOKEN_TTL);
        redisUtils.set(JwtConstants.SIGN_SECRET_KEY_PREFIX + userId + ":" + sessionId, signSecret, JwtConstants.LOGIN_TOKEN_TTL);

        // (8) 拼接返回结果.
        LoginRes loginRes = new LoginRes();
        loginRes.setToken(token);
        loginRes.setSignSecret(signSecret);

        return loginRes;
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        String sessionId = UserContext.getSessionId();
        if (StrUtil.isBlank(sessionId)) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        rbacAuthService.invalidateSession(userId, sessionId);
    }

    @Override
    public PageResult<UserDetailRes> page(UserPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysUserEntity> users = sysUserMapper.selectPage(req.getKeyword(), req.getStatus(), req.getRoleCode());
        PageInfo<SysUserEntity> pageInfo = new PageInfo<>(users);
        List<UserDetailRes> records = users.stream().map(this::toDetailRes).toList();
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), records);
    }

    @Override
    public UserDetailRes detail(Long id) {
        UserDetailRes result = toDetailRes(getUser(id));
        result.setRoleIds(sysUserMapper.selectRoleIds(id));
        return result;
    }

    @Override
    @Transactional
    public Long create(UserCreateReq req) {
        if (sysUserMapper.selectByUsername(req.getUsername()) != null) {
            throw BusinessException.of(CommonErrorCode.USERNAME_ALREADY_EXISTS);
        }

        SysUserEntity user = new SysUserEntity();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setUnionId(req.getUnionId());
        user.setStatus(StrUtil.blankToDefault(req.getStatus(), CommonStatusEnum.ACTIVE.name()));
        if (sysUserMapper.insert(user) != 1) {
            throw BusinessException.of(CommonErrorCode.USER_OPERATION_FAILED);
        }
        return user.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UserUpdateReq req) {
        getUser(id);
        SysUserEntity user = new SysUserEntity();
        user.setId(id);
        user.setPassword(req.getPassword());
        user.setNickname(req.getNickname());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setUnionId(req.getUnionId());
        user.setStatus(req.getStatus());
        if (sysUserMapper.updateById(user) != 1) {
            throw BusinessException.of(CommonErrorCode.USER_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getUser(id);
        sysUserMapper.deleteUserRoles(id);
        if (sysUserMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.USER_OPERATION_FAILED);
        }
        rbacAuthService.invalidateLogin(id);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, UserRoleAssignReq req) {
        getUser(id);
        List<Long> roleIds = req.getRoleIds().stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (roleIds.size() != req.getRoleIds().size()
                || (!roleIds.isEmpty() && sysRoleMapper.countByIds(roleIds) != roleIds.size())) {
            throw BusinessException.of(CommonErrorCode.USER_ROLE_INVALID);
        }
        sysUserMapper.deleteUserRoles(id);
        if (!roleIds.isEmpty()) {
            sysUserMapper.insertUserRoles(id, roleIds);
        }
        rbacAuthService.invalidateLogin(id);
    }

    private SysUserEntity getUser(Long id) {
        SysUserEntity user = sysUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of(CommonErrorCode.USER_DETAIL_NOT_FOUND);
        }
        return user;
    }

    private UserDetailRes toDetailRes(SysUserEntity user) {
        UserDetailRes result = new UserDetailRes();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setRealName(user.getRealName());
        result.setPhone(user.getPhone());
        result.setEmail(user.getEmail());
        result.setUnionId(user.getUnionId());
        result.setStatus(user.getStatus());
        result.setStatusDesc(CommonStatusEnum.getDescription(user.getStatus()));
        result.setCreatedAt(user.getCreatedAt());
        result.setUpdatedAt(user.getUpdatedAt());
        return result;
    }

    private void clearExpiredSessions(String userId, String sessionKey, Set<String> oldSessions) {
        if (oldSessions == null || oldSessions.isEmpty()) {
            return;
        }
        for (String oldSessionId : oldSessions) {
            String tokenKey = JwtConstants.JWT_TOKEN_KEY_PREFIX + userId + ":" + oldSessionId;
            String secretKey = JwtConstants.SIGN_SECRET_KEY_PREFIX + userId + ":" + oldSessionId;
            if (StrUtil.isBlank(redisUtils.get(tokenKey)) || StrUtil.isBlank(redisUtils.get(secretKey))) {
                redisUtils.removeFromSet(sessionKey, oldSessionId);
            }
        }
    }


    //创建一个生成token方法
    private String createToken(SysUserEntity sysUserEntity, String sessionId) {
        Map<String, Object> map = new HashMap<>();
        map.put(JwtConstants.JWT_CLAIM_USER_ID, sysUserEntity.getId());
        map.put(JwtConstants.JWT_CLAIM_EXPIRE_TIME,
                System.currentTimeMillis() + JwtConstants.LOGIN_TOKEN_TTL.toMillis());
        map.put(JwtConstants.JWT_CLAIM_ID, sessionId);// jti: token的一个短名字.

        //使用hutool工具类, 生成token
        return JWTUtil.createToken(map, JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // 生成一个签名方法. 目的.
    private String createSign() {
        // 1:生成一个数组
        byte[] bytes = new byte[SIGN_SECRET_BYTE_LENGTH];
        // 2: 使用随机数,对bytes进行填充. Random,可预测.
        SECURE_RANDOM.nextBytes(bytes);
        // 3: 使用Base64进行编码.
        // Base64案例: 标准Base64：9r03Vn9-2kQ_zrO5cG7fL1s9jBx4dT8hQw==
        // getUrlEncoder();方法的作用:  /  +   --->  -  _
        // withoutPadding();  是把最后的==去掉.
        // encodeToString(); 将bytes进行翻译成String,返回.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static void main(String[] args) {
        String password = "123456";
        //彩虹表: 123456 -- > e10adc3949ba59abbe56e057f20f883e
        // 加盐:  123456+random - > 随机生成  [可解决问题. 但是需要每次都自己生成一个随机数. (盐)]

        String hashpw = BCrypt.hashpw(password);
        System.out.println(hashpw);
        // $2a$10$yG3jh5m4Mrss.cVFBNm/tO6SsdDXdm82z9tyo7.WUezmouLYNlh1W
        // $2a$10$t4RYVyKRQfGzXDee32LJP.YbAS1OcDv1eg.OyCe8tO340iseQNcgS

        //校验方法.
        boolean checkpw = BCrypt.checkpw(password, "$2a$10$yG3jh5m4Mrss.cVFBNm/tO6SsdDXdm82z9tyo7.WUezmouLYNlh1W");
        System.out.println(checkpw);
    }
}
