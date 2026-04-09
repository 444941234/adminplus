package com.adminplus.service;

import com.adminplus.pojo.dto.response.WorkflowHookLogResponse;

import java.util.List;

/**
 * 工作流钩子日志服务接口
 *
 * @author AdminPlus
 */
public interface WorkflowHookLogService {

    /**
     * 查询工作流实例的所有钩子日志
     *
     * @param instanceId 工作流实例ID
     * @return 钩子日志列表
     */
    List<WorkflowHookLogResponse> listByInstanceId(String instanceId);

    /**
     * 查询工作流实例指定钩子点的日志
     *
     * @param instanceId 工作流实例ID
     * @param hookPoint  钩子点
     * @return 钩子日志列表
     */
    List<WorkflowHookLogResponse> listByInstanceIdAndHookPoint(String instanceId, String hookPoint);

    /**
     * 查询工作流实例指定节点的日志
     *
     * @param instanceId 工作流实例ID
     * @param nodeId     节点ID
     * @return 钩子日志列表
     */
    List<WorkflowHookLogResponse> listByInstanceIdAndNodeId(String instanceId, String nodeId);

    /**
     * 查询钩子配置的所有执行日志
     *
     * @param hookId 钩子配置ID
     * @return 钩子日志列表
     */
    List<WorkflowHookLogResponse> listByHookId(String hookId);

    /**
     * 查询钩子日志详情
     *
     * @param id 日志ID
     * @return 钩子日志
     */
    WorkflowHookLogResponse getById(String id);
}
