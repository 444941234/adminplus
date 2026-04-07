package com.adminplus.constants;

/**
 * Redis 相关常量
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public final class RedisConstants {

    private RedisConstants() {
        // 防止实例化
    }

    /**
     * 验证码缓存键前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
}
