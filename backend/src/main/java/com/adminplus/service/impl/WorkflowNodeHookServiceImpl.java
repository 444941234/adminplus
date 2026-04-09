package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import com.adminplus.repository.WorkflowNodeHookRepository;
import com.adminplus.service.WorkflowNodeHookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作流节点钩子配置服务实现
 *
 * @author AdminPlus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowNodeHookServiceImpl implements WorkflowNodeHookService {

    private final WorkflowNodeHookRepository hookRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public WorkflowNodeHookResponse create(WorkflowNodeHookRequest request) {
        log.info("创建钩子配置: nodeId={}, hookPoint={}", request.nodeId(), request.hookPoint());
        WorkflowNodeHookEntity entity = conversionService.convert(request, WorkflowNodeHookEntity.class);
        WorkflowNodeHookEntity saved = hookRepository.save(entity);
        return conversionService.convert(saved, WorkflowNodeHookResponse.class);
    }

    @Override
    @Transactional
    public WorkflowNodeHookResponse update(String id, WorkflowNodeHookRequest request) {
        log.info("更新钩子配置: id={}", id);
        WorkflowNodeHookEntity entity = conversionService.convert(request, WorkflowNodeHookEntity.class);
        entity.setId(id);
        WorkflowNodeHookEntity saved = hookRepository.save(entity);
        return conversionService.convert(saved, WorkflowNodeHookResponse.class);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("删除钩子配置: id={}", id);
        hookRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowNodeHookResponse getById(String id) {
        WorkflowNodeHookEntity entity = hookRepository.findById(id)
                .orElseThrow(() -> new BizException("钩子配置不存在"));
        return conversionService.convert(entity, WorkflowNodeHookResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeHookResponse> listByNodeId(String nodeId) {
        List<WorkflowNodeHookEntity> entities = hookRepository
                .findByNodeIdAndDeletedFalseOrderByPriorityAsc(nodeId);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowNodeHookResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeHookResponse> listByNodeIdAndHookPoint(String nodeId, String hookPoint) {
        List<WorkflowNodeHookEntity> entities = hookRepository
                .findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(nodeId, hookPoint);
        return entities.stream()
                .map(e -> conversionService.convert(e, WorkflowNodeHookResponse.class))
                .toList();
    }
}