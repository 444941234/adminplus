package com.adminplus.constants;

/**
 * 工作流实例状态常量
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
public interface WorkflowStatus {

    /**
     * 草稿状态
     */
    String DRAFT = "draft";

    /**
     * 运行中状态
     */
    String RUNNING = "running";

    /**
     * 已批准状态
     */
    String APPROVED = "approved";

    /**
     * 已拒绝状态
     */
    String REJECTED = "rejected";

    /**
     * 已取消状态
     */
    String CANCELLED = "cancelled";
}
