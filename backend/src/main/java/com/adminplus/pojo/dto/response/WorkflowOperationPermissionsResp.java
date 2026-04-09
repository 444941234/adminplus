package com.adminplus.pojo.dto.response;

/**
 * 工作流操作权限响应
 *
 * @author AdminPlus
 * @since 2026-03-27
 */
public record WorkflowOperationPermissionsResp(
        Boolean canApprove,
        Boolean canReject,
        Boolean canRollback,
        Boolean canAddSign,
        Boolean canTransfer,
        Boolean canUrge,
        Boolean canWithdraw,
        Boolean canCancel
) {
}
