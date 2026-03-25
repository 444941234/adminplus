package com.adminplus.statemachine.enums;

/**
 * 工作流状态枚举
 * <p>
 * 使用简单状态，节点信息通过 ExtendedState 跟踪
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public enum WorkflowState {
    /**
     * 草稿状态
     */
    DRAFT,

    /**
     * 运行中（节点信息在 ExtendedState.currentNodeId 中）
     */
    RUNNING,

    /**
     * 已通过
     */
    APPROVED,

    /**
     * 已拒绝
     */
    REJECTED,

    /**
     * 已取消
     */
    CANCELLED
}
