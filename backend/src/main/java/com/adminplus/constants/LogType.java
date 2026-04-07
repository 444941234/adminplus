package com.adminplus.constants;

/**
 * 日志类型枚举
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
public enum LogType {
    OPERATION(1, "操作日志"),
    LOGIN(2, "登录日志"),
    SYSTEM(3, "系统日志");

    private final int code;
    private final String description;

    LogType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LogType fromCode(int code) {
        for (LogType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown LogType code: " + code);
    }
}
