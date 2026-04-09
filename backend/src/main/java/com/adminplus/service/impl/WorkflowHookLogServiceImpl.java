package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.response.WorkflowHookLogResponse;
import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import com.adminplus.repository.WorkflowHookLogRepository;
import com.adminplus.service.WorkflowHookLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流钩子日志服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowHookLogServiceImpl implements WorkflowHookLogService {

    private final WorkflowHookLogRepository hookLogRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowHookLogResponse> listByInstanceId(String instanceId) {
        List<WorkflowHookLogEntity> entities = hookLogRepository
                .findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(instanceId);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowHookLogResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowHookLogResponse> listByInstanceIdAndHookPoint(String instanceId, String hookPoint) {
        List<WorkflowHookLogEntity> entities = hookLogRepository
                .findByInstanceIdAndHookPointAndDeletedFalseOrderByCreateTimeDesc(instanceId, hookPoint);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowHookLogResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowHookLogResponse> listByInstanceIdAndNodeId(String instanceId, String nodeId) {
        List<WorkflowHookLogEntity> entities = hookLogRepository
                .findByInstanceIdAndNodeIdAndDeletedFalseOrderByCreateTimeDesc(instanceId, nodeId);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowHookLogResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowHookLogResponse> listByHookId(String hookId) {
        List<WorkflowHookLogEntity> entities = hookLogRepository
                .findByHookIdAndDeletedFalseOrderByCreateTimeDesc(hookId);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowHookLogResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowHookLogResponse getById(String id) {
        WorkflowHookLogEntity entity = hookLogRepository.findById(id)
                .orElseThrow(() -> new BizException("钩子日志不存在"));
        return conversionService.convert(entity, WorkflowHookLogResponse.class);
    }
}