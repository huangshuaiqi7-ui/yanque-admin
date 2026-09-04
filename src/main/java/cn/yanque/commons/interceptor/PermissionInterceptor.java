package cn.yanque.commons.interceptor;

import cn.yanque.commons.annotation.RequireAuth;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.constant.RbacConstants;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.service.RbacAuthService;
import cn.yanque.commons.utils.BearMcpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * RBAC 权限校验拦截器，在 Token 校验之后、请求签名校验之前执行。
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private final RbacAuthService rbacAuthService;

    public PermissionInterceptor(RbacAuthService rbacAuthService) {
        this.rbacAuthService = rbacAuthService;
    }

    /**
     * 拦截器方法，在 Token 校验之后、请求签名校验之前执行。
     *
     * preHandle 拦截器中preHandle方法, 在到达控制器之前就拦截， true. 放行, 返回false则不执行控制器方法。
     *
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (BearMcpRequestUtils.isTrustedBearMcpRequest(request)) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        //1: 获取了标注在方法上注解信息.
        RequireAuth requireAuth = findRequireAuth(handlerMethod);
        // 2: 如果没有注解，则直接返回 true. login, logout方法, 不需要权限校验,注解的值空, 直接放行.
        if (requireAuth == null) {
            return true;
        }

        // 从ThreadLocal当中获取用户ID
        Long userId = UserContext.getUserId();
        // 如果用户ID为空，则抛出异常.
        if (userId == null) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }

        // 超级管理员不受普通角色和权限限制。
        if (rbacAuthService.hasRole(userId, RbacConstants.SUPER_ADMIN_ROLE)) {
            return true;
        }

        // 判断登录的用户是否拥有角色信息.
        boolean roleAllowed = requireAuth.roles().length == 0
                || rbacAuthService.hasAnyRole(userId, requireAuth.roles());

        // 判断登录的用户是否拥有权限
        boolean permissionAllowed = requireAuth.permissions().length == 0
                || rbacAuthService.hasAnyPermission(userId, requireAuth.permissions());

        // 没有角色, 也没有权限, 直接拒绝.抛出异常信息.
        if (!roleAllowed || !permissionAllowed) {
            throw BusinessException.of(CommonErrorCode.FORBIDDEN);
        }

        return true;
    }

    /*
     * 获取方法或者类上的 RequireAuth 注解
     */
    private RequireAuth findRequireAuth(HandlerMethod handlerMethod) {
        RequireAuth annotation = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireAuth.class);
        if (annotation != null) {
            return annotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequireAuth.class);
    }
}
