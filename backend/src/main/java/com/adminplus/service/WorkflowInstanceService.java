package com.adminplus.service;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;

import java.util.List;

/**
 * 工作流实例服务
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
public interface WorkflowInstanceService {

    /**
     * 发起工作流（保存为草稿）
     */
    WorkflowInstanceResp createDraft(WorkflowStartReq req);

    /**
     * 提交工作流
     */
    WorkflowInstanceResp submit(String instanceId);

    /**
     * 发起并提交工作流
     */
    WorkflowInstanceResp start(WorkflowStartReq req);

    /**
     * 查询工作流详情
     */
    WorkflowDetailResp getDetail(String instanceId);

    /**
     * 查询我发起的工作流
     */
    List<WorkflowInstanceResp> getMyWorkflows(String status);

    /**
     * 查询待我审批的工作流
     */
    List<WorkflowInstanceResp> getPendingApprovals();

    /**
     * 统计待审批数量
     */
    long countPendingApprovals();

    /**
     * 同意审批
     */
    WorkflowInstanceResp approve(String instanceId, ApprovalActionReq req);

    /**
     * 拒绝审批
     */
    WorkflowInstanceResp reject(String instanceId, ApprovalActionReq req);

    /**
     * 取消工作流
     */
    void cancel(String instanceId);

    /**
     * 撤回工作流（仅草稿或被拒绝的流程可撤回）
     */
    void withdraw(String instanceId);

    /**
     * 查询审批记录
     */
    List<WorkflowApprovalResp> getApprovals(String instanceId);
}
