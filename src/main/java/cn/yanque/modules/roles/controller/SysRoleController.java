package cn.yanque.modules.roles.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.annotation.RequireAuth;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePageReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RolePermissionAssignReq;
import cn.yanque.modules.roles.pojo.vo.reqvo.RoleSaveReq;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleDetailRes;
import cn.yanque.modules.roles.pojo.vo.resvo.RoleRes;
import cn.yanque.modules.roles.service.SysRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sysRole")
public class SysRoleController {
    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping
    @RequireAuth(permissions = "api:role:page")
    public ApiResponse<PageResult<RoleRes>> page(@Valid RolePageReq req) {
        return ApiResponse.success(sysRoleService.page(req));
    }

    @GetMapping("/{id}")
    @RequireAuth(permissions = "api:role:detail")
    public ApiResponse<RoleDetailRes> detail(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.detail(id));
    }

    @PostMapping
    @RequireAuth(permissions = "api:role:create")
    public ApiResponse<Long> create(@Valid @RequestBody RoleSaveReq req) {
        return ApiResponse.success(sysRoleService.create(req));
    }

    @PutMapping("/{id}")
    @RequireAuth(permissions = "api:role:update")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveReq req) {
        sysRoleService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequireAuth(permissions = "api:role:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/permissions")
    @RequireAuth(permissions = "api:role:assign-permission")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id,
                                               @Valid @RequestBody RolePermissionAssignReq req) {
        sysRoleService.assignPermissions(id, req);
        return ApiResponse.success();
    }
}
