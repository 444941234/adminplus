package com.adminplus.utils;

import org.springframework.web.util.HtmlUtils;

/**
 * XSS 防护工具类
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class XssUtils {

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

        // 移除路径遍历字符
        String sanitized = filename.replaceAll("\\.\\./", "")
                .replaceAll("\\.\\\\", "")
                .replaceAll("/", "")
                .replaceAll("\\\\", "");

        // 只保留字母、数字、下划线、点和连字符
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");

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
}