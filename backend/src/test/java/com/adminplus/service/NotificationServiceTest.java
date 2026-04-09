package com.adminplus.service;

import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.pojo.entity.NotificationEntity;
import com.adminplus.repository.NotificationRepository;
import com.adminplus.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationService Tests
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationSendRequest notificationReq;

    @BeforeEach
    void setUp() {
        notificationReq = new NotificationSendRequest();
        notificationReq.setType("workflow_approve");
        notificationReq.setRecipientId("user-001");
        notificationReq.setTitle("测试通知");
        notificationReq.setContent("这是一条测试通知");
    }

    @Test
    void testSendNotification() {
        // Given
        NotificationEntity savedEntity = new NotificationEntity();
        savedEntity.setId("notif-001");
        savedEntity.setType("workflow_approve");
        savedEntity.setRecipientId("user-001");
        savedEntity.setTitle("测试通知");
        savedEntity.setContent("这是一条测试通知");
        savedEntity.setStatus(0);
        savedEntity.setCreateTime(Instant.now());
        savedEntity.setUpdateTime(Instant.now());

        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(savedEntity);

        // When
        var result = notificationService.sendNotification(notificationReq);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("notif-001");
        assertThat(result.getType()).isEqualTo("workflow_approve");
        assertThat(result.getRecipientId()).isEqualTo("user-001");
        assertThat(result.getTitle()).isEqualTo("测试通知");

        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void testSendBatchNotification() {
        // Given
        List<String> recipientIds = List.of("user-001", "user-002", "user-003");

        when(notificationRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        notificationService.sendBatchNotification(recipientIds, notificationReq);

        // Then
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testMarkAsRead() {
        // Given
        NotificationEntity notification = new NotificationEntity();
        notification.setId("notif-001");
        notification.setRecipientId("user-001");
        notification.setStatus(0);

        when(notificationRepository.findById("notif-001")).thenReturn(java.util.Optional.of(notification));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(notification);

        // When
        notificationService.markAsRead("notif-001", "user-001");

        // Then
        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
        assertThat(notification.getStatus()).isEqualTo(1);
    }

    @Test
    void testMarkAsRead_WrongUser_ShouldThrow() {
        // Given
        NotificationEntity notification = new NotificationEntity();
        notification.setId("notif-001");
        notification.setRecipientId("user-001");

        when(notificationRepository.findById("notif-001")).thenReturn(java.util.Optional.of(notification));

        // When & Then
        try {
            notificationService.markAsRead("notif-001", "user-002");
            org.junit.jupiter.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无权操作");
        }

        verify(notificationRepository, never()).save(any(NotificationEntity.class));
    }

    @Test
    void testMarkAllAsRead() {
        // Given
        when(notificationRepository.markAllAsRead("user-001")).thenReturn(5);

        // When
        int count = notificationService.markAllAsRead("user-001");

        // Then
        assertThat(count).isEqualTo(5);
        verify(notificationRepository, times(1)).markAllAsRead("user-001");
    }

    @Test
    void testGetUnreadCount() {
        // Given
        when(notificationRepository.countByRecipientIdAndStatus("user-001", 0)).thenReturn(3L);

        // When
        long count = notificationService.getUnreadCount("user-001");

        // Then
        assertThat(count).isEqualTo(3L);
        verify(notificationRepository, times(1)).countByRecipientIdAndStatus("user-001", 0);
    }

    @Test
    void testDeleteNotification() {
        // Given
        NotificationEntity notification = new NotificationEntity();
        notification.setId("notif-001");
        notification.setRecipientId("user-001");

        when(notificationRepository.findById("notif-001")).thenReturn(java.util.Optional.of(notification));
        doNothing().when(notificationRepository).delete(any(NotificationEntity.class));

        // When
        notificationService.deleteNotification("notif-001", "user-001");

        // Then
        verify(notificationRepository, times(1)).delete(any(NotificationEntity.class));
    }

    @Test
    void testDeleteNotification_WrongUser_ShouldThrow() {
        // Given
        NotificationEntity notification = new NotificationEntity();
        notification.setId("notif-001");
        notification.setRecipientId("user-001");

        when(notificationRepository.findById("notif-001")).thenReturn(java.util.Optional.of(notification));

        // When & Then
        try {
            notificationService.deleteNotification("notif-001", "user-002");
            org.junit.jupiter.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无权操作");
        }

        verify(notificationRepository, never()).delete(any(NotificationEntity.class));
    }
}
