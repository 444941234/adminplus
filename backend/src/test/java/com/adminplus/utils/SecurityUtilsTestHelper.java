package com.adminplus.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityUtils 测试辅助方法
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
public class SecurityUtilsTestHelper {

    /**
     * 为测试设置认证上下文
     *
     * @param userId 用户ID
     */
    public static void setTestAuthentication(String userId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * 清除测试认证上下文
     */
    public static void clearTestAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
