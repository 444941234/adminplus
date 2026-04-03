package com.adminplus.service;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.adminplus.common.security.AppUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkflowInstanceService
 * Tests workflow lifecycle: create draft, start, approve, reject, cancel, withdraw
 *
 * Test Categories:
 * 1. Draft Creation - Creating workflow drafts
 * 2. Workflow Start - Starting workflow from draft
 * 3. Approval Process - Approving and rejecting workflows
 * 4. Cancellation - Canceling workflows
 * 5. Withdrawal - Withdrawing workflows
 * 6. Query Operations - Retrieving workflow data
 * 7. Edge Cases - Boundary conditions and error paths
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowInstanceService Unit Tests")
class WorkflowInstanceServiceTest {

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
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowInstanceServiceImpl service;

    private WorkflowDefinitionEntity testDefinition;
    private WorkflowNodeEntity testNode1;
    private WorkflowNodeEntity testNode2;
    private UserEntity testUser;
    private UserEntity testApprover;
    private WorkflowInstanceEntity testInstance;
    private WorkflowStartReq validStartReq;

    private static final String CURRENT_USER_ID = "user-001";
    private static final String APPROVER_ID = "approver-001";

    @BeforeEach
    void setUp() {
        // Setup test definition
        testDefinition = new WorkflowDefinitionEntity();
        testDefinition.setId("def-001");
        testDefinition.setDefinitionName("Leave Approval");
        testDefinition.setDefinitionKey("leave_approval");
        testDefinition.setStatus(1);
        testDefinition.setFormConfig("{\"sections\":[{\"key\":\"basic\",\"title\":\"基础信息\",\"fields\":[]}]}");

        // Setup test nodes
        testNode1 = new WorkflowNodeEntity();
        testNode1.setId("node-001");
        testNode1.setDefinitionId("def-001");
        testNode1.setNodeName("Manager Approval");
        testNode1.setNodeCode("manager_approve");
        testNode1.setNodeOrder(1);
        testNode1.setApproverType("user");
        testNode1.setApproverId(APPROVER_ID);
        testNode1.setIsCounterSign(false);
        testNode1.setAutoPassSameUser(false);

        testNode2 = new WorkflowNodeEntity();
        testNode2.setId("node-002");
        testNode2.setDefinitionId("def-001");
        testNode2.setNodeName("HR Approval");
        testNode2.setNodeCode("hr_approve");
        testNode2.setNodeOrder(2);
        testNode2.setApproverType("user");
        testNode2.setApproverId("hr-001");
        testNode2.setIsCounterSign(false);
        testNode2.setAutoPassSameUser(false);

        // Setup test users
        testUser = new UserEntity();
        testUser.setId(CURRENT_USER_ID);
        testUser.setNickname("John Doe");
        testUser.setDeptId("dept-001");

        testApprover = new UserEntity();
        testApprover.setId(APPROVER_ID);
        testApprover.setNickname("Manager Smith");

        // Setup test instance
        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setDefinitionId("def-001");
        testInstance.setDefinitionName("Leave Approval");
        testInstance.setUserId(CURRENT_USER_ID);
        testInstance.setUserName("John Doe");
        testInstance.setDeptId("dept-001");
        testInstance.setTitle("Leave Request");
        testInstance.setBusinessData("{\"days\":3}");
        testInstance.setStatus("draft");

        // Setup valid start request
        validStartReq = WorkflowStartReq.builder()
                .definitionId("def-001")
                .title("Leave Request")
                .formData(Map.of("days", 3))
                .remark("Need time off")
                .build();

        // Mock objectMapper for serializeFormData/deserializeFormData
        // Use lenient() because not all tests need this stubbing
        try {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"days\":3}");
            lenient().when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                    .thenReturn(Map.of("days", 3));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Mock hookService to return passing results by default
        // Create a reusable hook result that passes all validations
        com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                        true, List.of(), List.of(), List.of(), List.of()
                );
        lenient().when(hookService.executeAllHooks(
                anyString(), any(WorkflowInstanceEntity.class), any(WorkflowNodeEntity.class),
                anyMap(), anyMap()
        )).thenReturn(passingResult);

