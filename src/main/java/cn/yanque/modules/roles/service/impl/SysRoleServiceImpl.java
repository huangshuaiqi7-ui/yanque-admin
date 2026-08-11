package cn.yanque.modules.roles.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.service.RbacAuthService;
import cn.yanque.modules.permissions.mapper.SysPermissionMapper;
import cn.yanque.modules.permissions.pojo.entity.SysPermissionEntity;
import cn.yanque.modules.permissions.pojo.vo.resvo.PermissionTreeRes;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import cn.yanque.modules.roles.pojo.entity.SysRoleEntity;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePageReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePermissionAssignReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RoleSaveReq;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleDetailRes;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleRes;
import cn.yanque.modules.roles.service.SysRoleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final RbacAuthService rbacAuthService;

    public SysRoleServiceImpl(SysRoleMapper roleMapper, SysPermissionMapper permissionMapper,
                              RbacAuthService rbacAuthService) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rbacAuthService = rbacAuthService;
    }

    @Override
    public PageResult<RoleRes> page(RolePageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysRoleEntity> roles = roleMapper.selectPage(req.getKeyword(), req.getRoleCode(),
                req.getRoleName(), req.getStatus());
        PageInfo<SysRoleEntity> pageInfo = new PageInfo<>(roles);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(),
                roles.stream().map(this::toRoleRes).toList());
    }

    @Override
    public RoleDetailRes detail(Long id) {
        SysRoleEntity role = getRole(id);
        Set<Long> assignedIds = new HashSet<>(roleMapper.selectPermissionIds(id));
        RoleDetailRes result = new RoleDetailRes();
        copyRole(role, result);
        result.setPermissionIds(new ArrayList<>(assignedIds));
        result.setPermissions(buildPermissionTree(permissionMapper.selectAll(), assignedIds));
        return result;
    }

    @Override
    @Transactional
    public Long create(RoleSaveReq req) {
        checkRoleCode(req.getRoleCode(), null);
        SysRoleEntity role = toEntity(req);
        if (roleMapper.insert(role) != 1) {
            throw BusinessException.of(CommonErrorCode.ROLE_OPERATION_FAILED);
        }
        if (req.getPermissionIds() != null) {
            replacePermissions(role.getId(), req.getPermissionIds());
        }
        return role.getId();
    }

    @Override
    @Transactional
    public void update(Long id, RoleSaveReq req) {
        getRole(id);
        List<Long> affectedUserIds = roleMapper.selectUserIdsByRoleId(id);
        checkRoleCode(req.getRoleCode(), id);
        SysRoleEntity role = toEntity(req);
        role.setId(id);
        if (roleMapper.updateById(role) != 1) {
            throw BusinessException.of(CommonErrorCode.ROLE_OPERATION_FAILED);
        }
        if (req.getPermissionIds() != null) {
            replacePermissions(id, req.getPermissionIds());
        }
        rbacAuthService.invalidateLogins(affectedUserIds);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getRole(id);
        List<Long> affectedUserIds = roleMapper.selectUserIdsByRoleId(id);
        roleMapper.deleteUserRoles(id);
        roleMapper.deleteRolePermissions(id);
        if (roleMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.ROLE_OPERATION_FAILED);
        }
        rbacAuthService.invalidateLogins(affectedUserIds);
    }

    @Override
    @Transactional
    public void assignPermissions(Long id, RolePermissionAssignReq req) {
        getRole(id);
        replacePermissions(id, req.getPermissionIds());
        rbacAuthService.evictUsers(roleMapper.selectUserIdsByRoleId(id));
    }

    private void replacePermissions(Long roleId, List<Long> requestedIds) {
        List<Long> permissionIds = requestedIds.stream()
                .filter(Objects::nonNull).distinct().toList();
        if (permissionIds.size() != requestedIds.size()
                || (!permissionIds.isEmpty() && permissionMapper.countByIds(permissionIds) != permissionIds.size())) {
            throw BusinessException.of(CommonErrorCode.ROLE_PERMISSION_INVALID);
        }
        roleMapper.deleteRolePermissions(roleId);
        if (!permissionIds.isEmpty()) {
            roleMapper.insertRolePermissions(roleId, permissionIds);
        }
    }

    private SysRoleEntity getRole(Long id) {
        SysRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw BusinessException.of(CommonErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void checkRoleCode(String roleCode, Long currentId) {
        SysRoleEntity existing = roleMapper.selectByCode(roleCode);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw BusinessException.of(CommonErrorCode.ROLE_CODE_EXISTS);
        }
    }

    private SysRoleEntity toEntity(RoleSaveReq req) {
        SysRoleEntity role = new SysRoleEntity();
        role.setRoleCode(req.getRoleCode());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setStatus(StrUtil.blankToDefault(req.getStatus(), CommonStatusEnum.ACTIVE.name()));
        return role;
    }

    private RoleRes toRoleRes(SysRoleEntity role) {
        RoleRes result = new RoleRes();
        copyRole(role, result);
        return result;
    }

    private void copyRole(SysRoleEntity role, RoleRes result) {
        result.setId(role.getId());
        result.setRoleCode(role.getRoleCode());
        result.setRoleName(role.getRoleName());
        result.setDescription(role.getDescription());
        result.setStatus(role.getStatus());
        result.setStatusDesc(CommonStatusEnum.getDescription(role.getStatus()));
        result.setCreatedAt(role.getCreatedAt());
        result.setUpdatedAt(role.getUpdatedAt());
    }

    private List<PermissionTreeRes> buildPermissionTree(List<SysPermissionEntity> permissions,
                                                        Set<Long> assignedIds) {
        Map<Long, PermissionTreeRes> nodeMap = permissions.stream()
                .map(permission -> toTreeNode(permission, assignedIds))
                .collect(Collectors.toMap(PermissionTreeRes::getId, Function.identity(), (a, b) -> a,
                        LinkedHashMap::new));
        List<PermissionTreeRes> roots = new ArrayList<>();
        for (PermissionTreeRes node : nodeMap.values()) {
            PermissionTreeRes parent = nodeMap.get(node.getParentId());
            if (node.getParentId() == null || node.getParentId() == 0L || parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private PermissionTreeRes toTreeNode(SysPermissionEntity permission, Set<Long> assignedIds) {
        PermissionTreeRes node = new PermissionTreeRes();
        node.setId(permission.getId());
        node.setParentId(permission.getParentId());
        node.setPermissionCode(permission.getPermissionCode());
        node.setPermissionName(permission.getPermissionName());
        node.setPermissionType(permission.getPermissionType());
        node.setApiPath(permission.getApiPath());
        node.setSortNum(permission.getSortNum());
        node.setStatus(permission.getStatus());
        node.setAssigned(assignedIds.contains(permission.getId()));
        return node;
    }
}
