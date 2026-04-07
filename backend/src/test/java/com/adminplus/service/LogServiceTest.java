package com.adminplus.service;

import com.adminplus.common.properties.LogStorageProperties;
import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogPageResp;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.service.impl.LogStorageStrategySelector;
import com.adminplus.service.impl.LogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LogService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogService Unit Tests")
class LogServiceTest {

    @Mock
    private LogStorageStrategySelector storageStrategySelector;

    @Mock
    private LogStorageStrategy storageStrategy;

    @Mock
    private LogStorageProperties logStorageProperties;

    @Mock
    private LogStatisticsService logStatisticsService;

    @InjectMocks
    private LogServiceImpl logService;

    private LogEntity testLog;
    private LogQueryReq query;

    @BeforeEach
    void setUp() {
        lenient().when(storageStrategySelector.getStrategy()).thenReturn(storageStrategy);

        testLog = new LogEntity();
        testLog.setId("log-001");
        testLog.setUsername("testuser");
        testLog.setModule("用户管理");
        testLog.setLogType(1);
        testLog.setOperationType(1);
        testLog.setDescription("查询用户列表");
        testLog.setMethod("UserService.list");
        testLog.setParams("{}");
        testLog.setIp("192.168.1.1");
        testLog.setLocation("本地");
        testLog.setCostTime(100L);
        testLog.setStatus(1);
        testLog.setCreateTime(Instant.now());

        query = new LogQueryReq();
        query.setPage(1);
        query.setSize(10);
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return log when exists")
        void findById_WhenExists_ShouldReturnLog() {
            // Given
            when(storageStrategy.findById("log-001")).thenReturn(testLog);

            // When
            LogPageResp result = logService.findById("log-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("log-001");
        }

        @Test
        @DisplayName("should throw exception when log not found")
        void findById_WhenNotFound_ShouldThrowException() {
            // Given
            when(storageStrategy.findById("non-existent")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> logService.findById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("日志不存在");
        }
    }

    @Nested
    @DisplayName("findPage Tests")
    class FindPageTests {

        @Test
        @DisplayName("should return page result")
        void findPage_ShouldReturnPageResult() {
            // Given
            PageResultResp<LogPageResp> pageResult = new PageResultResp<>(
                    List.of(), 0L, 1, 10
            );
            when(storageStrategy.findPage(any(LogQueryReq.class))).thenReturn(pageResult);

            // When
            PageResultResp<LogPageResp> result = logService.findPage(query);

            // Then
            assertThat(result).isNotNull();
            verify(storageStrategy).findPage(any(LogQueryReq.class));
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("should delete log by id")
        void deleteById_ShouldDeleteLog() {
            // Given
            doNothing().when(storageStrategy).deleteById("log-001");

            // When
            logService.deleteById("log-001");

            // Then
            verify(storageStrategy).deleteById("log-001");
        }
    }

    @Nested
    @DisplayName("deleteByIds Tests")
    class DeleteByIdsTests {

        @Test
        @DisplayName("should delete logs by ids")
        void deleteByIds_ShouldDeleteLogs() {
            // Given
            List<String> ids = List.of("log-001", "log-002");
            when(storageStrategy.deleteByIds(ids)).thenReturn(2);

            // When
            logService.deleteByIds(ids);

            // Then
            verify(storageStrategy).deleteByIds(ids);
        }
    }

    @Nested
    @DisplayName("deleteByCondition Tests")
    class DeleteByConditionTests {

        @Test
        @DisplayName("should delete logs by condition")
        void deleteByCondition_ShouldDeleteLogs() {
            // Given
            when(storageStrategy.deleteByCondition(any(LogQueryReq.class))).thenReturn(5);

            // When
            Integer result = logService.deleteByCondition(query);

            // Then
            assertThat(result).isEqualTo(5);
            verify(storageStrategy).deleteByCondition(any(LogQueryReq.class));
        }
    }

    @Nested
    @DisplayName("cleanupExpiredLogs Tests")
    class CleanupExpiredLogsTests {

        @Test
        @DisplayName("should return 0 when cleanup is disabled")
        void cleanupExpiredLogs_WhenDisabled_ShouldReturnZero() {
            // Given
            LogStorageProperties.CleanupConfig cleanup = mock(LogStorageProperties.CleanupConfig.class);
            when(logStorageProperties.getCleanup()).thenReturn(cleanup);
            when(cleanup.isEnabled()).thenReturn(false);

            // When
            Integer result = logService.cleanupExpiredLogs();

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("should cleanup expired logs when enabled")
        void cleanupExpiredLogs_WhenEnabled_ShouldCleanup() {
            // Given
            LogStorageProperties.CleanupConfig cleanup = mock(LogStorageProperties.CleanupConfig.class);
            when(logStorageProperties.getCleanup()).thenReturn(cleanup);
            when(cleanup.isEnabled()).thenReturn(true);
            when(cleanup.getRetentionDays()).thenReturn(30);
            when(cleanup.getBatchSize()).thenReturn(100);
            when(storageStrategy.cleanupExpiredLogs(30, 100)).thenReturn(50);

            // When
            Integer result = logService.cleanupExpiredLogs();

            // Then
            assertThat(result).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getStatistics Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("should return statistics")
        void getStatistics_ShouldReturnStatistics() {
            // Given
            LogStatisticsResp stats = mock(LogStatisticsResp.class);
            when(logStatisticsService.getStatistics()).thenReturn(stats);

            // When
            LogStatisticsResp result = logService.getStatistics();

            // Then
            assertThat(result).isNotNull();
            verify(logStatisticsService).getStatistics();
        }
    }
}