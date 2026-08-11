package cn.yanque.commons.interceptor;
import cn.hutool.core.util.StrUtil; import cn.hutool.crypto.SecureUtil; import cn.hutool.crypto.digest.*;
import cn.yanque.commons.apires.CommonErrorCode; import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.exception.BusinessException; import cn.yanque.commons.utils.RedisUtils;
import jakarta.servlet.http.*; import org.springframework.stereotype.Component; import org.springframework.web.servlet.HandlerInterceptor;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.time.Duration; import java.util.Locale;
@Component public class PendingPayInterceptor implements HandlerInterceptor {
    private final RedisUtils redis;public PendingPayInterceptor(RedisUtils redis){this.redis=redis;}
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        String token=request.getHeader(JwtConstants.PENDING_PAY_TOKEN_HEADER);
        String timestampText=request.getHeader(JwtConstants.SIGN_TIMESTAMP_HEADER),nonce=request.getHeader(JwtConstants.SIGN_NONCE_HEADER),frontendSign=request.getHeader(JwtConstants.SIGN_HEADER);
        if(StrUtil.hasBlank(token,timestampText,nonce,frontendSign))throw BusinessException.of(CommonErrorCode.SIGN_HEADER_MISSING);
        if(StrUtil.isBlank(redis.get(JwtConstants.PENDING_PAY_TOKEN_KEY_PREFIX+token)))throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        long timestamp;try{timestamp=Long.parseLong(timestampText);}catch(NumberFormatException e){throw BusinessException.of(CommonErrorCode.SIGN_TIMESTAMP_INVALID);}
        long now=System.currentTimeMillis(),valid=JwtConstants.SIGN_VALID_DURATION.toMillis();if(Math.abs(now-timestamp)>valid)throw BusinessException.of(CommonErrorCode.SIGN_REQUEST_EXPIRED);
        Duration ttl=Duration.ofMillis(Math.max(1,timestamp+valid-now));String nonceKey=JwtConstants.PENDING_PAY_NONCE_KEY_PREFIX+token+":"+nonce;
        if(!Boolean.TRUE.equals(redis.setIfAbsent(nonceKey,"1",ttl)))throw BusinessException.of(CommonErrorCode.SIGN_NONCE_REPEATED);
        String secret=redis.get(JwtConstants.PENDING_PAY_SECRET_KEY_PREFIX+token);if(StrUtil.isBlank(secret))throw BusinessException.of(CommonErrorCode.SIGN_SECRET_NOT_FOUND);
        String source=String.join("\n",request.getMethod().toUpperCase(Locale.ROOT),request.getRequestURI(),StrUtil.nullToDefault(request.getQueryString(),""),timestampText,nonce);
        HMac hmac=SecureUtil.hmac(HmacAlgorithm.HmacSHA256,secret.getBytes(StandardCharsets.UTF_8));String backend=hmac.digestHex(source);
        if(!MessageDigest.isEqual(frontendSign.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),backend.getBytes(StandardCharsets.UTF_8)))throw BusinessException.of(CommonErrorCode.SIGN_INVALID);
        return true;
    }
}
