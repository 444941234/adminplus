package com.adminplus.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量更新菜单状态请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record MenuBatchStatusReq(

        @NotEmpty(message = "菜单ID列表不能为空")
        List<String> ids,

        @NotNull(message = "状态不能为空")
        Integer status
) {
}