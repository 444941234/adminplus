package com.adminplus.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * 日志脱敏转换器
 *
 * 功能：
 * - 自动脱敏日志中的敏感信息（密码、Token、身份证号、手机号等）
 * - 支持正则表达式匹配和替换
 * - 生产环境自动启用，开发环境可选
 *
 * 使用方法：
 * 在 logback-spring.xml 中配置：
 * <conversionRule conversionWord="maskedMsg" converterClass="com.adminplus.logging.LogMaskingConverter" />
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
public class LogMaskingConverter extends MessageConverter {

    // 密码脱敏：password=xxx 或 "password":"xxx"
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(password[=:]\\s*[\"']?)([^\\s\"',}]+)([\"'\\s,}]*)",
            Pattern.CASE_INSENSITIVE
    );

    // JWT Token 脱敏：Bearer xxx 或 Authorization: Bearer xxx
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)(eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)",
            Pattern.CASE_INSENSITIVE
    );

    // 身份证号脱敏：18位身份证号
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "(\\d{6})(\\d{8})(\\d{4})"
    );

    // 手机号脱敏：11位手机号
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(1[3-9]\\d)(\\d{4})(\\d{4})"
    );

    // 邮箱脱敏：xxx@xxx.com
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([a-zA-Z0-9._%+-]+)(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"
    );

    // SQL 语句中的敏感值脱敏
    private static final Pattern SQL_VALUE_PATTERN = Pattern.compile(
            "(VALUES\\s*\\([^)]*?)(password[^,)]*=[^,)]+)([^)]*\\))",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String originalMessage = super.convert(event);
        return maskSensitiveInfo(originalMessage);
    }

    /**
     * 脱敏敏感信息
     *
     * @param message 原始日志消息
     * @return 脱敏后的日志消息
     */
    private String maskSensitiveInfo(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // 脱敏密码
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1***$3");

        // 脱敏 JWT Token（只保留前 8 位和后 8 位）
        masked = TOKEN_PATTERN.matcher(masked).replaceAll(mr -> {
            String prefix = mr.group(1);
            String token = mr.group(2);
            if (token.length() > 16) {
                return prefix + token.substring(0, 8) + "..." + token.substring(token.length() - 8);
            } else {
                return prefix + "***";
            }
        });

        // 脱敏身份证号（隐藏出生日期）
        masked = ID_CARD_PATTERN.matcher(masked).replaceAll("$1********$3");

        // 脱敏手机号（隐藏中间 4 位）
        masked = PHONE_PATTERN.matcher(masked).replaceAll("$1****$3");

        // 脱敏邮箱（隐藏用户名部分）
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(mr -> {
            String username = mr.group(1);
            String domain = mr.group(2);
            if (username.length() > 2) {
                return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
            } else {
                return "***" + domain;
            }
        });

        // 脱敏 SQL 语句中的密码值
        masked = SQL_VALUE_PATTERN.matcher(masked).replaceAll("$1password=***$3");

        return masked;
    }
}