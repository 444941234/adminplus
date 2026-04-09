package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 配置组更新请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigGroupUpdateRequest(
        @Size(max = 50, message = "配置组名称长度不能超过50")
        String name,

        @Size(max = 50, message = "图标长度不能超过50")
        String icon,

        Integer sortOrder,

        @Size(max = 200, message = "描述长度不能超过200")
        String description
) {}
