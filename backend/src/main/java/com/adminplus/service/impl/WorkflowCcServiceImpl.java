package com.adminplus.service.impl;

import com.adminplus.pojo.entity.WorkflowCcEntity;
import com.adminplus.pojo.dto.response.WorkflowCcResponse;
import com.adminplus.repository.WorkflowCcRepository;
import com.adminplus.service.WorkflowCcService;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流抄送服务实现
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowCcServiceImpl implements WorkflowCcService {

    private final WorkflowCcRepository ccRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowCcResponse> getUserCcRecords(String userId) {
        log.info("查询用户抄送记录: userId={}", userId);
        return ccRepository.findByUserId(userId).stream()
                .map(this::toCcResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowCcResponse> getUnreadCcRecords(String userId) {
        log.info("查询用户未读抄送记录: userId={}", userId);
        return ccRepository.findUnreadByUserId(userId).stream()
                .map(this::toCcResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadCcRecords(String userId) {
        long count = ccRepository.countUnreadByUserId(userId);
        log.info("统计用户未读抄送数量: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(String ccId) {
        String userId = getCurrentUserId();
        log.info("标记抄送记录为已读: ccId={}, userId={}", ccId, userId);

        WorkflowCcEntity cc = ccRepository.findById(ccId)
                .orElseThrow(() -> new IllegalArgumentException("抄送记录不存在"));

        // 验证权限
        if (!cc.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限标记此抄送记录");
        }

        cc.markAsRead();
        ccRepository.save(cc);
    }

    @Override
    @Transactional
    public void markAsReadBatch(List<String> ccIds) {
        String userId = getCurrentUserId();
        log.info("批量标记抄送记录为已读: count={}, userId={}", ccIds.size(), userId);

        List<WorkflowCcEntity> ccList = ccRepository.findAllById(ccIds);

        // 过滤出属于当前用户的记录
        List<WorkflowCcEntity> userCcList = ccList.stream()
                .filter(cc -> cc.getUserId().equals(userId))
                .collect(Collectors.toList());

        // 标记为已读
        userCcList.forEach(WorkflowCcEntity::markAsRead);
        ccRepository.saveAll(userCcList);

        log.info("实际标记抄送记录为已读: count={}", userCcList.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowCcResponse> getInstanceCcRecords(String instanceId) {
        log.info("查询工作流实例抄送记录: instanceId={}", instanceId);
        return ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instanceId).stream()
                .map(this::toCcResponse)
                .collect(Collectors.toList());
    }

    private WorkflowCcResponse toCcResponse(WorkflowCcEntity entity) {
        return new WorkflowCcResponse(
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

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