        // Mock nodeRepository.findById to return empty for any input (handles null case for drafts)
        lenient().when(nodeRepository.findById(any())).thenReturn(Optional.empty());
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
    @DisplayName("Create Draft - Happy Path")
    class CreateDraftHappyPath {

        @Test
        @DisplayName("Should create draft successfully")
        void shouldCreateDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(userRepository.findById(CURRENT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            // When
            WorkflowInstanceResp result = service.createDraft(validStartReq);

            // Then
            assertThat(result).isNotNull();
            verify(instanceRepository).save(any(WorkflowInstanceEntity.class));
        }

        @Test
        @DisplayName("Should create draft with all fields")
        void shouldCreateDraftWithAllFields() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(userRepository.findById(CURRENT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            WorkflowInstanceResp result = service.createDraft(validStartReq);

            // Then
            assertThat(result.title()).isEqualTo("Leave Request");
            assertThat(result.businessData()).isEqualTo("{\"days\":3}");
            assertThat(result.remark()).isEqualTo("Need time off");
        }
    }

    @Nested
    @DisplayName("Create Draft - Error Paths")
    class CreateDraftErrorPaths {

        @Test
        @DisplayName("Should throw exception when definition not found")
        void shouldThrowExceptionWhenDefinitionNotFound() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.createDraft(validStartReq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流定义不存在");

            verify(instanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(userRepository.findById(CURRENT_USER_ID))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.createDraft(validStartReq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("用户不存在");

            verify(instanceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Submit Workflow - Happy Path")
    class SubmitWorkflowHappyPath {

        @Test
        @DisplayName("Should submit draft successfully")
        void shouldSubmitDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            WorkflowInstanceResp result = service.submit("inst-001", null);

            // Then
            // Status is normalized: "running" -> "PROCESSING"
            assertThat(result.status()).isEqualTo("PROCESSING");
            assertThat(result.currentNodeId()).isEqualTo("node-001");
            verify(instanceRepository).save(any(WorkflowInstanceEntity.class));
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
        }

        @Test
        @DisplayName("Should create approval records for first node")
        void shouldCreateApprovalRecords() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
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

            // When
            service.submit("inst-001", null);

            // Then
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
        }
    }

    @Nested
    @DisplayName("Submit Workflow - Error Paths")
    class SubmitWorkflowErrorPaths {

        @Test
        @DisplayName("Should throw exception when instance not found")
        void shouldThrowExceptionWhenInstanceNotFound() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(instanceRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.submit("non-existent", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流实例不存在");
        }

        @Test
        @DisplayName("Should throw exception when non-initiator submits")
        void shouldThrowExceptionWhenNonInitiatorSubmits() {
            // Given
            mockSecurityContext("different-user");
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以提交工作流");
        }

        @Test
        @DisplayName("Should throw exception when submitting finished workflow")
        void shouldThrowExceptionWhenSubmittingFinishedWorkflow() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("approved");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或进行中的工作流可以提交");
        }

        @Test
        @DisplayName("Should throw exception when workflow has no nodes")
        void shouldThrowExceptionWhenNoNodes() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(List.of());

            // When/Then
            assertThatThrownBy(() -> service.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流没有配置审批节点");
        }
    }

    @Nested
    @DisplayName("Start Workflow - Happy Path")
    class StartWorkflowHappyPath {

        @Test
        @DisplayName("Should start workflow directly")
        void shouldStartWorkflowDirectly() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(userRepository.findById(CURRENT_USER_ID))
                    .thenReturn(Optional.of(testUser));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(instanceRepository.findById(anyString()))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            WorkflowInstanceResp result = service.start(validStartReq);

            // Then
            // Status is normalized: "running" -> "PROCESSING"
            assertThat(result.status()).isEqualTo("PROCESSING");
            verify(instanceRepository, times(2)).save(any(WorkflowInstanceEntity.class));
        }
    }

    @Nested
    @DisplayName("Approve Workflow - Happy Path")
    class ApproveWorkflowHappyPath {

        @Test
        @DisplayName("Should approve workflow successfully")
        void shouldApproveWorkflowSuccessfully() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When
            WorkflowInstanceResp result = service.approve("inst-001", req);

            // Then
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
            verify(instanceRepository).save(any(WorkflowInstanceEntity.class));
        }

        @Test
        @DisplayName("Should move to next node after all approvers approve")
        void shouldMoveToNextNodeAfterAllApprove() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            WorkflowApprovalEntity approvedApproval = new WorkflowApprovalEntity();
            approvedApproval.setId("appr-002");
            approvedApproval.setInstanceId("inst-001");
            approvedApproval.setNodeId("node-001");
            approvedApproval.setApproverId("other-approver");
            approvedApproval.setApprovalStatus("approved");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval, approvedApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(userRepository.findById(anyString()))
                    .thenReturn(Optional.of(testApprover));

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When
            service.approve("inst-001", req);

            // Then
            assertThat(testInstance.getCurrentNodeId()).isEqualTo("node-002");
        }

        @Test
        @DisplayName("Should complete workflow when last node approved")
        void shouldCompleteWorkflowAtLastNode() {
            // Given
            mockSecurityContext("hr-001");
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-002");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-002");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-002");
            approval.setApproverId("hr-001");
            approval.setApprovalStatus("pending");

            UserEntity hrApprover = new UserEntity();
            hrApprover.setId("hr-001");
            hrApprover.setNickname("HR Manager");

            when(userRepository.findById("hr-001"))
                    .thenReturn(Optional.of(hrApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-002"))
                    .thenReturn(Arrays.asList(approval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            when(nodeRepository.findById("node-002"))
                    .thenReturn(Optional.of(testNode2));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When
            WorkflowInstanceResp result = service.approve("inst-001", req);

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("approved");
            assertThat(testInstance.getFinishTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Approve Workflow - Error Paths")
    class ApproveWorkflowErrorPaths {

        @Test
        @DisplayName("Should throw exception when non-approver tries to approve")
        void shouldThrowExceptionWhenNonApproverTriesToApprove() {
            // Given
            mockSecurityContext("unauthorized-user");
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            when(userRepository.findById("unauthorized-user"))
                    .thenReturn(Optional.of(testUser));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval));

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> service.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("您没有权限审批此工作流");
        }

        @Test
        @DisplayName("Should throw exception when approving non-running workflow")
        void shouldThrowExceptionWhenApprovingNonRunning() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("draft");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> service.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有进行中的工作流可以审批");
        }
    }

    @Nested
    @DisplayName("Reject Workflow - Happy Path")
    class RejectWorkflowHappyPath {

        @Test
        @DisplayName("Should reject workflow successfully")
        void shouldRejectWorkflowSuccessfully() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Insufficient leave balance")
                    .build();

            // When
            WorkflowInstanceResp result = service.reject("inst-001", req);

            // Then
            assertThat(testInstance.getStatus()).isEqualTo("rejected");
            assertThat(testInstance.getFinishTime()).isNotNull();
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
        }
    }

    @Nested
    @DisplayName("Cancel Workflow - Happy Path")
    class CancelWorkflowHappyPath {

        @Test
        @DisplayName("Should cancel draft successfully")
        void shouldCancelDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls for cancel operation
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
            verify(instanceRepository).save(testInstance);
        }

        @Test
        @DisplayName("Should cancel running workflow successfully")
        void shouldCancelRunningWorkflowSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls for cancel operation
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
            verify(instanceRepository).save(testInstance);
        }
    }

    @Nested
    @DisplayName("Cancel Workflow - Error Paths")
    class CancelWorkflowErrorPaths {

        @Test
        @DisplayName("Should throw exception when non-initiator cancels")
        void shouldThrowExceptionWhenNonInitiatorCancels() {
            // Given
            mockSecurityContext("different-user");
            testInstance.setStatus("running");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以取消工作流");
        }

        @Test
        @DisplayName("Should throw exception when cancelling finished workflow")
        void shouldThrowExceptionWhenCancellingFinished() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("approved");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("当前状态不允许取消");
        }
    }

