package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流抄送记录响应
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public record WorkflowCcResp(
        String id,
        String instanceId,
        String nodeId,
        String nodeName,
        String userId,
        String userName,
        String ccType,
        String ccContent,
        Boolean isRead,
        Instant readTime,
        Instant createTime
) {
}
