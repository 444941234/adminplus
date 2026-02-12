package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.Size;

/**
 * 更新角色请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RoleUpdateReq(
        @Size(max = 50, message = "角色名称长度不能超过50")
        String name,

        @Size(max = 200, message = "描述长度不能超过200")
        String description,

        Integer dataScope,

        Integer status,

        Integer sortOrder
) {
}