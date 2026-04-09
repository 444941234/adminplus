package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 角色视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RoleResponse(
        String id,
        String code,
        String name,
        String description,
        Integer dataScope,
        Integer status,
        Integer sortOrder,
        Instant createTime,
        Instant updateTime
) {
}