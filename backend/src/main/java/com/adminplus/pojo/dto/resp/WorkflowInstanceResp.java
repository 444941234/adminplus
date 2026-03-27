package com.adminplus.pojo.dto.resp;

import java.time.Instant;

/**
 * 工作流实例响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowInstanceResp(
        String id,
        String definitionId,
        String definitionName,
        String userId,
        String userName,
        String deptId,
        String deptName,
        String title,
        String businessData,
        String currentNodeId,
        String currentNodeName,
        String status,
        Instant submitTime,
        Instant finishTime,
        String remark,
        Instant createTime,
        // 待审批标志
        Boolean pendingApproval,
        // 当前用户是否可以审批
        Boolean canApprove,
        Boolean canWithdraw,
        Boolean canCancel,
        Boolean canUrge,
        Boolean canEditDraft,
        Boolean canSubmitDraft
) {
}
