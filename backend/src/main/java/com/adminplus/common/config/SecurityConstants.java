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

    public SecurityConstants() {}
}
