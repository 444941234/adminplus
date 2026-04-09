package com.adminplus.pojo.dto.response;

import java.time.Instant;

/**
 * 工作流节点响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowNodeResp(
        String id,
        String definitionId,
        String nodeName,
        String nodeCode,
        Integer nodeOrder,
        String approverType,
        String approverId,
        Boolean isCounterSign,
        Boolean autoPassSameUser,
        String description,
        Instant createTime
) {
}
