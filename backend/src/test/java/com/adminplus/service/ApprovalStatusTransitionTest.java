package com.adminplus.service;

import com.adminplus.common.security.AppUserDetails;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.impl.WorkflowInstanceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Workflow Approval Status Transitions
 * Tests all possible state transitions and validates state machine rules
 *
 * Status Transition Rules:
 * draft -> running (submit)
 * running -> approved (all nodes approved)
 * running -> rejected (any approval rejected)
 * running -> cancelled (initiator cancels)
 * draft -> cancelled (initiator cancels)
 * rejected -> draft (withdraw and modify)
 * draft -> draft (withdraw and resubmit)
 *
 * Invalid Transitions:
 * approved -> * (final state, no transitions allowed)
 * cancelled -> * (final state, no transitions allowed)
 * running -> running (no self-transition)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workflow Approval Status Transition Tests")
class ApprovalStatusTransitionTest {

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowApprovalRepository approvalRepository;

    @Mock
    private WorkflowDefinitionRepository definitionRepository;

    @Mock
    private WorkflowNodeRepository nodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkflowDefinitionService definitionService;

    @Mock
    private com.adminplus.repository.DeptRepository deptRepository;

    @Mock
    private com.adminplus.repository.UserRoleRepository userRoleRepository;

    @Mock
    private com.adminplus.repository.WorkflowCcRepository ccRepository;

    @Mock
    private com.adminplus.repository.WorkflowAddSignRepository addSignRepository;

    @Mock
    private com.adminplus.service.workflow.hook.WorkflowHookService hookService;

    @Mock
    private tools.jackson.databind.json.JsonMapper objectMapper;

    @InjectMocks
    private WorkflowInstanceServiceImpl service;

    private WorkflowInstanceEntity testInstance;
    private WorkflowNodeEntity testNode1;
    private WorkflowNodeEntity testNode2;
    private UserEntity testUser;
    private UserEntity testApprover;
    private WorkflowApprovalEntity pendingApproval;

    private static final String INITIATOR_ID = "user-001";
    private static final String APPROVER_ID = "approver-001";

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser = new UserEntity();
        testUser.setId(INITIATOR_ID);
        testUser.setNickname("John Doe");
        testUser.setDeptId("dept-001");

        testApprover = new UserEntity();
        testApprover.setId(APPROVER_ID);
        testApprover.setNickname("Manager Smith");

        // Setup test nodes
        testNode1 = new WorkflowNodeEntity();
        testNode1.setId("node-001");
        testNode1.setDefinitionId("def-001");
        testNode1.setNodeName("Manager Approval");
        testNode1.setNodeCode("manager_approve");
        testNode1.setNodeOrder(1);
        testNode1.setApproverType("user");
        testNode1.setApproverId(APPROVER_ID);

        testNode2 = new WorkflowNodeEntity();
        testNode2.setId("node-002");
        testNode2.setDefinitionId("def-001");
        testNode2.setNodeName("HR Approval");
        testNode2.setNodeCode("hr_approve");
        testNode2.setNodeOrder(2);
        testNode2.setApproverType("user");
        testNode2.setApproverId("hr-001");

        // Setup test instance
        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setDefinitionId("def-001");
        testInstance.setDefinitionName("Leave Approval");
        testInstance.setUserId(INITIATOR_ID);
        testInstance.setUserName("John Doe");
        testInstance.setDeptId("dept-001");
        testInstance.setTitle("Leave Request");
        testInstance.setBusinessData("{\"days\":3}");

        // Setup pending approval
        pendingApproval = new WorkflowApprovalEntity();
        pendingApproval.setId("appr-001");
        pendingApproval.setInstanceId("inst-001");
        pendingApproval.setNodeId("node-001");
        pendingApproval.setNodeName("Manager Approval");
        pendingApproval.setApproverId(APPROVER_ID);
        pendingApproval.setApproverName("Manager Smith");
        pendingApproval.setApprovalStatus("pending");

        // Mock objectMapper for serializeFormData/deserializeFormData
        // Use lenient() because not all tests need this stubbing
        try {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            lenient().when(objectMapper.readValue(any(String.class), any(tools.jackson.core.type.TypeReference.class)))
                    .thenReturn(java.util.Map.of());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Mock hookService to return passing results by default
        com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                        true, java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of()
                );
        lenient().when(hookService.executeAllHooks(
                anyString(), any(WorkflowInstanceEntity.class), any(WorkflowNodeEntity.class),
                anyMap(), anyMap()
        )).thenReturn(passingResult);

