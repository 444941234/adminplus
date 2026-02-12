package com.adminplus.common.filter;

import com.adminplus.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Token 黑名单过滤器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 检查 Token 是否在黑名单中
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Token 已被撤销: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":401,\"message\":\"Token 已被撤销，请重新登录\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}