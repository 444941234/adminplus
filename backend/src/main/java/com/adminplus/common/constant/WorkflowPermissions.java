package com.adminplus.common.constant;

/**
 * 工作流核心权限
 * <p>
 * 工作流实例的创建、更新、删除、审批等操作权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface WorkflowPermissions {

    /**
     * 创建流程权限
     */
    String CREATE = "workflow:create";

    /**
     * 更新流程权限
     */
    String UPDATE = "workflow:update";

    /**
     * 删除流程权限
     */
    String DELETE = "workflow:delete";

    /**
     * 保存草稿权限
     */
    String DRAFT = "workflow:draft";

    /**
     * 发起流程权限
     */
    String START = "workflow:start";

    /**
     * 审批通过权限
     */
    String APPROVE = "workflow:approve";

    /**
     * 审批驳回权限
     */
    String REJECT = "workflow:reject";

    /**
     * 取消流程权限
     */
    String CANCEL = "workflow:cancel";

    /**
     * 撤回流程权限
     */
    String WITHDRAW = "workflow:withdraw";

    /**
     * 回退流程权限
     */
    String ROLLBACK = "workflow:rollback";

    /**
     * 加签转办权限
     */
    String ADD_SIGN = "workflow:add-sign";

    /**
     * 催办权限
     */
    String URGE = "workflow:urge";
}
