package com.adminplus.utils;

import java.util.Arrays;
import java.util.List;

/**
 * XSS 清洗工具类
 * 供 Converter 使用，统一 XSS 处理逻辑
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class XssSanitizer {

    private XssSanitizer() {}

    private static final List<String> DEFAULT_SKIP_FIELDS =
        List.of("password", "username", "id", "code", "key", "token", "path", "url");

    /**
     * 清洗字符串（返回新字符串）
     */
    public static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return XssUtils.escape(value);
    }

    /**
     * 清洗字符串（null 安全，返回 null）
     */
    public static String sanitizeOrNull(String value) {
        if (value == null) {
            return null;
        }
        return XssUtils.escape(value);
    }

    /**
     * 批量清洗字符串数组
     */
    public static String[] sanitize(String[] values) {
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
            .map(XssSanitizer::sanitize)
            .toArray(String[]::new);
    }

    /**
     * 判断字段是否需要跳过 XSS 处理
     */
    public static boolean shouldSkip(String fieldName) {
        return DEFAULT_SKIP_FIELDS.contains(fieldName);
    }

    /**
     * 判断字段是否需要跳过 XSS 处理（带自定义跳过列表）
     */
    public static boolean shouldSkip(String fieldName, List<String> additionalSkipFields) {
        return DEFAULT_SKIP_FIELDS.contains(fieldName)
            || (additionalSkipFields != null && additionalSkipFields.contains(fieldName));
    }
}