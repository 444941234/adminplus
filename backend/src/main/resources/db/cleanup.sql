-- 清理所有表数据（保留表结构）
-- 注意：仅在开发环境使用

-- 清空所有业务表数据
DELETE FROM sys_user_role;
DELETE FROM sys_role_menu;
DELETE FROM sys_notification;
DELETE FROM sys_log;
DELETE FROM sys_workflow_approval;
DELETE FROM sys_workflow_instance;
DELETE FROM sys_workflow_node;
DELETE FROM sys_workflow_definition;
DELETE FROM sys_user;
DELETE FROM sys_role;
DELETE FROM sys_menu;
DELETE FROM sys_dept;
DELETE FROM sys_dict_item;
DELETE FROM sys_dict;
DELETE FROM sys_config;
DELETE FROM sys_config_group;
DELETE FROM sys_file;
DELETE FROM sys_form_template;

-- 清空Flyway历史记录（如果存在）
DELETE FROM flyway_schema_history;
