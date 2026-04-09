package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流定义响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowDefinitionResponse(
        String id,
        String definitionName,
        String definitionKey,
        String category,
        String description,
        Integer status,
        Integer version,
        String formConfig,
        Integer nodeCount,
        Instant createTime,
        Instant updateTime
) {
}
