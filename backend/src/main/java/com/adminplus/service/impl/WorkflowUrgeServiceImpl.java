package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.UrgeActionRequest;
import com.adminplus.pojo.dto.response.WorkflowUrgeResponse;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowUrgeService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.ServiceAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流催办服务实现
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowUrgeServiceImpl implements WorkflowUrgeService {

    private final WorkflowUrgeRepository urgeRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowApprovalRepository approvalRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final UserRepository userRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public void urgeWorkflow(String instanceId, UrgeActionRequest req) {
        String urgeUserId = getCurrentUserId();
        log.info("催办工作流: instanceId={}, urgeUserId={}", instanceId, urgeUserId);

        WorkflowInstanceEntity instance = EntityHelper.findByIdOrThrow(
                instanceRepository::findById, instanceId, "工作流实例不存在");

        // 只有发起人可以催办
        ServiceAssert.isTrue(instance.getUserId().equals(urgeUserId), "只有发起人可以催办工作流");

        // 只有运行中的工作流可以催办
        ServiceAssert.isTrue(instance.isRunning(), "只有运行中的工作流可以催办");

        // 获取当前节点
        WorkflowNodeEntity currentNode = EntityHelper.findByIdOrThrow(
                nodeRepository::findById, instance.getCurrentNodeId(), "当前节点不存在");

        // 获取当前节点的所有待审批记录
        List<WorkflowApprovalEntity> pendingApprovals = approvalRepository
                .findByInstanceIdAndNodeIdAndDeletedFalse(instanceId, instance.getCurrentNodeId())
                .stream()
                .filter(WorkflowApprovalEntity::isPending)
                .toList();

        ServiceAssert.isTrue(!pendingApprovals.isEmpty(), "当前节点没有待审批人");

        // 获取催办人信息
        UserEntity urgeUser = EntityHelper.findByIdOrThrow(
                userRepository::findById, urgeUserId, "催办人不存在");

        // 创建催办记录
        int urgeCount = 0;
        for (WorkflowApprovalEntity approval : pendingApprovals) {
            // 如果指定了目标审批人，则只催办该人
            if (req.targetApproverId() != null && !req.targetApproverId().isEmpty()
                    && !approval.getApproverId().equals(req.targetApproverId())) {
                continue;
            }

            WorkflowUrgeEntity urge = new WorkflowUrgeEntity();
            urge.setInstanceId(instanceId);
            urge.setNodeId(currentNode.getId());
            urge.setNodeName(currentNode.getNodeName());
            urge.setUrgeUserId(urgeUserId);
            urge.setUrgeUserName(urgeUser.getNickname());
            urge.setUrgeTargetId(approval.getApproverId());
            urge.setUrgeTargetName(approval.getApproverName());
            urge.setUrgeContent(req.content());
            urge.setIsRead(false);

            urgeRepository.save(urge);
            urgeCount++;

            // 发送催办通知
            sendUrgeNotification(instanceId, approval.getApproverId(), urgeUserId);
        }

        log.info("催办工作流完成: instanceId={}, urgeCount={}", instanceId, urgeCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowUrgeResponse> getReceivedUrgeRecords(String userId) {
        log.info("查询用户收到的催办记录: userId={}", userId);
        return urgeRepository.findByUrgeTargetId(userId).stream()
                .map(urge -> conversionService.convert(urge, WorkflowUrgeResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowUrgeResponse> getSentUrgeRecords(String userId) {
        log.info("查询用户发送的催办记录: userId={}", userId);
        return urgeRepository.findByUrgeUserId(userId).stream()
                .map(urge -> conversionService.convert(urge, WorkflowUrgeResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowUrgeResponse> getUnreadUrgeRecords(String userId) {
        log.info("查询用户未读催办记录: userId={}", userId);
        return urgeRepository.findUnreadByUrgeTargetId(userId).stream()
                .map(urge -> conversionService.convert(urge, WorkflowUrgeResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadUrgeRecords(String userId) {
        long count = urgeRepository.countUnreadByUrgeTargetId(userId);
        log.info("统计用户未读催办数量: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(String urgeId) {
        String userId = getCurrentUserId();
        log.info("标记催办记录为已读: urgeId={}, userId={}", urgeId, userId);

        WorkflowUrgeEntity urge = EntityHelper.findByIdOrThrow(
                urgeRepository::findById, urgeId, "催办记录不存在");

        // 验证权限（只有被催办人可以标记已读）
        ServiceAssert.isTrue(urge.getUrgeTargetId().equals(userId), "无权限标记此催办记录");

        urge.markAsRead();
        urgeRepository.save(urge);
    }

    @Override
    @Transactional
    public void markAsReadBatch(List<String> urgeIds) {
        String userId = getCurrentUserId();
        log.info("批量标记催办记录为已读: count={}, userId={}", urgeIds.size(), userId);

        List<WorkflowUrgeEntity> urgeList = urgeRepository.findAllById(urgeIds);

        // 过滤出属于当前用户的记录
        List<WorkflowUrgeEntity> userUrgeList = urgeList.stream()
                .filter(u -> u.getUrgeTargetId().equals(userId))
                .collect(Collectors.toList());

        // 标记为已读
        userUrgeList.forEach(WorkflowUrgeEntity::markAsRead);
        urgeRepository.saveAll(userUrgeList);

        log.info("实际标记催办记录为已读: count={}", userUrgeList.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowUrgeResponse> getInstanceUrgeRecords(String instanceId) {
        log.info("查询工作流实例催办记录: instanceId={}", instanceId);
        return urgeRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId).stream()
                .map(urge -> conversionService.convert(urge, WorkflowUrgeResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * 发送催办通知
     * <p>
     * 未来扩展点：可接入站内信、邮件、企业微信等通知渠道
     * </p>
     *
     * @param instanceId    工作流实例ID
     * @param targetUserId  目标用户ID（被催办人）
     * @param urgeUserId    催办人ID
     */
    private void sendUrgeNotification(String instanceId, String targetUserId, String urgeUserId) {
        log.info("发送催办通知: instanceId={}, targetUserId={}, urgeUserId={}",
                instanceId, targetUserId, urgeUserId);
        // 未来扩展：接入通知服务（站内信/邮件/企业微信等）
    }

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}