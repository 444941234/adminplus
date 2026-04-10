package com.adminplus.service.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;
import com.adminplus.pojo.dto.response.WorkflowApprovalResponse;
import com.adminplus.pojo.dto.response.WorkflowDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.pojo.dto.response.WorkflowCcResponse;
import com.adminplus.pojo.dto.response.WorkflowOperationPermissionsResponse;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.WorkflowAddSignRepository;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowCcRepository;
import com.adminplus.repository.WorkflowDefinitionRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.WorkflowInstanceService;
import com.adminplus.service.workflow.WorkflowAddSignService;
import com.adminplus.service.workflow.WorkflowApprovalService;
import com.adminplus.service.workflow.WorkflowDraftService;
import com.adminplus.service.workflow.WorkflowRollbackService;
import com.adminplus.service.workflow.impl.WorkflowPermissionChecker;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流实例服务实现 - 协调器
 * <p>
 * 作为协调器，将请求委托给专门的子服务处理：
 * - WorkflowDraftService: 草稿管理
 * - WorkflowApprovalService: 审批流程
 * - WorkflowRollbackService: 回退操作
 * - WorkflowAddSignService: 加签/转办
 * - WorkflowPermissionChecker: 权限检查
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    // ==================== 子服务依赖 ====================
    private final WorkflowDraftService draftService;
    private final WorkflowApprovalService approvalService;
    private final WorkflowRollbackService rollbackService;
    private final WorkflowAddSignService addSignService;
    private final WorkflowPermissionChecker permissionChecker;

    // ==================== 数据访问依赖 ====================
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowCcRepository ccRepository;
    private final WorkflowAddSignRepository addSignRepository;
    private final DeptRepository deptRepository;

    // ==================== 工具依赖 ====================
    private final ConversionService conversionService;
    private final JsonMapper objectMapper;

    // ==================== 草稿管理 - 委托给 WorkflowDraftService ====================

    @Override
    @Transactional
    public WorkflowInstanceResponse createDraft(WorkflowStartRequest request) {
        return draftService.createDraft(request);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDraftDetailResponse getDraftDetail(String instanceId) {
        return draftService.getDraftDetail(instanceId);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request) {
        return draftService.updateDraft(instanceId, request);
    }

    @Override
    @Transactional
    public void deleteDraft(String instanceId) {
        draftService.deleteDraft(instanceId);
    }

    // ==================== 审批流程 - 委托给 WorkflowApprovalService ====================

    @Override
    @Transactional
    public WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request) {
        return approvalService.submit(instanceId, request);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse start(WorkflowStartRequest request) {
        log.info("发起工作流: title={}", request.title());
        // 先创建草稿
        WorkflowInstanceResponse draft = createDraft(request);
        // 然后提交
        return submit(draft.id(), null);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request) {
        return approvalService.approve(instanceId, request);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request) {
        return approvalService.reject(instanceId, request);
    }

    @Override
    @Transactional
    public void cancel(String instanceId) {
        approvalService.cancel(instanceId);
    }

    @Override
    @Transactional
    public void withdraw(String instanceId) {
        approvalService.withdraw(instanceId);
    }

    // ==================== 回退操作 - 委托给 WorkflowRollbackService ====================

    @Override
    @Transactional
    public WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request) {
        return rollbackService.rollback(instanceId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeResponse> getRollbackableNodes(String instanceId) {
        return rollbackService.getRollbackableNodes(instanceId);
    }

    // ==================== 加签/转办 - 委托给 WorkflowAddSignService ====================

    @Override
    @Transactional
    public WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request) {
        return addSignService.addSign(instanceId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowAddSignResponse> getAddSignRecords(String instanceId) {
        return addSignService.getAddSignRecords(instanceId);
    }

    // ==================== 保留在此服务的独特方法 ====================

    /**
     * 查询工作流详情
     * <p>
     * 此方法返回 WorkflowDetailResponse，包含完整的流程信息、审批记录、节点列表等，
     * 不同于其他方法返回的 WorkflowInstanceResponse，因此保留在此服务
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public WorkflowDetailResponse getDetail(String instanceId) {
        String userId = getCurrentUserId();
        log.info("查询工作流详情: instanceId={}", instanceId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        // 权限检查：发起人、审批人、抄送人才可查看
        permissionChecker.checkViewAccess(instance, userId);

        // 查询审批记录
        List<WorkflowApprovalResponse> approvals = approvalRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(a -> conversionService.convert(a, WorkflowApprovalResponse.class))
                .collect(Collectors.toList());

        // 查询所有节点
        List<WorkflowNodeResponse> nodes = nodeRepository
                .findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(instance.getDefinitionId())
                .stream()
                .map(n -> conversionService.convert(n, WorkflowNodeResponse.class))
                .collect(Collectors.toList());

        // 查询当前节点
        WorkflowNodeResponse currentNode = null;
        if (instance.getCurrentNodeId() != null) {
            WorkflowNodeEntity nodeEntity = nodeRepository.findById(instance.getCurrentNodeId()).orElse(null);
            if (nodeEntity != null) {
                currentNode = conversionService.convert(nodeEntity, WorkflowNodeResponse.class);
            }
        }

        // 判断当前用户是否可以审批
        boolean canApprove = permissionChecker.canUserApprove(instance, userId);

        WorkflowDefinitionEntity definition = EntityHelper.findByIdOrThrow(
            definitionRepository::findById, instance.getDefinitionId(), "工作流定义不存在");

        // 查询抄送记录
        List<WorkflowCcResponse> ccRecords = ccRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(cc -> conversionService.convert(cc, WorkflowCcResponse.class))
                .collect(Collectors.toList());

        // 查询加签记录
        List<WorkflowAddSignResponse> addSignRecords = addSignRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId)
                .stream()
                .map(as -> conversionService.convert(as, WorkflowAddSignResponse.class))
                .collect(Collectors.toList());

        // 构建操作权限
        WorkflowOperationPermissionsResponse operationPermissions = permissionChecker
                .buildOperationPermissions(instance, userId, canApprove);

        return new WorkflowDetailResponse(
                toInstanceResponse(instance, instance.isRunning() && canApprove, canApprove),
                approvals,
                nodes,
                currentNode,
                canApprove,
                definition.getFormConfig(),
                deserializeFormData(instance.getBusinessData()),
                ccRecords,
                addSignRecords,
                operationPermissions
        );
    }

    /**
     * 查询我发起的工作流
     */
    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstanceResponse> getMyWorkflows(String status) {
        String userId = getCurrentUserId();
        log.info("查询我发起的工作流: userId={}, status={}", userId, status);

        List<WorkflowInstanceEntity> instances;
        if (status == null || status.isEmpty()) {
            instances = instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(userId);
        } else {
            instances = instanceRepository.findByUserIdAndStatusAndDeletedFalseOrderBySubmitTimeDesc(
                    userId, normalizeStatusForStorage(status));
        }

        // Batch fetch dept names to avoid N+1 queries
        Map<String, String> deptNameMap = batchGetDeptNames(instances);

        return instances.stream()
                .map(i -> toInstanceResponseWithDeptName(i, deptNameMap.get(i.getDeptId()), false, false))
                .collect(Collectors.toList());
    }

    /**
     * 查询待我审批的工作流
     */
    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstanceResponse> getPendingApprovals() {
        String userId = getCurrentUserId();
        log.info("查询待我审批的工作流: userId={}", userId);

        List<WorkflowInstanceEntity> instances = instanceRepository.findPendingApprovalsByUser(userId);

        // Batch fetch dept names to avoid N+1 queries
        Map<String, String> deptNameMap = batchGetDeptNames(instances);

        return instances.stream()
                .map(i -> toInstanceResponseWithDeptName(i, deptNameMap.get(i.getDeptId()), true, true))
                .collect(Collectors.toList());
    }

    /**
     * 统计待审批数量
     */
    @Override
    @Transactional(readOnly = true)
    public long countPendingApprovals() {
        String userId = getCurrentUserId();
        return instanceRepository.countPendingApprovalsByUser(userId);
    }

    /**
     * 查询审批记录
     */
    @Override
    @Transactional(readOnly = true)
    public List<WorkflowApprovalResponse> getApprovals(String instanceId) {
        String userId = getCurrentUserId();
        log.info("查询审批记录: instanceId={}", instanceId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        // 权限检查：发起人、审批人、抄送人才可查看
        permissionChecker.checkViewAccess(instance, userId);

        return approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId)
                .stream()
                .map(a -> conversionService.convert(a, WorkflowApprovalResponse.class))
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
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

    /**
     * 转换为响应对象
     */
    private WorkflowInstanceResponse toInstanceResponse(WorkflowInstanceEntity entity, Boolean pendingApproval, Boolean canApprove) {
        String currentUserId = getCurrentUserId();
        WorkflowInstanceResponse base = conversionService.convert(entity, WorkflowInstanceResponse.class);

        return withPermissions(base, currentUserId, pendingApproval, canApprove);
    }

    /**
     * 转换为响应对象（带预先查询的部门名称，避免 N+1 问题）
     */
    private WorkflowInstanceResponse toInstanceResponseWithDeptName(WorkflowInstanceEntity entity, String deptName, Boolean pendingApproval, Boolean canApprove) {
        String currentUserId = getCurrentUserId();
        WorkflowInstanceResponse base = conversionService.convert(entity, WorkflowInstanceResponse.class);

        // 如果预查询了 deptName，需要重新创建（converter 查询可能与预查询不同）
        if (deptName != null && base.deptName() == null) {
            base = new WorkflowInstanceResponse(
                    base.id(), base.definitionId(), base.definitionName(),
                    base.userId(), base.userName(), base.deptId(), deptName,
                    base.title(), base.businessData(),
                    base.currentNodeId(), base.currentNodeName(), base.status(),
                    base.submitTime(), base.finishTime(), base.remark(), base.createTime(),
                    null, null, null, null, null, null, null
            );
        }

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

    /**
     * 反序列化表单数据
     */
    private Map<String, Object> deserializeFormData(String businessData) {
        if (businessData == null || businessData.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(businessData, new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new IllegalArgumentException("业务表单数据解析失败", e);
        }
    }

    /**
     * 状态规范化（用于存储查询）
     */
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
}