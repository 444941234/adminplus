package com.adminplus.common.config;

import com.adminplus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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

    private final DashboardService dashboardService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("清除旧缓存数据...");
        try {
            dashboardService.clearStatsCache();
            log.info("缓存清理完成");
        } catch (Exception e) {
            log.warn("缓存清理失败（可能是首次启动，缓存不存在）: {}", e.getMessage());
        }
    }
}