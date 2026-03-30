package com.adminplus.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web 请求工具类
 * <p>
 * 提供 Web 层通用的请求相关方法
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class WebUtils {

    private WebUtils() {
        // 工具类不允许实例化
    }

    /**
     * 获取当前 HTTP 请求
     * <p>
     * 从 RequestContextHolder 获取当前请求上下文
     * </p>
     *
     * @return 当前请求，如果不在请求上下文中则返回 null
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前请求的客户端 IP
     *
     * @return 客户端 IP，如果不在请求上下文中则返回 "unknown"
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        return IpUtils.getClientIp(request);
    }

    /**
     * 获取当前请求的 URL
     *
     * @return 请求 URL，如果不在请求上下文中则返回 null
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURL().toString() : null;
    }

    /**
     * 获取当前请求的 URI
     *
     * @return 请求 URI，如果不在请求上下文中则返回 null
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取当前请求的 HTTP 方法
     *
     * @return HTTP 方法 (GET/POST/PUT/DELETE 等)，如果不在请求上下文中则返回 null
     */
    public static String getMethod() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getMethod() : null;
    }

    /**
     * 获取请求头
     *
     * @param name 请求头名称
     * @return 请求头值，如果不存在则返回 null
     */
    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * 判断当前是否在请求上下文中
     *
     * @return true 如果在请求上下文中
     */
    public static boolean isInRequestContext() {
        return RequestContextHolder.getRequestAttributes() != null;
    }
}