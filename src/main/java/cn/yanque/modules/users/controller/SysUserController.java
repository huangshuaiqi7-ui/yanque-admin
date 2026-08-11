package cn.yanque.modules.users.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.annotation.RequireAuth;
import cn.yanque.modules.users.pojo.vo.reqvo.LoginReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserCreateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserPageReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserUpdateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserRoleAssignReq;
import cn.yanque.modules.users.pojo.vo.resvo.LoginRes;
import cn.yanque.modules.users.pojo.vo.resvo.UserDetailRes;
import cn.yanque.modules.users.service.SysUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName SysUserController
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description SysUserController.控制器
 */

@RestController
@Slf4j // @RequestMapping("/api/sysUser")
@RequestMapping("/api/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     * @param req
     * @return username password
     *  如果前端传递的form-data数据:  username=lisi&password=21  --->直接封装到 LoginReq当中. 不需要@RequestBody 这个注解.
     *  如果前端传递的json数据:  {username:"lisi",password:"21"}  ---> 需要 @RequestBody 这个注解.
     */

    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.success(sysUserService.login(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        sysUserService.logout();
        return ApiResponse.success();
    }

  // @permessionAnno(role ="admin", permission = "sysUser:page")
    @GetMapping
    @RequireAuth(permissions = "api:user:page")
    public ApiResponse<PageResult<UserDetailRes>> page(@Valid UserPageReq req) {
        return ApiResponse.success(sysUserService.page(req));
    }

    @GetMapping("/{id}")
    @RequireAuth(permissions = "api:user:detail")
    public ApiResponse<UserDetailRes> detail(@PathVariable Long id) {
        return ApiResponse.success(sysUserService.detail(id));
    }

    @PostMapping
    @RequireAuth(permissions = "api:user:create")
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateReq req) {
        return ApiResponse.success(sysUserService.create(req));
    }

    @PutMapping("/{id}")
    @RequireAuth(permissions = "api:user:update")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody UserUpdateReq req) {
        sysUserService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @RequireAuth(permissions = "api:user:delete222")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/roles")
    @RequireAuth(permissions = "api:user:assign-role")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                         @Valid @RequestBody UserRoleAssignReq req) {
        sysUserService.assignRoles(id, req);
        return ApiResponse.success();
    }
}
