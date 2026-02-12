package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<DashboardStatsResp> getStats() {
        log.info("获取 Dashboard 统计数据");
        DashboardStatsResp stats = dashboardService.getStats();
        return ApiResponse.ok(stats);
    }

    @GetMapping("/user-growth")
    @Operation(summary = "获取用户增长趋势")
    public ApiResponse<ChartDataResp> getUserGrowth() {
        log.info("获取用户增长趋势数据 - 开始");
        try {
            ChartDataResp data = dashboardService.getUserGrowthData();
            log.info("获取用户增长趋势数据 - 成功, labels: {}, values: {}",
                     data.labels(), data.values());
            return ApiResponse.ok(data);
        } catch (Exception e) {
            log.error("获取用户增长趋势数据 - 失败", e);
            throw e;
        }
    }

    @GetMapping("/role-distribution")
    @Operation(summary = "获取角色分布")
    public ApiResponse<ChartDataResp> getRoleDistribution() {
        log.info("获取角色分布数据 - 开始");
        try {
            ChartDataResp data = dashboardService.getRoleDistributionData();
            log.info("获取角色分布数据 - 成功, labels: {}, values: {}",
                     data.labels(), data.values());
            return ApiResponse.ok(data);
        } catch (Exception e) {
            log.error("获取角色分布数据 - 失败", e);
            throw e;
        }
    }

    @GetMapping("/menu-distribution")
    @Operation(summary = "获取菜单类型分布")
    public ApiResponse<ChartDataResp> getMenuDistribution() {
        log.info("获取菜单类型分布数据 - 开始");
        try {
            ChartDataResp data = dashboardService.getMenuDistributionData();
            log.info("获取菜单类型分布数据 - 成功, labels: {}, values: {}",
                     data.labels(), data.values());
            return ApiResponse.ok(data);
        } catch (Exception e) {
            log.error("获取菜单类型分布数据 - 失败", e);
            throw e;
        }
    }

    @GetMapping("/recent-logs")
    @Operation(summary = "获取最近操作日志")
    public ApiResponse<List<OperationLogResp>> getRecentLogs() {
        log.info("获取最近操作日志");
        List<OperationLogResp> logs = dashboardService.getRecentOperationLogs();
        return ApiResponse.ok(logs);
    }

    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息")
    public ApiResponse<SystemInfoResp> getSystemInfo() {
        log.info("获取系统信息");
        SystemInfoResp info = dashboardService.getSystemInfo();
        return ApiResponse.ok(info);
    }

    @GetMapping("/online-users")
    @Operation(summary = "获取在线用户")
    public ApiResponse<List<OnlineUserResp>> getOnlineUsers() {
        log.info("获取在线用户列表");
        List<OnlineUserResp> users = dashboardService.getOnlineUsers();
        return ApiResponse.ok(users);
    }
}