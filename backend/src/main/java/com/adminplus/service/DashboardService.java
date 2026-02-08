package com.adminplus.service;

import com.adminplus.vo.*;

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
    DashboardStatsVO getStats();

    /**
     * 获取用户增长趋势数据
     *
     * @return 图表数据
     */
    ChartDataVO getUserGrowthData();

    /**
     * 获取角色分布数据
     *
     * @return 图表数据
     */
    ChartDataVO getRoleDistributionData();

    /**
     * 获取菜单类型分布数据
     *
     * @return 图表数据
     */
    ChartDataVO getMenuDistributionData();

    /**
     * 获取最近操作日志
     *
     * @return 操作日志列表
     */
    List<OperationLogVO> getRecentOperationLogs();

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    SystemInfoVO getSystemInfo();

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> getOnlineUsers();
}