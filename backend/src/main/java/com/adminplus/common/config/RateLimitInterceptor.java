package com.adminplus.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    // 登录接口限流：5次/分钟
    private static final int LOGIN_MAX_REQUESTS = 5;
    private static final int LOGIN_TIME_WINDOW = 60; // 60秒

    // 通用接口限流：100次/分钟
    private static final int GENERAL_MAX_REQUESTS = 100;
    private static final int GENERAL_TIME_WINDOW = 60; // 60秒

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        // 登录接口限流
        if ("/auth/login".equals(uri)) {
            return checkRateLimit(clientIp, "login", LOGIN_MAX_REQUESTS, LOGIN_TIME_WINDOW, response);
        }

        // 通用接口限流
        return checkRateLimit(clientIp, "general", GENERAL_MAX_REQUESTS, GENERAL_TIME_WINDOW, response);
    }

    /**
     * 检查请求频率限制
     *
     * @param clientIp 客户端IP
     * @param key 限流键
     * @param maxRequests 最大请求数
     * @param timeWindow 时间窗口（秒）
     * @param response HTTP响应
     * @return 是否允许通过
     */
    private boolean checkRateLimit(String clientIp, String key, int maxRequests, int timeWindow,
                                   HttpServletResponse response) throws IOException {
        String redisKey = "rate_limit:" + key + ":" + clientIp;

        // 获取当前计数
        String countStr = redisTemplate.opsForValue().get(redisKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= maxRequests) {
            // 超过限流
            log.warn("限流触发: IP={}, Key={}, Count={}", clientIp, key, count);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }

        // 增加计数
        if (count == 0) {
            // 第一次请求，设置过期时间
            redisTemplate.opsForValue().set(redisKey, "1", timeWindow, TimeUnit.SECONDS);
        } else {
            // 增加计数
            redisTemplate.opsForValue().increment(redisKey);
        }

        return true;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}