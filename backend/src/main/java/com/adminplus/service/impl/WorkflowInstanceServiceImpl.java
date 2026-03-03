package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowDefinitionService;
import com.adminplus.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流实例服务实现
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final UserRepository userRepository;
    private final WorkflowDefinitionService definitionService;

    @Override
    @Transactional
    public WorkflowInstanceResp createDraft(WorkflowStartReq req) {
        String userId = getCurrentUserId();
        log.info("创建工作流草稿: userId={}, definitionId={}, title={}", userId, req.definitionId(), req.title());

        WorkflowDefinitionEntity definition = definitionRepository.findById(req.definitionId())
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setDefinitionId(req.definitionId());
        instance.setDefinitionName(definition.getDefinitionName());
        instance.setUserId(userId);
        instance.setUserName(user.getNickname());
        instance.setDeptId(user.getDeptId());
        instance.setTitle(req.title());
        instance.setBusinessData(req.businessData());
        instance.setStatus("draft");
        instance.setRemark(req.remark());

        instance = instanceRepository.save(instance);

        log.info("工作流草稿创建成功: id={}", instance.getId());
        return toInstanceResponse(instance, false, false);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp submit(String instanceId) {
        String userId = getCurrentUserId();
        log.info("提交工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isDraft() && !instance.isRunning()) {
            throw new IllegalArgumentException("只有草稿或进行中的工作流可以提交");
        }

        // 验证是否为发起人
        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以提交工作流");
        }

        instance.setStatus("running");
        instance.setSubmitTime(Instant.now());

        // 获取工作流定义的第一个节点
        List<WorkflowNodeEntity> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId());

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("工作流没有配置审批节点");
        }

        WorkflowNodeEntity firstNode = nodes.get(0);
        instance.setCurrentNodeId(firstNode.getId());
        instance.setCurrentNodeName(firstNode.getNodeName());

        instance = instanceRepository.save(instance);

        // 创建审批记录
        createApprovalRecords(instance, firstNode);

        log.info("工作流提交成功: id={}, currentNode={}", instance.getId(), firstNode.getNodeName());
        return toInstanceResponse(instance, false, false);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp start(WorkflowStartReq req) {
        log.info("发起工作流: title={}", req.title());

        // 先创建草稿
        WorkflowInstanceResp draft = createDraft(req);

        // 然后提交
        return submit(draft.id());
    }

    @Override
    public WorkflowDetailResp getDetail(String instanceId) {
        String userId = getCurrentUserId();
        log.info("查询工作流详情: instanceId={}", instanceId);

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        // 查询审批记录
        List<WorkflowApprovalResp> approvals = approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(this::toApprovalResponse)
                .collect(Collectors.toList());

        // 查询所有节点
        List<WorkflowNodeResp> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId())
                .stream()
                .map(this::toNodeResponse)
                .collect(Collectors.toList());

        // 查询当前节点
        WorkflowNodeResp currentNode = null;
        if (instance.getCurrentNodeId() != null) {
            WorkflowNodeEntity nodeEntity = nodeRepository.findById(instance.getCurrentNodeId()).orElse(null);
            if (nodeEntity != null) {
                currentNode = toNodeResponse(nodeEntity);
            }
        }

        // 判断当前用户是否可以审批
        boolean canApprove = canUserApprove(instance, userId);

        return new WorkflowDetailResp(
                toInstanceResponse(instance, instance.isRunning() && canApprove, canApprove),
                approvals,
                nodes,
                currentNode,
                canApprove
        );
    }

    @Override
    public List<WorkflowInstanceResp> getMyWorkflows(String status) {
        String userId = getCurrentUserId();
        log.info("查询我发起的工作流: userId={}, status={}", userId, status);

        List<WorkflowInstanceEntity> instances;
        if (status == null || status.isEmpty()) {
            instances = instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(userId);
        } else {
            instances = instanceRepository.findByUserIdAndStatusAndDeletedFalseOrderBySubmitTimeDesc(userId, status);
        }

        return instances.stream()
                .map(i -> toInstanceResponse(i, false, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstanceResp> getPendingApprovals() {
        String userId = getCurrentUserId();
        log.info("查询待我审批的工作流: userId={}", userId);

        List<WorkflowInstanceEntity> instances = instanceRepository.findPendingApprovalsByUser(userId);

        return instances.stream()
                .map(i -> toInstanceResponse(i, true, true))
                .collect(Collectors.toList());
    }

    @Override
    public long countPendingApprovals() {
        String userId = getCurrentUserId();
        return instanceRepository.countPendingApprovalsByUser(userId);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req) {
        String userId = getCurrentUserId();
        log.info("同意审批: instanceId={}, userId={}", instanceId, userId);

        return processApproval(instanceId, req, "approved");
    }

    @Override
    @Transactional
    public WorkflowInstanceResp reject(String instanceId, ApprovalActionReq req) {
        String userId = getCurrentUserId();
        log.info("拒绝审批: instanceId={}, userId={}", instanceId, userId);

        return processApproval(instanceId, req, "rejected");
    }

    @Override
    @Transactional
    public void cancel(String instanceId) {
        String userId = getCurrentUserId();
        log.info("取消工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isCancellable()) {
            throw new IllegalArgumentException("当前状态不允许取消");
        }

        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以取消工作流");
        }

        instance.setStatus("cancelled");
        instance.setFinishTime(Instant.now());
        instanceRepository.save(instance);

        log.info("工作流已取消: id={}", instanceId);
    }

    @Override
    @Transactional
    public void withdraw(String instanceId) {
        String userId = getCurrentUserId();
        log.info("撤回工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以撤回工作流");
        }

        if (!instance.isDraft() && !instance.isRejected()) {
            throw new IllegalArgumentException("只有草稿或被拒绝的流程可以撤回");
        }

        // 删除所有审批记录
        List<WorkflowApprovalEntity> approvals = approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId);
        approvals.forEach(a -> {
            a.setDeleted(true);
            approvalRepository.save(a);
        });

        // 重置为草稿
        instance.setStatus("draft");
        instance.setCurrentNodeId(null);
        instance.setCurrentNodeName(null);
        instance.setSubmitTime(null);
        instanceRepository.save(instance);

        log.info("工作流已撤回: id={}", instanceId);
    }

    @Override
    public List<WorkflowApprovalResp> getApprovals(String instanceId) {
        log.info("查询审批记录: instanceId={}", instanceId);

        return approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(this::toApprovalResponse)
                .collect(Collectors.toList());
    }

    /**
     * 处理审批
     */
    private WorkflowInstanceResp processApproval(String instanceId, ApprovalActionReq req, String action) {
        String userId = getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isRunning()) {
            throw new IllegalArgumentException("只有进行中的工作流可以审批");
        }

        // 查询当前节点的待审批记录
        List<WorkflowApprovalEntity> pendingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending())
                .collect(Collectors.toList());

        // 查找当前用户的审批记录
        WorkflowApprovalEntity myApproval = pendingApprovals.stream()
                .filter(a -> a.getApproverId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("您没有权限审批此工作流"));

        // 更新审批记录
        myApproval.setApprovalStatus(action);
        myApproval.setComment(req.comment());
        myApproval.setAttachments(req.attachments());
        myApproval.setApprovalTime(Instant.now());
        myApproval.setApproverName(user.getNickname());
        approvalRepository.save(myApproval);

        // 判断是否需要继续流转
        WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
                .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

        if (action.equals("rejected")) {
            // 拒绝则直接结束
            instance.setStatus("rejected");
            instance.setFinishTime(Instant.now());
            log.info("工作流被拒绝: id={}", instanceId);
        } else {
            // 同意，检查是否所有人都已审批
            boolean allApproved = pendingApprovals.stream()
                    .allMatch(a -> a.isApproved());

            if (allApproved) {
                // 当前节点所有审批人都已同意，流转到下一节点
                moveToNextNode(instance);
            } else {
                log.info("等待其他审批人审批: instanceId={}", instanceId);
            }
        }

        instance = instanceRepository.save(instance);
        return toInstanceResponse(instance, false, canUserApprove(instance, userId));
    }

    /**
     * 流转到下一节点
     */
    private void moveToNextNode(WorkflowInstanceEntity instance) {
        List<WorkflowNodeEntity> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId());

        int currentIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(instance.getCurrentNodeId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex >= 0 && currentIndex < nodes.size() - 1) {
            // 有下一节点
            WorkflowNodeEntity nextNode = nodes.get(currentIndex + 1);
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeName(nextNode.getNodeName());

            // 创建下一节点的审批记录
            createApprovalRecords(instance, nextNode);

            log.info("工作流流转到下一节点: instanceId={}, nodeName={}", instance.getId(), nextNode.getNodeName());
        } else {
            // 已是最后节点，流程结束
            instance.setStatus("approved");
            instance.setFinishTime(Instant.now());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);

            log.info("工作流已全部审批通过: id={}", instance.getId());
        }
    }

    /**
     * 创建审批记录
     */
    private void createApprovalRecords(WorkflowInstanceEntity instance, WorkflowNodeEntity node) {
        List<String> approverIds = resolveApprovers(instance, node);

        for (String approverId : approverIds) {
            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node.getId());
            approval.setNodeName(node.getNodeName());
            approval.setApproverId(approverId);

            // 查询审批人姓名
            userRepository.findById(approverId).ifPresent(user -> {
                approval.setApproverName(user.getNickname());
            });

            approval.setApprovalStatus("pending");

            // 如果启用了"审批人自动通过"且审批人与发起人相同，则自动通过
            if (node.getAutoPassSameUser() && approverId.equals(instance.getUserId())) {
                approval.setApprovalStatus("approved");
                approval.setComment("系统自动通过（审批人与发起人相同）");
                approval.setApprovalTime(Instant.now());
            }

            approvalRepository.save(approval);
        }

        log.info("创建审批记录: instanceId={}, nodeId={}, approverCount={}", instance.getId(), node.getId(), approverIds.size());
    }

    /**
     * 解析审批人列表
     */
    private List<String> resolveApprovers(WorkflowInstanceEntity instance, WorkflowNodeEntity node) {
        List<String> approvers = new ArrayList<>();

        switch (node.getApproverType()) {
            case "user":
                // 指定用户
                if (node.getApproverId() != null) {
                    approvers.add(node.getApproverId());
                }
                break;

            case "role":
                // 角色
                // TODO: 实现基于角色的审批人解析
                break;

            case "dept":
                // 部门
                if (instance.getDeptId() != null) {
                    // TODO: 查询部门负责人
                }
                break;

            case "leader":
                // 部门领导
                if (instance.getDeptId() != null) {
                    // TODO: 查询部门领导
                }
                break;

            default:
                break;
        }

        if (approvers.isEmpty()) {
            throw new IllegalArgumentException("无法解析审批人: type=" + node.getApproverType());
        }

        return approvers;
    }

    /**
     * 判断用户是否可以审批
     */
    private boolean canUserApprove(WorkflowInstanceEntity instance, String userId) {
        if (!instance.isRunning() || instance.getCurrentNodeId() == null) {
            return false;
        }

        List<WorkflowApprovalEntity> pendingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instance.getId(), instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending())
                .collect(Collectors.toList());

        return pendingApprovals.stream()
                .anyMatch(a -> a.getApproverId().equals(userId));
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private WorkflowInstanceResp toInstanceResponse(WorkflowInstanceEntity entity, Boolean pendingApproval, Boolean canApprove) {
        return new WorkflowInstanceResp(
                entity.getId(),
                entity.getDefinitionId(),
                entity.getDefinitionName(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getDeptId(),
                entity.getTitle(),
                entity.getBusinessData(),
                entity.getCurrentNodeId(),
                entity.getCurrentNodeName(),
                entity.getStatus(),
                entity.getSubmitTime(),
                entity.getFinishTime(),
                entity.getRemark(),
                entity.getCreateTime(),
                pendingApproval,
                canApprove
        );
    }

    private WorkflowApprovalResp toApprovalResponse(WorkflowApprovalEntity entity) {
        return new WorkflowApprovalResp(
                entity.getId(),
                entity.getInstanceId(),
                entity.getNodeId(),
                entity.getNodeName(),
                entity.getApproverId(),
                entity.getApproverName(),
                entity.getApprovalStatus(),
                entity.getComment(),
                entity.getAttachments(),
                entity.getApprovalTime(),
                entity.getCreateTime()
        );
    }

    private WorkflowNodeResp toNodeResponse(WorkflowNodeEntity entity) {
        return new WorkflowNodeResp(
                entity.getId(),
                entity.getDefinitionId(),
                entity.getNodeName(),
                entity.getNodeCode(),
                entity.getNodeOrder(),
                entity.getApproverType(),
                entity.getApproverId(),
                entity.getIsCounterSign(),
                entity.getAutoPassSameUser(),
                entity.getDescription(),
                entity.getCreateTime()
        );
    }
}
