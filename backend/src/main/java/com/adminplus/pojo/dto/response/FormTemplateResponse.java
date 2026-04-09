package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 表单模板响应DTO
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
public record FormTemplateResponse(
        String id,
        String templateName,
        String templateCode,
        String category,
        String description,
        String formConfig,
        Integer status,
        Instant createTime,
        Instant updateTime
) {
}
