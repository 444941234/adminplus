package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.query.NotificationQuery;
import com.adminplus.pojo.dto.req.NotificationSendReq;
import com.adminplus.pojo.dto.resp.NotificationResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.NotificationService;
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
@RequestMapping("/v1/notifications")
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
    public ApiResponse<PageResultResp<NotificationResp>> getMyNotifications(NotificationQuery req) {
        String userId = getCurrentUserId();
        PageResultResp<NotificationResp> result = notificationService.getUserNotifications(userId, req);
        return ApiResponse.ok(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        String userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        String userId = getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ApiResponse.ok();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Integer> markAllAsRead() {
        String userId = getCurrentUserId();
        int count = notificationService.markAllAsRead(userId);
        return ApiResponse.ok(count);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable String id) {
        String userId = getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ApiResponse.ok();
    }

    /**
     * 创建测试通知（仅用于开发测试）
     */
    @PostMapping("/test-create")
    @PreAuthorize("hasAuthority('notification:send')")
    public ApiResponse<NotificationResp> createTestNotification() {
        String userId = getCurrentUserId();

        // 创建 3 条测试通知
        String[] types = {"workflow_approve", "workflow_cc", "workflow_urge"};
        String[] titles = {"审批通过通知", "抄送通知", "催办通知"};
        String[] contents = {
            "您的申请《请假申请》已通过审批。",
            "您被抄送了流程《采购审批》。",
            "请及时处理流程《报销申请》。"
        };

        for (int i = 0; i < 3; i++) {
            NotificationSendReq req = new NotificationSendReq();
            req.setType(types[i]);
            req.setRecipientId(userId);
            req.setTitle(titles[i]);
            req.setContent(contents[i]);
            req.setRelatedType("workflow");
            notificationService.sendNotification(req);
        }

        log.info("已创建 3 条测试通知给用户: {}", userId);
        return ApiResponse.ok(null);
    }

    /**
     * 获取当前登录用户的ID
     */
    private String getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        // 从 AppUserDetails 中获取用户ID
        if (authentication.getPrincipal() instanceof com.adminplus.common.security.AppUserDetails userDetails) {
            return userDetails.getId();
        }

        // 降级方案：从 JWT claims 中获取
        if (authentication.getCredentials() instanceof String token) {
            // JWT token 在 credentials 中
            try {
                var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                        .header("alg", "RS256")
                        .claim("sub", authentication.getName())
                        .build();
                String userId = jwt.getClaimAsString("userId");
                if (userId != null) {
                    return userId;
                }
            } catch (Exception ignored) {
            }
        }

        // 最后降级到 username（兼容旧逻辑）
        return authentication.getName();
    }
}