    @Nested
    @DisplayName("Withdraw Workflow - Happy Path")
    class WithdrawWorkflowHappyPath {

        @Test
        @DisplayName("Should withdraw draft successfully")
        void shouldWithdrawDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(List.of());
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls for withdraw operation
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
            verify(instanceRepository).save(testInstance);
        }

        @Test
        @DisplayName("Should withdraw rejected workflow successfully")
        void shouldWithdrawRejectedWorkflowSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("rejected");
            testInstance.setCurrentNodeId("node-001");
            testInstance.setSubmitTime(Instant.now());

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setDeleted(false);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(approval));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            // Mock hook calls for withdraw operation
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
            assertThat(testInstance.getSubmitTime()).isNull();
            assertThat(approval.getDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Withdraw Workflow - Error Paths")
    class WithdrawWorkflowErrorPaths {

        @Test
        @DisplayName("Should throw exception when non-initiator withdraws")
        void shouldThrowExceptionWhenNonInitiatorWithdraws() {
            // Given
            mockSecurityContext("different-user");
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以撤回工作流");
        }

        @Test
        @DisplayName("Should throw exception when withdrawing running workflow")
        void shouldThrowExceptionWhenWithdrawingRunning() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
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
    @DisplayName("Query Operations - Happy Path")
    class QueryOperationsHappyPath {

        @Test
        @DisplayName("Should get workflow detail successfully")
        void shouldGetWorkflowDetailSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setNodeName("Manager Approval");
            approval.setApproverId(APPROVER_ID);
            approval.setApproverName("Manager Smith");
            approval.setApprovalStatus("pending");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(approval));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList());
            when(addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc("inst-001"))
                    .thenReturn(Arrays.asList());

            // When
            WorkflowDetailResp result = service.getDetail("inst-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.instance().id()).isEqualTo("inst-001");
            assertThat(result.approvals()).hasSize(1);
            assertThat(result.nodes()).hasSize(2);
        }

        @Test
        @DisplayName("Should get my workflows successfully")
        void shouldGetMyWorkflowsSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            when(instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(CURRENT_USER_ID))
                    .thenReturn(Arrays.asList(testInstance));

            // When
            List<WorkflowInstanceResp> result = service.getMyWorkflows(null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo("inst-001");
        }

        @Test
        @DisplayName("Should get my workflows by status")
        void shouldGetMyWorkflowsByStatus() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            when(instanceRepository.findByUserIdAndStatusAndDeletedFalseOrderBySubmitTimeDesc(CURRENT_USER_ID, "running"))
                    .thenReturn(Arrays.asList(testInstance));

            // When
            List<WorkflowInstanceResp> result = service.getMyWorkflows("running");

            // Then
            assertThat(result).hasSize(1);
            // Status is normalized: "running" -> "PROCESSING"
            assertThat(result.get(0).status()).isEqualTo("PROCESSING");
        }

