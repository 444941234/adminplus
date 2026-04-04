package com.adminplus.constants;

/**
 * 工作流审批状态常量
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
public interface ApprovalStatus {

    /**
     * 待审批状态
     */
    String PENDING = "pending";

    /**
     * 已批准状态
     */
    String APPROVED = "approved";

    /**
     * 已拒绝状态
     */
    String REJECTED = "rejected";

    /**
     * 已转办状态
     */
    String TRANSFERRED = "transferred";
}
