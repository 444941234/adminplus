package com.adminplus.pojo.dto.response;

import java.util.Map;
import java.util.List;

/**
 * 工作流详情响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowDetailResponse(
        WorkflowInstanceResponse instance,
        List<WorkflowApprovalResponse> approvals,
        List<WorkflowNodeResponse> nodes,
        // 当前节点信息
        WorkflowNodeResponse currentNode,
        // 当前用户是否可以审批
        Boolean canApprove,
        String formConfig,
        Map<String, Object> formData,
        List<WorkflowCcResponse> ccRecords,
        List<WorkflowAddSignResponse> addSignRecords,
        WorkflowOperationPermissionsResponse operationPermissions
) {
}
