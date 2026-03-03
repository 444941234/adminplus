package com.adminplus.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * 密码强度工具类
 * <p>
 * 使用常数时间算法防止时序攻击
 * </p>
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class PasswordUtils {

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_MEDIUM_PASSWORD_LENGTH = 6;

    // 使用 Set 实现常数时间的特殊字符查找
    private static final Set<Character> SPECIAL_CHARS;

    static {
        SPECIAL_CHARS = new HashSet<>();
        SPECIAL_CHARS.add('!');
        SPECIAL_CHARS.add('@');
        SPECIAL_CHARS.add('#');
        SPECIAL_CHARS.add('$');
        SPECIAL_CHARS.add('%');
        SPECIAL_CHARS.add('^');
        SPECIAL_CHARS.add('&');
        SPECIAL_CHARS.add('*');
        SPECIAL_CHARS.add('(');
        SPECIAL_CHARS.add(')');
        SPECIAL_CHARS.add('_');
        SPECIAL_CHARS.add('+');
        SPECIAL_CHARS.add('-');
        SPECIAL_CHARS.add('=');
        SPECIAL_CHARS.add('[');
        SPECIAL_CHARS.add(']');
        SPECIAL_CHARS.add('{');
        SPECIAL_CHARS.add('}');
        SPECIAL_CHARS.add(';');
        SPECIAL_CHARS.add('\'');
        SPECIAL_CHARS.add(':');
        SPECIAL_CHARS.add('"');
        SPECIAL_CHARS.add('\\');
        SPECIAL_CHARS.add('|');
        SPECIAL_CHARS.add(',');
        SPECIAL_CHARS.add('.');
        SPECIAL_CHARS.add('<');
        SPECIAL_CHARS.add('>');
        SPECIAL_CHARS.add('/');
        SPECIAL_CHARS.add('?');
    }

    /**
     * 验证密码强度（强密码：至少12位，包含大小写字母、数字和特殊字符）
     * 使用常数时间算法防止时序攻击
     *
     * @param password 密码
     * @return 是否符合强密码要求
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        int length = password.length();
        if (length < MIN_PASSWORD_LENGTH || length > MAX_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;

        // 单次遍历，常数时间复杂度 O(n)
        for (int i = 0; i < length; i++) {
            char c = password.charAt(i);

            if (!hasDigit && c >= '0' && c <= '9') {
                hasDigit = true;
            } else if (!hasLower && c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (!hasUpper && c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (!hasSpecial && SPECIAL_CHARS.contains(c)) {
                hasSpecial = true;
            }

            // 早期退出优化：当所有条件都满足时立即返回
            if (hasDigit && hasLower && hasUpper && hasSpecial) {
                return true;
            }
        }

        return hasDigit && hasLower && hasUpper && hasSpecial;
    }

    /**
     * 验证密码强度（中等密码：至少6位）
     *
     * @param password 密码
     * @return 是否符合中等密码要求
     */
    public static boolean isMediumPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        int length = password.length();
        return length >= MIN_MEDIUM_PASSWORD_LENGTH && length <= MAX_PASSWORD_LENGTH;
    }

    /**
     * 获取密码强度描述
     *
     * @param password 密码
     * @return 强度描述
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }

        if (isStrongPassword(password)) {
            return "强";
        } else if (isMediumPassword(password)) {
            return "中";
        } else {
            return "弱";
        }
    }

    /**
     * 获取密码强度错误代码
     * 返回错误代码而非详细消息，避免泄露密码组成信息
     *
     * @param password 密码
     * @return 错误代码（0表示无错误）
     */
    public static int getPasswordStrengthHint(String password) {
        if (password == null || password.isEmpty()) {
            return 1; // 密码为空
        }

        int length = password.length();
        int errorCode = 0;

        if (length < MIN_PASSWORD_LENGTH) {
            errorCode |= 0x01; // 长度不足
        }
        if (length > MAX_PASSWORD_LENGTH) {
            errorCode |= 0x02; // 长度超限
        }

        // 单次遍历检查所有条件
        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (int i = 0; i < length; i++) {
            char c = password.charAt(i);

            if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else if (c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (SPECIAL_CHARS.contains(c)) {
                hasSpecial = true;
            }
        }

        if (!hasDigit) {
            errorCode |= 0x04; // 缺少数字
        }
        if (!hasLower) {
            errorCode |= 0x08; // 缺少小写字母
        }
        if (!hasUpper) {
            errorCode |= 0x10; // 缺少大写字母
        }
        if (!hasSpecial) {
            errorCode |= 0x20; // 缺少特殊字符
        }

        return errorCode;
    }

    /**
     * 将错误代码转换为用户友好的消息
     * 仅用于前端显示，不包含时序敏感信息
     *
     * @param errorCode 错误代码
     * @return 错误消息
     */
    public static String getErrorMessage(int errorCode) {
        if (errorCode == 0) {
            return "密码强度符合要求";
        }

        StringBuilder message = new StringBuilder();
        if ((errorCode & 0x01) != 0) {
            message.append("密码长度至少").append(MIN_PASSWORD_LENGTH).append("位；");
        }
        if ((errorCode & 0x02) != 0) {
            message.append("密码长度不能超过").append(MAX_PASSWORD_LENGTH).append("位；");
        }
        if ((errorCode & 0x04) != 0) {
            message.append("密码必须包含数字；");
        }
        if ((errorCode & 0x08) != 0) {
            message.append("密码必须包含小写字母；");
        }
        if ((errorCode & 0x10) != 0) {
            message.append("密码必须包含大写字母；");
        }
        if ((errorCode & 0x20) != 0) {
            message.append("密码必须包含特殊字符；");
        }

        if (message.length() > 0 && message.charAt(message.length() - 1) == '；') {
            message.setLength(message.length() - 1);
        }

        return message.toString();
    }
}
