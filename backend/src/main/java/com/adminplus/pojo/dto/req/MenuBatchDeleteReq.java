package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 批量删除菜单请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-08
 */
public record MenuBatchDeleteReq(

        @NotEmpty(message = "菜单ID列表不能为空")
        List<String> ids
) {
}