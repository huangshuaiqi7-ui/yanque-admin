package cn.yanque.commons.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.utils.RedisUtils;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Locale;

/**
 * 请求签名校验拦截器，用于防止请求参数被篡改和请求被重复提交。
 */
@Component
public class SignInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;

    public SignInterceptor(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // SseEmitter 会触发 Servlet 异步分发，这不是前端发起的新请求，
        // 不能再次消费同一个 nonce，否则会被误判为重复提交。
        if (DispatcherType.ASYNC.equals(request.getDispatcherType())) {
            return true;
        }

        // 1:获取了前端提交过来的参数信息. x-nonce, timestamp, sign
        String timestampText = request.getHeader(JwtConstants.SIGN_TIMESTAMP_HEADER);
        String nonce = request.getHeader(JwtConstants.SIGN_NONCE_HEADER);
        String frontendSign = request.getHeader(JwtConstants.SIGN_HEADER);
        //2:  对上述三个参数进行判断.
        if (StrUtil.hasBlank(timestampText, nonce, frontendSign)) {
            throw BusinessException.of(CommonErrorCode.SIGN_HEADER_MISSING);
        }

        //3: 时间戳使用前端 Date.now() 生成的毫秒值，允许前后五分钟的时钟偏差。
        //3.1 前端提交的时间,转换成毫秒值.
        long timestamp = parseTimestamp(timestampText);
        // 3.2 获取当前系统时间
        long now = System.currentTimeMillis();

        long validMillis = JwtConstants.SIGN_VALID_DURATION.toMillis();
        //超过5分钟过期.
        if (Math.abs(now - timestamp) > validMillis) {
            throw BusinessException.of(CommonErrorCode.SIGN_REQUEST_EXPIRED);
        }

        // 4: 从ThreadLocal当中获取用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        boolean studentRequest = request.getServletPath().startsWith("/student/");
        String sessionId = UserContext.getSessionId();
        if (!studentRequest && StrUtil.isBlank(sessionId)) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }

        // 5:nonce 保存至“请求时间戳 + 五分钟”，SETNX 失败说明请求已经执行过。
        long nonceExpireTime = timestamp + validMillis;
        Duration nonceTtl = Duration.ofMillis(Math.max(1L, nonceExpireTime - now));
        String noncePrefix = studentRequest ? JwtConstants.STUDENT_SIGN_NONCE_KEY_PREFIX
                : JwtConstants.SIGN_NONCE_KEY_PREFIX;
        String nonceKey = studentRequest ? noncePrefix + userId + ":" + nonce
                : noncePrefix + userId + ":" + sessionId + ":" + nonce;
        if (!Boolean.TRUE.equals(redisUtils.setIfAbsent(nonceKey, "1", nonceTtl))) {
            throw BusinessException.of(CommonErrorCode.SIGN_NONCE_REPEATED);
        }

        // 6: 获取用户对应的签名密钥
        String secretPrefix = studentRequest ? JwtConstants.STUDENT_SIGN_SECRET_KEY_PREFIX
                : JwtConstants.SIGN_SECRET_KEY_PREFIX;
        String secretKey = studentRequest ? secretPrefix + userId : secretPrefix + userId + ":" + sessionId;
        String signSecret = redisUtils.get(secretKey);
        if (StrUtil.isBlank(signSecret)) {
            throw BusinessException.of(CommonErrorCode.SIGN_SECRET_NOT_FOUND);
        }

        // query 使用原始查询字符串，不包含问号；没有查询参数时使用空字符串。
        String query = StrUtil.nullToDefault(request.getQueryString(), StrUtil.EMPTY);
        String source = String.join("\n",
                request.getMethod().toUpperCase(Locale.ROOT),
                request.getRequestURI(),
                query,
                timestampText,
                nonce);
        //对签名进行HmacSHA256计算，并转为16进制字符串。
        HMac hmac = SecureUtil.hmac(HmacAlgorithm.HmacSHA256,
                signSecret.getBytes(StandardCharsets.UTF_8));
        String backendSign = hmac.digestHex(source);
        // 7: 验证签名
        if (!constantTimeEquals(frontendSign, backendSign)) {
            throw BusinessException.of(CommonErrorCode.SIGN_INVALID);
        }
        return true;
    }

    private long parseTimestamp(String timestampText) {
        try {
            return Long.parseLong(timestampText);
        } catch (NumberFormatException exception) {
            throw BusinessException.of(CommonErrorCode.SIGN_TIMESTAMP_INVALID);
        }
    }

    /**
     * 使用恒定时间比较，降低签名比较过程中的时序攻击风险。
     */
    private boolean constantTimeEquals(String frontendSign, String backendSign) {
        byte[] frontendBytes = frontendSign.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        byte[] backendBytes = backendSign.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(frontendBytes, backendBytes);
    }
}
