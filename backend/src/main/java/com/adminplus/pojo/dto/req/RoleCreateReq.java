package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建角色请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RoleCreateReq(
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 50, message = "角色编码长度不能超过50")
        String code,

        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称长度不能超过50")
        String name,

        @Size(max = 200, message = "描述长度不能超过200")
        String description,

        @NotNull(message = "数据权限范围不能为空")
        Integer dataScope,

        Integer status,

        Integer sortOrder
) {
}