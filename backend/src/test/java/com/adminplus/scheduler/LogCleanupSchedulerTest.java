package com.adminplus.scheduler;

import com.adminplus.common.properties.LogStorageProperties;
import com.adminplus.repository.WorkflowHookLogRepository;
import com.adminplus.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

/**
 * LogCleanupScheduler 测试
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
class LogCleanupSchedulerTest {

    @Mock
    private LogService logService;

    @Mock
    private LogStorageProperties logStorageProperties;

    @Mock
    private LogStorageProperties.CleanupConfig cleanupConfig;

    @Mock
    private WorkflowHookLogRepository hookLogRepository;

    @InjectMocks
    private LogCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(logStorageProperties.getCleanup()).thenReturn(cleanupConfig);
    }

    @Test
    @DisplayName("当清理功能启用时，应执行日志清理")
    void shouldCleanupLogsWhenEnabled() {
        // Given
        when(cleanupConfig.isEnabled()).thenReturn(true);
        when(cleanupConfig.getRetentionDays()).thenReturn(90);
        when(logService.cleanupExpiredLogs()).thenReturn(100);
        when(hookLogRepository.deleteByCreateTimeBeforeAndDeletedFalse(any(Instant.class))).thenReturn(50);

        // When
        scheduler.cleanupExpiredLogs();
        scheduler.cleanupExpiredHookLogs();

        // Then
        verify(logService).cleanupExpiredLogs();
        verify(hookLogRepository).deleteByCreateTimeBeforeAndDeletedFalse(any(Instant.class));
    }

    @Test
    @DisplayName("当清理功能禁用时，应跳过日志清理")
    void shouldSkipCleanupWhenDisabled() {
        // Given
        when(cleanupConfig.isEnabled()).thenReturn(false);

        // When
        scheduler.cleanupExpiredLogs();
        scheduler.cleanupExpiredHookLogs();

        // Then
        verify(logService, never()).cleanupExpiredLogs();
        verify(hookLogRepository, never()).deleteByCreateTimeBeforeAndDeletedFalse(any(Instant.class));
    }

    @Test
    @DisplayName("当日志清理抛出异常时，应记录错误并继续")
    void shouldHandleExceptionDuringCleanup() {
        // Given
        when(cleanupConfig.isEnabled()).thenReturn(true);
        when(cleanupConfig.getRetentionDays()).thenReturn(90);
        when(logService.cleanupExpiredLogs()).thenThrow(new RuntimeException("Cleanup failed"));

        // When - should not throw
        scheduler.cleanupExpiredLogs();

        // Then - exception is caught and logged
        verify(logService).cleanupExpiredLogs();
    }
}