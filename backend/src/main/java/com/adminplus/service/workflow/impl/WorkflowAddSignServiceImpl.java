package com.adminplus.service.workflow.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.adminplus.common.exception.BizException;
import com.adminplus.enums.WorkflowStatus;
import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.WorkflowAddSignEntity;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.WorkflowAddSignRepository;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.workflow.WorkflowAddSignService;
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
 * 工作流加签服务实现
 * <p>
 * 负责工作流的加签和转办操作
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAddSignServiceImpl implements WorkflowAddSignService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowAddSignRepository addSignRepository;
    private final UserRepository userRepository;
    private final WorkflowHookService hookService;
    private final WorkflowPermissionChecker permissionChecker;
    private final ConversionService conversionService;
    private final JsonMapper objectMapper;

    @Override
    @Transactional
    public WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request) {
        String initiatorId = getCurrentUserId();
        log.info("加签/转办: instanceId={}, initiatorId={}, addType={}, addUserId={}",
                instanceId, initiatorId, request.addType(), request.addUserId());

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        // 只有运行中的工作流可以加签/转办
        ServiceAssert.isTrue(isRunning(instance.getStatus()), "只有运行中的工作流可以加签/转办");

        // 获取当前节点
        WorkflowNodeEntity currentNode = EntityHelper.findByIdOrThrow(
            nodeRepository::findById, instance.getCurrentNodeId(), "当前节点不存在");

        // 获取当前用户的审批记录
        WorkflowApprovalEntity myApproval = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(a -> a.isPending() && a.getApproverId().equals(initiatorId))
                .findFirst()
                .orElseThrow(() -> new BizException("您没有权限对当前流程进行加签/转办"));

        // 验证被加签人存在
        UserEntity addUser = EntityHelper.findByIdOrThrow(
            userRepository::findById, request.addUserId(), "被加签人不存在");

        // 获取发起人信息
        UserEntity initiator = EntityHelper.findByIdOrThrow(
            userRepository::findById, initiatorId, "加签发起人不存在");

        // 加签前钩子校验
        HookExecutionSummary preResult = hookService.executeAllHooks(
            "PRE_ADD_SIGN", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", request)
        );

        if (!preResult.allPassed()) {
            throw new BizException(400,
                preResult.blockingMessages().isEmpty() ? "加签前校验失败" : preResult.blockingMessages().getFirst());
        }

        // 处理转办
        if (request.addType() == AddSignRequest.AddSignType.TRANSFER) {
            WorkflowAddSignResponse result = handleTransfer(instance, currentNode, myApproval, addUser, initiator, request);

            // 加签后钩子执行
            HookExecutionSummary postResult = hookService.executeAllHooks(
                "POST_ADD_SIGN", instance, currentNode,
                deserializeFormData(instance.getBusinessData()),
                Map.of("req", request, "result", result)
            );
            if (!postResult.warningMessages().isEmpty()) {
                log.warn("加签后钩子警告: {}", postResult.warningMessages());
            }

            return result;
        }

        // 处理加签（前加签、后加签）
        WorkflowAddSignResponse result = handleAddSign(instance, currentNode, myApproval, addUser, initiator, request);

        // 加签后钩子执行
        HookExecutionSummary postResult = hookService.executeAllHooks(
            "POST_ADD_SIGN", instance, currentNode,
            deserializeFormData(instance.getBusinessData()),
            Map.of("req", request, "result", result)
        );
        if (!postResult.warningMessages().isEmpty()) {
            log.warn("加签后钩子警告: {}", postResult.warningMessages());
        }

        return result;
    }

    /**
     * 处理转办
     */
    private WorkflowAddSignResponse handleTransfer(
            WorkflowInstanceEntity instance,
            WorkflowNodeEntity currentNode,
            WorkflowApprovalEntity myApproval,
            UserEntity addUser,
            UserEntity initiator,
            AddSignRequest req) {

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

        return conversionService.convert(addSign, WorkflowAddSignResponse.class);
    }

    /**
     * 处理加签
     */
    private WorkflowAddSignResponse handleAddSign(
            WorkflowInstanceEntity instance,
            WorkflowNodeEntity currentNode,
            WorkflowApprovalEntity myApproval,
            UserEntity addUser,
            UserEntity initiator,
            AddSignRequest req) {

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
        if (Boolean.TRUE.equals(currentNode.getIsCounterSign())) {
            // 会签节点需要所有人都审批通过，加签后需要继续等待
            log.info("会签节点加签，需要所有审批人审批");
        }

        log.info("加签完成: instanceId={}, addType={}, addUser={}",
                instance.getId(), req.addType(), addUser.getNickname());

        return conversionService.convert(addSign, WorkflowAddSignResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowAddSignResponse> getAddSignRecords(String instanceId) {
        String userId = getCurrentUserId();
        log.info("查询加签记录: instanceId={}", instanceId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
            instanceRepository::findById, instanceId, "工作流实例不存在");

        // 权限检查：发起人、审批人、抄送人才可查看
        permissionChecker.checkViewAccess(instance, userId);

        return addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId)
                .stream()
                .map(as -> conversionService.convert(as, WorkflowAddSignResponse.class))
                .collect(Collectors.toList());
    }

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private boolean isRunning(String status) {
        return WorkflowStatus.RUNNING.getCode().equals(status);
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