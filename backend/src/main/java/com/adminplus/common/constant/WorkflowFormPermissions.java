package com.adminplus.common.constant;

/**
 * 工作流表单权限
 * <p>
 * 工作流表单模板的 CRUD 操作权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface WorkflowFormPermissions {

    /**
     * 查看表单模板权限
     */
    String VIEW = "workflow:form:view";

    /**
     * 表单模板列表权限
     */
    String LIST = "workflow:form:list";

    /**
     * 创建表单模板权限
     */
    String CREATE = "workflow:form:create";

    /**
     * 更新表单模板权限
     */
    String UPDATE = "workflow:form:update";

    /**
     * 删除表单模板权限
     */
    String DELETE = "workflow:form:delete";
}
