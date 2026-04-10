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
     *
     * @param req 工作流定义请求
     * @return 创建的工作流定义信息
     * @throws BizException 当工作流编码已存在时抛出
     */
    WorkflowDefinitionResponse create(WorkflowDefinitionRequest req);

    /**
     * 更新工作流定义
     *
     * @param id  工作流定义ID
     * @param req 工作流定义请求
     * @return 更新后的工作流定义信息
     * @throws BizException 当工作流定义不存在时抛出
     */
    WorkflowDefinitionResponse update(String id, WorkflowDefinitionRequest req);

    /**
     * 删除工作流定义
     *
     * @param id 工作流定义ID
     * @throws BizException 当工作流定义不存在或存在进行中的实例时抛出
     */
    void delete(String id);

    /**
     * 根据ID查询
     *
     * @param id 工作流定义ID
     * @return 工作流定义信息
     * @throws BizException 当工作流定义不存在时抛出
     */
    WorkflowDefinitionResponse getById(String id);

    /**
     * 查询所有工作流定义
     *
     * @return 所有工作流定义列表
     */
    List<WorkflowDefinitionResponse> listAll();

    /**
     * 查询启用的工作流定义
     *
     * @return 启用状态的工作流定义列表
     */
    List<WorkflowDefinitionResponse> listEnabled();

    /**
     * 添加工作流节点
     *
     * @param definitionId 工作流定义ID
     * @param req          工作流节点请求
     * @return 创建的节点信息
     * @throws BizException 当工作流定义不存在时抛出
     */
    WorkflowNodeResponse addNode(String definitionId, WorkflowNodeRequest req);

    /**
     * 更新工作流节点
     *
     * @param nodeId 工作流节点ID
     * @param req    工作流节点请求
     * @return 更新后的节点信息
     * @throws BizException 当节点不存在时抛出
     */
    WorkflowNodeResponse updateNode(String nodeId, WorkflowNodeRequest req);

    /**
     * 删除工作流节点
     *
     * @param nodeId 工作流节点ID
     * @throws BizException 当节点不存在时抛出
     */
    void deleteNode(String nodeId);

    /**
     * 查询工作流的所有节点
     *
     * @param definitionId 工作流定义ID
     * @return 节点列表
     */
    List<WorkflowNodeResponse> listNodes(String definitionId);
}
