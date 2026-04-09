package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;
import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import com.adminplus.repository.WorkflowNodeHookRepository;
import com.adminplus.service.WorkflowNodeHookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public WorkflowNodeHookResponse create(WorkflowNodeHookRequest request) {
        log.info("创建钩子配置: nodeId={}, hookPoint={}", request.nodeId(), request.hookPoint());
        WorkflowNodeHookEntity entity = toEntity(request);
        WorkflowNodeHookEntity saved = hookRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public WorkflowNodeHookResponse update(String id, WorkflowNodeHookRequest request) {
        log.info("更新钩子配置: id={}", id);
        WorkflowNodeHookEntity entity = toEntity(request);
        entity.setId(id);
        WorkflowNodeHookEntity saved = hookRepository.save(entity);
        return toDto(saved);
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
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeHookResponse> listByNodeId(String nodeId) {
        List<WorkflowNodeHookEntity> entities = hookRepository
                .findByNodeIdAndDeletedFalseOrderByPriorityAsc(nodeId);
        return entities.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowNodeHookResponse> listByNodeIdAndHookPoint(String nodeId, String hookPoint) {
        List<WorkflowNodeHookEntity> entities = hookRepository
                .findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(nodeId, hookPoint);
        return entities.stream().map(this::toDto).toList();
    }

    private WorkflowNodeHookEntity toEntity(WorkflowNodeHookRequest req) {
        WorkflowNodeHookEntity entity = new WorkflowNodeHookEntity();
        entity.setNodeId(req.nodeId());
        entity.setHookPoint(req.hookPoint());
        entity.setHookType(req.hookType());
        entity.setExecutorType(req.executorType());
        entity.setExecutorConfig(req.executorConfig());
        entity.setAsyncExecution(req.asyncExecution());
        entity.setBlockOnFailure(req.blockOnFailure());
        entity.setFailureMessage(req.failureMessage());
        entity.setPriority(req.priority());
        entity.setConditionExpression(req.conditionExpression());
        entity.setRetryCount(req.retryCount());
        entity.setRetryInterval(req.retryInterval() != null ? req.retryInterval() : 1000);
        entity.setHookName(req.hookName());
        entity.setDescription(req.description());
        return entity;
    }

    private WorkflowNodeHookResponse toDto(WorkflowNodeHookEntity entity) {
        return new WorkflowNodeHookResponse(
                entity.getId(),
                entity.getNodeId(),
                entity.getHookPoint(),
                entity.getHookType(),
                entity.getExecutorType(),
                entity.getExecutorConfig(),
                entity.getAsyncExecution(),
                entity.getBlockOnFailure(),
                entity.getFailureMessage(),
                entity.getPriority(),
                entity.getConditionExpression(),
                entity.getRetryCount(),
                entity.getRetryInterval(),
                entity.getHookName(),
                entity.getDescription(),
                entity.getCreateUser(),
                entity.getUpdateUser(),
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }
}
