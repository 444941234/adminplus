package com.adminplus.service;

import com.adminplus.pojo.dto.resp.*;

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
    DashboardStatsResp getStats();

    /**
     * 获取用户增长趋势数据
     *
     * @return 图表数据
     */
    ChartDataResp getUserGrowthData();

    /**
     * 获取角色分布数据
     *
     * @return 图表数据
     */
    ChartDataResp getRoleDistributionData();

    /**
     * 获取菜单类型分布数据
     *
     * @return 图表数据
     */
    ChartDataResp getMenuDistributionData();

    /**
     * 获取最近操作日志
     *
     * @return 操作日志列表
     */
    List<OperationLogResp> getRecentOperationLogs();

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    SystemInfoResp getSystemInfo();

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserResp> getOnlineUsers();
}