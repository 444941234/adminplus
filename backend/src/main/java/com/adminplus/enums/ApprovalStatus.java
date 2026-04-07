package com.adminplus.enums;

/**
 * 工作流审批状态枚举
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
public enum ApprovalStatus {
    PENDING("pending", "待审批"),
    APPROVED("approved", "已批准"),
    REJECTED("rejected", "已拒绝"),
    TRANSFERRED("transferred", "已转办");

    private final String code;
    private final String description;

    ApprovalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ApprovalStatus fromCode(String code) {
        for (ApprovalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalStatus code: " + code);
    }
}
