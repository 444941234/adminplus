package com.adminplus.constants;

/**
 * Redis 缓存键常量
 * <p>
 * 所有 Redis 键前缀统一在此定义，便于管理和维护
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface CacheConstants {

    // ==================== 验证码相关 ====================

    /**
     * 验证码缓存键前缀
     */
    String CAPTCHA_KEY_PREFIX = "captcha:";

    // ==================== Token 相关 ====================

    /**
     * Token 黑名单键前缀
     */
    String TOKEN_BLACKLIST_KEY_PREFIX = "token:blacklist:";

    /**
     * 用户 Token 集合键前缀
     */
    String USER_TOKENS_KEY_PREFIX = "user:tokens:";

    // ==================== 限流相关 ====================

    /**
     * 限流计数键前缀
     */
    String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
}
