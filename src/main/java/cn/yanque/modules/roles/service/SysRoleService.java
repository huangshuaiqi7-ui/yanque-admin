package cn.yanque.modules.roles.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePageReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePermissionAssignReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RoleSaveReq;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleDetailRes;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleRes;

public interface SysRoleService {
    PageResult<RoleRes> page(RolePageReq req);
    RoleDetailRes detail(Long id);
    Long create(RoleSaveReq req);
    void update(Long id, RoleSaveReq req);
    void delete(Long id);
    void assignPermissions(Long id, RolePermissionAssignReq req);
}
