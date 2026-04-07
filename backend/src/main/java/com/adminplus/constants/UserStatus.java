package com.adminplus.constants;

/**
 * 用户状态枚举
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public enum UserStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserStatus fromCode(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus code: " + code);
    }
}
