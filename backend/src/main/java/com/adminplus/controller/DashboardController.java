package com.adminplus.controller;

import com.adminplus.service.DashboardService;
import com.adminplus.utils.ApiResponse;
import com.adminplus.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/v1/sys/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard管理", description = "统计数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    @PreAuthorize("hasAuthority('dashboard:stats')")
    public ApiResponse<DashboardStatsVO> getStats() {
        log.info("获取 Dashboard 统计数据");
        DashboardStatsVO stats = dashboardService.getStats();
        return ApiResponse.ok(stats);
    }

    @GetMapping("/user-growth")
    @Operation(summary = "获取用户增长趋势")
    @PreAuthorize("hasAuthority('dashboard:stats')")
    public ApiResponse<ChartDataVO> getUserGrowth() {
        log.info("获取用户增长趋势数据");
        ChartDataVO data = dashboardService.getUserGrowthData();
        return ApiResponse.ok(data);
    }

    @GetMapping("/role-distribution")
    @Operation(summary = "获取角色分布")
    @PreAuthorize("hasAuthority('dashboard:stats')")
    public ApiResponse<ChartDataVO> getRoleDistribution() {
        log.info("获取角色分布数据");
        ChartDataVO data = dashboardService.getRoleDistributionData();
        return ApiResponse.ok(data);
    }

    @GetMapping("/menu-distribution")
    @Operation(summary = "获取菜单类型分布")
    @PreAuthorize("hasAuthority('dashboard:stats')")
    public ApiResponse<ChartDataVO> getMenuDistribution() {
        log.info("获取菜单类型分布数据");
        ChartDataVO data = dashboardService.getMenuDistributionData();
        return ApiResponse.ok(data);
    }

    @GetMapping("/recent-logs")
    @Operation(summary = "获取最近操作日志")
    @PreAuthorize("hasAuthority('dashboard:logs')")
    public ApiResponse<List<OperationLogVO>> getRecentLogs() {
        log.info("获取最近操作日志");
        List<OperationLogVO> logs = dashboardService.getRecentOperationLogs();
        return ApiResponse.ok(logs);
    }

    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息")
    @PreAuthorize("hasAuthority('dashboard:system')")
    public ApiResponse<SystemInfoVO> getSystemInfo() {
        log.info("获取系统信息");
        SystemInfoVO info = dashboardService.getSystemInfo();
        return ApiResponse.ok(info);
    }

    @GetMapping("/online-users")
    @Operation(summary = "获取在线用户")
    @PreAuthorize("hasAuthority('dashboard:online')")
    public ApiResponse<List<OnlineUserVO>> getOnlineUsers() {
        log.info("获取在线用户列表");
        List<OnlineUserVO> users = dashboardService.getOnlineUsers();
        return ApiResponse.ok(users);
    }
}