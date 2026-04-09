package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 配置变更历史视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigHistoryResp(
        String id,
        String configId,
        String configKey,
        String oldValue,
        String newValue,
        String remark,
        String operatorName,
        Instant createTime
) {}
