package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;

/**
 * 工作流审批服务接口
 * <p>
 * 负责工作流的审批流程处理，包括提交、同意、拒绝、取消、撤回等操作
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowApprovalService {

    /**
     * 提交工作流
     *
     * @param instanceId 工作流实例ID
     * @param request    工作流发起请求（可选，用于更新草稿数据）
     * @return 工作流实例响应
     */
    WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request);

    /**
     * 同意审批
     *
     * @param instanceId 工作流实例ID
     * @param request    审批操作请求
     * @return 工作流实例响应
     */
    WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request);

    /**
     * 拒绝审批
     *
     * @param instanceId 工作流实例ID
     * @param request    审批操作请求
     * @return 工作流实例响应
     */
    WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request);

    /**
     * 取消工作流
     *
     * @param instanceId 工作流实例ID
     */
    void cancel(String instanceId);

    /**
     * 撤回工作流
     *
     * @param instanceId 工作流实例ID
     */
    void withdraw(String instanceId);

    /**
     * 清理目标节点的旧审批记录并重新创建
     * <p>
     * 用于工作流回退时，清除目标节点的审批记录并重新创建待审批记录
     * </p>
     *
     * @param instance   工作流实例
     * @param targetNode 目标节点
     */
    void recreateApprovalsForNode(WorkflowInstanceEntity instance, WorkflowNodeEntity targetNode);
}