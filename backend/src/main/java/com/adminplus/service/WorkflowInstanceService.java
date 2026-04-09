package com.adminplus.service;

import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;
import com.adminplus.pojo.dto.response.WorkflowApprovalResponse;
import com.adminplus.pojo.dto.response.WorkflowDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;

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
    WorkflowInstanceResponse createDraft(WorkflowStartRequest request);

    /**
     * 提交工作流
     */
    WorkflowInstanceResponse submit(String instanceId, WorkflowStartRequest request);

    /**
     * 查询草稿详情
     */
    WorkflowDraftDetailResponse getDraftDetail(String instanceId);

    /**
     * 更新草稿
     */
    WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request);

    /**
     * 删除草稿
     */
    void deleteDraft(String instanceId);

    /**
     * 发起并提交工作流
     */
    WorkflowInstanceResponse start(WorkflowStartRequest request);

    /**
     * 查询工作流详情
     */
    WorkflowDetailResponse getDetail(String instanceId);

    /**
     * 查询我发起的工作流
     */
    List<WorkflowInstanceResponse> getMyWorkflows(String status);

    /**
     * 查询待我审批的工作流
     */
    List<WorkflowInstanceResponse> getPendingApprovals();

    /**
     * 统计待审批数量
     */
    long countPendingApprovals();

    /**
     * 同意审批
     */
    WorkflowInstanceResponse approve(String instanceId, ApprovalActionRequest request);

    /**
     * 拒绝审批
     */
    WorkflowInstanceResponse reject(String instanceId, ApprovalActionRequest request);

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
    List<WorkflowApprovalResponse> getApprovals(String instanceId);

    /**
     * 回退到上一节点
     *
     * @param instanceId 工作流实例ID
     * @param request        审批操作请求（包含回退理由和目标节点ID）
     * @return 更新后的工作流实例
     */
    WorkflowInstanceResponse rollback(String instanceId, ApprovalActionRequest request);

    /**
     * 获取可回退的节点列表
     *
     * @param instanceId 工作流实例ID
     * @return 可回退的节点列表
     */
    List<WorkflowNodeResponse> getRollbackableNodes(String instanceId);

    /**
     * 加签/转办
     *
     * @param instanceId 工作流实例ID
     * @param request        加签操作请求
     * @return 加签记录
     */
    WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request);

    /**
     * 获取工作流实例的加签记录
     *
     * @param instanceId 工作流实例ID
     * @return 加签记录列表
     */
    List<WorkflowAddSignResponse> getAddSignRecords(String instanceId);
}
