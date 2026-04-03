package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.WorkflowDefinitionReq;
import com.adminplus.pojo.dto.req.WorkflowNodeReq;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowDefinitionRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.WorkflowDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 工作流定义服务实现
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowNodeRepository nodeRepository;

    @Override
    @CacheEvict(value = "workflowEnabledDefinitions", allEntries = true)
    @Transactional
    public WorkflowDefinitionResp create(WorkflowDefinitionReq req) {
        log.info("创建工作流定义: {}", req.definitionName());

        // 检查键是否已存在
        if (definitionRepository.existsByDefinitionKeyAndDeletedFalse(req.definitionKey())) {
            throw new IllegalArgumentException("工作流标识已存在: " + req.definitionKey());
        }

        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.setDefinitionName(req.definitionName());
        entity.setDefinitionKey(req.definitionKey());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setVersion(1);
        entity.setFormConfig(req.formConfig());

        entity = definitionRepository.save(entity);

        log.info("工作流定义创建成功: id={}", entity.getId());
        return toResponse(entity);
    }

    @Override
    @CacheEvict(value = {"workflowEnabledDefinitions", "workflowNodes"}, allEntries = true)
    @Transactional
    public WorkflowDefinitionResp update(String id, WorkflowDefinitionReq req) {
        log.info("更新工作流定义: id={}", id);

        WorkflowDefinitionEntity entity = definitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + id));

        // 检查键是否与其他记录冲突
        definitionRepository.findByDefinitionKeyAndDeletedFalse(req.definitionKey())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("工作流标识已被使用: " + req.definitionKey());
                    }
                });

        entity.setDefinitionName(req.definitionName());
        entity.setDefinitionKey(req.definitionKey());
        entity.setCategory(req.category());
        entity.setDescription(req.description());
        entity.setStatus(req.status());
        entity.setFormConfig(req.formConfig());

        entity = definitionRepository.save(entity);

        log.info("工作流定义更新成功: id={}", id);
        return toResponse(entity);
    }

    @Override
    @CacheEvict(value = {"workflowEnabledDefinitions", "workflowNodes"}, allEntries = true)
    @Transactional
    public void delete(String id) {
        log.info("删除工作流定义: id={}", id);

        // 同时删除所有节点
        List<WorkflowNodeEntity> nodes = nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(id);
        nodes.forEach(node -> {
            node.setDeleted(true);
            nodeRepository.save(node);
        });

        definitionRepository.deleteById(id);
        log.info("工作流定义删除成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDefinitionResp getById(String id) {
        return definitionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResp> listAll() {
        List<WorkflowDefinitionEntity> definitions = definitionRepository.findAll();

        // 批量查询节点数量，避免 N+1
        Map<String, Long> nodeCountMap = batchGetNodeCounts(definitions);

        return definitions.stream()
                .map(entity -> toResponse(entity, nodeCountMap.getOrDefault(entity.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "workflowEnabledDefinitions", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResp> listEnabled() {
        List<WorkflowDefinitionEntity> definitions = definitionRepository.findByStatusAndDeletedFalseOrderByCreateTimeDesc(1);

        // 批量查询节点数量，避免 N+1
        Map<String, Long> nodeCountMap = batchGetNodeCounts(definitions);

        return definitions.stream()
                .map(entity -> toResponse(entity, nodeCountMap.getOrDefault(entity.getId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * 批量获取节点数量
     */
    private Map<String, Long> batchGetNodeCounts(List<WorkflowDefinitionEntity> definitions) {
        if (definitions.isEmpty()) {
            return Map.of();
        }

        List<String> definitionIds = definitions.stream()
                .map(WorkflowDefinitionEntity::getId)
                .toList();

        return nodeRepository.countByDefinitionIdsIn(definitionIds).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    @CacheEvict(value = "workflowNodes", key = "#definitionId")
    @Transactional
    public WorkflowNodeResp addNode(String definitionId, WorkflowNodeReq req) {
        log.info("添加工作流节点: definitionId={}, nodeName={}", definitionId, req.nodeName());

        // 验证工作流定义存在
        WorkflowDefinitionEntity definition = definitionRepository.findById(definitionId)
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + definitionId));

        WorkflowNodeEntity entity = new WorkflowNodeEntity();
        entity.setDefinitionId(definitionId);
        entity.setNodeName(req.nodeName());
        entity.setNodeCode(req.nodeCode());
        entity.setNodeOrder(req.nodeOrder());
        entity.setApproverType(req.approverType());
        entity.setApproverId(req.approverId());
        entity.setIsCounterSign(req.isCounterSign());
        entity.setAutoPassSameUser(req.autoPassSameUser());
        entity.setDescription(req.description());

        entity = nodeRepository.save(entity);

        log.info("工作流节点添加成功: id={}", entity.getId());
        return toNodeResponse(entity);
    }

    @Override
    @Transactional
    public WorkflowNodeResp updateNode(String nodeId, WorkflowNodeReq req) {
        log.info("更新工作流节点: nodeId={}", nodeId);

        WorkflowNodeEntity entity = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("工作流节点不存在: " + nodeId));

        entity.setNodeName(req.nodeName());
        entity.setNodeCode(req.nodeCode());
        entity.setNodeOrder(req.nodeOrder());
        entity.setApproverType(req.approverType());
        entity.setApproverId(req.approverId());
        entity.setIsCounterSign(req.isCounterSign());
        entity.setAutoPassSameUser(req.autoPassSameUser());
        entity.setDescription(req.description());

        entity = nodeRepository.save(entity);

        log.info("工作流节点更新成功: id={}", nodeId);
        return toNodeResponse(entity);
    }

    @Override
    @Transactional
    public void deleteNode(String nodeId) {
        log.info("删除工作流节点: nodeId={}", nodeId);
        nodeRepository.deleteById(nodeId);
        log.info("工作流节点删除成功: nodeId={}", nodeId);
    }

    @Override
    @Cacheable(value = "workflowNodes", key = "#definitionId", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<WorkflowNodeResp> listNodes(String definitionId) {
        log.info("Service层查询工作流节点: definitionId={}", definitionId);
        List<WorkflowNodeEntity> nodes = nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(definitionId);
        log.info("Service层查询结果: definitionId={}, 原始节点数={}", definitionId, nodes.size());
        return nodes.stream()
                .map(this::toNodeResponse)
                .collect(Collectors.toList());
    }

    private WorkflowDefinitionResp toResponse(WorkflowDefinitionEntity entity, long nodeCount) {
        return new WorkflowDefinitionResp(
                entity.getId(),
                entity.getDefinitionName(),
                entity.getDefinitionKey(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getVersion(),
                entity.getFormConfig(),
                (int) nodeCount,
                entity.getCreateTime(),
                entity.getUpdateTime()
        );
    }

    private WorkflowDefinitionResp toResponse(WorkflowDefinitionEntity entity) {
        return toResponse(entity, nodeRepository.countByDefinitionIdAndDeletedFalse(entity.getId()));
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
