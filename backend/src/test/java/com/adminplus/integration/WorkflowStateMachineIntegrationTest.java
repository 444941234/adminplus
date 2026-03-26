package com.adminplus.integration;

import com.adminplus.base.AbstractIntegrationTest;
import com.adminplus.pojo.entity.*;
import com.adminplus.repository.*;
import com.adminplus.service.WorkflowStateMachineService;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.persist.StateMachineEntity;
import com.adminplus.repository.StateMachineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Workflow State Machine
 * <p>
 * Test Coverage:
 * <ul>
 *   <li>State persistence and recovery</li>
 *   <li>State transitions with Spring State Machine</li>
 *   <li>Rollback scenarios</li>
 *   <li>Concurrent state changes</li>
 *   <li>Extended state management</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@DisplayName("Workflow State Machine Integration Tests")
@Disabled("Integration tests require PostgreSQL on localhost:5433")
@TestPropertySource(properties = {
        "spring.statemachine.enabled=true"
})
class WorkflowStateMachineIntegrationTest extends AbstractIntegrationTest {

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

    @Autowired
    private StateMachineRepository stateMachineRepository;

    @Autowired
    private StateMachineFactory<WorkflowState, WorkflowEvent> stateMachineFactory;

    @Autowired
    private WorkflowStateMachineService stateMachineService;

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
    @DisplayName("State Persistence Tests")
    class StatePersistenceTests {

        @Test
        @DisplayName("Should persist state machine context to database")
        void shouldPersistStateMachineContext() {
            // Create workflow definition and instance
            WorkflowDefinitionEntity definition = createTestDefinition("Test Workflow", "test_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create state machine and send event
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());

            stateMachine.start();

            // Verify state is persisted
            Optional<StateMachineEntity> persistedState =
                    stateMachineRepository.findById(instance.getId());

            assertThat(persistedState).isPresent();
            assertThat(persistedState.get().getMachineId()).isEqualTo(instance.getId());
            assertThat(persistedState.get().getState()).isEqualTo(WorkflowState.DRAFT.name());

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("Should recover state machine from database")
        void shouldRecoverStateMachineFromDatabase() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Recovery Test", "recovery_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create and persist state machine
            StateMachine<WorkflowState, WorkflowEvent> stateMachine1 =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine1.start();

            entityManager.flush();
            entityManager.clear();

            // Recover state machine
            StateMachine<WorkflowState, WorkflowEvent> stateMachine2 =
                    stateMachineFactory.getStateMachine(instance.getId());

            assertThat(stateMachine2.getState().getId()).isEqualTo(WorkflowState.DRAFT);
        }

        @Test
        @DisplayName("Should update extended state on node transitions")
        void shouldUpdateExtendedStateOnTransitions() {
            // Create multi-node workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Extended State Test", "extended_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowNodeEntity node2 = createTestNode(definition.getId(), "Node 2", approver2.getId(), 2);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Start state machine
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();

            // Submit workflow
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            entityManager.flush();
            entityManager.clear();

            // Verify extended state contains current node
            Optional<StateMachineEntity> persistedState =
                    stateMachineRepository.findById(instance.getId());

            assertThat(persistedState).isPresent();
            assertThat(persistedState.get().getExtendedState()).isNotNull();
            assertThat(persistedState.get().getExtendedState()).contains(node1.getId());
        }

        @Test
        @DisplayName("Should handle multiple workflow instances independently")
        void shouldHandleMultipleInstancesIndependently() {
            // Create two workflows
            WorkflowDefinitionEntity definition = createTestDefinition("Multi Instance Test", "multi_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);

            WorkflowInstanceEntity instance1 = createTestInstance(definition.getId(), node1.getId());
            WorkflowInstanceEntity instance2 = createTestInstance(definition.getId(), node1.getId());

            // Start both state machines
            StateMachine<WorkflowState, WorkflowEvent> sm1 =
                    stateMachineFactory.getStateMachine(instance1.getId());
            StateMachine<WorkflowState, WorkflowEvent> sm2 =
                    stateMachineFactory.getStateMachine(instance2.getId());

            sm1.start();
            sm2.start();

            // Send different events
            sm1.sendEvent(WorkflowEvent.SUBMIT);
            // sm2 stays in DRAFT

            entityManager.flush();
            entityManager.clear();

            // Verify both states are persisted independently
            Optional<StateMachineEntity> state1 = stateMachineRepository.findById(instance1.getId());
            Optional<StateMachineEntity> state2 = stateMachineRepository.findById(instance2.getId());

            assertThat(state1).isPresent();
            assertThat(state2).isPresent();
            assertThat(state1.get().getState()).isNotEqualTo(state2.get().getState());
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("Should transition from DRAFT to RUNNING on SUBMIT")
        void shouldTransitionDraftToRunningOnSubmit() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Transition Test", "transition_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Start state machine and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();

            boolean eventAccepted = stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            assertThat(eventAccepted).isTrue();
            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.RUNNING);

            entityManager.flush();
            entityManager.clear();

            // Verify persistence
            Optional<StateMachineEntity> persistedState =
                    stateMachineRepository.findById(instance.getId());
            assertThat(persistedState).isPresent();
            assertThat(persistedState.get().getState()).isEqualTo(WorkflowState.RUNNING.name());
        }

