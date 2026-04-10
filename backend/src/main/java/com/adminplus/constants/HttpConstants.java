package com.adminplus.constants;

/**
 * HTTP 相关常量
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface HttpConstants {

    // ==================== HTTP 头 ====================

    /**
     * Authorization 请求头名称
     */
    String AUTHORIZATION_HEADER = "Authorization";

    // ==================== HTTP 状态码 ====================

    /**
     * HTTP 429: Too Many Requests（请求过多）
     */
    int HTTP_TOO_MANY_REQUESTS = 429;
}