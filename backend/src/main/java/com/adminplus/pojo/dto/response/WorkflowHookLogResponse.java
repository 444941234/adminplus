package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流钩子日志响应对象
 *
 * @author AdminPlus
 */
public record WorkflowHookLogResponse(
        String id,
        String instanceId,
        String nodeId,
        String hookId,
        String hookSource,
        String hookPoint,
        String executorType,
        String executorConfig,
        Boolean success,
        String resultCode,
        String resultMessage,
        Long executionTime,
        Integer retryAttempts,
        Boolean async,
        String operatorId,
        String operatorName,
        String createUser,
        String updateUser,
        Instant createTime,
        Instant updateTime
) {
}
