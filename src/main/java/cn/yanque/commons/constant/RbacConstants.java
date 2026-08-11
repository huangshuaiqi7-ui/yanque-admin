package cn.yanque.commons.constant;

import java.time.Duration;

public final class RbacConstants {
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    public static final String USER_ROLE_KEY_PREFIX = "yanque:rbac:roles:";
    public static final String USER_PERMISSION_KEY_PREFIX = "yanque:rbac:permissions:";
    public static final String EMPTY_CACHE_VALUE = "__EMPTY__";
    public static final Duration AUTH_CACHE_TTL = Duration.ofMinutes(30);

    private RbacConstants() {
    }
}
