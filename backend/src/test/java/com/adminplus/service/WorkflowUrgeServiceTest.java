package com.adminplus.service;

import com.adminplus.pojo.dto.response.WorkflowUrgeResponse;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.impl.WorkflowUrgeServiceImpl;
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
 * WorkflowUrgeService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowUrgeService Unit Tests")
class WorkflowUrgeServiceTest {

    @Mock
    private WorkflowUrgeRepository urgeRepository;

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowApprovalRepository approvalRepository;

    @Mock
    private WorkflowNodeRepository nodeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkflowUrgeServiceImpl urgeService;

    private WorkflowUrgeEntity testUrge;
    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode;
    private UserEntity testUser;
    private String testUserId = "user-001";

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(testUserId);
        testUser.setNickname("测试用户");

        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("审批节点");

        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setUserId(testUserId);
        testInstance.setCurrentNodeId("node-001");
        testInstance.setStatus("running");

        testUrge = new WorkflowUrgeEntity();
        testUrge.setId("urge-001");
        testUrge.setInstanceId("inst-001");
        testUrge.setNodeId("node-001");
        testUrge.setNodeName("审批节点");
        testUrge.setUrgeUserId(testUserId);
        testUrge.setUrgeUserName("测试用户");
        testUrge.setUrgeTargetId("approver-001");
        testUrge.setUrgeTargetName("审批人");
        testUrge.setUrgeContent("请尽快审批");
        testUrge.setIsRead(false);
        testUrge.setCreateTime(Instant.now());
    }

    @Nested
    @DisplayName("getReceivedUrgeRecords Tests")
    class GetReceivedUrgeRecordsTests {

        @Test
        @DisplayName("should return received urge records")
        void getReceivedUrgeRecords_ShouldReturnRecords() {
            // Given
            when(urgeRepository.findByUrgeTargetId(testUserId)).thenReturn(List.of(testUrge));

            // When
            List<WorkflowUrgeResponse> result = urgeService.getReceivedUrgeRecords(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).urgeTargetId()).isEqualTo("approver-001");
        }

        @Test
        @DisplayName("should return empty list when no records")
        void getReceivedUrgeRecords_WhenNoRecords_ShouldReturnEmptyList() {
            // Given
            when(urgeRepository.findByUrgeTargetId(testUserId)).thenReturn(List.of());

            // When
            List<WorkflowUrgeResponse> result = urgeService.getReceivedUrgeRecords(testUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSentUrgeRecords Tests")
    class GetSentUrgeRecordsTests {

        @Test
        @DisplayName("should return sent urge records")
        void getSentUrgeRecords_ShouldReturnRecords() {
            // Given
            when(urgeRepository.findByUrgeUserId(testUserId)).thenReturn(List.of(testUrge));

            // When
            List<WorkflowUrgeResponse> result = urgeService.getSentUrgeRecords(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).urgeUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("should return empty list when no records")
        void getSentUrgeRecords_WhenNoRecords_ShouldReturnEmptyList() {
            // Given
            when(urgeRepository.findByUrgeUserId(testUserId)).thenReturn(List.of());

            // When
            List<WorkflowUrgeResponse> result = urgeService.getSentUrgeRecords(testUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUnreadUrgeRecords Tests")
    class GetUnreadUrgeRecordsTests {

        @Test
        @DisplayName("should return unread records")
        void getUnreadUrgeRecords_ShouldReturnUnreadRecords() {
            // Given
            when(urgeRepository.findUnreadByUrgeTargetId(testUserId)).thenReturn(List.of(testUrge));

            // When
            List<WorkflowUrgeResponse> result = urgeService.getUnreadUrgeRecords(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("countUnreadUrgeRecords Tests")
    class CountUnreadUrgeRecordsTests {

        @Test
        @DisplayName("should return unread count")
        void countUnreadUrgeRecords_ShouldReturnCount() {
            // Given
            when(urgeRepository.countUnreadByUrgeTargetId(testUserId)).thenReturn(3L);

            // When
            long result = urgeService.countUnreadUrgeRecords(testUserId);

            // Then
            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getInstanceUrgeRecords Tests")
    class GetInstanceUrgeRecordsTests {

        @Test
        @DisplayName("should return urge records for instance")
        void getInstanceUrgeRecords_ShouldReturnRecords() {
            // Given
            when(urgeRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc("inst-001"))
                    .thenReturn(List.of(testUrge));

            // When
            List<WorkflowUrgeResponse> result = urgeService.getInstanceUrgeRecords("inst-001");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).instanceId()).isEqualTo("inst-001");
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark urge as read successfully")
        void markAsRead_ShouldMarkAsRead() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("approver-001");
                testUrge.setUrgeTargetId("approver-001");
                when(urgeRepository.findById("urge-001")).thenReturn(Optional.of(testUrge));
                when(urgeRepository.save(any())).thenReturn(testUrge);

                // When
                urgeService.markAsRead("urge-001");

                // Then
                assertThat(testUrge.getIsRead()).isTrue();
                assertThat(testUrge.getReadTime()).isNotNull();
                verify(urgeRepository).save(any(WorkflowUrgeEntity.class));
            }
        }

        @Test
        @DisplayName("should throw exception when urge not found")
        void markAsRead_WhenNotFound_ShouldThrowException() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
                when(urgeRepository.findById("non-existent")).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> urgeService.markAsRead("non-existent"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("催办记录不存在");
            }
        }

        @Test
        @DisplayName("should throw exception when user not target")
        void markAsRead_WhenNotTarget_ShouldThrowException() {
            // Given
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("other-user");
                when(urgeRepository.findById("urge-001")).thenReturn(Optional.of(testUrge));

                // When & Then
                assertThatThrownBy(() -> urgeService.markAsRead("urge-001"))
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
            WorkflowUrgeEntity urge1 = new WorkflowUrgeEntity();
            urge1.setId("urge-001");
            urge1.setUrgeTargetId("approver-001");
            urge1.setIsRead(false);

            WorkflowUrgeEntity urge2 = new WorkflowUrgeEntity();
            urge2.setId("urge-002");
            urge2.setUrgeTargetId("approver-001");
            urge2.setIsRead(false);

            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("approver-001");
                when(urgeRepository.findAllById(List.of("urge-001", "urge-002")))
                        .thenReturn(List.of(urge1, urge2));
                when(urgeRepository.saveAll(any())).thenReturn(List.of(urge1, urge2));

                // When
                urgeService.markAsReadBatch(List.of("urge-001", "urge-002"));

                // Then
                assertThat(urge1.getIsRead()).isTrue();
                assertThat(urge2.getIsRead()).isTrue();
                verify(urgeRepository).saveAll(any());
            }
        }

        @Test
        @DisplayName("should only mark records targeting user")
        void markAsReadBatch_ShouldOnlyMarkUserRecords() {
            // Given
            WorkflowUrgeEntity userUrge = new WorkflowUrgeEntity();
            userUrge.setId("urge-001");
            userUrge.setUrgeTargetId("approver-001");
            userUrge.setIsRead(false);

            WorkflowUrgeEntity otherUrge = new WorkflowUrgeEntity();
            otherUrge.setId("urge-002");
            otherUrge.setUrgeTargetId("other-approver");
            otherUrge.setIsRead(false);

            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("approver-001");
                when(urgeRepository.findAllById(List.of("urge-001", "urge-002")))
                        .thenReturn(List.of(userUrge, otherUrge));
                when(urgeRepository.saveAll(any())).thenReturn(List.of(userUrge));

                // When
                urgeService.markAsReadBatch(List.of("urge-001", "urge-002"));

                // Then
                assertThat(userUrge.getIsRead()).isTrue();
                assertThat(otherUrge.getIsRead()).isFalse();
                verify(urgeRepository).saveAll(List.of(userUrge));
            }
        }
    }
}