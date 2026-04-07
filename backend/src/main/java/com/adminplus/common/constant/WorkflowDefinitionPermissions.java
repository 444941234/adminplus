package com.adminplus.common.constant;

/**
 * 工作流定义权限
 * <p>
 * 工作流模板/定义的 CRUD 操作权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface WorkflowDefinitionPermissions {

    /**
     * 创建工作流定义权限
     */
    String CREATE = "workflow:definition:create";

    /**
     * 更新工作流定义权限
     */
    String UPDATE = "workflow:definition:update";

    /**
     * 删除工作流定义权限
     */
    String DELETE = "workflow:definition:delete";
}
