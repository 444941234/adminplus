package com.adminplus.utils;

import com.adminplus.common.security.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 安全工具类
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class SecurityUtils {

    /**
     * 获取当前认证的用户信息
     * 支持自定义用户详情和 JWT 认证
     */
    public static AppUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未登录或登录已过期");
        }

        Object principal = authentication.getPrincipal();

        // 传统 Session 认证：principal 是 AppUserDetails
        if (principal instanceof AppUserDetails) {
            return (AppUserDetails) principal;
        }

        // JWT 认证：principal 是 Jwt 对象
        if (principal instanceof Jwt jwt) {
            // 从 JWT 中提取用户信息
            String username = jwt.getSubject();
            Object userIdClaim = jwt.getClaim("userId");

            if (userIdClaim == null) {
                throw new RuntimeException("JWT 中缺少用户 ID 信息");
            }

            String userId = userIdClaim.toString();

            // 创建一个简化的 AppUserDetails 对象
            // 注意：此对象仅包含基本信息，不包含密码等敏感信息
            return new AppUserDetails(
                    userId,
                    username,
                    null, // password - JWT 认证不需要密码
                    null, // nickname
                    null, // email
                    null, // phone
                    null, // avatar
                    1,    // status - 默认启用
                    null, // roles - 从 JWT authorities 中获取
                    null  // permissions - 从 JWT authorities 中获取
            );
        }

        throw new RuntimeException("未知的认证类型: " + principal.getClass().getName());
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未登录或登录已过期");
        }

        Object principal = authentication.getPrincipal();

        // 传统 Session 认证
        if (principal instanceof AppUserDetails userDetails) {
            return userDetails.getId();
        }

        // JWT 认证
        if (principal instanceof Jwt jwt) {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) {
                throw new RuntimeException("JWT 中缺少用户 ID 信息");
            }

            return userIdClaim.toString();
        }

        throw new RuntimeException("未知的认证类型: " + principal.getClass().getName());
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("未登录或登录已过期");
        }

        Object principal = authentication.getPrincipal();

        // 传统 Session 认证
        if (principal instanceof AppUserDetails userDetails) {
            return userDetails.getUsername();
        }

        // JWT 认证
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        throw new RuntimeException("未知的认证类型: " + principal.getClass().getName());
    }

    /**
     * 检查是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 获取当前用户ID，未登录时返回默认值 "system"
     * 用于审计等场景，避免因认证问题导致业务失败
     */
    public static String getCurrentUserIdOrDefault() {
        try {
            return getCurrentUserId();
        } catch (RuntimeException e) {
            return "system";
        }
    }

    /**
     * 获取当前用户名，未登录时返回默认值 "system"
     */
    public static String getCurrentUsernameOrDefault() {
        try {
            return getCurrentUsername();
        } catch (RuntimeException e) {
            return "system";
        }
    }
}