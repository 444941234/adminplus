package com.adminplus.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存清理启动器
 * <p>
 * 在应用启动时清除旧的缓存数据，避免因序列化格式变更导致的 ClassCastException
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleanupRunner implements ApplicationRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("清除旧缓存数据...");
        try {
            // 清除所有 dashboardStats 缓存
            Set<String> keys = redisTemplate.keys("dashboardStats*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("已清除 {} 个旧缓存 key: {}", deleted, keys);
            } else {
                log.info("没有需要清除的旧缓存");
            }
        } catch (Exception e) {
            log.warn("缓存清理失败: {}", e.getMessage());
        }
    }
}