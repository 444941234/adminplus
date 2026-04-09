package com.adminplus.service;

import com.adminplus.pojo.dto.request.UrgeActionRequest;
import com.adminplus.pojo.dto.response.WorkflowUrgeResponse;

import java.util.List;

/**
 * 工作流催办服务接口
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
public interface WorkflowUrgeService {

    /**
     * 催办工作流
     *
     * @param instanceId 工作流实例ID
     * @param req        催办操作请求
     */
    void urgeWorkflow(String instanceId, UrgeActionRequest req);

    /**
     * 获取用户收到的催办记录
     *
     * @param userId 用户ID
     * @return 催办记录列表
     */
    List<WorkflowUrgeResponse> getReceivedUrgeRecords(String userId);

    /**
     * 获取用户发送的催办记录
     *
     * @param userId 用户ID
     * @return 催办记录列表
     */
    List<WorkflowUrgeResponse> getSentUrgeRecords(String userId);

    /**
     * 获取用户的未读催办记录
     *
     * @param userId 用户ID
     * @return 未读催办记录列表
     */
    List<WorkflowUrgeResponse> getUnreadUrgeRecords(String userId);

    /**
     * 统计用户未读催办数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long countUnreadUrgeRecords(String userId);

    /**
     * 标记催办记录为已读
     *
     * @param urgeId 催办记录ID
     */
    void markAsRead(String urgeId);

    /**
     * 批量标记催办记录为已读
     *
     * @param urgeIds 催办记录ID列表
     */
    void markAsReadBatch(List<String> urgeIds);

    /**
     * 获取工作流实例的催办记录
     *
     * @param instanceId 工作流实例ID
     * @return 催办记录列表
     */
    List<WorkflowUrgeResponse> getInstanceUrgeRecords(String instanceId);
}