package com.adminplus.service;

import com.adminplus.pojo.dto.response.*;

import java.util.List;

/**
 * Dashboard 服务接口
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public interface DashboardService {

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    DashboardStatsResponse getStats();

    /**
     * 清除统计数据缓存
     */
    void clearStatsCache();

    /**
     * 获取用户增长趋势数据
     *
     * @return 图表数据
     */
    ChartDataResponse getUserGrowthData();

    /**
     * 获取角色分布数据
     *
     * @return 图表数据
     */
    ChartDataResponse getRoleDistributionData();

    /**
     * 获取菜单类型分布数据
     *
     * @return 图表数据
     */
    ChartDataResponse getMenuDistributionData();

    /**
     * 获取最近操作日志
     *
     * @return 操作日志列表
     */
    List<OperationLogResponse> getRecentOperationLogs();

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    SystemInfoResponse getSystemInfo();

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserResponse> getOnlineUsers();

    /**
     * 获取统计数据（用于 Statistics 页面）
     *
     * @return 统计数据
     */
    StatisticsResponse getStatistics();

    /**
     * 获取访问量趋势数据（最近7天）
     *
     * @return 图表数据
     */
    ChartDataResponse getVisitTrendData();
}