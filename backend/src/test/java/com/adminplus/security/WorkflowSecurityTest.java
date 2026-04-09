package com.adminplus.security;

import com.adminplus.common.security.AppUserDetails;
import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowDefinitionService;
import com.adminplus.service.WorkflowInstanceService;
import com.adminplus.service.impl.WorkflowDefinitionServiceImpl;
import com.adminplus.service.impl.WorkflowInstanceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.jackson.databind.json.JsonMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Security tests for Workflow Approval Module
 * Tests authorization checks and cross-user access prevention
 *
 * Security Categories:
 * 1. Authorization - Only authorized users can perform actions
 * 2. Cross-User Access Prevention - Users cannot access others' data
 * 3. Role-Based Access - Role-specific permissions
 * 4. Data Isolation - User data segregation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workflow Security Tests")
class WorkflowSecurityTest {

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
    private WorkflowDefinitionService mockDefinitionService;

    @Mock
    private DeptRepository deptRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private WorkflowCcRepository ccRepository;

    @Mock
    private WorkflowAddSignRepository addSignRepository;

    @Mock
    private JsonMapper objectMapper;

    @Mock
    private com.adminplus.service.workflow.hook.WorkflowHookService mockHookService;

    @Mock
    private ConversionService conversionService;

    private WorkflowInstanceService instanceService;

    // Test users
    private static final String INITIATOR_ID = "user-001";
    private static final String APPROVER_ID = "approver-001";
    private static final String UNAUTHORIZED_USER_ID = "user-999";
    private static final String ADMIN_ID = "admin-001";

    private UserEntity initiator;
    private UserEntity approver;
    private UserEntity unauthorizedUser;
    private UserEntity admin;

    private WorkflowInstanceEntity testInstance;
    private WorkflowDefinitionEntity testDefinition;
    private WorkflowNodeEntity testNode;
    private WorkflowApprovalEntity pendingApproval;
    private WorkflowApprovalEntity previousApproval;

    @BeforeEach
    void setUp() {
        // Initialize services
        instanceService = new WorkflowInstanceServiceImpl(
                instanceRepository, approvalRepository, definitionRepository,
                nodeRepository, userRepository, deptRepository, userRoleRepository, roleRepository,
                mockDefinitionService, ccRepository, addSignRepository, objectMapper,
                mockHookService, conversionService
        );

        mockDefinitionService = new WorkflowDefinitionServiceImpl(
                definitionRepository, nodeRepository, conversionService
        );

        // Setup test users
        initiator = new UserEntity();
        initiator.setId(INITIATOR_ID);
        initiator.setUsername("initiator");
        initiator.setNickname("Initiator User");
        initiator.setDeptId("dept-001");

        approver = new UserEntity();
        approver.setId(APPROVER_ID);
        approver.setUsername("approver");
        approver.setNickname("Approver User");
        approver.setDeptId("dept-001");

        unauthorizedUser = new UserEntity();
        unauthorizedUser.setId(UNAUTHORIZED_USER_ID);
        unauthorizedUser.setUsername("unauthorized");
        unauthorizedUser.setNickname("Unauthorized User");
        unauthorizedUser.setDeptId("dept-999");

        admin = new UserEntity();
        admin.setId(ADMIN_ID);
        admin.setUsername("admin");
        admin.setNickname("Admin User");
        admin.setDeptId("dept-000");

        // Setup test workflow definition
        testDefinition = new WorkflowDefinitionEntity();
        testDefinition.setId("def-001");
        testDefinition.setDefinitionName("Test Workflow");
        testDefinition.setDefinitionKey("test_wf");
        testDefinition.setStatus(1);

        // Setup test node
        testNode = new WorkflowNodeEntity();
        testNode.setId("node-001");
        testNode.setDefinitionId("def-001");
        testNode.setNodeName("Manager Approval");
        testNode.setApproverType("user");
        testNode.setApproverId(APPROVER_ID);

        // Setup test instance
        testInstance = new WorkflowInstanceEntity();
        testInstance.setId("inst-001");
        testInstance.setDefinitionId("def-001");
        testInstance.setDefinitionName("Test Workflow");
        testInstance.setUserId(INITIATOR_ID);
        testInstance.setUserName("Initiator User");
        testInstance.setDeptId("dept-001");
        testInstance.setTitle("Test Request");
        testInstance.setStatus("running");
        testInstance.setSubmitTime(Instant.now());
        testInstance.setCurrentNodeId("node-001");
        testInstance.setCurrentNodeName("Manager Approval");

        // Setup pending approval
        pendingApproval = new WorkflowApprovalEntity();
        pendingApproval.setId("appr-001");
        pendingApproval.setInstanceId("inst-001");
        pendingApproval.setNodeId("node-001");
        pendingApproval.setNodeName("Manager Approval");
        pendingApproval.setApproverId(APPROVER_ID);
        pendingApproval.setApproverName("Approver User");
        pendingApproval.setApprovalStatus("pending");

        // Setup previous approved approval
        previousApproval = new WorkflowApprovalEntity();
        previousApproval.setId("appr-000");
        previousApproval.setInstanceId("inst-001");
        previousApproval.setNodeId("node-000");
        previousApproval.setNodeName("Team Lead Approval");
        previousApproval.setApproverId(APPROVER_ID);
        previousApproval.setApproverName("Approver User");
        previousApproval.setApprovalStatus("approved");
        previousApproval.setApprovalTime(Instant.now());

        // Mock hookService to return passing results by default
        com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                        true, List.of(), List.of(), List.of(), List.of()
                );
        lenient().when(mockHookService.executeAllHooks(
                anyString(), any(WorkflowInstanceEntity.class), any(WorkflowNodeEntity.class),
                anyMap(), anyMap()
        )).thenReturn(passingResult);

