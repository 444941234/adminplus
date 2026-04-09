package com.adminplus.pojo.dto.response;

/**
 * 单个活动项视图对象
 *
 * @author AdminPlus
 * @since 2026-03-20
 */
public record ActivityItemResp(
        String id,
        String action,
        String timestamp,
        String type
) {
}