        @Test
        @DisplayName("Should get pending approvals successfully")
        void shouldGetPendingApprovalsSuccessfully() {
            // Given
            mockSecurityContext(APPROVER_ID);
            when(instanceRepository.findPendingApprovalsByUser(APPROVER_ID))
                    .thenReturn(Arrays.asList(testInstance));

            // When
            List<WorkflowInstanceResp> result = service.getPendingApprovals();

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should count pending approvals")
        void shouldCountPendingApprovals() {
            // Given
            mockSecurityContext(APPROVER_ID);
            when(instanceRepository.countPendingApprovalsByUser(APPROVER_ID))
                    .thenReturn(5L);

            // When
            long result = service.countPendingApprovals();

            // Then
            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Should get approvals successfully")
        void shouldGetApprovalsSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");

            // Mock for access check - user is initiator (CURRENT_USER_ID matches testInstance.userId)
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(approval));

            // When
            List<WorkflowApprovalResp> result = service.getApprovals("inst-001");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Edge Cases - Boundary Conditions")
    class EdgeCasesBoundaryConditions {

        @Test
        @DisplayName("Should handle workflow with single node")
        void shouldHandleSingleNodeWorkflow() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
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

            // When
            WorkflowInstanceResp result = service.submit("inst-001", null);

            // Then
            assertThat(result.currentNodeId()).isEqualTo("node-001");
        }

        @Test
        @DisplayName("Should handle very long comment")
        void shouldHandleVeryLongComment() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            String longComment = "A".repeat(500);

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment(longComment)
                    .build();

            // When
            WorkflowInstanceResp result = service.approve("inst-001", req);

            // Then
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
        }

