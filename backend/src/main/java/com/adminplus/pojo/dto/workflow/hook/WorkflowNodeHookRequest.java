package com.adminplus.pojo.dto.workflow.hook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 工作流节点钩子配置请求DTO
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
public record WorkflowNodeHookRequest(
        @NotBlank(message = "节点ID不能为空")
        String nodeId,

        @NotBlank(message = "钩子点不能为空")
        String hookPoint,

        @NotBlank(message = "钩子类型不能为空")
        String hookType,

        @NotBlank(message = "执行方式不能为空")
        String executorType,

        String executorConfig,

        @NotNull(message = "是否异步执行不能为空")
        Boolean asyncExecution,

        @NotNull(message = "失败时是否阻断不能为空")
        Boolean blockOnFailure,

        String failureMessage,

        @NotNull(message = "优先级不能为空")
        Integer priority,

        String conditionExpression,

        @NotNull(message = "重试次数不能为空")
        Integer retryCount,

        Integer retryInterval,

        String hookName,

        String description
) {
}