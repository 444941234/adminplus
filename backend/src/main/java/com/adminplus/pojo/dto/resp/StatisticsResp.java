package com.adminplus.pojo.dto.resp;

/**
 * 统计数据视图对象（用于 Statistics 页面）
 *
 * @author AdminPlus
 * @since 2026-03-02
 */
public record StatisticsResp(
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
        ChartDataResp userGrowthData,

        /**
         * 访问量趋势数据
         */
        ChartDataResp visitTrendData
) {
}