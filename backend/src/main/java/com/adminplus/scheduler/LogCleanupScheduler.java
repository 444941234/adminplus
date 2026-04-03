package com.adminplus.scheduler;

import com.adminplus.common.config.LogStorageProperties;
import com.adminplus.repository.WorkflowHookLogRepository;
import com.adminplus.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 日志清理定时任务
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupScheduler {

    private final LogService logService;
    private final LogStorageProperties logStorageProperties;
    private final WorkflowHookLogRepository hookLogRepository;

    /**
     * 定时清理过期日志
     * Cron 表达式由配置文件指定
     */
    @Scheduled(cron = "${app.logging.storage.cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredLogs() {
        var cleanupConfig = logStorageProperties.getCleanup();

        if (!cleanupConfig.isEnabled()) {
            log.debug("日志清理功能未启用，跳过执行");
            return;
        }

        log.info("开始执行日志清理任务，保留天数: {}", cleanupConfig.getRetentionDays());

        try {
            Integer deletedCount = logService.cleanupExpiredLogs();
            log.info("日志清理任务完成，共删除 {} 条过期日志", deletedCount);
        } catch (Exception e) {
            log.error("日志清理任务执行失败", e);
        }
    }

    /**
     * 定时清理过期的工作流钩子执行日志
     * 使用独立的 Cron 表达式，默认每天凌晨3点执行
     */
    @Scheduled(cron = "${app.workflow.hook.cleanup.cron:0 0 3 * * ?}")
    public void cleanupExpiredHookLogs() {
        var cleanupConfig = logStorageProperties.getCleanup();

        if (!cleanupConfig.isEnabled()) {
            log.debug("日志清理功能未启用，跳过钩子日志清理");
            return;
        }

        int retentionDays = cleanupConfig.getRetentionDays();
        log.info("开始执行钩子日志清理任务，保留天数: {}", retentionDays);

        try {
            Instant beforeTime = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
            int deletedCount = hookLogRepository.deleteByCreateTimeBeforeAndDeletedFalse(beforeTime);
            log.info("钩子日志清理任务完成，共删除 {} 条过期日志", deletedCount);
        } catch (Exception e) {
            log.error("钩子日志清理任务执行失败", e);
        }
    }
}
