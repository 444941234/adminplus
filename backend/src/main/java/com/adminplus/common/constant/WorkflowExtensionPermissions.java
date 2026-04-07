package com.adminplus.common.constant;

/**
 * 工作流扩展功能权限
 * <p>
 * 工作流抄送、催办、钩子等扩展功能权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface WorkflowExtensionPermissions {

    // ==================== 抄送权限 ====================

    /**
     * 工作流抄送 - 查看权限
     */
    String CC_READ = "workflow:cc:read";

    /**
     * 工作流抄送 - 列表权限
     */
    String CC_LIST = "workflow:cc:list";

    // ==================== 催办权限 ====================

    /**
     * 工作流催办 - 查看权限
     */
    String URGE_READ = "workflow:urge:read";

    /**
     * 工作流催办 - 列表权限
     */
    String URGE_LIST = "workflow:urge:list";

    // ==================== 钩子权限 ====================

    /**
     * 工作流钩子 - 查看权限
     */
    String HOOK_VIEW = "workflow:hook:view";

    /**
     * 工作流钩子 - 创建权限
     */
    String HOOK_CREATE = "workflow:hook:create";

    /**
     * 工作流钩子 - 更新权限
     */
    String HOOK_UPDATE = "workflow:hook:update";

    /**
     * 工作流钩子 - 删除权限
     */
    String HOOK_DELETE = "workflow:hook:delete";

    /**
     * 工作流钩子日志 - 查看权限
     */
    String HOOK_LOG_VIEW = "workflow:hook:log:view";
}
