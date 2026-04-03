package com.adminplus.service;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowApprovalRepository;
import com.adminplus.repository.WorkflowInstanceRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.impl.WorkflowStateMachineServiceImpl;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WorkflowStateMachineService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowStateMachineService Unit Tests")
class WorkflowStateMachineServiceTest {

    @Mock
    private StateMachineFactory<WorkflowState, WorkflowEvent> stateMachineFactory;

    @Mock
    private StateMachinePersister<WorkflowState, WorkflowEvent, String> persister;

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowApprovalRepository approvalRepository;

    @Mock
    private WorkflowNodeRepository nodeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StateMachine<WorkflowState, WorkflowEvent> stateMachine;

    @InjectMocks
    private WorkflowStateMachineServiceImpl stateMachineService;

    private WorkflowInstanceEntity testInstance;
    private WorkflowApprovalEntity testApproval;
    private WorkflowNodeEntity testNode;
    private WorkflowNodeEntity nextNode;
    private String testUserId = "user-001";

    @BeforeEach
    void setUp() {
        // Setup test instance
        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setDefinitionId("def-001");
        testInstance.setUserId("user-001");
        testInstance.setCurrentNodeId("node-001");
        testInstance.setCurrentNodeName("审批节点");
        testInstance.setStatus("running");
        testInstance.setCreateTime(Instant.now());

        // Setup test approval
        testApproval = new WorkflowApprovalEntity();
        testApproval.setId("approval-001");
        testApproval.setInstanceId("inst-001");
        testApproval.setNodeId("node-001");
        testApproval.setApproverId("user-001");
        testApproval.setApprovalStatus("pending");

        // Setup test nodes
        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setNodeName("审批节点");
        testNode.setNodeOrder(1);

        nextNode = new WorkflowNodeEntity();
        nextNode.setId("node-002");
        nextNode.setNodeName("下一审批节点");
        nextNode.setNodeOrder(2);
    }

    private void mockSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(testUserId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("approve Tests")
    class ApproveTests {

        @Test
        @DisplayName("should throw exception when instance not found")
        void approve_WhenInstanceNotFound_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.approve("non-existent",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流实例不存在");
        }

        @Test
        @DisplayName("should throw exception when instance not running")
        void approve_WhenInstanceNotRunning_ShouldThrowException() {
            // Given
            mockSecurityContext();
            testInstance.setStatus("approved");
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));

            // When & Then
            assertThatThrownBy(() -> stateMachineService.approve("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("只有运行中的工作流才能审批");
        }

        @Test
        @DisplayName("should throw exception when user has no approval permission")
        void approve_WhenUserHasNoPermission_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                    any(), any(), any(), any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.approve("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("无权限审批");
        }
    }

    @Nested
    @DisplayName("reject Tests")
    class RejectTests {

        @Test
        @DisplayName("should throw exception when instance not found")
        void reject_WhenInstanceNotFound_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.reject("non-existent",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流实例不存在");
        }

        @Test
        @DisplayName("should throw exception when instance not running")
        void reject_WhenInstanceNotRunning_ShouldThrowException() {
            // Given
            mockSecurityContext();
            testInstance.setStatus("approved");
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));

            // When & Then
            assertThatThrownBy(() -> stateMachineService.reject("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("只有运行中的工作流才能拒绝");
        }

        @Test
        @DisplayName("should throw exception when user has no approval permission")
        void reject_WhenUserHasNoPermission_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                    any(), any(), any(), any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.reject("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("无权限审批");
        }
    }

    @Nested
    @DisplayName("cancel Tests")
    class CancelTests {

        @Test
        @DisplayName("should throw exception when instance not found")
        void cancel_WhenInstanceNotFound_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.cancel("non-existent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流实例不存在");
        }

        @Test
        @DisplayName("should throw exception when instance not cancellable")
        void cancel_WhenInstanceNotCancellable_ShouldThrowException() {
            // Given
            mockSecurityContext();
            testInstance.setStatus("approved");
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));

            // When & Then
            assertThatThrownBy(() -> stateMachineService.cancel("inst-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("该工作流不能取消");
        }

        @Test
        @DisplayName("should throw exception when user is not owner")
        void cancel_WhenUserNotOwner_ShouldThrowException() {
            // Given
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("other-user");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));

            // When & Then
            assertThatThrownBy(() -> stateMachineService.cancel("inst-001"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("只有发起人可以取消工作流");
        }
    }

    @Nested
    @DisplayName("rollback Tests")
    class RollbackTests {

        @Test
        @DisplayName("should throw exception when instance not found")
        void rollback_WhenInstanceNotFound_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.rollback("non-existent",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流实例不存在");
        }

        @Test
        @DisplayName("should throw exception when instance not running")
        void rollback_WhenInstanceNotRunning_ShouldThrowException() {
            // Given
            mockSecurityContext();
            testInstance.setStatus("approved");
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));

            // When & Then
            assertThatThrownBy(() -> stateMachineService.rollback("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("只有运行中的工作流才能退回");
        }

        @Test
        @DisplayName("should throw exception when user has no approval permission")
        void rollback_WhenUserHasNoPermission_ShouldThrowException() {
            // Given
            mockSecurityContext();
            when(instanceRepository.findByIdForUpdate("inst-001")).thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndApproverIdAndApprovalStatusAndDeletedFalse(
                    any(), any(), any(), any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stateMachineService.rollback("inst-001",
                    new ApprovalActionReq("comment", null, null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("无权限审批");
        }
    }
}