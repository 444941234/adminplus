package com.adminplus.pojo.dto.response;

/**
 * 统计数据视图对象（用于 Statistics 页面）
 *
 * @author AdminPlus
 * @since 2026-03-02
 */
public record StatisticsResponse(
        /**
         * 总用户数
         */
        Long totalUsers,

        /**
         * 今日访问量
         */
        Long todayVisits,

        /**
         * 活跃用户数（今日有操作的用户）
         */
        Long activeUsers,

        /**
         * 今日新增注册
         */
        Long todayNewUsers,

        /**
         * 用户增长趋势数据
         */
        ChartDataResponse userGrowthData,

        /**
         * 访问量趋势数据
         */
        ChartDataResponse visitTrendData
) {
}