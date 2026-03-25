package com.adminplus.service;

import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.service.impl.LogStorageStrategySelector;
import com.adminplus.service.impl.LogStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LogStatisticsService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogStatisticsService Unit Tests")
class LogStatisticsServiceTest {

    @Mock
    private LogStorageStrategySelector storageStrategySelector;

    @Mock
    private LogStorageStrategy storageStrategy;

    @InjectMocks
    private LogStatisticsServiceImpl logStatisticsService;

    @BeforeEach
    void setUp() {
        lenient().when(storageStrategySelector.getStrategy()).thenReturn(storageStrategy);
    }

    @Nested
    @DisplayName("getStatistics Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("should return statistics with all counts")
        void getStatistics_ShouldReturnStatistics() {
            // Given
            when(storageStrategy.count()).thenReturn(100L);
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(10L);

            // When
            LogStatisticsResp result = logStatisticsService.getStatistics();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.totalCount()).isEqualTo(100L);
            assertThat(result.countByType()).isNotNull();
            assertThat(result.countByStatus()).isNotNull();
            assertThat(result.countByDate()).hasSize(7);
            assertThat(result.countByOperationType()).hasSize(7);
        }

        @Test
        @DisplayName("should return zero counts when no logs exist")
        void getStatistics_WhenNoLogs_ShouldReturnZeroCounts() {
            // Given
            when(storageStrategy.count()).thenReturn(0L);
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(0L);

            // When
            LogStatisticsResp result = logStatisticsService.getStatistics();

            // Then
            assertThat(result.totalCount()).isEqualTo(0L);
            assertThat(result.operationCount()).isEqualTo(0L);
            assertThat(result.loginCount()).isEqualTo(0L);
            assertThat(result.systemCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("should call storage strategy for each statistic")
        void getStatistics_ShouldCallStrategyMethods() {
            // Given
            when(storageStrategy.count()).thenReturn(100L);
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(10L);

            // When
            logStatisticsService.getStatistics();

            // Then
            verify(storageStrategy).count();
            verify(storageStrategy, atLeastOnce()).countByCondition(any(LogQueryDTO.class));
        }
    }

    @Nested
    @DisplayName("getTrendData Tests")
    class GetTrendDataTests {

        @Test
        @DisplayName("should return trend data for specified days")
        void getTrendData_ShouldReturnDataForDays() {
            // Given
            int days = 7;
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(5L);

            // When
            LogStatisticsResp result = logStatisticsService.getTrendData(days);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.countByDate()).hasSize(days);
        }

        @Test
        @DisplayName("should return trend data for 30 days")
        void getTrendData_ShouldReturnDataFor30Days() {
            // Given
            int days = 30;
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(3L);

            // When
            LogStatisticsResp result = logStatisticsService.getTrendData(days);

            // Then
            assertThat(result.countByDate()).hasSize(days);
        }

        @Test
        @DisplayName("should calculate total count from trend data")
        void getTrendData_ShouldCalculateTotalCount() {
            // Given
            int days = 3;
            when(storageStrategy.countByCondition(any(LogQueryDTO.class))).thenReturn(10L);

            // When
            LogStatisticsResp result = logStatisticsService.getTrendData(days);

            // Then
            assertThat(result.totalCount()).isEqualTo(30L); // 10 * 3 days
        }
    }
}