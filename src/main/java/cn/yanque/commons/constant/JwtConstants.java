package cn.yanque.commons.constant;

import java.time.Duration;

/**
 * JWT 登录认证相关常量。
 */
public final class JwtConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String LOGIN_PATH = "/api/sysUser/login";
    public static final String STUDENT_LOGIN_PATH = "/student/login";
    public static final String SIGN_TIMESTAMP_HEADER = "X-Timestamp";
    public static final String SIGN_NONCE_HEADER = "X-Nonce";
    public static final String SIGN_HEADER = "X-Sign";
    public static final String SIGN_NONCE_KEY_PREFIX = "yanque:sign:nonce:";
    public static final String JWT_SESSION_KEY_PREFIX = "yanque:jwt:sessions:";
    public static final String JWT_TOKEN_KEY_PREFIX = "yanque:jwt:token:";
    public static final String SIGN_SECRET_KEY_PREFIX = "yanque:sign:secret:";
    public static final String STUDENT_JWT_TOKEN_KEY_PREFIX = "yanque:student:jwt:token:";
    public static final String STUDENT_SIGN_SECRET_KEY_PREFIX = "yanque:student:sign:secret:";
    public static final String STUDENT_SIGN_NONCE_KEY_PREFIX = "yanque:student:sign:nonce:";
    public static final String PENDING_PAY_TOKEN_HEADER = "X-Pending-Pay-Token";
    public static final String PENDING_PAY_TOKEN_KEY_PREFIX = "yanque:pending-pay:token:";
    public static final String PENDING_PAY_SECRET_KEY_PREFIX = "yanque:pending-pay:secret:";
    public static final String PENDING_PAY_NONCE_KEY_PREFIX = "yanque:pending-pay:nonce:";
    public static final String JWT_SECRET = "yanque:jwt:token";
    public static final String JWT_CLAIM_USER_ID = "uid";
    public static final String JWT_CLAIM_EXPIRE_TIME = "expire_time";
    public static final String JWT_CLAIM_ID = "jti";
    public static final String JWT_CLAIM_SUBJECT_TYPE = "subject_type";
    public static final String JWT_SUBJECT_ADMIN = "ADMIN";
    public static final String JWT_SUBJECT_STUDENT = "STUDENT";
    public static final Duration LOGIN_TOKEN_TTL = Duration.ofHours(24);
    public static final Duration SIGN_VALID_DURATION = Duration.ofMinutes(5);
    public static final Duration PENDING_PAY_TOKEN_TTL = Duration.ofMinutes(30);

    private JwtConstants() {
    }
}
