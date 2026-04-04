package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.NotificationSendReq;
import com.adminplus.pojo.dto.resp.NotificationResp;
import com.adminplus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<NotificationResp> sendNotification(@RequestBody NotificationSendReq req) {
        log.info("发送通知: type={}, recipientId={}", req.getType(), req.getRecipientId());
        NotificationResp notification = notificationService.sendNotification(req);
        return ApiResponse.ok(notification);
    }

    /**
     * 批量发送通知
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<Void> sendBatchNotification(
            @RequestParam List<String> recipientIds,
            @RequestBody NotificationSendReq req) {
        log.info("批量发送通知: count={}", recipientIds.size());
        notificationService.sendBatchNotification(recipientIds, req);
        return ApiResponse.ok();
    }

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping
    public ApiResponse<Page<NotificationResp>> getMyNotifications(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 从 SecurityContext 获取当前用户ID
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<NotificationResp> notifications = notificationService.getUserNotifications(userId, status, pageable);
        return ApiResponse.ok(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        long count = notificationService.getUnreadCount(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        notificationService.markAsRead(id, userId);
        return ApiResponse.ok();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Integer> markAllAsRead() {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        int count = notificationService.markAllAsRead(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable String id) {
        String userId = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        notificationService.deleteNotification(id, userId);
        return ApiResponse.ok();
    }
}
