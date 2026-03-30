package com.adminplus.utils;

/**
 * 数据脱敏工具类
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public final class MaskingUtils {

    private MaskingUtils() {}

    /**
     * 隐藏用户名敏感信息
     * <p>
     * 保留首尾字符，中间用 *** 代替
     * </p>
     *
     * @param username 用户名
     * @return 脱敏后的用户名
     */
    public static String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}
