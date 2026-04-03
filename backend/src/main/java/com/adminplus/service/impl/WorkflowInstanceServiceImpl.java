package com.adminplus.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowDraftDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;
import com.adminplus.pojo.entity.*;
import com.adminplus.pojo.dto.req.AddSignReq;
import com.adminplus.pojo.dto.resp.WorkflowAddSignResp;
import com.adminplus.pojo.dto.resp.WorkflowCcResp;
import com.adminplus.pojo.dto.resp.WorkflowOperationPermissionsResp;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowDefinitionService;
import com.adminplus.service.WorkflowInstanceService;
import com.adminplus.service.workflow.hook.WorkflowHookService;
import com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary;
import com.adminplus.common.exception.BizException;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DeptRepository deptRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final WorkflowDefinitionService definitionService;
    private final WorkflowCcRepository ccRepository;
    private final WorkflowAddSignRepository addSignRepository;
    private final ObjectMapper objectMapper;
    private final WorkflowHookService hookService;

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
        instance.setBusinessData(serializeFormData(req.formData()));
        instance.setStatus("draft");
        instance.setRemark(req.remark());

        instance = instanceRepository.save(instance);

        log.info("工作流草稿创建成功: id={}", instance.getId());
        return toInstanceResponse(instance, false, false);
    }

    @Override
    @Transactional
    public WorkflowInstanceResp submit(String instanceId, WorkflowStartReq req) {
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

        if (req != null) {
            applyDraftChanges(instance, req);
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

        // 提交前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_SUBMIT", instance, firstNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req != null ? req : WorkflowStartReq.builder().definitionId("").title("").formData(Map.of()).build())
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "提交前校验失败" : preResult.blockingMessages().get(0));
        }

        instance.setCurrentNodeId(firstNode.getId());
        instance.setCurrentNodeName(firstNode.getNodeName());

        instance = instanceRepository.save(instance);

        // 创建审批记录
        createApprovalRecords(instance, firstNode);

        // 创建抄送记录（流程发起时抄送）
        createCcRecords(instance, firstNode, "start", instance.getRemark());

        // 提交后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_SUBMIT", instance, firstNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of()
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("提交后钩子警告: {}", postResult.warningMessages());
        }

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
        return submit(draft.id(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDraftDetailResp getDraftDetail(String instanceId) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以查看草稿");
        }
        if (!instance.isDraft()) {
            throw new IllegalArgumentException("当前流程不是草稿状态");
        }

        WorkflowDefinitionEntity definition = definitionRepository.findById(instance.getDefinitionId())
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

        return new WorkflowDraftDetailResp(
                toInstanceResponse(instance, false, false),
                definition.getFormConfig(),
                deserializeFormData(instance.getBusinessData())
        );
    }

    @Override
    @Transactional
    public WorkflowInstanceResp updateDraft(String instanceId, WorkflowStartReq req) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以更新草稿");
        }
        if (!instance.isDraft()) {
            throw new IllegalArgumentException("只有草稿状态可以更新");
        }

        applyDraftChanges(instance, req);
        WorkflowInstanceEntity saved = instanceRepository.save(instance);
        return toInstanceResponse(saved, false, false);
    }

    @Override
    @Transactional
    public void deleteDraft(String instanceId) {
        String userId = getCurrentUserId();
        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只有发起人可以删除草稿");
        }
        if (!instance.isDraft()) {
            throw new IllegalArgumentException("只有草稿状态可以删除");
        }

        instanceRepository.delete(instance);
    }

    @Override
    @Transactional(readOnly = true)
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

        WorkflowDefinitionEntity definition = definitionRepository.findById(instance.getDefinitionId())
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));

        List<WorkflowCcResp> ccRecords = ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(this::toCcResponse)
                .collect(Collectors.toList());

        List<WorkflowAddSignResp> addSignRecords = addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId)
                .stream()
                .map(this::toAddSignResponse)
                .collect(Collectors.toList());

        return new WorkflowDetailResp(
                toInstanceResponse(instance, instance.isRunning() && canApprove, canApprove),
                approvals,
                nodes,
                currentNode,
                canApprove,
                definition.getFormConfig(),
                deserializeFormData(instance.getBusinessData()),
                ccRecords,
                addSignRecords,
                buildOperationPermissions(instance, canApprove)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstanceResp> getMyWorkflows(String status) {
        String userId = getCurrentUserId();
        log.info("查询我发起的工作流: userId={}, status={}", userId, status);

        List<WorkflowInstanceEntity> instances;
        if (status == null || status.isEmpty()) {
            instances = instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(userId);
        } else {
            instances = instanceRepository.findByUserIdAndStatusAndDeletedFalseOrderBySubmitTimeDesc(userId, normalizeStatusForStorage(status));
        }

        // Batch fetch dept names to avoid N+1 queries
        Map<String, String> deptNameMap = batchGetDeptNames(instances);

        return instances.stream()
                .map(i -> toInstanceResponseWithDeptName(i, deptNameMap.get(i.getDeptId()), false, false))
                .collect(Collectors.toList());
    }

    /**
     * Batch fetch department names for workflow instances
     */
    private Map<String, String> batchGetDeptNames(List<WorkflowInstanceEntity> instances) {
        List<String> deptIds = instances.stream()
                .map(WorkflowInstanceEntity::getDeptId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();

        if (deptIds.isEmpty()) {
            return Map.of();
        }

        return deptRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(DeptEntity::getId, DeptEntity::getName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstanceResp> getPendingApprovals() {
        String userId = getCurrentUserId();
        log.info("查询待我审批的工作流: userId={}", userId);

        List<WorkflowInstanceEntity> instances = instanceRepository.findPendingApprovalsByUser(userId);

        // Batch fetch dept names to avoid N+1 queries
        Map<String, String> deptNameMap = batchGetDeptNames(instances);

        return instances.stream()
                .map(i -> toInstanceResponseWithDeptName(i, deptNameMap.get(i.getDeptId()), true, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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

        // 取消前钩子校验
        WorkflowNodeEntity currentNode = null;
        if (instance.getCurrentNodeId() != null) {
            currentNode = nodeRepository.findById(instance.getCurrentNodeId()).orElse(null);
        }

        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_CANCEL", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of()
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "取消前校验失败" : preResult.blockingMessages().get(0));
        }

        instance.setStatus("cancelled");
        instance.setFinishTime(Instant.now());
        instanceRepository.save(instance);

        // 取消后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_CANCEL", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of()
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("取消后钩子警告: {}", postResult.warningMessages());
        }

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

        // 撤回前钩子校验
        WorkflowNodeEntity currentNode = null;
        if (instance.getCurrentNodeId() != null) {
            currentNode = nodeRepository.findById(instance.getCurrentNodeId()).orElse(null);
        }

        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_WITHDRAW", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of()
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "撤回前校验失败" : preResult.blockingMessages().get(0));
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

        // 撤回后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_WITHDRAW", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of()
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("撤回后钩子警告: {}", postResult.warningMessages());
        }

        log.info("工作流已撤回: id={}", instanceId);
    }

    @Override
    @Transactional(readOnly = true)
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

        // 查询当前节点
        WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
                .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

        // 审批前钩子校验
        String hookPoint = action.equals("approved") ? "PRE_APPROVE" : "PRE_REJECT";
        HookExecutionSummary preResult = hookService.executeAllHooks(
            hookPoint, instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req, "action", action)
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "审批前校验失败" : preResult.blockingMessages().get(0));
        }

        // 更新审批记录
        myApproval.setApprovalStatus(action);
        myApproval.setComment(req.comment());
        myApproval.setAttachments(req.attachments());
        myApproval.setApprovalTime(Instant.now());
        myApproval.setApproverName(user.getNickname());
        approvalRepository.save(myApproval);

        // 判断是否需要继续流转
        if (action.equals("rejected")) {
            // 拒绝则直接结束
            instance.setStatus("rejected");
            instance.setFinishTime(Instant.now());
            log.info("工作流被拒绝: id={}", instanceId);

            // 创建抄送记录（拒绝时抄送）
            createCcRecords(instance, currentNode, "reject", req.comment());

            // 拒绝后钩子执行
            HookExecutionSummary postResult = hookService.executeAllHooks(
                "POST_REJECT", instance, currentNode,
                deserializeFormData(instance.getBusinessData()),
                Map.of("req", req)
            );
            if (!postResult.warningMessages().isEmpty()) {
                log.warn("拒绝后钩子警告: {}", postResult.warningMessages());
            }
        } else {
            // 同意，检查是否所有人都已审批
            boolean allApproved = pendingApprovals.stream()
                    .allMatch(a -> a.isApproved());

            if (allApproved) {
                // 当前节点所有审批人都已同意，流转到下一节点
                moveToNextNode(instance);

                // 创建抄送记录（审批通过时抄送）
                createCcRecords(instance, currentNode, "approve", req.comment());

                // 同意后钩子执行
                HookExecutionSummary postResult = hookService.executeAllHooks(
                    "POST_APPROVE", instance, currentNode,
                    deserializeFormData(instance.getBusinessData()),
                    Map.of("req", req)
                );
                if (!postResult.warningMessages().isEmpty()) {
                    log.warn("同意后钩子警告: {}", postResult.warningMessages());
                }
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
                // 角色 - 查找具有该角色的所有用户
                if (node.getApproverId() != null) {
                    // approverId 可能是角色ID、角色编码或角色名称
                    String roleId = node.getApproverId();
                    RoleEntity role = null;

                    // 如果是角色编码（以 ROLE_ 开头），先查找角色
                    if (roleId.startsWith("ROLE_")) {
                        role = roleRepository.findByCode(roleId).orElse(null);
                        if (role == null) {
                            log.warn("找不到角色编码: {}", node.getApproverId());
                        }
                    } else {
                        // 尝试按名称查找角色
                        role = roleRepository.findByName(roleId).orElse(null);
                        if (role == null) {
                            // 尝试按ID查找角色
                            role = roleRepository.findById(roleId).orElse(null);
                        }
                        if (role == null) {
                            log.warn("找不到角色: {}", node.getApproverId());
                        }
                    }

                    if (role != null) {
                        roleId = role.getId();
                        List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(roleId);
                        approvers.addAll(userRoles.stream().map(UserRoleEntity::getUserId).toList());
                    }
                }
                break;

            case "dept":
            case "leader":
                // 部门/部门领导 - 查找部门的负责人
                if (instance.getDeptId() != null) {
                    DeptEntity dept = deptRepository.findById(instance.getDeptId()).orElse(null);
                    if (dept != null && dept.getLeader() != null) {
                        // leader 字段存储的是用户ID
                        approvers.add(dept.getLeader());
                    }
                }
                break;

            default:
                break;
        }

        // 如果找不到审批人，抛出异常而非静默使用 admin
        if (approvers.isEmpty()) {
            log.error("无法解析审批人: type={}, node={}", node.getApproverType(), node.getNodeName());
            throw new BizException("无法解析审批人，请联系管理员配置审批流程: " + node.getNodeName());
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
        return SecurityUtils.getCurrentUserId();
    }

    private WorkflowInstanceResp toInstanceResponse(WorkflowInstanceEntity entity, Boolean pendingApproval, Boolean canApprove) {
        String currentUserId = getCurrentUserId();
        String deptName = deptRepository.findById(entity.getDeptId())
                .map(DeptEntity::getName)
                .orElse(null);

        return toInstanceResponseWithDeptName(entity, deptName, pendingApproval, canApprove);
    }

    /**
     * 转换为响应对象（带预先查询的部门名称，避免 N+1 问题）
     */
    private WorkflowInstanceResp toInstanceResponseWithDeptName(WorkflowInstanceEntity entity, String deptName, Boolean pendingApproval, Boolean canApprove) {
        String currentUserId = getCurrentUserId();

        return new WorkflowInstanceResp(
                entity.getId(),
                entity.getDefinitionId(),
                entity.getDefinitionName(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getDeptId(),
                deptName,
                entity.getTitle(),
                entity.getBusinessData(),
                entity.getCurrentNodeId(),
                entity.getCurrentNodeName(),
                normalizeStatusForResponse(entity.getStatus()),
                entity.getSubmitTime(),
                entity.getFinishTime(),
                entity.getRemark(),
                entity.getCreateTime(),
                pendingApproval,
                canApprove,
                entity.getUserId().equals(currentUserId) && !entity.isRunning() && !entity.isApproved() && !entity.isFinished(),
                entity.getUserId().equals(currentUserId) && entity.isCancellable(),
                entity.getUserId().equals(currentUserId) && entity.isRunning(),
                entity.getUserId().equals(currentUserId) && entity.isDraft(),
                entity.getUserId().equals(currentUserId) && entity.isDraft()
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

    @Override
    @Transactional
    public WorkflowInstanceResp rollback(String instanceId, ApprovalActionReq req) {
        String userId = getCurrentUserId();
        log.info("回退工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        if (!instance.isRunning()) {
            throw new IllegalArgumentException("只有进行中的工作流可以回退");
        }

        // 验证审批权限
        WorkflowApprovalEntity approval = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending() && a.getApproverId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("您没有权限回退此工作流"));

        // 解析目标节点ID（从请求中获取）
        String requestedTargetNodeId = req.targetNodeId();
        String finalTargetNodeId;
        if (requestedTargetNodeId == null || requestedTargetNodeId.isEmpty()) {
            // 如果没有指定目标节点，则回退到上一节点
            finalTargetNodeId = findPreviousNodeId(instance);
            if (finalTargetNodeId == null) {
                throw new IllegalArgumentException("没有可以回退的节点");
            }
        } else {
            finalTargetNodeId = requestedTargetNodeId;
        }

        // 验证目标节点是否在可回退范围内
        List<WorkflowNodeResp> rollbackableNodes = getRollbackableNodes(instanceId);
        String targetNodeIdForValidation = finalTargetNodeId; // effectively final variable for lambda
        boolean isValidTarget = rollbackableNodes.stream()
                .anyMatch(n -> n.id().equals(targetNodeIdForValidation));
        if (!isValidTarget) {
            throw new IllegalArgumentException("无法回退到指定节点");
        }

        WorkflowNodeEntity targetNode = nodeRepository.findById(finalTargetNodeId)
                .orElseThrow(() -> new IllegalArgumentException("目标节点不存在"));

        // 获取当前节点
        WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
                .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

        // 回退前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_ROLLBACK", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req, "targetNodeId", finalTargetNodeId)
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
        approval.setComment(req.comment());
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
        cleanupAndCreateApprovals(instance, targetNode);

        // 回退后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_ROLLBACK", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req, "targetNodeId", finalTargetNodeId)
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("回退后钩子警告: {}", postResult.warningMessages());
        }

        log.info("工作流已回退: instanceId={}, fromNode={}, toNode={}",
                instanceId, currentNodeName, targetNode.getNodeName());

        return toInstanceResponse(instance, false, canUserApprove(instance, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeResp> getRollbackableNodes(String instanceId) {
        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

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
                .collect(Collectors.toList());

        // 只返回已审批通过的节点，且不是当前节点
        return allNodes.stream()
                .filter(n -> approvedNodeIds.contains(n.getId()) && !n.getId().equals(instance.getCurrentNodeId()))
                .map(this::toNodeResponse)
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
                .collect(Collectors.toList());

        // 找到最近一个已审批通过的节点（不是当前节点）
        for (int i = approvedNodeIds.size() - 1; i >= 0; i--) {
            String nodeId = approvedNodeIds.get(i);
            if (!nodeId.equals(instance.getCurrentNodeId())) {
                return nodeId;
            }
        }

        return null;
    }

    /**
     * 清理目标节点的旧审批记录并重新创建
     */
    private void cleanupAndCreateApprovals(WorkflowInstanceEntity instance, WorkflowNodeEntity targetNode) {
        // 查找目标节点的所有审批记录
        List<WorkflowApprovalEntity> existingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instance.getId(), targetNode.getId());

        // 如果存在旧的审批记录，将其标记为已删除
        if (!existingApprovals.isEmpty()) {
            existingApprovals.forEach(a -> a.setDeleted(true));
            approvalRepository.saveAll(existingApprovals);
        }

        // 重新创建审批记录
        createApprovalRecords(instance, targetNode);
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

    /**
     * 创建抄送记录
     *
     * @param instance    工作流实例
     * @param node        当前节点
     * @param ccType      抄送类型（approve/reject/rollback/start）
     * @param ccContent   抄送内容
     */
    private void createCcRecords(WorkflowInstanceEntity instance, WorkflowNodeEntity node, String ccType, String ccContent) {
        try {
            // 解析节点配置的抄送用户ID列表
            Set<String> ccUserIds = new HashSet<>();

            // 1. 从节点的 ccUserIds 字段解析
            if (node.getCcUserIds() != null && !node.getCcUserIds().isEmpty()) {
                // 假设存储格式为 JSON 数组字符串: ["user1", "user2"]
                String userIdsStr = node.getCcUserIds().trim();
                if (userIdsStr.startsWith("[") && userIdsStr.endsWith("]")) {
                    userIdsStr = userIdsStr.substring(1, userIdsStr.length() - 1);
                    String[] userIds = userIdsStr.split(",");
                    for (String uid : userIds) {
                        uid = uid.trim().replace("\"", "").replace("'", "");
                        if (!uid.isEmpty()) {
                            ccUserIds.add(uid);
                        }
                    }
                }
            }

            // 2. 从节点的 ccRoleIds 字段解析角色，获取该角色的所有用户
            if (node.getCcRoleIds() != null && !node.getCcRoleIds().isEmpty()) {
                String roleIdsStr = node.getCcRoleIds().trim();
                if (roleIdsStr.startsWith("[") && roleIdsStr.endsWith("]")) {
                    roleIdsStr = roleIdsStr.substring(1, roleIdsStr.length() - 1);
                    String[] roleIds = roleIdsStr.split(",");
                    for (String rid : roleIds) {
                        rid = rid.trim().replace("\"", "").replace("'", "");
                        if (!rid.isEmpty()) {
                            List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(rid);
                            ccUserIds.addAll(userRoles.stream().map(UserRoleEntity::getUserId).collect(Collectors.toSet()));
                        }
                    }
                }
            }

            // 创建抄送记录
            for (String ccUserId : ccUserIds) {
                WorkflowCcEntity cc = new WorkflowCcEntity();
                cc.setInstanceId(instance.getId());
                cc.setNodeId(node.getId());
                cc.setNodeName(node.getNodeName());
                cc.setUserId(ccUserId);
                cc.setCcType(ccType);
                cc.setCcContent(ccContent);
                cc.setIsRead(false);

                // 查询用户姓名
                userRepository.findById(ccUserId).ifPresent(user -> {
                    cc.setUserName(user.getNickname());
                });

                ccRepository.save(cc);
            }

            if (!ccUserIds.isEmpty()) {
                log.info("创建抄送记录: instanceId={}, node={}, ccType={}, ccCount={}",
                        instance.getId(), node.getNodeName(), ccType, ccUserIds.size());
            }
        } catch (Exception e) {
            log.error("创建抄送记录失败: instanceId={}, node={}", instance.getId(), node.getNodeName(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    @Transactional
    public WorkflowAddSignResp addSign(String instanceId, AddSignReq req) {
        String initiatorId = getCurrentUserId();
        log.info("加签/转办: instanceId={}, initiatorId={}, addType={}, addUserId={}",
                instanceId, initiatorId, req.addType(), req.addUserId());

        WorkflowInstanceEntity instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("工作流实例不存在"));

        // 只有运行中的工作流可以加签/转办
        if (!instance.isRunning()) {
            throw new IllegalArgumentException("只有运行中的工作流可以加签/转办");
        }

        // 获取当前节点
        WorkflowNodeEntity currentNode = nodeRepository.findById(instance.getCurrentNodeId())
                .orElseThrow(() -> new IllegalArgumentException("当前节点不存在"));

        // 获取当前用户的审批记录
        WorkflowApprovalEntity myApproval = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending() && a.getApproverId().equals(initiatorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("您没有权限对当前流程进行加签/转办"));

        // 验证被加签人存在
        UserEntity addUser = userRepository.findById(req.addUserId())
                .orElseThrow(() -> new IllegalArgumentException("被加签人不存在"));

        // 获取发起人信息
        UserEntity initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("加签发起人不存在"));

        // 加签前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_ADD_SIGN", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req)
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "加签前校验失败" : preResult.blockingMessages().get(0));
        }

        // 处理转办
        if (req.addType() == AddSignReq.AddSignType.TRANSFER) {
            WorkflowAddSignResp result = handleTransfer(instance, currentNode, myApproval, addUser, initiator, req);

            // 加签后钩子执行
            HookExecutionSummary postResult = hookService.executeAllHooks(
                "POST_ADD_SIGN", instance, currentNode,
                deserializeFormData(instance.getBusinessData()),
                Map.of("req", req, "result", result)
            );
            if (!postResult.warningMessages().isEmpty()) {
                log.warn("加签后钩子警告: {}", postResult.warningMessages());
            }

            return result;
        }

        // 处理加签（前加签、后加签）
        WorkflowAddSignResp result = handleAddSign(instance, currentNode, myApproval, addUser, initiator, req);

        // 加签后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_ADD_SIGN", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", req, "result", result)
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("加签后钩子警告: {}", postResult.warningMessages());
        }

        return result;
    }

    /**
     * 处理转办
     */
    private WorkflowAddSignResp handleTransfer(
            WorkflowInstanceEntity instance,
            WorkflowNodeEntity currentNode,
            WorkflowApprovalEntity myApproval,
            UserEntity addUser,
            UserEntity initiator,
            AddSignReq req) {

        log.info("处理转办: instanceId={}, fromUser={}, toUser={}",
                instance.getId(), initiator.getId(), req.addUserId());

        // 更新原始审批记录为已转办
        myApproval.setApprovalStatus("transferred");
        myApproval.setComment("已转办给：" + addUser.getNickname() + "。原因：" + req.reason());
        myApproval.setApprovalTime(Instant.now());
        approvalRepository.save(myApproval);

        // 创建新的审批记录给被转办人
        WorkflowApprovalEntity newApproval = new WorkflowApprovalEntity();
        newApproval.setInstanceId(instance.getId());
        newApproval.setNodeId(currentNode.getId());
        newApproval.setNodeName(currentNode.getNodeName());
        newApproval.setApproverId(addUser.getId());
        newApproval.setApproverName(addUser.getNickname());
        newApproval.setApprovalStatus("pending");
        approvalRepository.save(newApproval);

        // 创建加签记录
        WorkflowAddSignEntity addSign = new WorkflowAddSignEntity();
        addSign.setInstanceId(instance.getId());
        addSign.setNodeId(currentNode.getId());
        addSign.setNodeName(currentNode.getNodeName());
        addSign.setInitiatorId(initiator.getId());
        addSign.setInitiatorName(initiator.getNickname());
        addSign.setAddUserId(addUser.getId());
        addSign.setAddUserName(addUser.getNickname());
        addSign.setAddType("transfer");
        addSign.setAddReason(req.reason());
        addSign.setOriginalApproverId(myApproval.getApproverId());
        addSignRepository.save(addSign);

        log.info("转办完成: instanceId={}, original={}, new={}",
                instance.getId(), initiator.getNickname(), addUser.getNickname());

        return toAddSignResponse(addSign);
    }

    /**
     * 处理加签
     */
    private WorkflowAddSignResp handleAddSign(
            WorkflowInstanceEntity instance,
            WorkflowNodeEntity currentNode,
            WorkflowApprovalEntity myApproval,
            UserEntity addUser,
            UserEntity initiator,
            AddSignReq req) {

        log.info("处理加签: instanceId={}, initiatorId={}, addUserId={}, addType={}",
                instance.getId(), initiator.getId(), req.addUserId(), req.addType());

        // 创建新的审批记录给被加签人
        WorkflowApprovalEntity newApproval = new WorkflowApprovalEntity();
        newApproval.setInstanceId(instance.getId());
        newApproval.setNodeId(currentNode.getId());
        newApproval.setNodeName(currentNode.getNodeName());
        newApproval.setApproverId(addUser.getId());
        newApproval.setApproverName(addUser.getNickname());
        newApproval.setApprovalStatus("pending");
        approvalRepository.save(newApproval);

        // 创建加签记录
        WorkflowAddSignEntity addSign = new WorkflowAddSignEntity();
        addSign.setInstanceId(instance.getId());
        addSign.setNodeId(currentNode.getId());
        addSign.setNodeName(currentNode.getNodeName());
        addSign.setInitiatorId(initiator.getId());
        addSign.setInitiatorName(initiator.getNickname());
        addSign.setAddUserId(addUser.getId());
        addSign.setAddUserName(addUser.getNickname());
        addSign.setAddType(req.addType().name().toLowerCase());
        addSign.setAddReason(req.reason());
        addSignRepository.save(addSign);

        // 如果是会签节点，需要重新计算是否所有人都已审批
        if (currentNode.getIsCounterSign()) {
            // 会签节点需要所有人都审批通过，加签后需要继续等待
            log.info("会签节点加签，需要所有审批人审批");
        }

        log.info("加签完成: instanceId={}, addType={}, addUser={}",
                instance.getId(), req.addType(), addUser.getNickname());

        return toAddSignResponse(addSign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowAddSignResp> getAddSignRecords(String instanceId) {
        log.info("查询加签记录: instanceId={}", instanceId);
        return addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId)
                .stream()
                .map(this::toAddSignResponse)
                .collect(Collectors.toList());
    }

    private WorkflowAddSignResp toAddSignResponse(WorkflowAddSignEntity entity) {
        return new WorkflowAddSignResp(
                entity.getId(),
                entity.getInstanceId(),
                entity.getNodeId(),
                entity.getNodeName(),
                entity.getInitiatorId(),
                entity.getInitiatorName(),
                entity.getAddUserId(),
                entity.getAddUserName(),
                entity.getAddType(),
                entity.getAddReason(),
                entity.getOriginalApproverId(),
                entity.getCreateTime()
        );
    }

    private WorkflowCcResp toCcResponse(WorkflowCcEntity entity) {
        return new WorkflowCcResp(
                entity.getId(),
                entity.getInstanceId(),
                entity.getNodeId(),
                entity.getNodeName(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getCcType(),
                entity.getCcContent(),
                entity.getIsRead(),
                entity.getReadTime(),
                entity.getCreateTime()
        );
    }

    private void applyDraftChanges(WorkflowInstanceEntity instance, WorkflowStartReq req) {
        if (!instance.getDefinitionId().equals(req.definitionId())) {
            WorkflowDefinitionEntity definition = definitionRepository.findById(req.definitionId())
                    .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在"));
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
        }

        instance.setTitle(req.title());
        instance.setBusinessData(serializeFormData(req.formData()));
        instance.setRemark(req.remark());
    }

    private String serializeFormData(Map<String, Object> formData) {
        try {
            return objectMapper.writeValueAsString(formData == null ? Collections.emptyMap() : formData);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("表单数据格式不正确", e);
        }
    }

    private Map<String, Object> deserializeFormData(String businessData) {
        if (businessData == null || businessData.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(businessData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("业务表单数据解析失败", e);
        }
    }

    private WorkflowOperationPermissionsResp buildOperationPermissions(WorkflowInstanceEntity instance, boolean canApprove) {
        String currentUserId = getCurrentUserId();
        boolean isOwner = Objects.equals(instance.getUserId(), currentUserId);

        return new WorkflowOperationPermissionsResp(
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

    private String normalizeStatusForStorage(String status) {
        return switch (status == null ? "" : status.toUpperCase(Locale.ROOT)) {
            case "DRAFT" -> "draft";
            case "PENDING", "PROCESSING" -> "running";
            case "APPROVED", "FINISHED", "COMPLETED" -> "approved";
            case "REJECTED", "WITHDRAWN" -> "rejected";
            case "CANCELLED" -> "cancelled";
            default -> status == null ? null : status.toLowerCase(Locale.ROOT);
        };
    }

    private String normalizeStatusForResponse(String status) {
        return switch (status) {
            case "draft" -> "DRAFT";
            case "running" -> "PROCESSING";
            case "approved" -> "APPROVED";
            case "rejected" -> "REJECTED";
            case "cancelled" -> "CANCELLED";
            default -> status == null ? null : status.toUpperCase(Locale.ROOT);
        };
    }
}
