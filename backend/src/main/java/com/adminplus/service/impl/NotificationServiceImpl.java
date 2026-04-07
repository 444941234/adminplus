package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.NotificationSendReq;
import com.adminplus.pojo.dto.resp.NotificationResp;
import com.adminplus.pojo.entity.NotificationEntity;
import com.adminplus.repository.NotificationRepository;
import com.adminplus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务实现
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResp sendNotification(NotificationSendReq req) {
        log.info("发送通知: type={}, recipientId={}", req.getType(), req.getRecipientId());

        NotificationEntity notification = new NotificationEntity();
        notification.setType(req.getType());
        notification.setRecipientId(req.getRecipientId());
        notification.setTitle(req.getTitle());
        notification.setContent(req.getContent());
        notification.setRelatedId(req.getRelatedId());
        notification.setRelatedType(req.getRelatedType());
        notification.setStatus(0); // 0-未读, 1-已读

        notification = notificationRepository.save(notification);

        log.info("通知发送成功: id={}", notification.getId());

        return toResp(notification);
    }

    @Override
    @Transactional
    public void sendBatchNotification(List<String> recipientIds, NotificationSendReq req) {
        log.info("批量发送通知: count={}, type={}", recipientIds.size(), req.getType());

        List<NotificationEntity> notifications = recipientIds.stream()
                .map(recipientId -> {
                    NotificationEntity notification = new NotificationEntity();
                    notification.setType(req.getType());
                    notification.setRecipientId(recipientId);
                    notification.setTitle(req.getTitle());
                    notification.setContent(req.getContent());
                    notification.setRelatedId(req.getRelatedId());
                    notification.setRelatedType(req.getRelatedType());
                    notification.setStatus(0);
                    return notification;
                })
                .toList();

        notificationRepository.saveAll(notifications);

        log.info("批量通知发送成功: count={}", notifications.size());
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId, String userId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此通知");
        }

        notification.setStatus(1);
        notificationRepository.save(notification);

        log.info("通知已标记为已读: id={}", notificationId);
    }

    @Override
    @Transactional
    public int markAllAsRead(String userId) {
        int count = notificationRepository.markAllAsRead(userId);
        log.info("批量标记通知为已读: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    public Page<NotificationResp> getUserNotifications(String userId, Integer status, Pageable pageable) {
        Page<NotificationEntity> page;

        if (status == null) {
            page = notificationRepository.findByRecipientIdOrderByCreateTimeDesc(userId, pageable);
        } else {
            page = notificationRepository.findByRecipientIdAndStatusOrderByCreateTimeDesc(userId, status, pageable);
        }

        return page.map(this::toResp);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndStatus(userId, 0);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

        if (!notification.getRecipientId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此通知");
        }

        notificationRepository.delete(notification);

        log.info("通知已删除: id={}", notificationId);
    }

    private NotificationResp toResp(NotificationEntity entity) {
        NotificationResp resp = new NotificationResp();
        resp.setId(entity.getId());
        resp.setType(entity.getType());
        resp.setRecipientId(entity.getRecipientId());
        resp.setTitle(entity.getTitle());
        resp.setContent(entity.getContent());
        resp.setRelatedId(entity.getRelatedId());
        resp.setRelatedType(entity.getRelatedType());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime().toString());
        resp.setUpdateTime(entity.getUpdateTime().toString());
        return resp;
    }
}
