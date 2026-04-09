package com.adminplus.controller;

import com.adminplus.pojo.dto.response.ChartDataResponse;
import com.adminplus.pojo.dto.response.DashboardStatsResponse;
import com.adminplus.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DashboardController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Unit Tests")
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Nested
    @DisplayName("getStats Tests")
    class GetStatsTests {

        @Test
        @DisplayName("should return dashboard stats")
        void getStats_ShouldReturnStats() throws Exception {
            // Given
            DashboardStatsResponse stats = new DashboardStatsResponse(100L, 10L, 50L, 1000L);
            when(dashboardService.getStats()).thenReturn(stats);

            // When & Then
            mockMvc.perform(get("/v1/sys/dashboard/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userCount").value(100));
        }
    }

    @Nested
    @DisplayName("getUserGrowth Tests")
    class GetUserGrowthTests {

        @Test
        @DisplayName("should return user growth data")
        void getUserGrowth_ShouldReturnData() throws Exception {
            // Given
            ChartDataResponse data = new ChartDataResponse(
                    List.of("2026-01", "2026-02", "2026-03"),
                    List.of(10L, 20L, 30L)
            );
            when(dashboardService.getUserGrowthData()).thenReturn(data);

            // When & Then
            mockMvc.perform(get("/v1/sys/dashboard/user-growth"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.labels[0]").value("2026-01"));
        }
    }

    @Nested
    @DisplayName("getRoleDistribution Tests")
    class GetRoleDistributionTests {

        @Test
        @DisplayName("should return role distribution data")
        void getRoleDistribution_ShouldReturnData() throws Exception {
            // Given
            ChartDataResponse data = new ChartDataResponse(
                    List.of("管理员", "普通用户"),
                    List.of(5L, 95L)
            );
            when(dashboardService.getRoleDistributionData()).thenReturn(data);

            // When & Then
            mockMvc.perform(get("/v1/sys/dashboard/role-distribution"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.labels[0]").value("管理员"));
        }
    }

    @Nested
    @DisplayName("getMenuDistribution Tests")
    class GetMenuDistributionTests {

        @Test
        @DisplayName("should return menu distribution data")
        void getMenuDistribution_ShouldReturnData() throws Exception {
            // Given
            ChartDataResponse data = new ChartDataResponse(
                    List.of("目录", "菜单", "按钮"),
                    List.of(10L, 30L, 50L)
            );
            when(dashboardService.getMenuDistributionData()).thenReturn(data);

            // When & Then
            mockMvc.perform(get("/v1/sys/dashboard/menu-distribution"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.labels[0]").value("目录"));
        }
    }

    @Nested
    @DisplayName("getVisitTrend Tests")
    class GetVisitTrendTests {

        @Test
        @DisplayName("should return visit trend data")
        void getVisitTrend_ShouldReturnData() throws Exception {
            // Given
            ChartDataResponse data = new ChartDataResponse(
                    List.of("周一", "周二", "周三"),
                    List.of(100L, 150L, 200L)
            );
            when(dashboardService.getVisitTrendData()).thenReturn(data);

            // When & Then
            mockMvc.perform(get("/v1/sys/dashboard/visit-trend"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.labels[0]").value("周一"));
        }
    }
}