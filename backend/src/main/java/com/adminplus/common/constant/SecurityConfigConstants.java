package com.adminplus.common.constant;

import java.time.Duration;

/**
 * 安全配置常量
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface SecurityConfigConstants {

    /**
     * JWT Token 类型前缀（HTTP Authorization Header）
     */
    String BEARER_PREFIX = "Bearer ";

    /**
     * HSTS 最大缓存时间（1 年）
     */
    long HSTS_MAX_AGE_SECONDS = Duration.ofDays(365).toSeconds();

    /**
     * CORS 预检请求缓存时间（1 小时）
     */
    long CORS_MAX_AGE_SECONDS = Duration.ofHours(1).toSeconds();
}