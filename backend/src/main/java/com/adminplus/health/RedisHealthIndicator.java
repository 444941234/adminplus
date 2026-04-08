package com.adminplus.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 健康检查
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return Health.up()
                    .withDetail("redis", "Redis")
                    .build();
        } catch (Exception e) {
            log.error("Redis health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("redis", e.getMessage())
                    .build();
        }
    }
}