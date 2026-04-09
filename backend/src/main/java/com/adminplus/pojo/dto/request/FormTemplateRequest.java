package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 表单模板请求DTO
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
public record FormTemplateRequest(
        @NotBlank(message = "表单名称不能为空")
        String templateName,

        @NotBlank(message = "表单标识不能为空")
        String templateCode,

        String category,

        String description,

        @NotNull(message = "状态不能为空")
        Integer status,

        String formConfig
) {
}
