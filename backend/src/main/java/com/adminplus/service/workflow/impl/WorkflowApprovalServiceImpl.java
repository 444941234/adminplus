package com.adminplus.service.workflow.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.adminplus.common.exception.BizException;
import com.adminplus.enums.WorkflowStatus;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowCcEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowCcRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.workflow.WorkflowApprovalService;
import com.adminplus.service.workflow.WorkflowApproverResolver;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流审批服务实现
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowApprovalServiceImpl implements WorkflowApprovalService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowCcRepository ccRepository;
    private final UserRepository userRepository;
    private final WorkflowApproverResolver approverResolver;
    private final WorkflowHookService hookService;
    private final ConversionService conversionService;
    private final JsonMapper objectMapper;

    @Override
    @Transactional
    public WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request) {
        String userId = getCurrentUserId();
        log.info("提交工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.isDraft() || instance.isRunning(), "只有草稿或进行中的工作流可以提交");

        // 验证是否为发起人
        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以提交工作流");

        // 更新草稿数据（如果提供了request）
        if (request != null) {
            applyDraftChanges(instance, request);
        }

        instance.setStatus(WorkflowStatus.RUNNING.getCode());
        instance.setSubmitTime(Instant.now());

        // 获取工作流定义的第一个节点
        List<WorkflowNodeEntity> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId());

        ServiceAssert.isTrue(!nodes.isEmpty(), "工作流没有配置审批节点");

        WorkflowNodeEntity firstNode = nodes.get(0);

        // 提交前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_SUBMIT", instance, firstNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", request != null ? request : WorkflowStartRequest.builder().definitionId("").title("").formData(Map.of()).build())
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
    public WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request) {
        String userId = getCurrentUserId();
        log.info("同意审批: instanceId={}, userId={}", instanceId, userId);

        return processApproval(instanceId, request, "approved");
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request) {
        String userId = getCurrentUserId();
        log.info("拒绝审批: instanceId={}, userId={}", instanceId, userId);

        return processApproval(instanceId, request, "rejected");
    }

    @Override
    @Transactional
    public void cancel(String instanceId) {
        String userId = getCurrentUserId();
        log.info("取消工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.isCancellable(), "当前状态不允许取消");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以取消工作流");

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

        instance.setStatus(WorkflowStatus.CANCELLED.getCode());
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
    public void recreateApprovalsForNode(WorkflowInstanceEntity instance, WorkflowNodeEntity targetNode) {
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

    @Override
    @Transactional
    public void withdraw(String instanceId) {
        String userId = getCurrentUserId();
        log.info("撤回工作流: instanceId={}, userId={}", instanceId, userId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.getUserId().equals(userId), "只有发起人可以撤回工作流");

        ServiceAssert.isTrue(instance.isDraft() || instance.isRejected(), "只有草稿或被拒绝的流程可以撤回");

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
        instance.setStatus(WorkflowStatus.DRAFT.getCode());
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

    /**
     * 处理审批
     */
    private WorkflowInstanceResponse processApproval(String instanceId, ApprovalActionRequest request, String action) {
        String userId = getCurrentUserId();
        UserEntity user = EntityHelper.findByIdOrThrow(
            userRepository::findById, userId, "用户不存在");

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        ServiceAssert.isTrue(instance.isRunning(), "只有进行中的工作流可以审批");

        // 查询当前节点的待审批记录
        List<WorkflowApprovalEntity> pendingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(WorkflowApprovalEntity::isPending)
                .collect(Collectors.toList());

        // 查找当前用户的审批记录
        WorkflowApprovalEntity myApproval = pendingApprovals.stream()
                .filter(a -> a.getApproverId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BizException("您没有权限审批此工作流"));

        // 查询当前节点
        WorkflowNodeEntity currentNode = EntityHelper.findByIdOrThrow(
            nodeRepository::findById, instance.getCurrentNodeId(), "当前节点不存在");

        // 审批前钩子校验
        String hookPoint = action.equals("approved") ? "PRE_APPROVE" : "PRE_REJECT";
        HookExecutionSummary preResult = hookService.executeAllHooks(
            hookPoint, instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("request", request, "action", action)
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "审批前校验失败" : preResult.blockingMessages().get(0));
        }

        // 更新审批记录
        myApproval.setApprovalStatus(action);
        myApproval.setComment(request.comment());
        myApproval.setAttachments(request.attachments());
        myApproval.setApprovalTime(Instant.now());
        myApproval.setApproverName(user.getNickname());
        approvalRepository.save(myApproval);

        // 判断是否需要继续流转
        if (action.equals("rejected")) {
            // 拒绝则直接结束
            instance.setStatus(WorkflowStatus.REJECTED.getCode());
            instance.setFinishTime(Instant.now());
            log.info("工作流被拒绝: id={}", instanceId);

            // 创建抄送记录（拒绝时抄送）
            createCcRecords(instance, currentNode, "reject", request.comment());

            // 拒绝后钩子执行
            HookExecutionSummary postResult = hookService.executeAllHooks(
                "POST_REJECT", instance, currentNode,
                deserializeFormData(instance.getBusinessData()),
                Map.of("request", request)
            );
            if (!postResult.warningMessages().isEmpty()) {
                log.warn("拒绝后钩子警告: {}", postResult.warningMessages());
            }
        } else {
            // 同意，检查是否所有人都已审批
            boolean allApproved = pendingApprovals.stream()
                    .allMatch(WorkflowApprovalEntity::isApproved);

            if (allApproved) {
                // 当前节点所有审批人都已同意，流转到下一节点
                moveToNextNode(instance);

                // 创建抄送记录（审批通过时抄送）
                createCcRecords(instance, currentNode, "approve", request.comment());

                // 同意后钩子执行
                HookExecutionSummary postResult = hookService.executeAllHooks(
                    "POST_APPROVE", instance, currentNode,
                    deserializeFormData(instance.getBusinessData()),
                    Map.of("request", request)
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
            instance.setStatus(WorkflowStatus.APPROVED.getCode());
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
        List<String> approverIds = approverResolver.resolveApprovers(instance, node);

        // Batch fetch approver names to avoid N+1 queries
        Map<String, String> approverNames = approverResolver.batchGetApproverNames(approverIds);

        for (String approverId : approverIds) {
            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node.getId());
            approval.setNodeName(node.getNodeName());
            approval.setApproverId(approverId);
            approval.setApproverName(approverNames.get(approverId));
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
                            // TODO: 需要UserRoleRepository来获取角色对应的用户
                            // 这里暂时跳过，因为WorkflowApproverResolver没有这个方法
                            log.warn("解析角色抄送人暂未实现: roleId={}", rid);
                        }
                    }
                }
            }

            // 创建抄送记录
            // Batch fetch user names to avoid N+1 queries
            Map<String, String> userNames = approverResolver.batchGetApproverNames(List.copyOf(ccUserIds));

            for (String ccUserId : ccUserIds) {
                WorkflowCcEntity cc = new WorkflowCcEntity();
                cc.setInstanceId(instance.getId());
                cc.setNodeId(node.getId());
                cc.setNodeName(node.getNodeName());
                cc.setUserId(ccUserId);
                cc.setCcType(ccType);
                cc.setCcContent(ccContent);
                cc.setIsRead(false);
                cc.setUserName(userNames.get(ccUserId));

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
                .filter(WorkflowApprovalEntity::isPending)
                .toList();

        return pendingApprovals.stream()
                .anyMatch(a -> a.getApproverId().equals(userId));
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
        return "PROCESSING".equals(status);
    }

    private boolean isDraft(String status) {
        return "DRAFT".equals(status);
    }

    private boolean isApproved(String status) {
        return "APPROVED".equals(status);
    }

    private boolean isFinished(String status) {
        return "APPROVED".equals(status) || "REJECTED".equals(status) || "CANCELLED".equals(status);
    }

    private boolean isCancellable(String status) {
        return "PROCESSING".equals(status) || "DRAFT".equals(status);
    }

    /**
     * 更新草稿数据
     */
    private void applyDraftChanges(WorkflowInstanceEntity instance, WorkflowStartRequest req) {
        instance.setTitle(req.title());
        instance.setBusinessData(serializeFormData(req.formData()));
        instance.setRemark(req.remark());
    }

    private String serializeFormData(Map<String, Object> formData) {
        try {
            return objectMapper.writeValueAsString(formData == null ? Collections.emptyMap() : formData);
        } catch (JacksonException e) {
            throw new BizException("表单数据格式不正确", e);
        }
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