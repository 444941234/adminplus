package com.adminplus.pojo.dto.resp;

/**
 * Dashboard 统计数据视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record DashboardStatsResp(
        /**
         * 用户总数
         */
        Long userCount,

        /**
         * 角色总数
         */
        Long roleCount,

        /**
         * 菜单总数
         */
        Long menuCount,

        /**
         * 日志总数
         */
        Long logCount
) {
}