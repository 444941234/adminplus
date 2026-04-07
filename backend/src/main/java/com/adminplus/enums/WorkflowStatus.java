package com.adminplus.enums;

/**
 * 工作流实例状态枚举
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
public enum WorkflowStatus {
    DRAFT("draft", "草稿"),
    RUNNING("running", "运行中"),
    APPROVED("approved", "已批准"),
    REJECTED("rejected", "已拒绝"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    WorkflowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static WorkflowStatus fromCode(String code) {
        for (WorkflowStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown WorkflowStatus code: " + code);
    }
}
