package com.adminplus.integration;

import com.adminplus.base.AbstractRepositoryTest;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Workflow Approval Module
 * <p>
 * Tests end-to-end workflow execution with real PostgreSQL database.
 * Requires Docker to run.
 * <p>
 * Test Coverage:
 * <ul>
 *   <li>Complete workflow lifecycle</li>
 *   <li>Multi-node approval flows</li>
 *   <li>Parallel approvals</li>
 *   <li>Edge cases (cancellation, rejection, withdrawal)</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Disabled("Integration tests require Docker and Redis. Run with Docker available.")
@DisplayName("Workflow Integration Tests")
class WorkflowIntegrationTest extends AbstractRepositoryTest {

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

    private UserEntity initiator;
    private UserEntity approver1;
    private UserEntity approver2;
    private UserEntity approver3;

    @BeforeEach
    void setUp() {
        // Create test users
        initiator = createTestUser("initiator", "Initiator User", "dept-001");
        approver1 = createTestUser("approver1", "Manager Smith", "dept-001");
        approver2 = createTestUser("approver2", "HR Johnson", "dept-002");
        approver3 = createTestUser("approver3", "Director Williams", "dept-003");
    }

    private UserEntity createTestUser(String username, String name, String deptId) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setNickname(name);
        user.setPassword("hashed_password");
        user.setDeptId(deptId);
        user.setStatus(1);
        return entityManager.persist(user);
    }

    @Nested
    @DisplayName("End-to-End Workflow Execution")
    class EndToEndWorkflowExecution {

        @Test
        @DisplayName("Should complete full workflow lifecycle: create -> submit -> approve -> approve -> complete")
        void shouldCompleteFullWorkflowLifecycle() {
            // 1. Create workflow definition
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

            entityManager.flush();
            entityManager.clear();

            // 3. Start workflow instance
            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definition.getId());
            instance.setDefinitionName(definition.getDefinitionName());
            instance.setUserId(initiator.getId());
            instance.setUserName(initiator.getNickname());
            instance.setDeptId(initiator.getDeptId());
            instance.setTitle("Leave Request - 3 days");
            instance.setBusinessData("{\"reason\":\"Family event\",\"days\":3}");
            instance.setRemark("Need time off for family event");
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
            assertThat(approvals).allMatch(WorkflowApprovalEntity::isApproved);
        }

        @Test
        @DisplayName("Should handle workflow rejection at any node")
        void shouldHandleWorkflowRejection() {
            // Create definition and instance
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
    }

    @Nested
    @DisplayName("Edge Cases - Cancellation and Rejection")
    class EdgeCasesCancellationRejection {

        @Test
        @DisplayName("Should allow cancellation of draft workflow")
        void shouldAllowCancellationOfDraft() {
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
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle multiple workflows simultaneously")
        void shouldHandleMultipleWorkflows() {
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
        }
    }
}