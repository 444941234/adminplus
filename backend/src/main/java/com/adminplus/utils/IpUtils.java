package com.adminplus.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类
 * <p>
 * 统一处理客户端 IP 获取逻辑，支持代理服务器场景
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtils() {
        // 工具类不允许实例化
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 优先级：
     * <ol>
     *   <li>X-Forwarded-For 头（反向代理）</li>
     *   <li>X-Real-IP 头（Nginx）</li>
     *   <li>Proxy-Client-IP 头（Apache）</li>
     *   <li>WL-Proxy-Client-IP 头（WebLogic）</li>
     *   <li>HTTP_CLIENT_IP 头</li>
     *   <li>HTTP_X_FORWARDED_FOR 头</li>
     *   <li>request.getRemoteAddr()</li>
     * </ol>
     * </p>
     *
     * @param request HTTP 请求
     * @return 客户端 IP 地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");

        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理 X-Forwarded-For 多 IP 情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 处理本地访问
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IP;
        }

        return ip;
    }

    /**
     * 判断 IP 是否无效
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为内网 IP
     *
     * @param ip IP 地址
     * @return 是否为内网 IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 本地地址
        if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }

        // 10.0.0.0 - 10.255.255.255
        if (ip.startsWith("10.")) {
            return true;
        }

        // 172.16.0.0 - 172.31.255.255
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 192.168.0.0 - 192.168.255.255
        return ip.startsWith("192.168.");
    }
}