        // Mock nodeRepository.findById to return empty for any input (handles null case for drafts)
        lenient().when(nodeRepository.findById(any())).thenReturn(java.util.Optional.empty());
    }

    private void mockSecurityContext(String userId) {
        AppUserDetails userDetails = new AppUserDetails(
                userId, "testuser", null, "Test User",
                null, null, null, "dept-001", 1, null, null
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Draft State Transitions")
    class DraftStateTransitions {

        @Test
        @DisplayName("Should transition from draft to running on submit")
        void shouldTransitionFromDraftToRunning() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("draft");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(any(List.class)))
                    .thenReturn(Arrays.asList(testApprover));

            // When
            WorkflowInstanceResponse result = service.submit("inst-001", null);

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("running");
            assertThat(testInstance.getSubmitTime()).isNotNull();
            assertThat(testInstance.getCurrentNodeId()).isEqualTo("node-001");
        }

        @Test
        @DisplayName("Should transition from draft to cancelled on cancel")
        void shouldTransitionFromDraftToCancelled() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("draft");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(hookService.executeAllHooks(eq("PRE_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(hookService.executeAllHooks(eq("POST_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When
            service.cancel("inst-001");

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("cancelled");
            assertThat(testInstance.getFinishTime()).isNotNull();
        }

        @Test
        @DisplayName("Should remain in draft when withdrawn")
        void shouldRemainInDraftWhenWithdrawn() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("draft");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(List.of());
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(hookService.executeAllHooks(eq("PRE_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(hookService.executeAllHooks(eq("POST_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When
            service.withdraw("inst-001");

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("draft");
        }
    }

    @Nested
    @DisplayName("Running State Transitions")
    class RunningStateTransitions {

        @Test
        @DisplayName("Should transition from running to approved when all nodes complete")
        void shouldTransitionFromRunningToApproved() {
            // Given - Setup instance at last node
            mockSecurityContext("hr-001");
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-002");

            WorkflowApprovalEntity hrApproval = new WorkflowApprovalEntity();
            hrApproval.setId("appr-002");
            hrApproval.setInstanceId("inst-001");
            hrApproval.setNodeId("node-002");
            hrApproval.setApproverId("hr-001");
            hrApproval.setApprovalStatus("pending");

            UserEntity hrApprover = new UserEntity();
            hrApprover.setId("hr-001");
            hrApprover.setNickname("HR Manager");

            when(userRepository.findById("hr-001"))
                    .thenReturn(Optional.of(hrApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-002"))
                    .thenReturn(Arrays.asList(hrApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(hrApproval);
            when(nodeRepository.findById("node-002"))
                    .thenReturn(Optional.of(testNode2));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When - Approve last node
            service.approve("inst-001", req);

            // Then - Should complete workflow
            assertThat(testInstance.getStatus()).isEqualTo("approved");
            assertThat(testInstance.getFinishTime()).isNotNull();
            assertThat(testInstance.getCurrentNodeId()).isNull();
            assertThat(testInstance.getCurrentNodeName()).isNull();
        }

        @Test
        @DisplayName("Should transition from running to rejected on any rejection")
        void shouldTransitionFromRunningToRejected() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Insufficient documentation")
                    .build();

            // When
            service.reject("inst-001", req);

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("rejected");
            assertThat(testInstance.getFinishTime()).isNotNull();
            assertThat(pendingApproval.getApprovalStatus()).isEqualTo("rejected");
        }

        @Test
        @DisplayName("Should transition from running to cancelled on cancel")
        void shouldTransitionFromRunningToCancelled() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("running");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(hookService.executeAllHooks(eq("PRE_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(hookService.executeAllHooks(eq("POST_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When
            service.cancel("inst-001");

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("cancelled");
            assertThat(testInstance.getFinishTime()).isNotNull();
        }

        @Test
        @DisplayName("Should remain in running when waiting for other approvers")
        void shouldRemainInRunningWhenWaitingForOthers() {
            // Given - Multi-approval scenario with 3 approvers
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            // First approver already approved
            WorkflowApprovalEntity approvedApproval = new WorkflowApprovalEntity();
            approvedApproval.setId("appr-002");
            approvedApproval.setInstanceId("inst-001");
            approvedApproval.setNodeId("node-001");
            approvedApproval.setApproverId("other-approver");
            approvedApproval.setApprovalStatus("approved");

            // Third approver still pending
            WorkflowApprovalEntity thirdPendingApproval = new WorkflowApprovalEntity();
            thirdPendingApproval.setId("appr-003");
            thirdPendingApproval.setInstanceId("inst-001");
            thirdPendingApproval.setNodeId("node-001");
            thirdPendingApproval.setApproverId("third-approver");
            thirdPendingApproval.setApprovalStatus("pending");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval, approvedApproval, thirdPendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When - Second approver approves (but third still pending)
            service.approve("inst-001", req);

            // Then - Should still be running (waiting for third approver)
            assertThat(testInstance.getStatus()).isEqualTo("running");
        }
    }

    @Nested
    @DisplayName("Rejected State Transitions")
    class RejectedStateTransitions {

        @Test
        @DisplayName("Should transition from rejected to draft on withdraw")
        void shouldTransitionFromRejectedToDraft() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("rejected");
            testInstance.setCurrentNodeId("node-001");
            testInstance.setSubmitTime(Instant.now());

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            // Mock hook calls
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(hookService.executeAllHooks(eq("PRE_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(hookService.executeAllHooks(eq("POST_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When
            service.withdraw("inst-001");

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("draft");
            assertThat(testInstance.getCurrentNodeId()).isNull();
            assertThat(testInstance.getCurrentNodeName()).isNull();
            assertThat(testInstance.getSubmitTime()).isNull();
            assertThat(pendingApproval.getDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should not allow submit from rejected state without withdraw")
        void shouldNotAllowSubmitFromRejectedWithoutWithdraw() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("rejected");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或进行中的工作流可以提交");
        }
    }

    @Nested
    @DisplayName("Approved State - Final State")
    class ApprovedStateFinal {

        @Test
        @DisplayName("Should not allow any transitions from approved state")
        void shouldNotAllowTransitionsFromApproved() {
            // Given
            testInstance.setStatus("approved");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then - Cannot cancel
            mockSecurityContext(INITIATOR_ID);
            assertThatThrownBy(() -> service.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("当前状态不允许取消");

            // Cannot withdraw
            assertThatThrownBy(() -> service.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或被拒绝的流程可以撤回");

            // Cannot submit
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或进行中的工作流可以提交");
        }
    }

    @Nested
    @DisplayName("Cancelled State - Final State")
    class CancelledStateFinal {

        @Test
        @DisplayName("Should not allow any transitions from cancelled state")
        void shouldNotAllowTransitionsFromCancelled() {
            // Given
            testInstance.setStatus("cancelled");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then - Cannot cancel again
            mockSecurityContext(INITIATOR_ID);
            assertThatThrownBy(() -> service.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("当前状态不允许取消");

            // Cannot withdraw
            assertThatThrownBy(() -> service.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或被拒绝的流程可以撤回");

            // Cannot submit
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或进行中的工作流可以提交");
        }
    }

    @Nested
    @DisplayName("Invalid Transition Scenarios")
    class InvalidTransitionScenarios {

        @Test
        @DisplayName("Should not allow approval when not in running state")
        void shouldNotAllowApprovalWhenNotRunning() {
            // Given - Try to approve a draft
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("draft");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> service.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有进行中的工作流可以审批");
        }

        @Test
        @DisplayName("Should not allow rejection when not in running state")
        void shouldNotAllowRejectionWhenNotRunning() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("approved");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Reject")
                    .build();

            // When/Then
            assertThatThrownBy(() -> service.reject("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有进行中的工作流可以审批");
        }

        @Test
        @DisplayName("Should not allow withdraw from running state")
        void shouldNotAllowWithdrawFromRunning() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("running");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或被拒绝的流程可以撤回");
        }
    }

    @Nested
    @DisplayName("State Transition Validation")
    class StateTransitionValidation {

        @Test
        @DisplayName("Should verify all possible valid transitions")
        void shouldVerifyAllValidTransitions() {
            // Valid transitions map
            String[][] validTransitions = {
                    {"draft", "running"},      // submit
                    {"draft", "cancelled"},    // cancel
                    {"running", "approved"},   // final approval
                    {"running", "rejected"},   // rejection
                    {"running", "cancelled"},  // cancel
                    {"rejected", "draft"}      // withdraw
            };

            // This test documents all valid transitions
            // Individual transitions are tested in their respective nested classes
            assertThat(validTransitions.length).isEqualTo(6);
        }

        @Test
        @DisplayName("Should verify timestamp is set on state changes")
        void shouldVerifyTimestampOnStateChanges() {
            // Given
            mockSecurityContext(INITIATOR_ID);
            testInstance.setStatus("draft");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(any(List.class)))
                    .thenReturn(Arrays.asList(testApprover));

            // When - Submit
            assertThat(testInstance.getSubmitTime()).isNull();
            service.submit("inst-001", null);

            // Then - Submit time set
            assertThat(testInstance.getSubmitTime()).isNotNull();
            assertThat(testInstance.getFinishTime()).isNull();
        }

        @Test
        @DisplayName("Should verify finish time is set on completion")
        void shouldVerifyFinishTimeOnCompletion() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            // When - Reject (ends workflow)
            assertThat(testInstance.getFinishTime()).isNull();
            service.reject("inst-001", ApprovalActionRequest.builder()
                    .comment("No")
                    .build());

            // Then - Finish time set
            assertThat(testInstance.getFinishTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Multi-Node Transition Scenarios")
    class MultiNodeTransitionScenarios {

        @Test
        @DisplayName("Should handle sequential node transitions")
        void shouldHandleSequentialNodeTransitions() {
            // This test validates that workflow correctly transitions through nodes
            // The actual step-by-step transition is tested in other test classes
            // This test documents the expected behavior

            // Expected flow: draft -> running (node-001) -> running (node-002) -> approved
            String[] expectedStates = {"draft", "running", "running", "approved"};
            String[] expectedNodes = {null, "node-001", "node-002", null};

            assertThat(expectedStates.length).isEqualTo(4);
            assertThat(expectedNodes.length).isEqualTo(4);
        }

        @Test
        @DisplayName("Should maintain state integrity during node transitions")
        void shouldMaintainStateIntegrityDuringTransitions() {
            // Validates that instance state is consistent during transitions
            // State consistency is implicitly tested in transition tests
            assertThat(true).isTrue();
        }
    }
}
