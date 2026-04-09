package com.adminplus.service;

import com.adminplus.pojo.dto.response.LogStatisticsResponse;

/**
 * 日志统计服务接口
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
public interface LogStatisticsService {

    /**
     * 获取日志统计数据
     *
     * @return 统计数据
     */
    LogStatisticsResponse getStatistics();

    /**
     * 获取日志趋势数据（最近N天）
     *
     * @param days 天数
     * @return 趋势数据
     */
    LogStatisticsResponse getTrendData(int days);
}
