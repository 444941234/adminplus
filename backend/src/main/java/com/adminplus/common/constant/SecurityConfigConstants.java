package com.adminplus.common.constant;

import java.time.Duration;

/**
 * 安全配置常量
 * <p>
 * JWT、HSTS、CORS 等安全相关配置
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface SecurityConfigConstants {

    // ==================== JWT 相关 ====================

    /**
     * RSA 密钥最小位数（符合 NIST 推荐）
     */
    int MIN_RSA_KEY_SIZE = 2048;

    /**
     * JWT 权限前缀
     */
    String ROLE_PREFIX = "ROLE_";

    /**
     * JWT 权限声明名称
     */
    String AUTHORITIES_CLAIM_NAME = "scope";

    /**
     * JWT Token 类型前缀（HTTP Authorization Header）
     */
    String BEARER_PREFIX = "Bearer ";

    /**
     * 默认用户角色（无角色时使用）
     */
    String DEFAULT_SCOPE = "USER";

    // ==================== HSTS 相关 ====================

    /**
     * HSTS 最大缓存时间（1 年）
     */
    long HSTS_MAX_AGE_SECONDS = Duration.ofDays(365).toSeconds();

    // ==================== CORS 相关 ====================

    /**
     * CORS 预检请求缓存时间（1 小时）
     */
    long CORS_MAX_AGE_SECONDS = Duration.ofHours(1).toSeconds();
}
