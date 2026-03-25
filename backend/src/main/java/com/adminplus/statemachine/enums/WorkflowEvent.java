package com.adminplus.statemachine.enums;

/**
 * 工作流事件枚举
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public enum WorkflowEvent {
    /**
     * 提交事件（DRAFT -> RUNNING）
     */
    SUBMIT,

    /**
     * 同意事件（RUNNING -> RUNNING 下一节点 或 RUNNING -> APPROVED）
     */
    APPROVE,

    /**
     * 拒绝事件（RUNNING -> REJECTED）
     */
    REJECT,

    /**
     * 取消事件（RUNNING -> CANCELLED）
     */
    CANCEL,

    /**
     * 退回事件（RUNNING -> RUNNING 上一节点 或 RUNNING -> DRAFT）
     */
    ROLLBACK
}
