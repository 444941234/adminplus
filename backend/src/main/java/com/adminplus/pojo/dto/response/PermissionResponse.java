package com.adminplus.pojo.dto.response;

/**
 * 权限视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record PermissionResponse(
        String id,
        String permKey,
        String name,
        Integer type,
        String parentId
) {
}