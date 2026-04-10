package com.adminplus.service.impl;

import com.adminplus.pojo.dto.query.NotificationQuery;
import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.pojo.dto.response.NotificationResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.NotificationEntity;
import com.adminplus.repository.NotificationRepository;
import com.adminplus.service.NotificationService;
import com.adminplus.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService conversionService;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationSendRequest request) {
        NotificationEntity notification = new NotificationEntity();
        notification.setType(request.getType());
        notification.setRecipientId(request.getRecipientId());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setRelatedId(request.getRelatedId());
        notification.setRelatedType(request.getRelatedType());
        notification.setStatus(0); // 0-未读, 1-已读

        notification = notificationRepository.save(notification);

        return conversionService.convert(notification, NotificationResponse.class);
    }

    @Override
    @Transactional
    public void sendBatchNotification(List<String> recipientIds, NotificationSendRequest request) {
        List<NotificationEntity> notifications = recipientIds.stream()
                .map(recipientId -> {
                    NotificationEntity notification = new NotificationEntity();
                    notification.setType(request.getType());
                    notification.setRecipientId(recipientId);
                    notification.setTitle(request.getTitle());
                    notification.setContent(request.getContent());
                    notification.setRelatedId(request.getRelatedId());
                    notification.setRelatedType(request.getRelatedType());
                    notification.setStatus(0);
                    return notification;
                })
                .toList();

        notificationRepository.saveAll(notifications);
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
    public PageResultResponse<NotificationResponse> getUserNotifications(String userId, NotificationQuery query) {
        Pageable pageable = PageUtils.toPageableDesc(query.getPage(), query.getSize(), "createTime");

        Page<NotificationEntity> pageResult;
        if (query.getStatus() == null) {
            pageResult = notificationRepository.findByRecipientIdOrderByCreateTimeDesc(userId, pageable);
        } else {
            pageResult = notificationRepository.findByRecipientIdAndStatusOrderByCreateTimeDesc(userId, query.getStatus(), pageable);
        }

        return PageResultResponse.from(pageResult, e -> conversionService.convert(e, NotificationResponse.class));
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
}
