package com.adminplus.service;

import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;

import java.util.List;

/**
 * 工作流节点钩子配置服务接口
 *
 * @author AdminPlus
 */
public interface WorkflowNodeHookService {

    /**
     * 创建钩子配置
     *
     * @param request 钩子配置请求
     * @return 钩子配置响应
     */
    WorkflowNodeHookResponse create(WorkflowNodeHookRequest request);

    /**
     * 更新钩子配置
     *
     * @param id  钩子配置ID
     * @param request 钩子配置请求
     * @return 钩子配置响应
     */
    WorkflowNodeHookResponse update(String id, WorkflowNodeHookRequest request);

    /**
     * 删除钩子配置
     *
     * @param id 钩子配置ID
     */
    void delete(String id);

    /**
     * 查询钩子配置详情
     *
     * @param id 钩子配置ID
     * @return 钩子配置响应
     */
    WorkflowNodeHookResponse getById(String id);

    /**
     * 查询节点的所有钩子配置
     *
     * @param nodeId 节点ID
     * @return 钩子配置列表
     */
    List<WorkflowNodeHookResponse> listByNodeId(String nodeId);

    /**
     * 查询节点指定钩子点的配置
     *
     * @param nodeId    节点ID
     * @param hookPoint 钩子点
     * @return 钩子配置列表
     */
    List<WorkflowNodeHookResponse> listByNodeIdAndHookPoint(String nodeId, String hookPoint);
}
