package cn.yanque.modules.permissions.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.enums.PermissionTypeEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.service.RbacAuthService;
import cn.yanque.modules.permissions.mapper.SysPermissionMapper;
import cn.yanque.modules.permissions.pojo.entity.SysPermissionEntity;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionPageReq;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionSaveReq;
import cn.yanque.modules.permissions.pojo.vo.resvo.PermissionRes;
import cn.yanque.modules.permissions.service.SysPermissionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {
    private final SysPermissionMapper permissionMapper;
    private final RbacAuthService rbacAuthService;

    public SysPermissionServiceImpl(SysPermissionMapper permissionMapper, RbacAuthService rbacAuthService) {
        this.permissionMapper = permissionMapper;
        this.rbacAuthService = rbacAuthService;
    }

    @Override
    public PageResult<PermissionRes> page(PermissionPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysPermissionEntity> permissions = permissionMapper.selectPage(req.getKeyword(), req.getParentId(),
                req.getPermissionCode(), req.getPermissionName(), req.getPermissionType(), req.getStatus());
        PageInfo<SysPermissionEntity> pageInfo = new PageInfo<>(permissions);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(),
                permissions.stream().map(this::toRes).toList());
    }

    @Override
    public PermissionRes detail(Long id) {
        return toRes(getPermission(id));
    }

    @Override
    @Transactional
    public Long create(PermissionSaveReq req) {
        checkPermissionCode(req.getPermissionCode(), null);
        validateParent(req.getParentId(), null);
        validateApiPath(req);
        SysPermissionEntity permission = toEntity(req);
        if (permissionMapper.insert(permission) != 1) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_OPERATION_FAILED);
        }
        return permission.getId();
    }

    @Override
    @Transactional
    public void update(Long id, PermissionSaveReq req) {
        getPermission(id);
        List<Long> affectedUserIds = permissionMapper.selectUserIdsByPermissionId(id);
        checkPermissionCode(req.getPermissionCode(), id);
        validateParent(req.getParentId(), id);
        validateApiPath(req);
        SysPermissionEntity permission = toEntity(req);
        permission.setId(id);
        if (permissionMapper.updateById(permission) != 1) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_OPERATION_FAILED);
        }
        rbacAuthService.evictUsers(affectedUserIds);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getPermission(id);
        List<Long> affectedUserIds = permissionMapper.selectUserIdsByPermissionId(id);
        if (permissionMapper.countByParentId(id) > 0) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_HAS_CHILDREN);
        }
        permissionMapper.deleteRolePermissionsByPermissionId(id);
        if (permissionMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_OPERATION_FAILED);
        }
        rbacAuthService.evictUsers(affectedUserIds);
    }

    private SysPermissionEntity getPermission(Long id) {
        SysPermissionEntity permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_NOT_FOUND);
        }
        return permission;
    }

    private void checkPermissionCode(String permissionCode, Long currentId) {
        SysPermissionEntity existing = permissionMapper.selectByCode(permissionCode);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_CODE_EXISTS);
        }
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == 0L) {
            return;
        }
        Set<Long> visited = new HashSet<>();
        Long cursor = parentId;
        while (cursor != 0L) {
            if ((currentId != null && currentId.equals(cursor)) || !visited.add(cursor)) {
                throw BusinessException.of(CommonErrorCode.PERMISSION_PARENT_INVALID);
            }
            SysPermissionEntity parent = permissionMapper.selectById(cursor);
            if (parent == null) {
                throw BusinessException.of(CommonErrorCode.PERMISSION_PARENT_NOT_FOUND);
            }
            cursor = parent.getParentId();
        }
    }

    private void validateApiPath(PermissionSaveReq req) {
        if (PermissionTypeEnum.API.name().equals(req.getPermissionType()) && StrUtil.isBlank(req.getApiPath())) {
            throw BusinessException.of(CommonErrorCode.PERMISSION_API_PATH_REQUIRED);
        }
    }

    private SysPermissionEntity toEntity(PermissionSaveReq req) {
        SysPermissionEntity permission = new SysPermissionEntity();
        permission.setParentId(req.getParentId());
        permission.setPermissionCode(req.getPermissionCode());
        permission.setPermissionName(req.getPermissionName());
        permission.setPermissionType(req.getPermissionType());
        permission.setApiPath(req.getApiPath());
        permission.setSortNum(req.getSortNum());
        permission.setDescription(req.getDescription());
        permission.setStatus(StrUtil.blankToDefault(req.getStatus(), CommonStatusEnum.ACTIVE.name()));
        return permission;
    }

    private PermissionRes toRes(SysPermissionEntity permission) {
        PermissionRes result = new PermissionRes();
        result.setId(permission.getId());
        result.setParentId(permission.getParentId());
        result.setPermissionCode(permission.getPermissionCode());
        result.setPermissionName(permission.getPermissionName());
        result.setPermissionType(permission.getPermissionType());
        result.setApiPath(permission.getApiPath());
        result.setSortNum(permission.getSortNum());
        result.setDescription(permission.getDescription());
        result.setStatus(permission.getStatus());
        result.setStatusDesc(CommonStatusEnum.getDescription(permission.getStatus()));
        result.setCreatedAt(permission.getCreatedAt());
        result.setUpdatedAt(permission.getUpdatedAt());
        return result;
    }
}
