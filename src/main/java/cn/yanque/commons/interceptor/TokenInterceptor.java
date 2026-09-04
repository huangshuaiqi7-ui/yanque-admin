package cn.yanque.commons.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.utils.BearMcpRequestUtils;
import cn.yanque.commons.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录 Token 校验拦截器。
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;

    public TokenInterceptor(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (BearMcpRequestUtils.isTrustedBearMcpRequest(request)) {
            return true;
        }

        //获取前端传递的token
        String token = getToken(request);
        // 验证token,是否为空
        if (StrUtil.isBlank(token)) {
            throw BusinessException.of(CommonErrorCode.TOKEN_NOT_FOUND);
        }

        // 解析token,验证token
        JWT jwt = parseAndVerifyToken(token);
        boolean studentRequest = request.getServletPath().startsWith("/student/");
        String subjectType = String.valueOf(jwt.getPayload(JwtConstants.JWT_CLAIM_SUBJECT_TYPE));
        if ((studentRequest && !JwtConstants.JWT_SUBJECT_STUDENT.equals(subjectType))
                || (!studentRequest && JwtConstants.JWT_SUBJECT_STUDENT.equals(subjectType))) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        // 验证token是否过期
        long expireTime = getLongClaim(jwt, JwtConstants.JWT_CLAIM_EXPIRE_TIME);
        if (System.currentTimeMillis() >= expireTime) {
            throw BusinessException.of(CommonErrorCode.TOKEN_EXPIRED);
        }

        // 获取当前用户 id
        Long userId = getLongClaim(jwt, JwtConstants.JWT_CLAIM_USER_ID);
        String sessionId = getStringClaim(jwt, JwtConstants.JWT_CLAIM_ID);
        // 多端登录时，每一次登录都有自己的 jti，会话集合里存在这个 jti 才算登录态有效。
        String tokenKeyPrefix = studentRequest ? JwtConstants.STUDENT_JWT_TOKEN_KEY_PREFIX
                : JwtConstants.JWT_TOKEN_KEY_PREFIX;
        String redisTokenKey = studentRequest ? tokenKeyPrefix + userId : tokenKeyPrefix + userId + ":" + sessionId;
        String redisToken = redisUtils.get(redisTokenKey);
        if (StrUtil.isBlank(redisToken) || !token.equals(redisToken)) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        if (!studentRequest
                && !Boolean.TRUE.equals(redisUtils.isSetMember(JwtConstants.JWT_SESSION_KEY_PREFIX + userId, sessionId))) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }

        // 存入ThreadLocal
        UserContext.setUserId(userId);
        UserContext.setSessionId(sessionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader(JwtConstants.AUTHORIZATION_HEADER);
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        if (authorization.startsWith(JwtConstants.BEARER_PREFIX)) {
            return authorization.substring(JwtConstants.BEARER_PREFIX.length()).trim();
        }
        return authorization.trim();
    }

    private JWT parseAndVerifyToken(String token) {
        try {
            if (!JWTUtil.verify(token, JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8))) {
                throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
            }
            return JWTUtil.parseToken(token);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
    }

    private Long getLongClaim(JWT jwt, String claimName) {
        try {
            Object claimValue = jwt.getPayload(claimName);
            if (claimValue == null) {
                throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
            }
            return Long.valueOf(claimValue.toString());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
    }

    private String getStringClaim(JWT jwt, String claimName) {
        Object claimValue = jwt.getPayload(claimName);
        if (claimValue == null || StrUtil.isBlank(claimValue.toString())) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        return claimValue.toString();
    }
}
