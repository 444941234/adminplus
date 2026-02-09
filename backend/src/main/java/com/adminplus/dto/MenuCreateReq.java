package com.adminplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建菜单请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record MenuCreateReq(
         String parentId,

        @NotNull(message = "菜单类型不能为空")
        Integer type,

        @NotBlank(message = "菜单名称不能为空")
        @Size(max = 50, message = "菜单名称长度不能超过50")
        String name,

        @Size(max = 200, message = "路由路径长度不能超过200")
        String path,

        @Size(max = 200, message = "组件路径长度不能超过200")
        String component,

        @Size(max = 100, message = "权限标识符长度不能超过100")
        String permKey,

        @Size(max = 50, message = "图标长度不能超过50")
        String icon,

        Integer sortOrder,

        @NotNull(message = "是否可见不能为空")
        Integer visible,

        @NotNull(message = "状态不能为空")
        Integer status
) {
}