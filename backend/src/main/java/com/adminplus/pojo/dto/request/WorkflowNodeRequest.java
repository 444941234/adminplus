package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 工作流节点创建/更新请求
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Builder
public record WorkflowNodeRequest(
        @NotBlank(message = "节点名称不能为空")
        String nodeName,

        @NotBlank(message = "节点编码不能为空")
        String nodeCode,

        @NotNull(message = "节点顺序不能为空")
        Integer nodeOrder,

        @NotBlank(message = "审批类型不能为空")
        String approverType,

        String approverId,

        @NotNull(message = "是否会签不能为空")
        Boolean isCounterSign,

        @NotNull(message = "自动通过设置不能为空")
        Boolean autoPassSameUser,

        String description
) {
}
