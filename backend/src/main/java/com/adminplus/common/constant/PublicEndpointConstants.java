package com.adminplus.common.constant;

/**
 * 公开 API 端点配置
 * <p>
 * 无需认证的公开端点和认证端点配置
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface PublicEndpointConstants {

    /**
     * 无需认证的公开端点
     * 注意：由于配置了 server.servlet.context-path=/api，Spring Security 匹配器不需要包含 /api 前缀
     * Spring Security 在 context-path 之后的路径上进行匹配
     */
    String[] PUBLIC_ENDPOINTS = {
            "/v1/auth/login",
            "/v1/captcha/**",
            "/v1/verify/**",
            "/v1/sys/cache/emergency-clear",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/actuator/health"
    };

    /**
     * 认证端点（用于 CSRF 配置）
     */
    String[] AUTH_ENDPOINTS = {
            "/v1/auth/login"
    };
}
