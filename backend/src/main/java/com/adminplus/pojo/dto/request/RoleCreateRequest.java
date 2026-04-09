package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建角色请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RoleCreateRequest(
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 50, message = "角色编码长度不能超过50")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_:-]{1,49}$", message = "角色编码需以字母开头，只能包含字母、数字、下划线、冒号或短横线")
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