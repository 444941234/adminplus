package com.adminplus.service;

import com.adminplus.pojo.dto.response.WorkflowCcResponse;

import java.util.List;

/**
 * 工作流抄送服务接口
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public interface WorkflowCcService {

    /**
     * 获取用户的所有抄送记录
     *
     * @param userId 用户ID
     * @return 抄送记录列表
     */
    List<WorkflowCcResponse> getUserCcRecords(String userId);

    /**
     * 获取用户的未读抄送记录
     *
     * @param userId 用户ID
     * @return 未读抄送记录列表
     */
    List<WorkflowCcResponse> getUnreadCcRecords(String userId);

    /**
     * 统计用户未读抄送数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long countUnreadCcRecords(String userId);

    /**
     * 标记抄送记录为已读
     *
     * @param ccId 抄送记录ID
     */
    void markAsRead(String ccId);

    /**
     * 批量标记抄送记录为已读
     *
     * @param ccIds 抄送记录ID列表
     */
    void markAsReadBatch(List<String> ccIds);

    /**
     * 获取工作流实例的所有抄送记录
     *
     * @param instanceId 工作流实例ID
     * @return 抄送记录列表
     */
    List<WorkflowCcResponse> getInstanceCcRecords(String instanceId);
}
