package com.adminplus.common.config;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 安全相关常量
 * <p>
 * 集中管理安全配置中的常量定义
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Component
public class SecurityConstants {

    // ==================== JWT 相关 ====================

    /**
     * RSA 密钥最小位数（符合 NIST 推荐）
     */
    public static final int MIN_RSA_KEY_SIZE = 2048;

    /**
     * JWT 权限前缀
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * JWT 权限声明名称
     */
    public static final String AUTHORITIES_CLAIM_NAME = "scope";

    // ==================== HSTS 相关 ====================

    /**
     * HSTS 最大缓存时间（1 年）
     */
    public static final long HSTS_MAX_AGE_SECONDS = Duration.ofDays(365).toSeconds();

    // ==================== CORS 相关 ====================

    /**
     * CORS 预检请求缓存时间（1 小时）
     */
    public static final long CORS_MAX_AGE_SECONDS = Duration.ofHours(1).toSeconds();

    // ==================== 公开端点 ====================

    /**
     * 无需认证的公开端点
     * 注意：由于配置了 server.servlet.context-path=/api，Spring Security 匹配器不需要包含 /api 前缀
     * Spring Security 在 context-path 之后的路径上进行匹配
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/v1/auth/login",
            "/v1/captcha/**",
            "/v1/verify/**",
            "/actuator/health"
    };

    /**
     * 认证端点（用于 CSRF 配置）
     */
    public static final String[] AUTH_ENDPOINTS = {
            "/v1/auth/login"
    };

    // ==================== 配置管理权限 ====================

    /**
     * 配置分组 - 查询权限
     */
    public static final String CONFIG_GROUP_QUERY = "config:group:query";

    /**
     * 配置分组 - 列表权限
     */
    public static final String CONFIG_GROUP_LIST = "config:group:list";

    /**
     * 配置分组 - 新增权限
     */
    public static final String CONFIG_GROUP_ADD = "config:group:add";

    /**
     * 配置分组 - 编辑权限
     */
    public static final String CONFIG_GROUP_EDIT = "config:group:edit";

    /**
     * 配置分组 - 删除权限
     */
    public static final String CONFIG_GROUP_DELETE = "config:group:delete";

    /**
     * 配置项 - 查询权限
     */
    public static final String CONFIG_QUERY = "config:query";

    /**
     * 配置项 - 列表权限
     */
    public static final String CONFIG_LIST = "config:list";

    /**
     * 配置项 - 新增权限
     */
    public static final String CONFIG_ADD = "config:add";

    /**
     * 配置项 - 编辑权限
     */
    public static final String CONFIG_EDIT = "config:edit";

    /**
     * 配置项 - 删除权限
     */
    public static final String CONFIG_DELETE = "config:delete";

    /**
     * 配置项 - 导出权限
     */
    public static final String CONFIG_EXPORT = "config:export";

    /**
     * 配置项 - 导入权限
     */
    public static final String CONFIG_IMPORT = "config:import";

    /**
     * 配置项 - 回滚权限
     */
    public static final String CONFIG_ROLLBACK = "config:rollback";

    /**
     * 配置项 - 手动生效权限
     */
    public static final String CONFIG_APPLY = "config:apply";

    // ==================== 工作流权限 ====================

    /**
     * 工作流 - 创建流程权限
     */
    public static final String WORKFLOW_CREATE = "workflow:create";

    /**
     * 工作流 - 更新流程权限
     */
    public static final String WORKFLOW_UPDATE = "workflow:update";

    /**
     * 工作流 - 删除流程权限
     */
    public static final String WORKFLOW_DELETE = "workflow:delete";

    /**
     * 工作流 - 保存草稿权限
     */
    public static final String WORKFLOW_DRAFT = "workflow:draft";

    /**
     * 工作流 - 发起流程权限
     */
    public static final String WORKFLOW_START = "workflow:start";

    /**
     * 工作流 - 审批通过权限
     */
    public static final String WORKFLOW_APPROVE = "workflow:approve";

    /**
     * 工作流 - 审批驳回权限
     */
    public static final String WORKFLOW_REJECT = "workflow:reject";

    /**
     * 工作流 - 取消流程权限
     */
    public static final String WORKFLOW_CANCEL = "workflow:cancel";

    /**
     * 工作流 - 撤回流程权限
     */
    public static final String WORKFLOW_WITHDRAW = "workflow:withdraw";

    /**
     * 工作流 - 回退流程权限
     */
    public static final String WORKFLOW_ROLLBACK = "workflow:rollback";

    /**
     * 工作流 - 加签转办权限
     */
    public static final String WORKFLOW_ADD_SIGN = "workflow:add-sign";

    /**
     * 工作流 - 催办权限
     */
    public static final String WORKFLOW_URGE = "workflow:urge";

    /**
     * 工作流定义 - 创建权限
     */
    public static final String WORKFLOW_DEFINITION_CREATE = "workflow:definition:create";

    /**
     * 工作流定义 - 更新权限
     */
    public static final String WORKFLOW_DEFINITION_UPDATE = "workflow:definition:update";

    /**
     * 工作流定义 - 删除权限
     */
    public static final String WORKFLOW_DEFINITION_DELETE = "workflow:definition:delete";

    /**
     * 工作流抄送 - 查看权限
     */
    public static final String WORKFLOW_CC_READ = "workflow:cc:read";

    /**
     * 工作流抄送 - 列表权限
     */
    public static final String WORKFLOW_CC_LIST = "workflow:cc:list";

    /**
     * 工作流催办 - 查看权限
     */
    public static final String WORKFLOW_URGE_READ = "workflow:urge:read";

    /**
     * 工作流催办 - 列表权限
     */
    public static final String WORKFLOW_URGE_LIST = "workflow:urge:list";

    // ==================== 表单模板权限 ====================

    /**
     * 表单模板 - 查看权限
     */
    public static final String WORKFLOW_FORM_VIEW = "workflow:form:view";

    /**
     * 表单模板 - 列表权限
     */
    public static final String WORKFLOW_FORM_LIST = "workflow:form:list";

    /**
     * 表单模板 - 创建权限
     */
    public static final String WORKFLOW_FORM_CREATE = "workflow:form:create";

    /**
     * 表单模板 - 更新权限
     */
    public static final String WORKFLOW_FORM_UPDATE = "workflow:form:update";

    /**
     * 表单模板 - 删除权限
     */
    public static final String WORKFLOW_FORM_DELETE = "workflow:form:delete";

    // ==================== 工作流钩子权限 ====================

    /**
     * 工作流钩子 - 查看权限
     */
    public static final String WORKFLOW_HOOK_VIEW = "workflow:hook:view";

    /**
     * 工作流钩子 - 创建权限
     */
    public static final String WORKFLOW_HOOK_CREATE = "workflow:hook:create";

    /**
     * 工作流钩子 - 更新权限
     */
    public static final String WORKFLOW_HOOK_UPDATE = "workflow:hook:update";

    /**
     * 工作流钩子 - 删除权限
     */
    public static final String WORKFLOW_HOOK_DELETE = "workflow:hook:delete";

    /**
     * 工作流钩子日志 - 查看权限
     */
    public static final String WORKFLOW_HOOK_LOG_VIEW = "workflow:hook:log:view";

    public SecurityConstants() {}
}
