package com.adminplus.common.constant;

/**
 * 配置管理权限
 * <p>
 * 配置分组和配置项的 CRUD 操作权限
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface ConfigPermissions {

    // ==================== 配置分组权限 ====================

    /**
     * 配置分组 - 查询权限
     */
    String GROUP_QUERY = "config:group:query";

    /**
     * 配置分组 - 列表权限
     */
    String GROUP_LIST = "config:group:list";

    /**
     * 配置分组 - 新增权限
     */
    String GROUP_ADD = "config:group:add";

    /**
     * 配置分组 - 编辑权限
     */
    String GROUP_EDIT = "config:group:edit";

    /**
     * 配置分组 - 删除权限
     */
    String GROUP_DELETE = "config:group:delete";

    // ==================== 配置项权限 ====================

    /**
     * 配置项 - 查询权限
     */
    String QUERY = "config:query";

    /**
     * 配置项 - 列表权限
     */
    String LIST = "config:list";

    /**
     * 配置项 - 新增权限
     */
    String ADD = "config:add";

    /**
     * 配置项 - 编辑权限
     */
    String EDIT = "config:edit";

    /**
     * 配置项 - 删除权限
     */
    String DELETE = "config:delete";

    /**
     * 配置项 - 导出权限
     */
    String EXPORT = "config:export";

    /**
     * 配置项 - 导入权限
     */
    String IMPORT = "config:import";

    /**
     * 配置项 - 回滚权限
     */
    String ROLLBACK = "config:rollback";

    /**
     * 配置项 - 手动生效权限
     */
    String APPLY = "config:apply";
}
