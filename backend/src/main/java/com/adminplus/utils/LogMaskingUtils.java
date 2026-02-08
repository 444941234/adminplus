package com.adminplus.utils;

import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 *
 * 用于在日志中隐藏敏感信息，如密码、Token、手机号、邮箱等
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
public class LogMaskingUtils {

    // 敏感信息匹配模式
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(password|pwd|passwd)\\s*[=:]+\\s*[\\S]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(token|jwt|access_token|refresh_token)\\s*[=:]+\\s*[\\S]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");

    /**
     * 脱敏日志消息
     *
     * @param message 原始日志消息
     * @return 脱敏后的日志消息
     */
    public static String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // 脱敏密码
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1=***");

        // 脱敏 Token
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("$1=***");

        // 脱敏手机号（保留前3位和后4位）
        masked = PHONE_PATTERN.matcher(masked).replaceAll("$1****$2");

        // 脱敏邮箱（保留第一个字符和@后面的域名）
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(maskEmail("$1", "$2"));

        // 脱敏身份证号（保留前6位和后4位）
        masked = ID_CARD_PATTERN.matcher(masked).replaceAll("$1********$2");

        // 脱敏银行卡号（保留前4位和后4位）
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("$1********$2");

        return masked;
    }

    /**
     * 脱敏邮箱
     */
    private static String maskEmail(String prefix, String domain) {
        if (prefix == null || prefix.isEmpty()) {
            return "****@" + domain;
        }
        String maskedPrefix = prefix.charAt(0) + "***" + prefix.substring(prefix.length() - 1);
        return maskedPrefix + "@" + domain;
    }

    /**
     * 脱敏用户名（只显示首尾字符）
     */
    public static String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "***";
        }
        if (username.length() <= 2) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }

    /**
     * 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        String masked = email;
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(maskEmail("$1", "$2"));
        return masked;
    }

    /**
     * 脱敏 Token（只显示前8位和后8位）
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        if (token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    /**
     * 脱敏 IP 地址（保留前两段）
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }

        return parts[0] + "." + parts[1] + ".*.*";
    }
}