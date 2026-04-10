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

    /**
     * 判断当前状态是否可以提交
     */
    public boolean canSubmit() {
        return this == DRAFT || this == RUNNING;
    }

    /**
     * 判断当前状态是否可以审批
     */
    public boolean canApprove() {
        return this == RUNNING;
    }

    /**
     * 判断当前状态是否可以拒绝
     */
    public boolean canReject() {
        return this == RUNNING;
    }

    /**
     * 判断当前状态是否可以取消
     */
    public boolean canCancel() {
        return this == DRAFT || this == RUNNING;
    }

    /**
     * 判断当前状态是否可以撤回
     */
    public boolean canWithdraw() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * 判断当前状态是否可以回退
     */
    public boolean canRollback() {
        return this == RUNNING;
    }

    /**
     * 判断当前状态是否可以更新草稿
     */
    public boolean canUpdateDraft() {
        return this == DRAFT;
    }

    /**
     * 判断当前状态是否可以删除草稿
     */
    public boolean canDeleteDraft() {
        return this == DRAFT;
    }

    /**
     * 判断当前状态是否已结束
     */
    public boolean isFinished() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }

    /**
     * 从字符串解析状态（支持code和name，不区分大小写）
     */
    public static WorkflowStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (WorkflowStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)
                || status.name().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知工作流状态: " + code);
    }

    /**
     * 判断是否为有效状态码
     */
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
