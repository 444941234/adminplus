package com.adminplus.utils;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class XssUtils {

    // 危险的 JavaScript 事件
    private static final Pattern SCRIPT_EVENT_PATTERN = Pattern.compile(
            "on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );

    // 危险的 HTML 标签
    private static final Pattern DANGEROUS_TAG_PATTERN = Pattern.compile(
            "<(script|iframe|object|embed|form|input|button|meta|link|style)",
            Pattern.CASE_INSENSITIVE
    );

    // javascript: 协议
    private static final Pattern JS_PROTOCOL_PATTERN = Pattern.compile(
            "javascript:",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 过滤 HTML 标签，防止 XSS 攻击
     *
     * @param input 输入字符串
     * @return 过滤后的字符串
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 清理输入，移除危险的 HTML/JavaScript
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // 移除危险的 HTML 标签
        String sanitized = DANGEROUS_TAG_PATTERN.matcher(input).replaceAll("");

        // 移除 JavaScript 事件
        sanitized = SCRIPT_EVENT_PATTERN.matcher(sanitized).replaceAll("");

        // 移除 javascript: 协议
        sanitized = JS_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("");

        return sanitized.trim();
    }

    /**
     * 批量过滤多个字符串
     *
     * @param inputs 输入字符串数组
     * @return 过滤后的字符串数组
     */
    public static String[] escape(String[] inputs) {
        if (inputs == null) {
            return null;
        }
        String[] result = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            result[i] = escape(inputs[i]);
        }
        return result;
    }

    /**
     * 清理文件名，防止路径遍历攻击
     *
     * @param filename 文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        // 移除路径遍历字符和危险字符
        String sanitized = filename.replaceAll("\\.\\./", "")
                .replaceAll("\\.\\\\", "")
                .replaceAll("/", "")
                .replaceAll("\\\\", "")
                .replaceAll("\\x00", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll("[<>:\"|?*]", ""); // Windows 不允许的字符

        // 移除控制字符，但保留 Unicode 字符（支持中文等）
        sanitized = sanitized.replaceAll("[\\x00-\\x1f]", "");

        // 限制文件名长度
        if (sanitized.length() > 255) {
            // 保留扩展名
            int lastDot = sanitized.lastIndexOf('.');
            if (lastDot > 0) {
                String ext = sanitized.substring(lastDot);
                sanitized = sanitized.substring(0, 255 - ext.length()) + ext;
            } else {
                sanitized = sanitized.substring(0, 255);
            }
        }

        return sanitized;
    }

    /**
     * 验证文件扩展名是否在允许列表中
     *
     * @param filename 文件名
     * @param allowedExtensions 允许的扩展名列表（小写）
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String filename, String[] allowedExtensions) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return false;
        }

        String extension = filename.substring(lastDotIndex).toLowerCase();
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证路径是否安全（防止路径遍历）
     *
     * @param path 路径
     * @return 是否安全
     */
    public static boolean isSafePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // 检查路径遍历字符
        if (path.contains("..") || path.contains("~")) {
            return false;
        }

        // 检查绝对路径
        if (path.startsWith("/") || path.contains(":")) {
            return false;
        }

        return true;
    }
}