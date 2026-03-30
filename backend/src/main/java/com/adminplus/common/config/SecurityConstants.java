package com.adminplus.common.config;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 安全相关常量
 * <p>
 * 集中管理安全配置中的常量定义
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Component
public class SecurityConstants {

    // ==================== JWT 相关 ====================

    /**
     * RSA 密钥最小位数（符合 NIST 推荐）
     */
    public static final int MIN_RSA_KEY_SIZE = 2048;

    /**
     * JWT 权限前缀
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * JWT 权限声明名称
     */
    public static final String AUTHORITIES_CLAIM_NAME = "scope";

    // ==================== HSTS 相关 ====================

    /**
     * HSTS 最大缓存时间（1 年）
     */
    public static final long HSTS_MAX_AGE_SECONDS = Duration.ofDays(365).toSeconds();

    // ==================== CORS 相关 ====================

    /**
     * CORS 预检请求缓存时间（1 小时）
     */
    public static final long CORS_MAX_AGE_SECONDS = Duration.ofHours(1).toSeconds();

    // ==================== 公开端点 ====================

    /**
     * 无需认证的公开端点
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/v1/auth/login",
            "/v1/captcha/**",
            "/actuator/health"
    };

    /**
     * 认证端点（用于 CSRF 配置）
     */
    public static final String[] AUTH_ENDPOINTS = {
            "/v1/auth/login"
    };

    public SecurityConstants() {}
}
