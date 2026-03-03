package com.adminplus.service;

import com.adminplus.pojo.dto.req.WorkflowDefinitionReq;
import com.adminplus.pojo.dto.req.WorkflowNodeReq;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;

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
    WorkflowDefinitionResp create(WorkflowDefinitionReq req);

    /**
     * 更新工作流定义
     */
    WorkflowDefinitionResp update(String id, WorkflowDefinitionReq req);

    /**
     * 删除工作流定义
     */
    void delete(String id);

    /**
     * 根据ID查询
     */
    WorkflowDefinitionResp getById(String id);

    /**
     * 查询所有工作流定义
     */
    List<WorkflowDefinitionResp> listAll();

    /**
     * 查询启用的工作流定义
     */
    List<WorkflowDefinitionResp> listEnabled();

    /**
     * 添加工作流节点
     */
    WorkflowNodeResp addNode(String definitionId, WorkflowNodeReq req);

    /**
     * 更新工作流节点
     */
    WorkflowNodeResp updateNode(String nodeId, WorkflowNodeReq req);

    /**
     * 删除工作流节点
     */
    void deleteNode(String nodeId);

    /**
     * 查询工作流的所有节点
     */
    List<WorkflowNodeResp> listNodes(String definitionId);
}
