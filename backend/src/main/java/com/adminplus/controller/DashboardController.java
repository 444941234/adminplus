package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard 控制器
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@RestController
@RequestMapping("/v1/sys/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard管理", description = "统计数据接口")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public ApiResponse<DashboardStatsResp> getStats() {
        return ApiResponse.ok(dashboardService.getStats());
    }

    @GetMapping("/user-growth")
    @Operation(summary = "获取用户增长趋势")
    public ApiResponse<ChartDataResp> getUserGrowth() {
        return ApiResponse.ok(dashboardService.getUserGrowthData());
    }

    @GetMapping("/role-distribution")
    @Operation(summary = "获取角色分布")
    public ApiResponse<ChartDataResp> getRoleDistribution() {
        return ApiResponse.ok(dashboardService.getRoleDistributionData());
    }

    @GetMapping("/menu-distribution")
    @Operation(summary = "获取菜单类型分布")
    public ApiResponse<ChartDataResp> getMenuDistribution() {
        return ApiResponse.ok(dashboardService.getMenuDistributionData());
    }

    @GetMapping("/recent-logs")
    @Operation(summary = "获取最近操作日志")
    public ApiResponse<List<OperationLogResp>> getRecentLogs() {
        return ApiResponse.ok(dashboardService.getRecentOperationLogs());
    }

    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息")
    @PreAuthorize("hasAuthority('system:config') or hasAuthority('*')")
    public ApiResponse<SystemInfoResp> getSystemInfo() {
        return ApiResponse.ok(dashboardService.getSystemInfo());
    }

    @GetMapping("/online-users")
    @Operation(summary = "获取在线用户")
    @PreAuthorize("hasAuthority('user:query') or hasAuthority('*')")
    public ApiResponse<List<OnlineUserResp>> getOnlineUsers() {
        return ApiResponse.ok(dashboardService.getOnlineUsers());
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据（Statistics页面）")
    public ApiResponse<StatisticsResp> getStatistics() {
        return ApiResponse.ok(dashboardService.getStatistics());
    }

    @GetMapping("/visit-trend")
    @Operation(summary = "获取访问量趋势")
    public ApiResponse<ChartDataResp> getVisitTrend() {
        return ApiResponse.ok(dashboardService.getVisitTrendData());
    }
}
