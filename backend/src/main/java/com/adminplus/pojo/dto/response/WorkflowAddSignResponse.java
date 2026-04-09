package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流加签记录响应
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public record WorkflowAddSignResponse(
        String id,
        String instanceId,
        String nodeId,
        String nodeName,
        String initiatorId,
        String initiatorName,
        String addUserId,
        String addUserName,
        String addType,
        String addReason,
        String originalApproverId,
        Instant createTime
) {
}