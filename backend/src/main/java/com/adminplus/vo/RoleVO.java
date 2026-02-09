package com.adminplus.vo;

import java.time.Instant;
import java.util.List;

/**
 * 角色视图对象
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RoleVO(
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