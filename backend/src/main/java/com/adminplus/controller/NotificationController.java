package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.enums.OperationType;
import com.adminplus.pojo.dto.query.NotificationQuery;
import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.pojo.dto.response.NotificationResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.NotificationService;
import com.adminplus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Slf4j
@RestController
@RequestMapping(value = "/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping
    @OperationLog(module = "通知管理", type = OperationType.CREATE, description = "发送通知 {#request.type}")
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<NotificationResponse> sendNotification(@RequestBody NotificationSendRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ApiResponse.ok(response);
    }

    /**
     * 批量发送通知
     */
    @PostMapping("/batch")
    @OperationLog(module = "通知管理", type = OperationType.CREATE, description = "批量发送通知 {#recipientIds.size}人")
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<Void> sendBatchNotification(
            @RequestParam List<String> recipientIds,
            @RequestBody NotificationSendRequest request) {
        notificationService.sendBatchNotification(recipientIds, request);
        return ApiResponse.ok();
    }

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping
    public ApiResponse<PageResultResponse<NotificationResponse>> getMyNotifications(NotificationQuery query) {
        String userId = SecurityUtils.getCurrentUserId();
        PageResultResponse<NotificationResponse> result = notificationService.getUserNotifications(userId, query);
        return ApiResponse.ok(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ApiResponse.ok();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Integer> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        int count = notificationService.markAllAsRead(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "通知管理", type = OperationType.DELETE, description = "删除通知 {#id}")
    public ApiResponse<Void> deleteNotification(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ApiResponse.ok();
    }

    /**
     * 创建测试通知（仅用于开发测试）
     */
    @PostMapping("/test-create")
    @OperationLog(module = "通知管理", type = OperationType.CREATE, description = "创建测试通知")
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<NotificationResponse> createTestNotification() {
        String userId = SecurityUtils.getCurrentUserId();

        // 创建 3 条测试通知
        String[] types = {"workflow_approve", "workflow_cc", "workflow_urge"};
        String[] titles = {"审批通过通知", "抄送通知", "催办通知"};
        String[] contents = {
            "您的申请《请假申请》已通过审批。",
            "您被抄送了流程《采购审批》。",
            "请及时处理流程《报销申请》。"
        };

        for (int i = 0; i < 3; i++) {
            NotificationSendRequest req = new NotificationSendRequest();
            req.setType(types[i]);
            req.setRecipientId(userId);
            req.setTitle(titles[i]);
            req.setContent(contents[i]);
            req.setRelatedType("workflow");
            notificationService.sendNotification(req);
        }

        return ApiResponse.ok(null);
    }
}
