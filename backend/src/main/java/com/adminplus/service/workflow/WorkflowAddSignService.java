package com.adminplus.service.workflow;

import com.adminplus.pojo.dto.request.AddSignRequest;
import com.adminplus.pojo.dto.response.WorkflowAddSignResponse;

import java.util.List;

/**
 * 工作流加签服务
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowAddSignService {

    /**
     * 加签/转办
     */
    WorkflowAddSignResponse addSign(String instanceId, AddSignRequest request);

    /**
     * 获取加签记录
     */
    List<WorkflowAddSignResponse> getAddSignRecords(String instanceId);
}