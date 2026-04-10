package com.adminplus.common.interceptor;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.constants.CacheConstants;
import com.adminplus.constants.HttpConstants;
import com.adminplus.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
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
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String clientIp = IpUtils.getClientIp(request);

        AppProperties.RateLimit rateLimit = appProperties.getRateLimit();

        // 登录接口限流
        if ("/auth/login".equals(uri)) {
            return checkRateLimit(clientIp, "login",
                    rateLimit.getLoginMaxRequests(),
                    rateLimit.getLoginTimeWindow(),
                    response);
        }

        // 通用接口限流
        return checkRateLimit(clientIp, "general",
                rateLimit.getGeneralMaxRequests(),
                rateLimit.getGeneralTimeWindow(),
                response);
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
        String redisKey = CacheConstants.RATE_LIMIT_KEY_PREFIX + key + ":" + clientIp;

        // 原子递增，首次请求时设置过期时间
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);
        }

        if (count != null && count > maxRequests) {
            log.warn("限流触发: IP={}, Key={}, Count={}", clientIp, key, count);
            response.setStatus(HttpConstants.HTTP_TOO_MANY_REQUESTS);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + HttpConstants.HTTP_TOO_MANY_REQUESTS + ",\"message\":\"请求过于频繁，请稍后再试\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }

        return true;
    }
}
