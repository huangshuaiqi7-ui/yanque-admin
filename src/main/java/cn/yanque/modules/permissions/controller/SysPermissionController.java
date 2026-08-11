package cn.yanque.modules.permissions.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.annotation.RequireAuth;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionPageReq;
import cn.yanque.modules.permissions.pojo.vo.reqvo.PermissionSaveReq;
import cn.yanque.modules.permissions.pojo.vo.resvo.PermissionRes;
import cn.yanque.modules.permissions.service.SysPermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sysPermission")
public class SysPermissionController {
    private final SysPermissionService sysPermissionService;

    public SysPermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @GetMapping
    @RequireAuth(permissions = "api:permission:page")
    public ApiResponse<PageResult<PermissionRes>> page(@Valid PermissionPageReq req) {
        return ApiResponse.success(sysPermissionService.page(req));
    }

    @GetMapping("/{id}")
    @RequireAuth(permissions = "api:permission:detail")
    public ApiResponse<PermissionRes> detail(@PathVariable Long id) {
        return ApiResponse.success(sysPermissionService.detail(id));
    }

    @PostMapping
    @RequireAuth(permissions = "api:permission:create")
    public ApiResponse<Long> create(@Valid @RequestBody PermissionSaveReq req) {
        return ApiResponse.success(sysPermissionService.create(req));
    }

    @PutMapping("/{id}")
    @RequireAuth(permissions = "api:permission:update")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionSaveReq req) {
        sysPermissionService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequireAuth(permissions = "api:permission:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysPermissionService.delete(id);
        return ApiResponse.success();
    }
}
