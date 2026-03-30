package com.adminplus.pojo.dto.resp;

import java.time.Instant;

/**
 * 配置项视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigResp(
        String id,
        String groupId,
        String groupName,
        String name,
        String key,
        String value,
        String valueType,
        String effectType,
        String defaultValue,
        String description,
        Boolean isRequired,
        String validationRule,
        Integer sortOrder,
        Integer status,
        Instant createTime,
        Instant updateTime
) {}
