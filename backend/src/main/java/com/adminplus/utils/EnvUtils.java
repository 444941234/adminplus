package com.adminplus.utils;

/**
 * 环境检测工具类
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class EnvUtils {

    private EnvUtils() {
        // 工具类禁止实例化
    }

    /**
     * 判断是否为生产环境
     *
     * @param env 环境标识
     * @return 是否为生产环境
     */
    public static boolean isProduction(String env) {
        return "prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env);
    }
}