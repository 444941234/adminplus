package com.adminplus.enums;

/**
 * 通用状态枚举
 * <p>
 * 用于系统中启用/禁用状态的统一管理，替代魔法数字 0/1
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public enum CommonStatus {
    /**
     * 禁用状态
     */
    DISABLED(0, "禁用"),
    /**
     * 启用状态
     */
    ENABLED(1, "启用");

    private final int code;
    private final String description;

    CommonStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举
     * @throws IllegalArgumentException 未知的状态码
     */
    public static CommonStatus fromCode(int code) {
        for (CommonStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CommonStatus code: " + code);
    }

    /**
     * 判断是否为启用状态
     *
     * @param code 状态码
     * @return true 如果启用
     */
    public static boolean isEnabled(Integer code) {
        return code != null && code == ENABLED.code;
    }

    /**
     * 判断是否为禁用状态
     *
     * @param code 状态码
     * @return true 如果禁用
     */
    public static boolean isDisabled(Integer code) {
        return code != null && code == DISABLED.code;
    }
}