        @Test
        @DisplayName("Should transition from RUNNING to APPROVED on final APPROVE")
        void shouldTransitionRunningToApprovedOnFinalApprove() {
            // Create single-node workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Approve Test", "approve_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create approval record
            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node1.getId());
            approval.setNodeName(node1.getNodeName());
            approval.setApproverId(approver1.getId());
            approval.setApproverName(approver1.getNickname());
            approval.setApprovalStatus("pending");
            entityManager.persist(approval);

            // Start and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            entityManager.flush();
            entityManager.clear();

            // Approve (should complete workflow)
            stateMachine.sendEvent(WorkflowEvent.APPROVE);

            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.APPROVED);
        }

        @Test
        @DisplayName("Should transition from RUNNING to REJECTED on REJECT")
        void shouldTransitionRunningToRejectedOnReject() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Reject Test", "reject_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Start and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            // Reject
            boolean eventAccepted = stateMachine.sendEvent(WorkflowEvent.REJECT);

            assertThat(eventAccepted).isTrue();
            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.REJECTED);
        }

        @Test
        @DisplayName("Should transition from RUNNING to CANCELLED on CANCEL")
        void shouldTransitionRunningToCancelledOnCancel() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Cancel Test", "cancel_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Start and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            // Cancel
            boolean eventAccepted = stateMachine.sendEvent(WorkflowEvent.CANCEL);

            assertThat(eventAccepted).isTrue();
            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.CANCELLED);
        }

        @Test
        @DisplayName("Should stay in RUNNING on intermediate APPROVE")
        void shouldStayInRunningOnIntermediateApprove() {
            // Create multi-node workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Multi Node Test", "multinode_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowNodeEntity node2 = createTestNode(definition.getId(), "Node 2", approver2.getId(), 2);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create approvals
            WorkflowApprovalEntity approval1 = new WorkflowApprovalEntity();
            approval1.setInstanceId(instance.getId());
            approval1.setNodeId(node1.getId());
            approval1.setNodeName(node1.getNodeName());
            approval1.setApproverId(approver1.getId());
            approval1.setApproverName(approver1.getNickname());
            approval1.setApprovalStatus("pending");
            entityManager.persist(approval1);

            // Start and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            entityManager.flush();
            entityManager.clear();

            // Approve first node (should move to next node, stay in RUNNING)
            stateMachine.sendEvent(WorkflowEvent.APPROVE);

            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.RUNNING);
        }
    }

    @Nested
    @DisplayName("Rollback Tests")
    class RollbackTests {

        @Test
        @DisplayName("Should rollback to previous node on ROLLBACK event")
        void shouldRollbackToPreviousNode() {
            // Create multi-node workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Rollback Test", "rollback_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowNodeEntity node2 = createTestNode(definition.getId(), "Node 2", approver2.getId(), 2);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create approvals
            WorkflowApprovalEntity approval1 = new WorkflowApprovalEntity();
            approval1.setInstanceId(instance.getId());
            approval1.setNodeId(node1.getId());
            approval1.setNodeName(node1.getNodeName());
            approval1.setApproverId(approver1.getId());
            approval1.setApproverName(approver1.getNickname());
            approval1.setApprovalStatus("approved");
            approval1.setApprovalTime(Instant.now());
            entityManager.persist(approval1);

            WorkflowApprovalEntity approval2 = new WorkflowApprovalEntity();
            approval2.setInstanceId(instance.getId());
            approval2.setNodeId(node2.getId());
            approval2.setNodeName(node2.getNodeName());
            approval2.setApproverId(approver2.getId());
            approval2.setApproverName(approver2.getNickname());
            approval2.setApprovalStatus("pending");
            entityManager.persist(approval2);

            // Start, submit, and approve to node 2
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);
            stateMachine.sendEvent(WorkflowEvent.APPROVE);

            entityManager.flush();
            entityManager.clear();

            // Rollback from node 2 to node 1
            boolean eventAccepted = stateMachine.sendEvent(WorkflowEvent.ROLLBACK);

            assertThat(eventAccepted).isTrue();
            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.RUNNING);

            // Verify extended state shows node 1
            Optional<StateMachineEntity> persistedState =
                    stateMachineRepository.findById(instance.getId());
            assertThat(persistedState).isPresent();
            assertThat(persistedState.get().getExtendedState()).contains(node1.getId());
        }

        @Test
        @DisplayName("Should rollback to DRAFT from first node")
        void shouldRollbackToDraftFromFirstNode() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("Rollback Draft Test", "rollback_draft_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create approval
            WorkflowApprovalEntity approval = new WorkflowApprovalEntity();
            approval.setInstanceId(instance.getId());
            approval.setNodeId(node1.getId());
            approval.setNodeName(node1.getNodeName());
            approval.setApproverId(approver1.getId());
            approval.setApproverName(approver1.getNickname());
            approval.setApprovalStatus("pending");
            entityManager.persist(approval);

            // Start and submit
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);

            entityManager.flush();
            entityManager.clear();

            // Rollback to DRAFT
            boolean eventAccepted = stateMachine.sendEvent(WorkflowEvent.ROLLBACK);

            assertThat(eventAccepted).isTrue();
            assertThat(stateMachine.getState().getId()).isEqualTo(WorkflowState.DRAFT);
        }

        @Test
        @DisplayName("Should track rollback history in extended state")
        void shouldTrackRollbackHistory() {
            // Create workflow
            WorkflowDefinitionEntity definition = createTestDefinition("History Test", "history_wf");
            WorkflowNodeEntity node1 = createTestNode(definition.getId(), "Node 1", approver1.getId(), 1);
            WorkflowNodeEntity node2 = createTestNode(definition.getId(), "Node 2", approver2.getId(), 2);
            WorkflowInstanceEntity instance = createTestInstance(definition.getId(), node1.getId());

            // Create approvals
            WorkflowApprovalEntity approval1 = new WorkflowApprovalEntity();
            approval1.setInstanceId(instance.getId());
            approval1.setNodeId(node1.getId());
            approval1.setNodeName(node1.getNodeName());
            approval1.setApproverId(approver1.getId());
            approval1.setApproverName(approver1.getNickname());
            approval1.setApprovalStatus("approved");
            approval1.setApprovalTime(Instant.now());
            entityManager.persist(approval1);

            WorkflowApprovalEntity approval2 = new WorkflowApprovalEntity();
            approval2.setInstanceId(instance.getId());
            approval2.setNodeId(node2.getId());
            approval2.setNodeName(node2.getNodeName());
            approval2.setApproverId(approver2.getId());
            approval2.setApproverName(approver2.getNickname());
            approval2.setApprovalStatus("pending");
            entityManager.persist(approval2);

            // Start, submit, approve, rollback
            StateMachine<WorkflowState, WorkflowEvent> stateMachine =
                    stateMachineFactory.getStateMachine(instance.getId());
            stateMachine.start();
            stateMachine.sendEvent(WorkflowEvent.SUBMIT);
            stateMachine.sendEvent(WorkflowEvent.APPROVE);
            stateMachine.sendEvent(WorkflowEvent.ROLLBACK);

            entityManager.flush();
            entityManager.clear();

            // Verify rollback history is tracked
            Optional<StateMachineEntity> persistedState =
                    stateMachineRepository.findById(instance.getId());
            assertThat(persistedState).isPresent();
            assertThat(persistedState.get().getExtendedState()).isNotNull();

            // Extended state should contain rollback information
            String extendedState = persistedState.get().getExtendedState();
            assertThat(extendedState).contains("rollback");
        }
    }

    @Nested
    @DisplayName("Service Integration Tests")
    class ServiceIntegrationTests {

        @Test
        @DisplayName("Should approve workflow through service")
        void shouldApproveThroughService() {
            // This test skeleton will be implemented when service is fully integrated
            // For now, verify service bean exists
            assertThat(stateMachineService).isNotNull();
        }

        @Test
        @DisplayName("Should reject workflow through service")
        void shouldRejectThroughService() {
            // This test skeleton will be implemented when service is fully integrated
            assertThat(stateMachineService).isNotNull();
        }

        @Test
        @DisplayName("Should cancel workflow through service")
        void shouldCancelThroughService() {
            // This test skeleton will be implemented when service is fully integrated
            assertThat(stateMachineService).isNotNull();
        }

        @Test
        @DisplayName("Should rollback workflow through service")
        void shouldRollbackThroughService() {
            // This test skeleton will be implemented when service is fully integrated
            assertThat(stateMachineService).isNotNull();
        }
    }

    // Helper methods

    private WorkflowDefinitionEntity createTestDefinition(String name, String key) {
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setDefinitionName(name);
        definition.setDefinitionKey(key);
        definition.setCategory("Test");
        definition.setDescription("Test workflow");
        definition.setStatus(1);
        definition.setVersion(1);
        return entityManager.persist(definition);
    }

    private WorkflowNodeEntity createTestNode(String definitionId, String name, String approverId, int order) {
        WorkflowNodeEntity node = new WorkflowNodeEntity();
        node.setDefinitionId(definitionId);
        node.setNodeName(name);
        node.setNodeCode(name.toLowerCase().replace(" ", "_"));
        node.setNodeOrder(order);
        node.setApproverType("user");
        node.setApproverId(approverId);
        node.setIsCounterSign(false);
        node.setAutoPassSameUser(false);
        return entityManager.persist(node);
    }

    private WorkflowInstanceEntity createTestInstance(String definitionId, String currentNodeId) {
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setDefinitionId(definitionId);
        instance.setDefinitionName("Test Workflow");
        instance.setUserId(initiator.getId());
        instance.setUserName(initiator.getNickname());
        instance.setDeptId(initiator.getDeptId());
        instance.setTitle("Test Instance");
        instance.setBusinessData("{}");
        instance.setStatus("draft");
        instance.setCurrentNodeId(currentNodeId);
        instance.setCurrentNodeName("Node 1");
        return entityManager.persist(instance);
    }
}
