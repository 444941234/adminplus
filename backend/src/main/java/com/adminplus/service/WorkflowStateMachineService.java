package com.adminplus.service;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;

/**
 * 工作流状态机服务接口
 * <p>
 * 提供基于Spring State Machine的工作流状态管理功能
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public interface WorkflowStateMachineService {

    /**
     * 同意审批
     *
     * @param instanceId 工作流实例ID
     * @param req        审批操作请求
     * @return 更新后的工作流实例
     */
    WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest req);

    /**
     * 拒绝审批
     *
     * @param instanceId 工作流实例ID
     * @param req        审批操作请求
     * @return 更新后的工作流实例
     */
    WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest req);

    /**
     * 取消工作流
     *
     * @param instanceId 工作流实例ID
     * @return 更新后的工作流实例
     */
    WorkflowInstanceResponse cancel(String instanceId);

    /**
     * 退回到上一节点
     *
     * @param instanceId 工作流实例ID
     * @param req        审批操作请求（包含退回理由）
     * @return 更新后的工作流实例
     */
    WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest req);
}
