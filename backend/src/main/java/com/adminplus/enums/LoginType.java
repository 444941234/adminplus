package com.adminplus.enums;

/**
 * 登录类型枚举
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public enum LoginType {
    LOGIN(1, "登录"),
    LOGOUT(2, "登出");

    private final int code;
    private final String description;

    LoginType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}