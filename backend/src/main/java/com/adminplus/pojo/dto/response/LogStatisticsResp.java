package com.adminplus.pojo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 日志统计响应 VO
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Schema(description = "日志统计响应")
public record LogStatisticsResp(
        @Schema(description = "总日志数")
        Long totalCount,

        @Schema(description = "操作日志数")
        Long operationCount,

        @Schema(description = "登录日志数")
        Long loginCount,

        @Schema(description = "系统日志数")
        Long systemCount,

        @Schema(description = "今日日志数")
        Long todayCount,

        @Schema(description = "成功日志数")
        Long successCount,

        @Schema(description = "失败日志数")
        Long failureCount,

        @Schema(description = "按类型统计")
        Map<Integer, Long> countByType,

        @Schema(description = "按状态统计")
        Map<Integer, Long> countByStatus,

        @Schema(description = "按日期统计（最近7天）")
        Map<String, Long> countByDate,

        @Schema(description = "按操作类型统计")
        Map<Integer, Long> countByOperationType
) {
}
