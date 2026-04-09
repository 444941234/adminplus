package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流节点钩子配置响应对象
 *
 * @author AdminPlus
 */
public record WorkflowNodeHookResponse(
        String id,
        String nodeId,
        String hookPoint,
        String hookType,
        String executorType,
        String executorConfig,
        Boolean asyncExecution,
        Boolean blockOnFailure,
        String failureMessage,
        Integer priority,
        String conditionExpression,
        Integer retryCount,
        Integer retryInterval,
        String hookName,
        String description,
        String createUser,
        String updateUser,
        Instant createTime,
        Instant updateTime
) {
}
