package com.adminplus.service.workflow.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.response.WorkflowOperationPermissionsResponse;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowCcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 工作流权限检查器
 * <p>
 * 提供工作流实例的权限检查功能，包括查看权限验证、审批权限判断和操作权限构建
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Component
@RequiredArgsConstructor
public class WorkflowPermissionChecker {

    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowCcRepository ccRepository;

    /**
     * 检查用户是否有权限查看工作流实例详情
     * <p>
     * 用户可查看的条件：
     * 1. 用户是发起人
     * 2. 用户是审批人（当前或历史）
     * 3. 用户是抄送人
     * </p>
     *
     * @param instance 工作流实例
     * @param userId   用户ID
     * @throws BizException 如果用户无权查看
     */
    public void checkViewAccess(WorkflowInstanceEntity instance, String userId) {
        // 1. 检查是否是发起人
        if (instance.getUserId().equals(userId)) {
            return;
        }

        // 2. 检查是否是审批人（当前或历史）
        boolean isApprover = approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId())
                .stream()
                .anyMatch(a -> a.getApproverId().equals(userId));

        if (isApprover) {
            return;
        }

        // 3. 检查是否是抄送人
        boolean isCcReceiver = ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId())
                .stream()
                .anyMatch(cc -> cc.getUserId().equals(userId));

        if (isCcReceiver) {
            return;
        }

        // 无权限访问
        throw new BizException("您无权查看该工作流实例");
    }

    /**
     * 判断用户是否可以审批当前工作流实例
     *
     * @param instance 工作流实例
     * @param userId   用户ID
     * @return 如果用户可以审批则返回true，否则返回false
     */
    public boolean canUserApprove(WorkflowInstanceEntity instance, String userId) {
        if (!instance.isRunning() || instance.getCurrentNodeId() == null) {
            return false;
        }

        return approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instance.getId(), instance.getCurrentNodeId())
                .stream()
                .filter(WorkflowApprovalEntity::isPending)
                .anyMatch(a -> a.getApproverId().equals(userId));
    }

    /**
     * 构建工作流操作权限响应
     *
     * @param instance   工作流实例
     * @param userId     当前用户ID
     * @param canApprove 是否可以审批
     * @return 操作权限响应对象
     */
    public WorkflowOperationPermissionsResponse buildOperationPermissions(
            WorkflowInstanceEntity instance, String userId, boolean canApprove) {
        boolean isOwner = Objects.equals(instance.getUserId(), userId);

        return new WorkflowOperationPermissionsResponse(
                canApprove,
                canApprove,
                canApprove,
                canApprove,
                canApprove,
                isOwner && instance.isRunning(),
                isOwner && (instance.isDraft() || instance.isRejected()),
                isOwner && instance.isCancellable()
        );
    }
}