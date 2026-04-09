package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流审批记录响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowApprovalResp(
        String id,
        String instanceId,
        String nodeId,
        String nodeName,
        String approverId,
        String approverName,
        String approvalStatus,
        String comment,
        String attachments,
        Instant approvalTime,
        Instant createTime
) {
}
