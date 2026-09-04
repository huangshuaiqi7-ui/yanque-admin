package cn.yanque.commons.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public final class BearMcpRequestUtils {
    public static final String HEADER = "X-Bear-Mcp-Request";

    private static final Set<String> LOOPBACK_ADDRESSES = Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

    private BearMcpRequestUtils() {
    }

    public static boolean isTrustedBearMcpRequest(HttpServletRequest request) {
        String marker = request.getHeader(HEADER);
        if (!"true".equalsIgnoreCase(StrUtil.trim(marker))) {
            return false;
        }
        return LOOPBACK_ADDRESSES.contains(request.getRemoteAddr());
    }
}
