package com.adminplus.service;

import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.dto.request.WorkflowNodeRequest;
import com.adminplus.pojo.dto.response.WorkflowDefinitionResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;

import java.util.List;

/**
 * 工作流定义服务
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public interface WorkflowDefinitionService {

    /**
     * 创建工作流定义
     */
    WorkflowDefinitionResponse create(WorkflowDefinitionRequest req);

    /**
     * 更新工作流定义
     */
    WorkflowDefinitionResponse update(String id, WorkflowDefinitionRequest req);

    /**
     * 删除工作流定义
     */
    void delete(String id);

    /**
     * 根据ID查询
     */
    WorkflowDefinitionResponse getById(String id);

    /**
     * 查询所有工作流定义
     */
    List<WorkflowDefinitionResponse> listAll();

    /**
     * 查询启用的工作流定义
     */
    List<WorkflowDefinitionResponse> listEnabled();

    /**
     * 添加工作流节点
     */
    WorkflowNodeResponse addNode(String definitionId, WorkflowNodeRequest req);

    /**
     * 更新工作流节点
     */
    WorkflowNodeResponse updateNode(String nodeId, WorkflowNodeRequest req);

    /**
     * 删除工作流节点
     */
    void deleteNode(String nodeId);

    /**
     * 查询工作流的所有节点
     */
    List<WorkflowNodeResponse> listNodes(String definitionId);
}
