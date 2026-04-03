package com.adminplus.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存清理 Runner
 * <p>
 * 在应用启动时清理所有旧格式的 Redis 缓存数据，
 * 解决缓存序列化格式变更导致的反序列化异常问题。
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleanupRunner implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("检查并清理旧格式缓存数据...");

        try {
            // 清理所有缓存键（Spring Cache 格式: cacheName::key）
            Set<String> keys = redisTemplate.keys("*");

            if (keys != null && !keys.isEmpty()) {
                // 过滤出 Spring Cache 相关的键（包含 :: 的键）
                Set<String> cacheKeys = keys.stream()
                        .filter(key -> key.contains("::") ||
                                key.startsWith("roles") ||
                                key.startsWith("dashboardStats") ||
                                key.startsWith("menuTree") ||
                                key.startsWith("userMenus") ||
                                key.startsWith("userPermissions") ||
                                key.startsWith("userRoles") ||
                                key.startsWith("rolePermissions") ||
                                key.startsWith("allPermissions") ||
                                key.startsWith("deptTree") ||
                                key.startsWith("dict") ||
                                key.startsWith("dictItem") ||
                                key.startsWith("config") ||
                                key.startsWith("configGroup") ||
                                key.startsWith("workflow") ||
                                key.startsWith("formTemplate"))
                        .collect(java.util.stream.Collectors.toSet());

                if (!cacheKeys.isEmpty()) {
                    redisTemplate.delete(cacheKeys);
                    log.info("清理旧缓存数据: {} 个键", cacheKeys.size());
                } else {
                    log.info("无需清理缓存数据");
                }
            }
        } catch (Exception e) {
            log.warn("缓存清理失败，将继续启动: {}", e.getMessage());
        }
    }
}