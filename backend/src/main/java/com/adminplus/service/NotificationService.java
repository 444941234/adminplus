package com.adminplus.service;

import com.adminplus.pojo.dto.query.NotificationQuery;
import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.pojo.dto.response.NotificationResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public interface NotificationService {

    /**
     * 发送通知
     *
     * @param request 通知发送请求
     * @return 通知响应
     */
    NotificationResponse sendNotification(NotificationSendRequest request);

    /**
     * 批量发送通知
     *
     * @param recipientIds 接收人ID列表
     * @param request 通知发送请求模板
     */
    void sendBatchNotification(List<String> recipientIds, NotificationSendRequest request);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(String notificationId, String userId);

    /**
     * 批量标记通知为已读
     *
     * @param userId 用户ID
     * @return 更新的数量
     */
    int markAllAsRead(String userId);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param query 查询条件
     * @return 通知列表
     */
    PageResultResponse<NotificationResponse> getUserNotifications(String userId, NotificationQuery query);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long getUnreadCount(String userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void deleteNotification(String notificationId, String userId);
}
