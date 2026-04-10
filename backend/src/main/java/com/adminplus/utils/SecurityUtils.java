package com.adminplus.utils;

import com.adminplus.common.security.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.function.Function;

/**
 * 安全工具类
 * <p>
 * 提供统一的安全上下文访问接口，支持 Session 和 JWT 两种认证方式。
 * 所有方法均为线程安全，可在任意上下文中调用。
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public final class SecurityUtils {

    /**
     * 超级管理员角色编码
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * 默认系统用户标识，用于审计日志等场景
     */
    private static final String DEFAULT_SYSTEM_USER = "system";

    private SecurityUtils() {
        // 工具类，禁止实例化
    }

    // ==================== 核心方法 ====================

    /**
     * 获取当前认证的用户信息
     *
     * @return 当前用户详情
     * @throws IllegalStateException 未登录或登录已过期
     */
    public static AppUserDetails getCurrentUser() {
        return resolvePrincipal(
                userDetails -> userDetails,
                AppUserDetails::fromJwt
        );
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     * @throws IllegalStateException 未登录或登录已过期
     */
    public static String getCurrentUserId() {
        return resolvePrincipal(
                AppUserDetails::getId,
                jwt -> {
                    String userId = jwt.getClaimAsString("userId");
                    if (userId == null) {
                        throw new IllegalStateException("JWT 中缺少用户 ID 信息");
                    }
                    return userId;
                }
        );
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     * @throws IllegalStateException 未登录或登录已过期
     */
    public static String getCurrentUsername() {
        return resolvePrincipal(
                AppUserDetails::getUsername,
                jwt -> {
                    String username = jwt.getSubject();
                    if (username == null) {
                        throw new IllegalStateException("JWT 中缺少用户名信息");
                    }
                    return username;
                }
        );
    }

    /**
     * 获取当前用户的部门ID
     *
     * @return 部门ID，可能为 null
     * @throws IllegalStateException 未登录或登录已过期
     */
    public static String getCurrentUserDeptId() {
        AppUserDetails user = getCurrentUser();
        return user != null ? user.getDeptId() : null;
    }

    // ==================== 判断方法 ====================

    /**
     * 判断当前用户是否已登录
     *
     * @return true 如果已认证且非匿名用户
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !isAnonymousUser(authentication);
    }

    /**
     * 判断当前用户是否为超级管理员
     *
     * @return true 如果拥有 ROLE_ADMIN 角色
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    // ==================== 容错方法 ====================

    /**
     * 获取当前用户ID，未登录时返回默认值
     * <p>
     * 用于审计日志等场景，避免因认证问题导致业务失败
     *
     * @return 用户ID 或 "system"
     */
    public static String getCurrentUserIdOrDefault() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return DEFAULT_SYSTEM_USER;
        }
    }

    /**
     * 获取当前用户名，未登录时返回默认值
     *
     * @return 用户名 或 "system"
     */
    public static String getCurrentUsernameOrDefault() {
        try {
            return getCurrentUsername();
        } catch (IllegalStateException e) {
            return DEFAULT_SYSTEM_USER;
        }
    }

    // ==================== 私有解析方法 ====================

    /**
     * 从认证对象的 principal 中统一解析数据
     * <p>
     * 消除 Session/JWT 两种认证方式的重复判断逻辑
     *
     * @param sessionExtractor Session 认证时的数据提取器
     * @param jwtExtractor     JWT 认证时的数据提取器
     * @param <T>              返回值类型
     * @return 提取的数据
     * @throws IllegalStateException 认证无效或数据缺失
     */
    private static <T> T resolvePrincipal(
            Function<AppUserDetails, T> sessionExtractor,
            Function<Jwt, T> jwtExtractor) {

        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();

        // Session 认证
        if (principal instanceof AppUserDetails userDetails) {
            return sessionExtractor.apply(userDetails);
        }

        // JWT 认证
        if (principal instanceof Jwt jwt) {
            return jwtExtractor.apply(jwt);
        }

        // 未知认证类型
        String principalType = (principal != null) ? principal.getClass().getName() : "null";
        throw new IllegalStateException("未知的认证类型: " + principalType);
    }

    /**
     * 获取当前认证对象
     *
     * @return 认证对象
     * @throws IllegalStateException 未登录或登录已过期
     */
    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("未登录或登录已过期");
        }
        return authentication;
    }

    /**
     * 判断是否为匿名用户
     *
     * @param authentication 认证对象
     * @return true 如果是匿名用户
     */
    private static boolean isAnonymousUser(Authentication authentication) {
        return "anonymousUser".equals(authentication.getPrincipal())
                || authentication.getPrincipal() instanceof String
                && "anonymousUser".equals(authentication.getPrincipal());
    }
}
