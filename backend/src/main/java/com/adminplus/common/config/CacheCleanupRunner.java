package com.adminplus.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存清理启动器
 * <p>
 * 在应用启动时清除旧的缓存数据，避免因序列化格式变更导致的问题
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleanupRunner implements ApplicationRunner {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("清除 Redis 旧缓存数据...");
        try {
            // 清除 adminplus 前缀的所有缓存
            Set<String> keys = stringRedisTemplate.keys("adminplus:*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除 {} 个旧缓存 key", deleted);
            } else {
                log.info("没有需要清除的旧缓存");
            }
        } catch (Exception e) {
            log.warn("缓存清理失败: {}", e.getMessage());
        }
    }
}