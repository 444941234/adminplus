package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

/**
 * 工作流发起请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record WorkflowStartRequest(
        @NotBlank(message = "工作流定义ID不能为空")
        String definitionId,

        @NotBlank(message = "流程标题不能为空")
        String title,

        Map<String, Object> formData,

        String remark
) {
}
