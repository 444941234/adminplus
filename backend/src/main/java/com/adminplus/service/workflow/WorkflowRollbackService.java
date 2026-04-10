package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;

import java.util.List;

/**
 * 工作流回退服务
 * <p>
 * 负责工作流的回退操作，包括回退到指定节点、获取可回退节点列表等功能
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowRollbackService {

    /**
     * 回退工作流
     * <p>
     * 将工作流从当前节点回退到指定的历史审批节点，支持回退到任意已审批通过的节点
     * </p>
     *
     * @param instanceId 工作流实例ID
     * @param request    审批操作请求，包含目标节点ID和回退备注
     * @return 工作流实例响应
     */
    WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request);

    /**
     * 获取可回退的节点列表
     * <p>
     * 返回当前工作流实例可以回退到的节点列表，仅包含已审批通过且非当前节点的节点
     * </p>
     *
     * @param instanceId 工作流实例ID
     * @return 可回退节点列表
     */
    List<WorkflowNodeResponse> getRollbackableNodes(String instanceId);
}