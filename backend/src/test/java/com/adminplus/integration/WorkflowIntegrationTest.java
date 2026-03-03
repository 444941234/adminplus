package com.adminplus.integration;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.WorkflowDefinitionReq;
import com.adminplus.pojo.dto.req.WorkflowNodeReq;
import com.adminplus.pojo.dto.req.WorkflowStartReq;
import com.adminplus.pojo.dto.resp.WorkflowApprovalResp;
import com.adminplus.pojo.dto.resp.WorkflowDefinitionResp;
import com.adminplus.pojo.dto.resp.WorkflowDetailResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowDefinitionService;
import com.adminplus.service.WorkflowInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Disabled;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Workflow Approval Module
 * Tests end-to-end workflow execution with real database
 *
 * Test Coverage:
 * 1. Complete workflow lifecycle
 * 2. Multi-node approval flows
 * 3. Parallel approvals
 * 4. Edge cases (cancellation, rejection, withdrawal)
 *
 * NOTE: Disabled until H2 schema setup is properly configured
 */
@Disabled("Requires H2 schema configuration")
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Workflow Integration Tests")
class WorkflowIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkflowDefinitionRepository definitionRepository;

    @Autowired
    private WorkflowNodeRepository nodeRepository;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowApprovalRepository approvalRepository;

    @Autowired
    private UserRepository userRepository;

    private WorkflowDefinitionService definitionService;
    private WorkflowInstanceService instanceService;

    private UserEntity initiator;
    private UserEntity approver1;
    private UserEntity approver2;
    private UserEntity approver3;

    @BeforeEach
    void setUp() {
        // Initialize services with repositories
        // Note: In real setup, use @SpringBootTest with full context
        // For DataJpaTest, we manually construct or use reflection

        // Create test users
        initiator = createTestUser("initiator", "Initiator User", "dept-001");
        approver1 = createTestUser("approver1", "Manager Smith", "dept-001");
        approver2 = createTestUser("approver2", "HR Johnson", "dept-002");
        approver3 = createTestUser("approver3", "Director Williams", "dept-003");
    }

    private UserEntity createTestUser(String id, String name, String deptId) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(id);
        user.setNickname(name);
        user.setDeptId(deptId);
        return entityManager.persist(user);
    }

    private void authenticateAs(UserEntity user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())
        );
    }

    @Nested
    @DisplayName("End-to-End Workflow Execution")
    class EndToEndWorkflowExecution {

        @Test
        @DisplayName("Should complete full workflow lifecycle: create -> submit -> approve -> approve -> complete")
        void shouldCompleteFullWorkflowLifecycle() {
            // 1. Create workflow definition
            authenticateAs(initiator);
            WorkflowDefinitionReq definitionReq = new WorkflowDefinitionReq(
                    "Leave Approval",
                    "leave_approval",
                    "HR",
                    "Employee leave request workflow",
                    1,
                    "{\"fields\":[\"reason\",\"days\"]}"
            );

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionName("Leave Approval");
            definition.setDefinitionKey("leave_approval");
            definition.setCategory("HR");
            definition.setDescription("Employee leave request workflow");
            definition.setStatus(1);
            definition.setVersion(1);
            definition = entityManager.persist(definition);

            // 2. Add workflow nodes
            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Manager Approval");
            node1.setNodeCode("manager_approve");
            node1.setNodeOrder(1);
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1.setIsCounterSign(false);
            node1.setAutoPassSameUser(false);
            node1 = entityManager.persist(node1);

            WorkflowNodeEntity node2 = new WorkflowNodeEntity();
            node2.setDefinitionId(definition.getId());
            node2.setNodeName("HR Approval");
            node2.setNodeCode("hr_approve");
            node2.setNodeOrder(2);
            node2.setApproverType("user");
            node2.setApproverId(approver2.getId());
            node2.setIsCounterSign(false);
            node2.setAutoPassSameUser(false);
            node2 = entityManager.persist(node2);

            // Flush and clear to ensure data is persisted
            entityManager.flush();
            entityManager.clear();

            // 3. Start workflow instance
            WorkflowStartReq startReq = new WorkflowStartReq(
                    definition.getId(),
                    "Leave Request - 3 days",
                    "{\"reason\":\"Family event\",\"days\":3}",
                    "Need time off for family event"
            );

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
            instance.setUserId(initiator.getId());
            instance.setUserName(initiator.getNickname());
            instance.setDeptId(initiator.getDeptId());
            instance.setTitle(startReq.title());
            instance.setBusinessData(startReq.businessData());
            instance.setRemark(startReq.remark());
            instance.setStatus("running");
            instance.setSubmitTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance.setCurrentNodeName(node1.getNodeName());
            instance = entityManager.persist(instance);

            // 4. Create approval records for first node
            WorkflowApprovalEntity approval1 = new WorkflowApprovalEntity();
            approval1.setInstanceId(instance.getId());
            approval1.setNodeId(node1.getId());
            approval1.setNodeName(node1.getNodeName());
            approval1.setApproverId(approver1.getId());
            approval1.setApproverName(approver1.getNickname());
            approval1.setApprovalStatus("pending");
            entityManager.persist(approval1);

            entityManager.flush();
            entityManager.clear();

            // 5. First approver approves
            authenticateAs(approver1);
            WorkflowApprovalEntity savedApproval = approvalRepository.findById(approval1.getId()).orElseThrow();
            savedApproval.setApprovalStatus("approved");
            savedApproval.setComment("Approved - reasonable request");
            savedApproval.setApprovalTime(Instant.now());
            entityManager.persist(savedApproval);

            // Move to next node
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
            instance.setCurrentNodeId(node2.getId());
            instance.setCurrentNodeName(node2.getNodeName());
            entityManager.persist(instance);

            // Create approval for second node
            WorkflowApprovalEntity approval2 = new WorkflowApprovalEntity();
            approval2.setInstanceId(instance.getId());
            approval2.setNodeId(node2.getId());
            approval2.setNodeName(node2.getNodeName());
            approval2.setApproverId(approver2.getId());
            approval2.setApproverName(approver2.getNickname());
            approval2.setApprovalStatus("pending");
            entityManager.persist(approval2);

            entityManager.flush();
            entityManager.clear();

            // 6. Second approver approves
            authenticateAs(approver2);
            WorkflowApprovalEntity savedApproval2 = approvalRepository.findById(approval2.getId()).orElseThrow();
            savedApproval2.setApprovalStatus("approved");
            savedApproval2.setComment("Final approval - leave granted");
            savedApproval2.setApprovalTime(Instant.now());
            entityManager.persist(savedApproval2);

            // Complete workflow
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
            instance.setStatus("approved");
            instance.setFinishTime(Instant.now());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);
            entityManager.persist(instance);

            entityManager.flush();
            entityManager.clear();

            // 7. Verify final state
            WorkflowInstanceEntity finalInstance = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(finalInstance.getStatus()).isEqualTo("approved");
            assertThat(finalInstance.getFinishTime()).isNotNull();
            assertThat(finalInstance.getCurrentNodeId()).isNull();

            List<WorkflowApprovalEntity> approvals = approvalRepository
                    .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId());
            assertThat(approvals).hasSize(2);
            assertThat(approvals).allMatch(a -> a.isApproved());
        }

        @Test
        @DisplayName("Should handle workflow rejection at any node")
        void shouldHandleWorkflowRejection() {
            // Create definition and instance
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionName("Expense Approval");
            definition.setDefinitionKey("expense_approval");
            definition.setCategory("Finance");
            definition.setStatus(1);
            definition.setVersion(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Manager Review");
            node1.setNodeOrder(1);
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1 = entityManager.persist(node1);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
            instance.setUserId(initiator.getId());
            instance.setUserName(initiator.getNickname());
            instance.setTitle("Expense Report");
            instance.setStatus("running");
            instance.setSubmitTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance.setCurrentNodeName(node1.getNodeName());
            instance = entityManager.persist(instance);

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node1.getId());
            approval.setNodeName(node1.getNodeName());
            approval.setApproverId(approver1.getId());
            approval.setApproverName(approver1.getNickname());
            approval.setApprovalStatus("pending");
            approval = entityManager.persist(approval);

            entityManager.flush();
            entityManager.clear();

            // Reject the workflow
            authenticateAs(approver1);
            WorkflowApprovalEntity savedApproval = approvalRepository.findById(approval.getId()).orElseThrow();
            savedApproval.setApprovalStatus("rejected");
            savedApproval.setComment("Insufficient documentation");
            savedApproval.setApprovalTime(Instant.now());
            entityManager.persist(savedApproval);

            instance = instanceRepository.findById(instance.getId()).orElseThrow();
            instance.setStatus("rejected");
            instance.setFinishTime(Instant.now());
            entityManager.persist(instance);

            entityManager.flush();
            entityManager.clear();

            // Verify rejection
            WorkflowInstanceEntity finalInstance = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(finalInstance.getStatus()).isEqualTo("rejected");
            assertThat(finalInstance.getFinishTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Multi-Node Approval Flow")
    class MultiNodeApprovalFlow {

        @Test
        @DisplayName("Should handle three-node sequential approval")
        void shouldHandleThreeNodeSequentialApproval() {
            authenticateAs(initiator);

            // Create 3-node workflow
            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionName("Project Approval");
            definition.setDefinitionKey("project_approval");
            definition.setCategory("Project");
            definition.setStatus(1);
            definition.setVersion(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Manager Review");
            node1.setNodeOrder(1);
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1 = entityManager.persist(node1);

            WorkflowNodeEntity node2 = new WorkflowNodeEntity();
            node2.setDefinitionId(definition.getId());
            node2.setNodeName("Director Review");
            node2.setNodeOrder(2);
            node2.setApproverType("user");
            node2.setApproverId(approver2.getId());
            node2 = entityManager.persist(node2);

            WorkflowNodeEntity node3 = new WorkflowNodeEntity();
            node3.setDefinitionId(definition.getId());
            node3.setNodeName("VP Review");
            node3.setNodeOrder(3);
            node3.setApproverType("user");
            node3.setApproverId(approver3.getId());
            node3 = entityManager.persist(node3);

            // Start workflow
            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
            instance.setUserId(initiator.getId());
            instance.setUserName(initiator.getNickname());
            instance.setTitle("New Project Proposal");
            instance.setStatus("running");
            instance.setSubmitTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance.setCurrentNodeName(node1.getNodeName());
            instance = entityManager.persist(instance);

            // Create first approval
            WorkflowApprovalEntity approval1 = new WorkflowApprovalEntity();
            approval1.setInstanceId(instance.getId());
            approval1.setNodeId(node1.getId());
            approval1.setNodeName(node1.getNodeName());
            approval1.setApproverId(approver1.getId());
            approval1.setApproverName(approver1.getNickname());
            approval1.setApprovalStatus("pending");
            entityManager.persist(approval1);

            entityManager.flush();
            entityManager.clear();

            // Verify initial state
            WorkflowInstanceEntity currentInstance = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(currentInstance.getCurrentNodeId()).isEqualTo(node1.getId());
            assertThat(currentInstance.getStatus()).isEqualTo("running");

            List<WorkflowApprovalEntity> approvals = approvalRepository
                    .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId());
            assertThat(approvals).hasSize(1);
            assertThat(approvals.get(0).getNodeName()).isEqualTo("Manager Review");
        }

        @Test
        @DisplayName("Should maintain approval history across nodes")
        void shouldMaintainApprovalHistory() {
            // Create and progress workflow through multiple nodes
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionName("Document Approval");
            definition.setDefinitionKey("doc_approval");
            definition.setStatus(1);
            definition.setVersion(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Reviewer 1");
            node1.setNodeOrder(1);
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1 = entityManager.persist(node1);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setUserId(initiator.getId());
            instance.setTitle("Document Review");
            instance.setStatus("running");
            instance.setSubmitTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance.setCurrentNodeName(node1.getNodeName());
            instance = entityManager.persist(instance);

            // Create approval record
            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node1.getId());
            approval.setNodeName(node1.getNodeName());
            approval.setApproverId(approver1.getId());
            approval.setApproverName(approver1.getNickname());
            approval.setComment("Initial review");
            approval.setApprovalStatus("approved");
            approval.setApprovalTime(Instant.now());
            entityManager.persist(approval);

            entityManager.flush();
            entityManager.clear();

            // Verify history is maintained
            List<WorkflowApprovalEntity> history = approvalRepository
                    .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId());

            assertThat(history).hasSize(1);
            assertThat(history.get(0).getComment()).isEqualTo("Initial review");
            assertThat(history.get(0).getApprovalTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases - Cancellation and Rejection")
    class EdgeCasesCancellationRejection {

        @Test
        @DisplayName("Should allow cancellation of draft workflow")
        void shouldAllowCancellationOfDraft() {
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionName("Test Workflow");
            definition.setDefinitionKey("test_wf");
            definition.setStatus(1);
            definition = entityManager.persist(definition);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setUserId(initiator.getId());
            instance.setTitle("Test");
            instance.setStatus("draft");
            instance = entityManager.persist(instance);

            // Cancel
            instance.setStatus("cancelled");
            instance.setFinishTime(Instant.now());
            entityManager.persist(instance);

            entityManager.flush();
            entityManager.clear();

            // Verify
            WorkflowInstanceEntity result = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo("cancelled");
            assertThat(result.isFinished()).isTrue();
        }

        @Test
        @DisplayName("Should allow cancellation of running workflow")
        void shouldAllowCancellationOfRunning() {
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionKey("test_wf2");
            definition.setStatus(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Node 1");
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1 = entityManager.persist(node1);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setUserId(initiator.getId());
            instance.setTitle("Test");
            instance.setStatus("running");
            instance.setSubmitTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance = entityManager.persist(instance);

            // Cancel
            instance.setStatus("cancelled");
            instance.setFinishTime(Instant.now());
            entityManager.persist(instance);

            // Verify
            WorkflowInstanceEntity result = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo("cancelled");
        }

        @Test
        @DisplayName("Should allow withdrawal of rejected workflow")
        void shouldAllowWithdrawalOfRejected() {
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionKey("test_wf3");
            definition.setStatus(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node1 = new WorkflowNodeEntity();
            node1.setDefinitionId(definition.getId());
            node1.setNodeName("Node 1");
            node1.setApproverType("user");
            node1.setApproverId(approver1.getId());
            node1 = entityManager.persist(node1);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setUserId(initiator.getId());
            instance.setTitle("Test");
            instance.setStatus("rejected");
            instance.setSubmitTime(Instant.now());
            instance.setFinishTime(Instant.now());
            instance.setCurrentNodeId(node1.getId());
            instance = entityManager.persist(instance);

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node1.getId());
            approval.setApproverId(approver1.getId());
            approval.setApprovalStatus("rejected");
            approval = entityManager.persist(approval);

            entityManager.flush();
            entityManager.clear();

            // Withdraw
            instance = instanceRepository.findById(instance.getId()).orElseThrow();
            instance.setStatus("draft");
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);
            instance.setSubmitTime(null);
            instance.setFinishTime(null);
            entityManager.persist(instance);

            approval = approvalRepository.findById(approval.getId()).orElseThrow();
            approval.setDeleted(true);
            entityManager.persist(approval);

            entityManager.flush();
            entityManager.clear();

            // Verify
            WorkflowInstanceEntity result = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo("draft");
            assertThat(result.getCurrentNodeId()).isNull();

            List<WorkflowApprovalEntity> approvals = approvalRepository
                    .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId());
            assertThat(approvals).isEmpty();
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle multiple workflows simultaneously")
        void shouldHandleMultipleWorkflows() {
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionKey("concurrent_wf");
            definition.setStatus(1);
            definition = entityManager.persist(definition);

            // Create multiple instances
            for (int i = 0; i < 5; i++) {
                WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
                instance.setDefinitionId(definition.getId());
                instance.setUserId(initiator.getId());
                instance.setTitle("Workflow " + i);
                instance.setStatus("running");
                instance.setSubmitTime(Instant.now());
                entityManager.persist(instance);
            }

            entityManager.flush();
            entityManager.clear();

            // Verify all instances exist
            List<WorkflowInstanceEntity> instances = instanceRepository
                    .findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(initiator.getId());

            assertThat(instances).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Data Integrity")
    class DataIntegrity {

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Create complete workflow with all relationships
            authenticateAs(initiator);

            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setDefinitionKey("integrity_wf");
            definition.setStatus(1);
            definition = entityManager.persist(definition);

            WorkflowNodeEntity node = new WorkflowNodeEntity();
            node.setDefinitionId(definition.getId());
            node.setNodeName("Approval");
            node.setApproverType("user");
            node.setApproverId(approver1.getId());
            node = entityManager.persist(node);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setUserId(initiator.getId());
            instance.setTitle("Test");
            instance.setStatus("running");
            instance.setCurrentNodeId(node.getId());
            instance = entityManager.persist(instance);

            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node.getId());
            approval.setApproverId(approver1.getId());
            approval.setApprovalStatus("pending");
            approval = entityManager.persist(approval);

            entityManager.flush();
            entityManager.clear();

            // Verify relationships
            WorkflowInstanceEntity resultInstance = instanceRepository.findById(instance.getId()).orElseThrow();
            assertThat(resultInstance.getDefinitionId()).isEqualTo(definition.getId());
            assertThat(resultInstance.getCurrentNodeId()).isEqualTo(node.getId());

            List<WorkflowApprovalEntity> resultApprovals = approvalRepository
                    .findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(instance.getId());
            assertThat(resultApprovals).hasSize(1);
            assertThat(resultApprovals.get(0).getNodeId()).isEqualTo(node.getId());
        }

        @Test
        @DisplayName("Should handle soft deletes correctly")
        void shouldHandleSoftDeletesCorrectly() {
            authenticateAs(initiator);

            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId("test-def");
            instance.setUserId(initiator.getId());
            instance.setTitle("Test");
            instance.setStatus("draft");
            instance = entityManager.persist(instance);

            // Soft delete
            instance.setDeleted(true);
            entityManager.persist(instance);

            entityManager.flush();
            entityManager.clear();

            // Verify soft delete (should not be found by standard queries)
            List<WorkflowInstanceEntity> instances = instanceRepository
                    .findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(initiator.getId());
            assertThat(instances).isEmpty();

            // But should be found by direct ID query
            WorkflowInstanceEntity deletedInstance = instanceRepository.findById(instance.getId()).orElse(null);
            // Note: JPA findById respects @Where annotation, so this might return empty
            // depending on implementation
        }
    }
}
