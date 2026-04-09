package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 复制菜单请求 DTO
 *
 * @author AdminPlus
 * @since 2026-04-09
 */
public record CopyMenuReq(

        @NotBlank(message = "目标父级ID不能为空")
        String targetParentId
) {
}
