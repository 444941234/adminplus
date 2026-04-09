package com.adminplus.pojo.dto.response;

import java.util.Map;
import java.util.List;

/**
 * 工作流详情响应
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public record WorkflowDetailResp(
        WorkflowInstanceResp instance,
        List<WorkflowApprovalResp> approvals,
        List<WorkflowNodeResp> nodes,
        // 当前节点信息
        WorkflowNodeResp currentNode,
        // 当前用户是否可以审批
        Boolean canApprove,
        String formConfig,
        Map<String, Object> formData,
        List<WorkflowCcResp> ccRecords,
        List<WorkflowAddSignResp> addSignRecords,
        WorkflowOperationPermissionsResp operationPermissions
) {
}
