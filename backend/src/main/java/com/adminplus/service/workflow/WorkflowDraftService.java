package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowDraftDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;

/**
 * 工作流草稿服务接口
 * <p>
 * 负责工作流草稿的创建、查询、更新和删除
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowDraftService {

    /**
     * 创建工作流草稿
     *
     * @param request 工作流发起请求
     * @return 创建的工作流实例响应
     */
    WorkflowInstanceResponse createDraft(WorkflowStartRequest request);

    /**
     * 获取草稿详情
     *
     * @param instanceId 工作流实例ID
     * @return 草稿详情响应
     */
    WorkflowDraftDetailResponse getDraftDetail(String instanceId);

    /**
     * 更新草稿
     *
     * @param instanceId 工作流实例ID
     * @param request    工作流发起请求
     * @return 更新后的工作流实例响应
     */
    WorkflowInstanceResponse updateDraft(String instanceId, WorkflowStartRequest request);

    /**
     * 删除草稿
     *
     * @param instanceId 工作流实例ID
     */
    void deleteDraft(String instanceId);
}