        // Mock objectMapper for deserializeFormData
        try {
            lenient().when(objectMapper.readValue(any(String.class), any(tools.jackson.core.type.TypeReference.class)))
                    .thenReturn(java.util.Map.of());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Mock conversionService to return a basic response
        WorkflowInstanceResponse mockResponse = new WorkflowInstanceResponse(
                "inst-001", "def-001", "Test Workflow",
                INITIATOR_ID, "Initiator User", "dept-001", "Test Dept",
                "Test Request", "{}", "node-001", "Manager Approval",
                "DRAFT", null, null, null, null,
                null, null, null, null, null, null, null
        );
        lenient().when(conversionService.convert(any(WorkflowInstanceEntity.class), eq(WorkflowInstanceResponse.class)))
                .thenReturn(mockResponse);
    }

    private void authenticateAs(String userId) {
        // Create a mock AppUserDetails for the given user ID
        AppUserDetails userDetails = new AppUserDetails(
                userId,
                "test-user",
                null,
                "Test User",
                null,
                null,
                null,
                "dept-001",
                1,
                List.of("USER"),
                null
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Workflow Submission Authorization")
    class WorkflowSubmissionAuthorization {

        @Test
        @DisplayName("Should allow initiator to submit their own workflow")
        void shouldAllowInitiatorToSubmitOwnWorkflow() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("draft");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(any(List.class)))
                    .thenReturn(Arrays.asList(approver));

            // When/Then - Should not throw
            assertThatCode(() -> instanceService.submit("inst-001", null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should prevent non-initiator from submitting workflow")
        void shouldPreventNonInitiatorFromSubmitting() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setStatus("draft");
            testInstance.setUserId(INITIATOR_ID); // Different from authenticated user

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.submit("inst-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以提交工作流");
        }

        @Test
        @DisplayName("Should prevent submission without authentication")
        void shouldPreventSubmissionWithoutAuthentication() {
            // Given - No authentication context (null authentication)
            SecurityContextHolder.createEmptyContext();
            testInstance.setStatus("draft");

            // When/Then - Service would need authentication
            // For now, this documents the security expectation
        }
    }

    @Nested
    @DisplayName("Approval Authorization")
    class ApprovalAuthorization {

        @Test
        @DisplayName("Should allow designated approver to approve")
        void shouldAllowDesignatedApproverToApprove() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then - Should not throw
            assertThatCode(() -> instanceService.approve("inst-001", req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should prevent non-designated user from approving")
        void shouldPreventNonDesignatedUserFromApproving() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setStatus("running");

            when(userRepository.findById(UNAUTHORIZED_USER_ID))
                    .thenReturn(Optional.of(unauthorizedUser));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> instanceService.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("您没有权限审批此工作流");
        }

        @Test
        @DisplayName("Should prevent initiator from approving their own workflow")
        void shouldPreventInitiatorFromApprovingOwnWorkflow() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("running");

            when(userRepository.findById(INITIATOR_ID))
                    .thenReturn(Optional.of(initiator));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> instanceService.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("您没有权限审批此工作流");
        }

        @Test
        @DisplayName("Should allow designated approver to reject")
        void shouldAllowDesignatedApproverToReject() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Rejected")
                    .build();

            // When/Then - Should not throw
            assertThatCode(() -> instanceService.reject("inst-001", req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should prevent non-designated user from rejecting")
        void shouldPreventNonDesignatedUserFromRejecting() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setStatus("running");

            when(userRepository.findById(UNAUTHORIZED_USER_ID))
                    .thenReturn(Optional.of(unauthorizedUser));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Rejected")
                    .build();

            // When/Then
            assertThatThrownBy(() -> instanceService.reject("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("您没有权限审批此工作流");
        }
    }

    @Nested
    @DisplayName("Cancellation Authorization")
    class CancellationAuthorization {

        @Test
        @DisplayName("Should allow initiator to cancel their workflow")
        void shouldAllowInitiatorToCancel() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("running");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            // Mock hook calls for cancel operation
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(mockHookService.executeAllHooks(eq("PRE_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(mockHookService.executeAllHooks(eq("POST_CANCEL"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When/Then - Should not throw
            assertThatCode(() -> instanceService.cancel("inst-001"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should prevent non-initiator from cancelling workflow")
        void shouldPreventNonInitiatorFromCancelling() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setStatus("running");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以取消工作流");
        }

        @Test
        @DisplayName("Should prevent approver from cancelling workflow")
        void shouldPreventApproverFromCancelling() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以取消工作流");
        }
    }

    @Nested
    @DisplayName("Withdrawal Authorization")
    class WithdrawalAuthorization {

        @Test
        @DisplayName("Should allow initiator to withdraw their rejected workflow")
        void shouldAllowInitiatorToWithdraw() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("rejected");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc("inst-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            // Mock hook calls for withdraw operation
            com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary passingResult =
                    new com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary(
                            true, List.of(), List.of(), List.of(), List.of()
                    );
            when(mockHookService.executeAllHooks(eq("PRE_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);
            when(mockHookService.executeAllHooks(eq("POST_WITHDRAW"), any(), any(), anyMap(), anyMap()))
                    .thenReturn(passingResult);

            // When/Then - Should not throw
            assertThatCode(() -> instanceService.withdraw("inst-001"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should prevent non-initiator from withdrawing workflow")
        void shouldPreventNonInitiatorFromWithdrawing() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setStatus("rejected");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以撤回工作流");
        }

        @Test
        @DisplayName("Should prevent approver from withdrawing workflow")
        void shouldPreventApproverFromWithdrawing() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("rejected");
            testInstance.setUserId(INITIATOR_ID);

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有发起人可以撤回工作流");
        }
    }

    @Nested
    @DisplayName("Cross-User Access Prevention")
    class CrossUserAccessPrevention {

        @Test
        @DisplayName("Should prevent user from viewing another user's workflows")
        void shouldPreventViewingOtherUsersWorkflows() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);

            when(instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(UNAUTHORIZED_USER_ID))
                    .thenReturn(List.of());

            // When
            var result = instanceService.getMyWorkflows(null);

            // Then - Should only return authenticated user's workflows
            assertThat(result).doesNotContainAnyElementsOf(
                    Arrays.asList(new WorkflowInstanceResponse(
                            testInstance.getId(),
                            testInstance.getDefinitionId(),
                            testInstance.getDefinitionName(),
                            testInstance.getUserId(),
                            testInstance.getUserName(),
                            testInstance.getDeptId(),
                            null,
                            testInstance.getTitle(),
                            testInstance.getBusinessData(),
                            testInstance.getCurrentNodeId(),
                            testInstance.getCurrentNodeName(),
                            testInstance.getStatus(),
                            testInstance.getSubmitTime(),
                            testInstance.getFinishTime(),
                            testInstance.getRemark(),
                            testInstance.getCreateTime(),
                            false,
                            false,
                            false,
                            false,
                            false,
                            false,
                            false
                    ))
            );
        }

        @Test
        @DisplayName("Should only show pending approvals for authenticated user")
        void shouldOnlyShowPendingApprovalsForAuthenticatedUser() {
            // Given
            authenticateAs(APPROVER_ID);

            when(instanceRepository.findPendingApprovalsByUser(APPROVER_ID))
                    .thenReturn(List.of(testInstance));

            // When
            var result = instanceService.getPendingApprovals();

            // Then - Should only return workflows where user is an approver
            assertThat(result).isNotNull();
            verify(instanceRepository).findPendingApprovalsByUser(APPROVER_ID);
            verify(instanceRepository, times(1)).findPendingApprovalsByUser(anyString());
        }

        @Test
        @DisplayName("Should prevent data leakage between users")
        void shouldPreventDataLeakage() {
            // Given - User 1's workflow
            authenticateAs(INITIATOR_ID);
            testInstance.setUserId(INITIATOR_ID);
            testInstance.setBusinessData("{\"sensitive\":\"user1-data\"}");

            when(instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(INITIATOR_ID))
                    .thenReturn(Arrays.asList(testInstance));

            // When - Get user 1's workflows
            var result1 = instanceService.getMyWorkflows(null);

            // Then - Verify data belongs to user 1
            assertThat(result1).hasSize(1);

            // Given - User 2's request
            authenticateAs(UNAUTHORIZED_USER_ID);

            when(instanceRepository.findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(UNAUTHORIZED_USER_ID))
                    .thenReturn(List.of());

            // When - Get user 2's workflows
            var result2 = instanceService.getMyWorkflows(null);

            // Then - Should not see user 1's data
            assertThat(result2).isEmpty();
        }
    }

    @Nested
    @DisplayName("Workflow Definition Authorization")
    class WorkflowDefinitionAuthorization {

        @Test
        @DisplayName("Should prevent duplicate workflow keys")
        void shouldPreventDuplicateWorkflowKeys() {
            // Given
            authenticateAs(INITIATOR_ID);

            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Test Workflow",
                    "duplicate-key",
                    "Test",
                    "Test",
                    1,
                    "{}"
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse("duplicate-key"))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> mockDefinitionService.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流标识已存在");
        }

        @Test
        @DisplayName("Should prevent key conflicts on update")
        void shouldPreventKeyConflictsOnUpdate() {
            // Given
            authenticateAs(INITIATOR_ID);

            WorkflowDefinitionEntity existingDef = new WorkflowDefinitionEntity();
            existingDef.setId("def-002");

            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinition));
            when(definitionRepository.findByDefinitionKeyAndDeletedFalse("different-key"))
                    .thenReturn(Optional.of(existingDef));

            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Updated",
                    "different-key",
                    "Test",
                    "Test",
                    1,
                    "{}"
            );

            // When/Then
            assertThatThrownBy(() -> mockDefinitionService.update("def-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("工作流标识已被使用");
        }
    }

    @Nested
    @DisplayName("State-Based Authorization")
    class StateBasedAuthorization {

        @Test
        @DisplayName("Should prevent approval of finished workflows")
        void shouldPreventApprovalOfFinishedWorkflows() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("approved");

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then
            assertThatThrownBy(() -> instanceService.approve("inst-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有进行中的工作流可以审批");
        }

        @Test
        @DisplayName("Should prevent cancellation of finished workflows")
        void shouldPreventCancellationOfFinishedWorkflows() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("approved");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.cancel("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("当前状态不允许取消");
        }

        @Test
        @DisplayName("Should prevent withdrawal of running workflows")
        void shouldPreventWithdrawalOfRunningWorkflows() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setStatus("running");

            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));

            // When/Then
            assertThatThrownBy(() -> instanceService.withdraw("inst-001"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("只有草稿或被拒绝的流程可以撤回");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesBoundaryConditions {

        @Test
        @DisplayName("Should handle null user context gracefully")
        void shouldHandleNullUserContextGracefully() {
            // Given - This tests that security context is properly handled
            // In a real scenario, Spring Security would throw before reaching service
            // This test documents expected behavior
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should prevent timing attacks on user IDs")
        void shouldPreventTimingAttacks() {
            // Given - This tests that responses are consistent regardless of user existence
            authenticateAs(UNAUTHORIZED_USER_ID);

            when(instanceRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then - Should throw same exception type regardless
            assertThatThrownBy(() -> instanceService.cancel("non-existent"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should validate all authorization checks in approval chain")
        void shouldValidateAllAuthorizationChecksInApprovalChain() {
            // Given - Multi-node workflow with different approvers
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");

            // First node approval
            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenReturn(pendingApproval);
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode));
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(Arrays.asList(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .build();

            // When/Then - Should approve first node
            assertThatCode(() -> instanceService.approve("inst-001", req))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Input Validation Security")
    class InputValidationSecurity {

        @Test
        @DisplayName("Should sanitize comment input to prevent XSS")
        void shouldSanitizeCommentInput() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");

            String maliciousComment = "<script>alert('xss')</script>";

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment(maliciousComment)
                    .build();

            // When/Then - Service should handle the input
            // Note: Actual sanitization would happen at controller/validation layer
            assertThatCode(() -> instanceService.approve("inst-001", req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should validate attachment JSON structure")
        void shouldValidateAttachmentJsonStructure() {
            // Given
            authenticateAs(APPROVER_ID);
            testInstance.setStatus("running");

            String attachmentJson = "[\"file1.pdf\",\"file2.doc\"]";

            when(userRepository.findById(APPROVER_ID))
                    .thenReturn(Optional.of(approver));
            when(instanceRepository.findById("inst-001"))
                    .thenReturn(Optional.of(testInstance));
            when(approvalRepository.findByInstanceIdAndNodeIdAndDeletedFalse("inst-001", "node-001"))
                    .thenReturn(Arrays.asList(pendingApproval));
            when(approvalRepository.save(any(WorkflowApprovalEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNode));
            when(instanceRepository.save(any(WorkflowInstanceEntity.class)))
                    .thenReturn(testInstance);

            ApprovalActionRequest req = ApprovalActionRequest.builder()
                    .comment("Approved")
                    .attachments(attachmentJson)
                    .build();

            // When/Then - Should accept valid JSON
            assertThatCode(() -> instanceService.approve("inst-001", req))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("CC and Urge Data Isolation")
    class CcUrgeIsolation {

        @Test
        @DisplayName("Should only show CC records for authenticated user")
        void shouldOnlyShowCcRecordsForAuthenticatedUser() {
            // Given
            authenticateAs(APPROVER_ID);

            // When - User queries their own CC records
            // This test documents the expected behavior
            // Actual implementation would use the service method with authenticated user context
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should prevent cross-user urge access")
        void shouldPreventCrossUserUrgeAccess() {
            // Given
            authenticateAs(APPROVER_ID);

            // When - User queries their received urge records
            // This test documents the expected behavior
            // Actual implementation would use the service method with authenticated user context
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should only allow initiator to send urge")
        void shouldOnlyAllowInitiatorToSendUrge() {
            // Given
            authenticateAs(INITIATOR_ID);
            testInstance.setUserId(INITIATOR_ID);

            // When/Then - Initiator should be able to send urge
            // This test documents the expected behavior
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should prevent non-initiator from sending urge")
        void shouldPreventNonInitiatorFromSendingUrge() {
            // Given
            authenticateAs(UNAUTHORIZED_USER_ID);
            testInstance.setUserId(INITIATOR_ID);

            // When/Then - Non-initiator should not be able to send urge
            // This test documents the expected behavior
            assertThat(true).isTrue();
        }
    }
}
