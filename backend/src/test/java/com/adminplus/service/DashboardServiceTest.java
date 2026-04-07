package com.adminplus.service;

import com.adminplus.pojo.dto.resp.*;
import com.adminplus.repository.*;
import com.adminplus.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * DashboardService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private LogRepository logRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    @DisplayName("getStats Tests")
    class GetStatsTests {

        @Test
        @DisplayName("should return correct statistics")
        void getStats_ShouldReturnCorrectStatistics() {
            // Given
            when(userRepository.countByDeletedFalse()).thenReturn(100L);
            when(roleRepository.countByDeletedFalse()).thenReturn(10L);
            when(menuRepository.countByDeletedFalse()).thenReturn(50L);
            when(logRepository.countByCreateTimeBetweenAndDeletedFalse(any(), any())).thenReturn(20L);  // 今日日志数

            // When
            DashboardStatsResp result = dashboardService.getStats();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userCount()).isEqualTo(100L);
            assertThat(result.roleCount()).isEqualTo(10L);
            assertThat(result.menuCount()).isEqualTo(50L);
            assertThat(result.logCount()).isEqualTo(20L);  // 今日日志数

            verify(userRepository).countByDeletedFalse();
            verify(roleRepository).countByDeletedFalse();
            verify(menuRepository).countByDeletedFalse();
            verify(logRepository).countByCreateTimeBetweenAndDeletedFalse(any(), any());
        }

        @Test
        @DisplayName("should return zero counts when repositories are empty")
        void getStats_ShouldReturnZeroCounts_WhenRepositoriesAreEmpty() {
            // Given
            when(userRepository.countByDeletedFalse()).thenReturn(0L);
            when(roleRepository.countByDeletedFalse()).thenReturn(0L);
            when(menuRepository.countByDeletedFalse()).thenReturn(0L);
            when(logRepository.countByCreateTimeBetweenAndDeletedFalse(any(), any())).thenReturn(0L);

            // When
            DashboardStatsResp result = dashboardService.getStats();

            // Then
            assertThat(result.userCount()).isEqualTo(0L);
            assertThat(result.roleCount()).isEqualTo(0L);
            assertThat(result.menuCount()).isEqualTo(0L);
            assertThat(result.logCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getUserGrowthData Tests")
    class GetUserGrowthDataTests {

        @Test
        @DisplayName("should return chart data with 7 days")
        void getUserGrowthData_ShouldReturn7DaysData() {
            // Given
            when(userRepository.countByCreateTimeBetweenAndDeletedFalse(any(), any())).thenReturn(0L);

            // When
            ChartDataResp result = dashboardService.getUserGrowthData();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.labels()).hasSize(7);
            assertThat(result.values()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("getRoleDistributionData Tests")
    class GetRoleDistributionDataTests {

        @Test
        @DisplayName("should return role distribution data")
        void getRoleDistributionData_ShouldReturnData() {
            // Given
            when(roleRepository.findByDeletedFalse()).thenReturn(List.of());
            when(userRoleRepository.findAll()).thenReturn(List.of());

            // When
            ChartDataResp result = dashboardService.getRoleDistributionData();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.labels()).isNotNull();
            assertThat(result.values()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMenuDistributionData Tests")
    class GetMenuDistributionDataTests {

        @Test
        @DisplayName("should return menu distribution data")
        void getMenuDistributionData_ShouldReturnData() {
            // Given
            when(menuRepository.countByTypeAndDeletedFalse(anyInt())).thenReturn(0L);

            // When
            ChartDataResp result = dashboardService.getMenuDistributionData();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.labels()).isNotNull();
            assertThat(result.values()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getRecentOperationLogs Tests")
    class GetRecentOperationLogsTests {

        @Test
        @DisplayName("should return recent logs")
        void getRecentOperationLogs_ShouldReturnLogs() {
            // Given
            when(logRepository.findTop10ByDeletedFalseOrderByCreateTimeDesc())
                    .thenReturn(List.of());

            // When
            List<OperationLogResp> result = dashboardService.getRecentOperationLogs();

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSystemInfo Tests")
    class GetSystemInfoTests {

        @Test
        @DisplayName("should return system info with fallback db version")
        void getSystemInfo_ShouldReturnInfo() {
            // When - DataSource 是 mock，无法获取真实连接，会返回 "Unknown"
            SystemInfoResp result = dashboardService.getSystemInfo();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.systemName()).isEqualTo("AdminPlus");
            // 当 mock DataSource 无法获取连接时，返回 "Unknown"
            assertThat(result.databaseVersion()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getOnlineUsers Tests")
    class GetOnlineUsersTests {

        @Test
        @DisplayName("should return online users list")
        void getOnlineUsers_ShouldReturnList() {
            // Given - Mock 返回空的 token 列表
            when(refreshTokenRepository.findValidTokens(any())).thenReturn(List.of());

            // When
            List<OnlineUserResp> result = dashboardService.getOnlineUsers();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(refreshTokenRepository).findValidTokens(any());
        }
    }

    @Nested
    @DisplayName("getStatistics Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("should return statistics")
        void getStatistics_ShouldReturnStatistics() {
            // Given
            when(userRepository.countByDeletedFalse()).thenReturn(100L);
            when(logRepository.countByCreateTimeBetweenAndDeletedFalse(any(), any())).thenReturn(50L);
            when(logRepository.countDistinctUsersByTimeRange(any(), any())).thenReturn(30L);

            // When
            StatisticsResp result = dashboardService.getStatistics();

            // Then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getVisitTrendData Tests")
    class GetVisitTrendDataTests {

        @Test
        @DisplayName("should return visit trend data with 7 days")
        void getVisitTrendData_ShouldReturn7DaysData() {
            // Given
            when(logRepository.countByCreateTimeBetweenAndDeletedFalse(any(), any())).thenReturn(0L);

            // When
            ChartDataResp result = dashboardService.getVisitTrendData();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.labels()).hasSize(7);
            assertThat(result.values()).hasSize(7);
        }
    }
}