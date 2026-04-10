package com.adminplus.service.workflow.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.adminplus.common.exception.BizException;
import com.adminplus.enums.WorkflowStatus;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.workflow.WorkflowApprovalService;
import com.adminplus.service.workflow.WorkflowRollbackService;
import com.adminplus.service.workflow.hook.WorkflowHookService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.ServiceAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流回退服务实现
 * <p>
 * 负责工作流的回退操作，包括回退到指定节点、获取可回退节点列表等功能
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowRollbackServiceImpl implements WorkflowRollbackService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowApprovalService approvalService;
    private final WorkflowHookService hookService;
    private final WorkflowPermissionChecker permissionChecker;
    private final ConversionService conversionService;
    private final JsonMapper objectMapper;

    @Override
    @Transactional
    public WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request) {
        String userId = getCurrentUserId();
        log.info("回退工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.isRunning(), "只有进行中的工作流可以回退");

        // 验证审批权限
        WorkflowApprovalEntity approval = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending() && a.getApproverId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BizException("您没有权限回退此工作流"));

        // 解析目标节点ID（从请求中获取）
        String requestedTargetNodeId = request.targetNodeId();
        String finalTargetNodeId;
        if (requestedTargetNodeId == null || requestedTargetNodeId.isEmpty()) {
            // 如果没有指定目标节点，则回退到上一节点
            finalTargetNodeId = findPreviousNodeId(instance);
            if (finalTargetNodeId == null) {
                throw new BizException("没有可以回退的节点");
            }
        } else {
            finalTargetNodeId = requestedTargetNodeId;
        }

        // 验证目标节点是否在可回退范围内
        List<WorkflowNodeResponse> rollbackableNodes = getRollbackableNodes(instanceId);
        String targetNodeIdForValidation = finalTargetNodeId;
        boolean isValidTarget = rollbackableNodes.stream()
                .anyMatch(n -> n.id().equals(targetNodeIdForValidation));
        ServiceAssert.isTrue(isValidTarget, "无法回退到指定节点");

        WorkflowNodeEntity targetNode = EntityHelper.findByIdOrThrow(
            nodeRepository::findById, finalTargetNodeId, "目标节点不存在");

        // 获取当前节点
        WorkflowNodeEntity currentNode = EntityHelper.findByIdOrThrow(
            nodeRepository::findById, instance.getCurrentNodeId(), "当前节点不存在");

        // 回退前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_ROLLBACK", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", request, "targetNodeId", finalTargetNodeId)
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "回退前校验失败" : preResult.blockingMessages().get(0));
        }

        // 获取当前节点信息
        String currentNodeId = instance.getCurrentNodeId();
        String currentNodeName = instance.getCurrentNodeName();

        // 更新当前审批记录为回退状态
        approval.setApprovalStatus("rejected");
        approval.setComment(request.comment());
        approval.setApprovalTime(Instant.now());
        approval.setIsRollback(true);
        approval.setRollbackFromNodeId(currentNodeId);
        approval.setRollbackFromNodeName(currentNodeName);
        approvalRepository.save(approval);

        // 更新实例状态到目标节点
        instance.setCurrentNodeId(finalTargetNodeId);
        instance.setCurrentNodeName(targetNode.getNodeName());
        instanceRepository.save(instance);

        // 清理目标节点的旧审批记录，重新创建
        approvalService.recreateApprovalsForNode(instance, targetNode);

        // 回退后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_ROLLBACK", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", request, "targetNodeId", finalTargetNodeId)
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("回退后钩子警告: {}", postResult.warningMessages());
        }

        log.info("工作流已回退: instanceId={}, fromNode={}, toNode={}",
                instanceId, currentNodeName, targetNode.getNodeName());

        return toInstanceResponse(instance, false, permissionChecker.canUserApprove(instance, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeResponse> getRollbackableNodes(String instanceId) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        // 权限检查：发起人、审批人、抄送人才可查看
        permissionChecker.checkViewAccess(instance, userId);

        // 获取所有节点
        List<WorkflowNodeEntity> allNodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId());

        // 获取已审批通过的节点列表（从审批记录中获取）
        List<String> approvedNodeIds = approvalRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .filter(a -> a.isApproved() && !a.getIsRollback())
                .map(WorkflowApprovalEntity::getNodeId)
                .distinct()
                .toList();

        // 只返回已审批通过的节点，且不是当前节点
        return allNodes.stream()
                .filter(n -> approvedNodeIds.contains(n.getId()) && !n.getId().equals(instance.getCurrentNodeId()))
                .map(n -> conversionService.convert(n, WorkflowNodeResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * 查找上一个节点ID
     */
    private String findPreviousNodeId(WorkflowInstanceEntity instance) {
        List<WorkflowNodeEntity> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId());

        // 获取已审批通过的节点列表
        List<String> approvedNodeIds = approvalRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId())
                .stream()
                .filter(a -> a.isApproved() && !a.getIsRollback())
                .map(WorkflowApprovalEntity::getNodeId)
                .distinct()
                .toList();

        // 找到最近一个已审批通过的节点（不是当前节点）
        for (int i = approvedNodeIds.size() - 1; i >= 0; i--) {
            String nodeId = approvedNodeIds.get(i);
            if (!nodeId.equals(instance.getCurrentNodeId())) {
                return nodeId;
            }
        }

        return null;
    }

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private WorkflowInstanceResponse toInstanceResponse(WorkflowInstanceEntity entity, Boolean pendingApproval, Boolean canApprove) {
        String currentUserId = getCurrentUserId();
        WorkflowInstanceResponse base = conversionService.convert(entity, WorkflowInstanceResponse.class);

        return withPermissions(base, currentUserId, pendingApproval, canApprove);
    }

    /**
     * 补充权限字段
     */
    private WorkflowInstanceResponse withPermissions(WorkflowInstanceResponse base, String currentUserId, Boolean pendingApproval, Boolean canApprove) {
        return new WorkflowInstanceResponse(
                base.id(),
                base.definitionId(),
                base.definitionName(),
                base.userId(),
                base.userName(),
                base.deptId(),
                base.deptName(),
                base.title(),
                base.businessData(),
                base.currentNodeId(),
                base.currentNodeName(),
                base.status(),
                base.submitTime(),
                base.finishTime(),
                base.remark(),
                base.createTime(),
                pendingApproval,
                canApprove,
                base.userId().equals(currentUserId) && !isRunning(base.status()) && !isApproved(base.status()) && !isFinished(base.status()),
                base.userId().equals(currentUserId) && isCancellable(base.status()),
                base.userId().equals(currentUserId) && isRunning(base.status()),
                base.userId().equals(currentUserId) && isDraft(base.status()),
                base.userId().equals(currentUserId) && isDraft(base.status())
        );
    }

    private boolean isRunning(String status) {
        return WorkflowStatus.RUNNING.getCode().equals(status);
    }

    private boolean isDraft(String status) {
        return WorkflowStatus.DRAFT.getCode().equals(status);
    }

    private boolean isApproved(String status) {
        return WorkflowStatus.APPROVED.getCode().equals(status);
    }

    private boolean isFinished(String status) {
        return WorkflowStatus.APPROVED.getCode().equals(status)
            || WorkflowStatus.REJECTED.getCode().equals(status)
            || WorkflowStatus.CANCELLED.getCode().equals(status);
    }

    private boolean isCancellable(String status) {
        return WorkflowStatus.RUNNING.getCode().equals(status)
            || WorkflowStatus.DRAFT.getCode().equals(status);
    }

    private Map<String, Object> deserializeFormData(String businessData) {
        if (businessData == null || businessData.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(businessData, new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new BizException("业务表单数据解析失败", e);
        }
    }
}