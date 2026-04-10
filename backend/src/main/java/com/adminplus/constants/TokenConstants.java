package com.adminplus.constants;

import java.time.Duration;

/**
 * Token 相关常量
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface TokenConstants {

    // ==================== Token 过期时间 ====================

    /**
     * Access Token 过期时间（2小时）
     */
    Duration ACCESS_TOKEN_EXPIRATION = Duration.ofHours(2);

    /**
     * Refresh Token 过期时间（7天）
     */
    Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);
}