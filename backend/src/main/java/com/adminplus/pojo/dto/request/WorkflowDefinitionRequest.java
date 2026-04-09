package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 工作流定义创建/更新请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record WorkflowDefinitionRequest(
        @NotBlank(message = "工作流名称不能为空")
        String definitionName,

        @NotBlank(message = "工作流标识不能为空")
        String definitionKey,

        String category,

        String description,

        @NotNull(message = "状态不能为空")
        Integer status,

        String formConfig
) {
}
