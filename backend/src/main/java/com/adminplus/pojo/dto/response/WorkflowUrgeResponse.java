package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流催办记录响应
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public record WorkflowUrgeResponse(
        String id,
        String instanceId,
        String nodeId,
        String nodeName,
        String urgeUserId,
        String urgeUserName,
        String urgeTargetId,
        String urgeTargetName,
        String urgeContent,
        Boolean isRead,
        Instant readTime,
        Instant createTime
) {
}