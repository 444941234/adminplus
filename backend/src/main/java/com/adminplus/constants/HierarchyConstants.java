package com.adminplus.constants;

/**
 * 层级结构相关常量（部门、菜单等）
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface HierarchyConstants {

    /**
     * 根节点父ID标识
     */
    String ROOT_PARENT_ID = "0";

    /**
     * 根节点 ancestors 前缀
     */
    String ROOT_ANCESTORS = "0,";

    // ==================== 日志模块名称 ====================

    /**
     * 认证管理模块名称
     */
    String MODULE_AUTH = "认证管理";

    /**
     * 部门管理模块名称
     */
    String MODULE_DEPT = "部门管理";

    /**
     * 菜单管理模块名称
     */
    String MODULE_MENU = "菜单管理";

    /**
     * 用户管理模块名称
     */
    String MODULE_USER = "用户管理";

    /**
     * 角色管理模块名称
     */
    String MODULE_ROLE = "角色管理";

    /**
     * 字典管理模块名称
     */
    String MODULE_DICT = "字典管理";

    /**
     * 字典项管理模块名称
     */
    String MODULE_DICT_ITEM = "字典项管理";

    /**
     * 参数配置管理模块名称
     */
    String MODULE_CONFIG = "配置管理";

    /**
     * 通知公告模块名称
     */
    String MODULE_NOTICE = "通知公告";
}