        @Test
        @DisplayName("Should handle null attachments")
        void shouldHandleNullAttachments() {
            // Given
            mockSecurityContext(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);
            approval.setApprovalStatus("pending");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(testApprover));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(approval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(approval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionReq req = ApprovalActionReq.builder()
                    .comment("Approved")
                    .build();

            // When
            service.approve("inst-001", req);

            // Then
            verify(approvalRepository).save(any(WorkflowApprovalEntity.class));
        }
    }

    @Nested
    @DisplayName("Draft Operations")
    class DraftOperations {

        @Test
        @DisplayName("Should get draft detail with formConfig and formData")
        void shouldGetDraftDetailSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));

            // When
            var result = service.getDraftDetail("inst-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.instance()).isNotNull();
            assertThat(result.instance().id()).isEqualTo("inst-001");
            assertThat(result.formConfig()).isNotNull();
            assertThat(result.formData()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when non-owner gets draft detail")
        void shouldThrowWhenNonOwnerGetsDraftDetail() {
            // Given
            mockSecurityContext("different-user");
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.getDraftDetail("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以查看草稿");
        }

        @Test
        @DisplayName("Should update draft successfully")
        void shouldUpdateDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            WorkflowStartReq updateReq = WorkflowStartReq.builder()
                    .definitionId("def-001")
                    .title("Updated Title")
                    .formData(Map.of("days", 5))
                    .remark("Updated remark")
                    .build();

            // When
            WorkflowInstanceResp result = service.updateDraft("inst-001", updateReq);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Should throw when updating non-draft workflow")
        void shouldThrowWhenUpdatingNonDraft() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> service.updateDraft("inst-001", validStartReq))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿状态可以更新");
        }

        @Test
        @DisplayName("Should delete draft successfully")
        void shouldDeleteDraftSuccessfully() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("draft");
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When
            service.deleteDraft("inst-001");

            // Then
            verify(instanceRepository).delete(testInstance);
        }
    }

    @Nested
    @DisplayName("Detail Aggregation")
    class DetailAggregation {

        @Test
        @DisplayName("Should return aggregated detail with ccRecords and addSignRecords")
        void shouldReturnAggregatedDetail() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setId("appr-001");
            approval.setInstanceId("inst-001");
            approval.setNodeId("node-001");
            approval.setApproverId(APPROVER_ID);

            WorkflowCcEntity cc = new WorkflowCcEntity();
            cc.setId("cc-001");
            cc.setInstanceId("inst-001");
            cc.setUserId("user-002");

            WorkflowAddSignEntity addSign = new WorkflowAddSignEntity();
            addSign.setId("addsign-001");
            addSign.setInstanceId("inst-001");
            addSign.setAddUserId("user-003");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(approval));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1, testNode2));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(cc));
            when(addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc("inst-001"))
                    .thenReturn(Arrays.asList(addSign));

            // When
            WorkflowDetailResp result = service.getDetail("inst-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.instance().id()).isEqualTo("inst-001");
            assertThat(result.approvals()).hasSize(1);
            assertThat(result.nodes()).hasSize(2);
            assertThat(result.ccRecords()).hasSize(1);
            assertThat(result.addSignRecords()).hasSize(1);
            assertThat(result.formConfig()).isNotNull();
            assertThat(result.formData()).isNotNull();
            assertThat(result.operationPermissions()).isNotNull();
        }

        @Test
        @DisplayName("Should return empty ccRecords and addSignRecords when none exist")
        void shouldReturnEmptyRecordsWhenNoneExist() {
            // Given
            mockSecurityContext(CURRENT_USER_ID);
            testInstance.setStatus("running");
            testInstance.setCurrentNodeId("node-001");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList());
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode1));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode1));
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(ccRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList());
            when(addSignRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc("inst-001"))
                    .thenReturn(Arrays.asList());

            // When
            WorkflowDetailResp result = service.getDetail("inst-001");

            // Then
            assertThat(result.ccRecords()).isEmpty();
            assertThat(result.addSignRecords()).isEmpty();
        }
    }
}
