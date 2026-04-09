package com.adminplus.service;

import com.adminplus.pojo.entity.WorkflowCcEntity;
import com.adminplus.pojo.dto.response.WorkflowCcResponse;
import com.adminplus.repository.WorkflowCcRepository;
import com.adminplus.service.impl.WorkflowCcServiceImpl;
import com.adminplus.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WorkflowCcService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowCcService Unit Tests")
class WorkflowCcServiceTest {

    @Mock
    private WorkflowCcRepository ccRepository;

    @InjectMocks
    private WorkflowCcServiceImpl ccService;

    private WorkflowCcEntity testCc;
    private String testUserId = "user-001";

    @BeforeEach
    void setUp() {
        testCc = new WorkflowCcEntity();
        testCc.setId("cc-001");
        testCc.setInstanceId("inst-001");
        testCc.setNodeId("node-001");
        testCc.setNodeName("审批节点");
        testCc.setUserId(testUserId);
        testCc.setUserName("测试用户");
        testCc.setCcType("approve");
        testCc.setCcContent("审批通过通知");
        testCc.setIsRead(false);
        testCc.setCreateTime(Instant.now());
    }

    @Nested
    @DisplayName("getUserCcRecords Tests")
    class GetUserCcRecordsTests {

        @Test
        @DisplayName("should return cc records for user")
        void getUserCcRecords_ShouldReturnRecords() {
            // Given
            when(ccRepository.findByUserId(testUserId)).thenReturn(List.of(testCc));

            // When
            List<WorkflowCcResponse> result = ccService.getUserCcRecords(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).userId()).isEqualTo(testUserId);
            assertThat(result.get(0).nodeName()).isEqualTo("审批节点");
        }

        @Test
        @DisplayName("should return empty list when no records")
        void getUserCcRecords_WhenNoRecords_ShouldReturnEmptyList() {
            // Given
            when(ccRepository.findByUserId(testUserId)).thenReturn(List.of());

            // When
            List<WorkflowCcResponse> result = ccService.getUserCcRecords(testUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUnreadCcRecords Tests")
    class GetUnreadCcRecordsTests {

        @Test
        @DisplayName("should return unread records")
        void getUnreadCcRecords_ShouldReturnUnreadRecords() {
            // Given
            when(ccRepository.findUnreadByUserId(testUserId)).thenReturn(List.of(testCc));

            // When
            List<WorkflowCcResponse> result = ccService.getUnreadCcRecords(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isRead()).isFalse();
        }

        @Test
        @DisplayName("should return empty list when no unread records")
        void getUnreadCcRecords_WhenNoneUnread_ShouldReturnEmptyList() {
            // Given
            when(ccRepository.findUnreadByUserId(testUserId)).thenReturn(List.of());

            // When
            List<WorkflowCcResponse> result = ccService.getUnreadCcRecords(testUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countUnreadCcRecords Tests")
    class CountUnreadCcRecordsTests {

        @Test
        @DisplayName("should return unread count")
        void countUnreadCcRecords_ShouldReturnCount() {
            // Given
            when(ccRepository.countUnreadByUserId(testUserId)).thenReturn(5L);

            // When
            long result = ccService.countUnreadCcRecords(testUserId);

            // Then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("should return zero when no unread")
        void countUnreadCcRecords_WhenNone_ShouldReturnZero() {
            // Given
            when(ccRepository.countUnreadByUserId(testUserId)).thenReturn(0L);

            // When
            long result = ccService.countUnreadCcRecords(testUserId);

            // Then
            assertThat(result).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getInstanceCcRecords Tests")
    class GetInstanceCcRecordsTests {

        @Test
        @DisplayName("should return cc records for instance")
        void getInstanceCcRecords_ShouldReturnRecords() {
            // Given
            when(ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(List.of(testCc));

            // When
            List<WorkflowCcResponse> result = ccService.getInstanceCcRecords("inst-001");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).instanceId()).isEqualTo("inst-001");
        }

        @Test
        @DisplayName("should return empty list when no records")
        void getInstanceCcRecords_WhenNoRecords_ShouldReturnEmptyList() {
            // Given
            when(ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(List.of());

            // When
            List<WorkflowCcResponse> result = ccService.getInstanceCcRecords("inst-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark cc as read successfully")
        void markAsRead_ShouldMarkAsRead() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
                when(ccRepository.findById("cc-001")).thenReturn(Optional.of(testCc));
                when(ccRepository.save(any())).thenReturn(testCc);

                // When
                ccService.markAsRead("cc-001");

                // Then
                assertThat(testCc.getIsRead()).isTrue();
                assertThat(testCc.getReadTime()).isNotNull();
                verify(ccRepository).save(any(WorkflowCcEntity.class));
            }
        }

        @Test
        @DisplayName("should throw exception when cc not found")
        void markAsRead_WhenNotFound_ShouldThrowException() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
                when(ccRepository.findById("non-existent")).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> ccService.markAsRead("non-existent"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("抄送记录不存在");
            }
        }

        @Test
        @DisplayName("should throw exception when user not owner")
        void markAsRead_WhenNotOwner_ShouldThrowException() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("other-user");
                when(ccRepository.findById("cc-001")).thenReturn(Optional.of(testCc));

                // When & Then
                assertThatThrownBy(() -> ccService.markAsRead("cc-001"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("无权限");
            }
        }
    }

    @Nested
    @DisplayName("markAsReadBatch Tests")
    class MarkAsReadBatchTests {

        @Test
        @DisplayName("should mark batch as read successfully")
        void markAsReadBatch_ShouldMarkAsRead() {
            // Given
            WorkflowCcEntity cc1 = new WorkflowCcEntity();
            cc1.setId("cc-001");
            cc1.setUserId(testUserId);
            cc1.setIsRead(false);

            WorkflowCcEntity cc2 = new WorkflowCcEntity();
            cc2.setId("cc-002");
            cc2.setUserId(testUserId);
            cc2.setIsRead(false);

            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
                when(ccRepository.findAllById(List.of("cc-001", "cc-002")))
                        .thenReturn(List.of(cc1, cc2));
                when(ccRepository.saveAll(any())).thenReturn(List.of(cc1, cc2));

                // When
                ccService.markAsReadBatch(List.of("cc-001", "cc-002"));

                // Then
                assertThat(cc1.getIsRead()).isTrue();
                assertThat(cc2.getIsRead()).isTrue();
                verify(ccRepository).saveAll(any());
            }
        }

        @Test
        @DisplayName("should only mark records belonging to user")
        void markAsReadBatch_ShouldOnlyMarkUserRecords() {
            // Given
            WorkflowCcEntity userCc = new WorkflowCcEntity();
            userCc.setId("cc-001");
            userCc.setUserId(testUserId);
            userCc.setIsRead(false);

            WorkflowCcEntity otherCc = new WorkflowCcEntity();
            otherCc.setId("cc-002");
            otherCc.setUserId("other-user");
            otherCc.setIsRead(false);

            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
                when(ccRepository.findAllById(List.of("cc-001", "cc-002")))
                        .thenReturn(List.of(userCc, otherCc));
                when(ccRepository.saveAll(any())).thenReturn(List.of(userCc));

                // When
                ccService.markAsReadBatch(List.of("cc-001", "cc-002"));

                // Then
                assertThat(userCc.getIsRead()).isTrue();
                assertThat(otherCc.getIsRead()).isFalse(); // Should not be marked
                verify(ccRepository).saveAll(List.of(userCc));
            }
        }
    }
}