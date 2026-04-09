package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 配置组视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigGroupResponse(
        String id,
        String name,
        String code,
        String icon,
        Integer sortOrder,
        String description,
        Integer status,
        Long configCount,
        Instant createTime,
        Instant updateTime
) {}
