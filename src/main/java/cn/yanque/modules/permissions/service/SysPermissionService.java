package cn.yanque.modules.permissions.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionPageReq;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionSaveReq;
import cn.yanque.modules.permissions.pojo.vo.resvo.PermissionRes;

public interface SysPermissionService {
    PageResult<PermissionRes> page(PermissionPageReq req);
    PermissionRes detail(Long id);
    Long create(PermissionSaveReq req);
    void update(Long id, PermissionSaveReq req);
    void delete(Long id);
}
