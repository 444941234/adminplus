package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 工作流发起请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record WorkflowStartReq(
        @NotBlank(message = "工作流定义ID不能为空")
        String definitionId,

        @NotBlank(message = "流程标题不能为空")
        String title,

        String businessData,

        String remark
) {
}
