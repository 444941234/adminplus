package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.extendedstate.WorkflowExtendedState;
import com.adminplus.service.WorkflowStateMachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工作流状态机服务实现
 * <p>
 * 使用Spring State Machine管理工作流状态转换
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStateMachineServiceImpl implements WorkflowStateMachineService {

    private final StateMachineFactory<WorkflowState, WorkflowEvent> stateMachineFactory;
    private final StateMachinePersister<WorkflowState, WorkflowEvent, String> persister;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req) {
        log.info("Approving workflow instance: {}", instanceId);

        // 获取当前用户ID
        String userId = getCurrentUserId();

        // 悲观锁查询工作流实例
        WorkflowInstanceEntity instance = instanceRepository.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        // 验证状态
        if (!instance.isRunning()) {
            throw new IllegalStateException("只有运行中的工作流才能审批");
        }

        // 验证审批权限
        WorkflowApprovalEntity approval = approvalRepository
                .findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                        instanceId, instance.getCurrentNodeId(), userId, "pending")
                .orElseThrow(() -> new IllegalArgumentException("无权限审批该工作流"));

        // 获取当前节点
        WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
                .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

        // 查找下一节点
        WorkflowNodeEntity nextNode = findNextNode(instance, currentNode)
                .orElseThrow(() -> new IllegalArgumentException("无法找到下一节点"));

        // 获取或恢复状态机
        StateMachine<WorkflowState, WorkflowEvent> sm = getStateMachine(instanceId);

        // 构建消息头
        Map<String, Object> headers = new HashMap<>();
        headers.put("approverId", userId);
        headers.put("comment", req.comment());
        headers.put("attachments", req.attachments());
        headers.put("targetNodeId", nextNode.getId());
        headers.put("approvalId", approval.getId());

        // 发送APPROVE事件
        Message<WorkflowEvent> message = MessageBuilder
                .withPayload(WorkflowEvent.APPROVE)
                .copyHeaders(headers)
                .build();

        boolean accepted = sm.sendEvent(message);
        if (!accepted) {
            throw new IllegalStateException("状态机事件被拒绝");
        }

        // 持久化状态机
        try {
            persister.persist(sm, instanceId);
        } catch (Exception e) {
            log.error("Failed to persist state machine for instance: {}", instanceId, e);
            throw new RuntimeException("状态机持久化失败", e);
        }

        // 更新审批记录
        approval.setApprovalStatus("approved");
        approval.setComment(req.comment());
        approval.setAttachments(req.attachments());
        approval.setApprovalTime(java.time.Instant.now());
        approvalRepository.save(approval);

        // 更新实例状态
        instance.setCurrentNodeId(nextNode.getId());
        instance.setCurrentNodeName(nextNode.getNodeName());
        instance.setStateMachineContext(serializeExtendedState(sm));
        instanceRepository.save(instance);

        // 返回更新后的实例
        return convertToResp(instance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp reject(String instanceId, ApprovalActionReq req) {
        log.info("Rejecting workflow instance: {}", instanceId);

        String userId = getCurrentUserId();

        // 悲观锁查询
        WorkflowInstanceEntity instance = instanceRepository.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isRunning()) {
            throw new IllegalStateException("只有运行中的工作流才能拒绝");
        }

        // 验证权限
        WorkflowApprovalEntity approval = approvalRepository
                .findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                        instanceId, instance.getCurrentNodeId(), userId, "pending")
                .orElseThrow(() -> new IllegalArgumentException("无权限审批该工作流"));

        // 获取状态机
        StateMachine<WorkflowState, WorkflowEvent> sm = getStateMachine(instanceId);

        // 构建消息
        Map<String, Object> headers = new HashMap<>();
        headers.put("approverId", userId);
        headers.put("comment", req.comment());
        headers.put("attachments", req.attachments());
        headers.put("approvalId", approval.getId());

        Message<WorkflowEvent> message = MessageBuilder
                .withPayload(WorkflowEvent.REJECT)
                .copyHeaders(headers)
                .build();

        boolean accepted = sm.sendEvent(message);
        if (!accepted) {
            throw new IllegalStateException("状态机事件被拒绝");
        }

        // 持久化
        try {
            persister.persist(sm, instanceId);
        } catch (Exception e) {
            log.error("Failed to persist state machine for instance: {}", instanceId, e);
            throw new RuntimeException("状态机持久化失败", e);
        }

        // 更新审批记录
        approval.setApprovalStatus("rejected");
        approval.setComment(req.comment());
        approval.setAttachments(req.attachments());
        approval.setApprovalTime(java.time.Instant.now());
        approvalRepository.save(approval);

        // 更新实例状态
        instance.setStatus("rejected");
        instance.setFinishTime(java.time.Instant.now());
        instance.setStateMachineContext(serializeExtendedState(sm));
        instanceRepository.save(instance);

        return convertToResp(instance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp cancel(String instanceId) {
        log.info("Cancelling workflow instance: {}", instanceId);

        String userId = getCurrentUserId();

        // 悲观锁查询
        WorkflowInstanceEntity instance = instanceRepository.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isCancellable()) {
            throw new IllegalStateException("该工作流不能取消");
        }

        // 只有发起人可以取消
        if (!instance.getUserId().equals(userId)) {
            throw new IllegalStateException("只有发起人可以取消工作流");
        }

        // 获取状态机
        StateMachine<WorkflowState, WorkflowEvent> sm = getStateMachine(instanceId);

        // 发送CANCEL事件
        boolean accepted = sm.sendEvent(WorkflowEvent.CANCEL);
        if (!accepted) {
            throw new IllegalStateException("状态机事件被拒绝");
        }

        // 持久化
        try {
            persister.persist(sm, instanceId);
        } catch (Exception e) {
            log.error("Failed to persist state machine for instance: {}", instanceId, e);
            throw new RuntimeException("状态机持久化失败", e);
        }

        // 更新实例状态
        instance.setStatus("cancelled");
        instance.setFinishTime(java.time.Instant.now());
        instance.setStateMachineContext(serializeExtendedState(sm));
        instanceRepository.save(instance);

        return convertToResp(instance);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp rollback(String instanceId, ApprovalActionReq req) {
        log.info("Rolling back workflow instance: {}", instanceId);

        String userId = getCurrentUserId();

        // 悲观锁查询
        WorkflowInstanceEntity instance = instanceRepository.findByIdForUpdate(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isRunning()) {
            throw new IllegalStateException("只有运行中的工作流才能退回");
        }

        // 验证权限
        WorkflowApprovalEntity approval = approvalRepository
                .findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                        instanceId, instance.getCurrentNodeId(), userId, "pending")
                .orElseThrow(() -> new IllegalArgumentException("无权限审批该工作流"));

        // 获取状态机
        StateMachine<WorkflowState, WorkflowEvent> sm = getStateMachine(instanceId);

        // 获取上一节点
        String previousNodeId = WorkflowExtendedState.getPreviousNodeId(sm);
        if (previousNodeId == null) {
            throw new IllegalStateException("没有可以退回的节点");
        }

        WorkflowNodeEntity previousNode = nodeRepository.findById(previousNodeId)
                .orElseThrow(() -> new IllegalArgumentException("上一节点不存在"));

        // 构建消息
        Map<String, Object> headers = new HashMap<>();
        headers.put("approverId", userId);
        headers.put("comment", req.comment());
        headers.put("targetNodeId", previousNodeId);
        headers.put("rollbackFromNodeId", instance.getCurrentNodeId());
        headers.put("rollbackFromNodeName", instance.getCurrentNodeName());
        headers.put("approvalId", approval.getId());

        Message<WorkflowEvent> message = MessageBuilder
                .withPayload(WorkflowEvent.ROLLBACK)
                .copyHeaders(headers)
                .build();

        boolean accepted = sm.sendEvent(message);
        if (!accepted) {
            throw new IllegalStateException("状态机事件被拒绝");
        }

        // 持久化
        try {
            persister.persist(sm, instanceId);
        } catch (Exception e) {
            log.error("Failed to persist state machine for instance: {}", instanceId, e);
            throw new RuntimeException("状态机持久化失败", e);
        }

        // 更新审批记录
        approval.setApprovalStatus("rejected");
        approval.setComment(req.comment());
        approval.setApprovalTime(java.time.Instant.now());
        approval.setIsRollback(true);
        approval.setRollbackFromNodeId(instance.getCurrentNodeId());
        approval.setRollbackFromNodeName(instance.getCurrentNodeName());
        approvalRepository.save(approval);

        // 更新实例状态
        instance.setCurrentNodeId(previousNodeId);
        instance.setCurrentNodeName(previousNode.getNodeName());
        instance.setStateMachineContext(serializeExtendedState(sm));
        instanceRepository.save(instance);

        return convertToResp(instance);
    }

    /**
     * 获取状态机实例（如果存在则恢复，否则创建新的）
     */
    private StateMachine<WorkflowState, WorkflowEvent> getStateMachine(String instanceId) {
        StateMachine<WorkflowState, WorkflowEvent> sm = stateMachineFactory.getStateMachine(instanceId);

        try {
            // 尝试从数据库恢复状态机状态
            persister.restore(sm, instanceId);
            log.debug("Restored state machine for instance: {}", instanceId);
        } catch (Exception e) {
            log.debug("No existing state machine for instance {}, creating new one: {}",
                    instanceId, e.getMessage());
            // 新状态机，初始化扩展状态
            WorkflowExtendedState.setInstanceId(sm, instanceId);
        }

        return sm;
    }

    /**
     * 查找下一节点
     */
    private java.util.Optional<WorkflowNodeEntity> findNextNode(
            WorkflowInstanceEntity instance,
            WorkflowNodeEntity currentNode) {

        // 简单实现：根据nodeOrder查找下一节点
        List<WorkflowNodeEntity> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrder(instance.getDefinitionId());

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(currentNode.getId()) && i + 1 < nodes.size()) {
                return java.util.Optional.of(nodes.get(i + 1));
            }
        }

        return java.util.Optional.empty();
    }

    /**
     * 序列化扩展状态为JSON字符串
     */
    @SuppressWarnings("unchecked")
    private String serializeExtendedState(StateMachine<WorkflowState, WorkflowEvent> sm) {
        try {
            Map<Object, Object> variables = sm.getExtendedState().getVariables();
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            log.warn("Failed to serialize extended state, falling back to toString()", e);
            return sm.getExtendedState().getVariables().toString();
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 转换为响应对象
     */
    private WorkflowInstanceResp convertToResp(WorkflowInstanceEntity entity) {
        String currentUserId = getCurrentUserId();
        boolean isOwner = currentUserId.equals(entity.getUserId());

        // pendingApproval: 当前节点是否有待审批记录
        boolean pendingApproval = entity.isRunning()
                && approvalRepository.countByInstanceIdAndApprovalStatusAndDeletedFalse(
                        entity.getId(), "pending") > 0;

        // canApprove: 当前用户是否可以审批当前节点
        boolean canApprove = false;
        if (entity.isRunning() && entity.getCurrentNodeId() != null) {
            canApprove = approvalRepository
                    .findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                            entity.getId(), entity.getCurrentNodeId(), currentUserId, "pending")
                    .isPresent();
        }

        // canWithdraw: 运行中且是发起人
        boolean canWithdraw = entity.isRunning() && isOwner;

        // canCancel: 运行中且是发起人
        boolean canCancel = entity.isRunning() && isOwner;

        // canUrge: 运行中且有pending审批，且是发起人
        boolean canUrge = entity.isRunning() && isOwner && pendingApproval;

        // canEditDraft: 草稿且是发起人
        boolean canEditDraft = entity.isDraft() && isOwner;

        // canSubmitDraft: 草稿且是发起人
        boolean canSubmitDraft = entity.isDraft() && isOwner;

        return new WorkflowInstanceResp(
                entity.getId(),
                entity.getDefinitionId(),
                entity.getDefinitionName(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getDeptId(),
                null,
                entity.getTitle(),
                entity.getBusinessData(),
                entity.getCurrentNodeId(),
                entity.getCurrentNodeName(),
                entity.getStatus() == null ? null : entity.getStatus().toUpperCase(),
                entity.getSubmitTime(),
                entity.getFinishTime(),
                entity.getRemark(),
                entity.getCreateTime(),
                pendingApproval,
                canApprove,
                canWithdraw,
                canCancel,
                canUrge,
                canEditDraft,
                canSubmitDraft
        );
    }
}
