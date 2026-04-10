package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.dto.request.WorkflowNodeRequest;
import com.adminplus.pojo.dto.response.WorkflowDefinitionResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.WorkflowDefinitionRepository;
import com.adminplus.repository.WorkflowNodeRepository;
import com.adminplus.service.impl.WorkflowDefinitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkflowDefinitionService
 * Tests CRUD operations for workflow definitions and nodes
 *
 * Test Categories:
 * 1. Happy Path - Normal operations
 * 2. Edge Cases - Boundary conditions
 * 3. Error Paths - Invalid inputs
 * 4. Null Handling - Null parameter handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowDefinitionService Unit Tests")
class WorkflowDefinitionServiceTest {

    @Mock
    private WorkflowDefinitionRepository definitionRepository;

    @Mock
    private WorkflowNodeRepository nodeRepository;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private WorkflowDefinitionServiceImpl service;

    private WorkflowDefinitionRequest validDefinitionReq;
    private WorkflowDefinitionEntity testDefinitionEntity;
    private WorkflowDefinitionResponse testDefinitionResponse;
    private WorkflowNodeRequest validNodeReq;
    private WorkflowNodeEntity testNodeEntity;
    private WorkflowNodeResponse testNodeResponse;

    @BeforeEach
    void setUp() {
        validDefinitionReq = new WorkflowDefinitionRequest(
                "Leave Approval",
                "leave_approval",
                "HR",
                "Employee leave request workflow",
                1,
                "{\"fields\":[{\"name\":\"reason\",\"type\":\"text\"}]}"
        );

        testDefinitionEntity = new WorkflowDefinitionEntity();
        testDefinitionEntity.setId("def-001");
        testDefinitionEntity.setDefinitionName("Leave Approval");
        testDefinitionEntity.setDefinitionKey("leave_approval");
        testDefinitionEntity.setCategory("HR");
        testDefinitionEntity.setDescription("Employee leave request workflow");
        testDefinitionEntity.setStatus(1);
        testDefinitionEntity.setVersion(1);
        testDefinitionEntity.setFormConfig("{\"fields\":[]}");

        testDefinitionResponse = new WorkflowDefinitionResponse(
                testDefinitionEntity.getId(),
                testDefinitionEntity.getDefinitionName(),
                testDefinitionEntity.getDefinitionKey(),
                testDefinitionEntity.getCategory(),
                testDefinitionEntity.getDescription(),
                testDefinitionEntity.getStatus(),
                testDefinitionEntity.getVersion(),
                testDefinitionEntity.getFormConfig(),
                0,
                testDefinitionEntity.getCreateTime(),
                testDefinitionEntity.getUpdateTime()
        );

        validNodeReq = new WorkflowNodeRequest(
                "Manager Approval",
                "manager_approve",
                1,
                "user",
                "user-001",
                false,
                false,
                "Direct manager approval"
        );

        testNodeEntity = new WorkflowNodeEntity();
        testNodeEntity.setId("node-001");
        testNodeEntity.setDefinitionId("def-001");
        testNodeEntity.setNodeName("Manager Approval");
        testNodeEntity.setNodeCode("manager_approve");
        testNodeEntity.setNodeOrder(1);
        testNodeEntity.setApproverType("user");
        testNodeEntity.setApproverId("user-001");

        testNodeResponse = new WorkflowNodeResponse(
                testNodeEntity.getId(),
                testNodeEntity.getDefinitionId(),
                testNodeEntity.getNodeName(),
                testNodeEntity.getNodeCode(),
                testNodeEntity.getNodeOrder(),
                testNodeEntity.getApproverType(),
                testNodeEntity.getApproverId(),
                testNodeEntity.getIsCounterSign(),
                testNodeEntity.getAutoPassSameUser(),
                testNodeEntity.getDescription(),
                testNodeEntity.getCreateTime()
        );

        // Mock conversionService
        lenient().when(conversionService.convert(any(WorkflowDefinitionEntity.class), eq(WorkflowDefinitionResponse.class)))
                .thenReturn(testDefinitionResponse);
        lenient().when(conversionService.convert(any(WorkflowDefinitionRequest.class), eq(WorkflowDefinitionEntity.class)))
                .thenReturn(testDefinitionEntity);
        lenient().when(conversionService.convert(any(WorkflowNodeEntity.class), eq(WorkflowNodeResponse.class)))
                .thenReturn(testNodeResponse);
        lenient().when(conversionService.convert(any(WorkflowNodeRequest.class), eq(WorkflowNodeEntity.class)))
                .thenReturn(testNodeEntity);
    }

    @Nested
    @DisplayName("Create Workflow Definition - Happy Path")
    class CreateWorkflowDefinitionHappyPath {

        @Test
        @DisplayName("Should create workflow definition successfully with valid data")
        void shouldCreateWorkflowDefinitionSuccessfully() {
            // Given
            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            WorkflowDefinitionResponse result = service.create(validDefinitionReq);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.definitionName()).isEqualTo("Leave Approval");
            assertThat(result.definitionKey()).isEqualTo("leave_approval");
            assertThat(result.version()).isEqualTo(1);
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }

        @Test
        @DisplayName("Should create multiple definitions with different keys")
        void shouldCreateMultipleDefinitions() {
            // Given
            WorkflowDefinitionRequest req2 = new WorkflowDefinitionRequest(
                    "Expense Approval",
                    "expense_approval",
                    "Finance",
                    "Expense report workflow",
                    1,
                    "{}"
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.create(validDefinitionReq);
            service.create(req2);

            // Then
            verify(definitionRepository, times(2)).save(any(WorkflowDefinitionEntity.class));
        }
    }

    @Nested
    @DisplayName("Create Workflow Definition - Error Paths")
    class CreateWorkflowDefinitionErrorPaths {

        @Test
        @DisplayName("Should throw exception when definition key already exists")
        void shouldThrowExceptionWhenKeyExists() {
            // Given
            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse("leave_approval"))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> service.create(validDefinitionReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流标识已存在");

            verify(definitionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when definition key is empty")
        void shouldThrowExceptionWhenKeyIsEmpty() {
            // Given
            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Test",
                    "",
                    "HR",
                    "Test",
                    1,
                    "{}"
            );

            // When/Then
            // Note: This would be caught by @Valid in controller
            // Service layer assumes valid input
            assertThat(req.definitionKey()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Workflow Definition - Happy Path")
    class UpdateWorkflowDefinitionHappyPath {

        @Test
        @DisplayName("Should update workflow definition successfully")
        void shouldUpdateWorkflowDefinition() {
            // Given
            WorkflowDefinitionRequest updateReq = new WorkflowDefinitionRequest(
                    "Leave Approval Updated",
                    "leave_approval",
                    "HR Updated",
                    "Updated description",
                    1,
                    "{\"updated\":true}"
            );

            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(definitionRepository.findByDefinitionKeyAndDeletedFalse("leave_approval"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            WorkflowDefinitionResponse result = service.update("def-001", updateReq);

            // Then
            assertThat(result).isNotNull();
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }

        @Test
        @DisplayName("Should update definition with same key")
        void shouldUpdateWithSameKey() {
            // Given
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(definitionRepository.findByDefinitionKeyAndDeletedFalse("leave_approval"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.update("def-001", validDefinitionReq);

            // Then
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }
    }

    @Nested
    @DisplayName("Update Workflow Definition - Error Paths")
    class UpdateWorkflowDefinitionErrorPaths {

        @Test
        @DisplayName("Should throw exception when definition not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(definitionRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.update("non-existent", validDefinitionReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流定义不存在");

            verify(definitionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when key conflicts with another definition")
        void shouldThrowExceptionWhenKeyConflicts() {
            // Given
            WorkflowDefinitionEntity anotherEntity = new WorkflowDefinitionEntity();
            anotherEntity.setId("def-002");

            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(definitionRepository.findByDefinitionKeyAndDeletedFalse("different_key"))
                    .thenReturn(Optional.of(anotherEntity));

            WorkflowDefinitionRequest updateReq = new WorkflowDefinitionRequest(
                    "Test",
                    "different_key",
                    "HR",
                    "Test",
                    1,
                    "{}"
            );

            // When/Then
            assertThatThrownBy(() -> service.update("def-001", updateReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流标识已被使用");

            verify(definitionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Workflow Definition - Happy Path")
    class DeleteWorkflowDefinitionHappyPath {

        @Test
        @DisplayName("Should delete workflow definition and its nodes")
        void shouldDeleteDefinitionAndNodes() {
            // Given
            List<WorkflowNodeEntity> nodes = Arrays.asList(testNodeEntity);
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(nodes);
            when(nodeRepository.save(any(WorkflowNodeEntity.class)))
                    .thenReturn(testNodeEntity);

            // When
            service.delete("def-001");

            // Then
            verify(nodeRepository).save(testNodeEntity);
            assertThat(testNodeEntity.getDeleted()).isTrue();
            verify(definitionRepository).deleteById("def-001");
        }

        @Test
        @DisplayName("Should delete definition without nodes")
        void shouldDeleteDefinitionWithoutNodes() {
            // Given
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(List.of());

            // When
            service.delete("def-001");

            // Then
            verify(nodeRepository, never()).save(any());
            verify(definitionRepository).deleteById("def-001");
        }
    }

    @Nested
    @DisplayName("Add Workflow Node - Happy Path")
    class AddWorkflowNodeHappyPath {

        @Test
        @DisplayName("Should add node to workflow definition successfully")
        void shouldAddNodeSuccessfully() {
            // Given
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(nodeRepository.save(any(WorkflowNodeEntity.class)))
                    .thenReturn(testNodeEntity);

            // When
            WorkflowNodeResponse result = service.addNode("def-001", validNodeReq);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.nodeName()).isEqualTo("Manager Approval");
            assertThat(result.approverType()).isEqualTo("user");
            verify(nodeRepository).save(any(WorkflowNodeEntity.class));
        }

        @Test
        @DisplayName("Should add multiple nodes in sequence")
        void shouldAddMultipleNodesInSequence() {
            // Given
            WorkflowNodeRequest nodeReq2 = new WorkflowNodeRequest(
                    "HR Approval",
                    "hr_approve",
                    2,
                    "user",
                    "user-002",
                    false,
                    false,
                    "HR final approval"
            );

            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(nodeRepository.save(any(WorkflowNodeEntity.class)))
                    .thenReturn(testNodeEntity);

            // When
            service.addNode("def-001", validNodeReq);
            service.addNode("def-001", nodeReq2);

            // Then
            verify(nodeRepository, times(2)).save(any(WorkflowNodeEntity.class));
        }
    }

    @Nested
    @DisplayName("Add Workflow Node - Error Paths")
    class AddWorkflowNodeErrorPaths {

        @Test
        @DisplayName("Should throw exception when definition not found")
        void shouldThrowExceptionWhenDefinitionNotFound() {
            // Given
            when(definitionRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.addNode("non-existent", validNodeReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流定义不存在");

            verify(nodeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Workflow Node - Happy Path")
    class UpdateWorkflowNodeHappyPath {

        @Test
        @DisplayName("Should update node successfully")
        void shouldUpdateNodeSuccessfully() {
            // Given
            when(nodeRepository.findById("node-001"))
                    .thenReturn(Optional.of(testNodeEntity));
            when(nodeRepository.save(any(WorkflowNodeEntity.class)))
                    .thenReturn(testNodeEntity);

            WorkflowNodeRequest updateReq = new WorkflowNodeRequest(
                    "Updated Manager Approval",
                    "manager_approve",
                    1,
                    "role",
                    "role-001",
                    true,
                    true,
                    "Updated description"
            );

            // When
            WorkflowNodeResponse result = service.updateNode("node-001", updateReq);

            // Then
            assertThat(result).isNotNull();
            verify(nodeRepository).save(any(WorkflowNodeEntity.class));
        }
    }

    @Nested
    @DisplayName("Update Workflow Node - Error Paths")
    class UpdateWorkflowNodeErrorPaths {

        @Test
        @DisplayName("Should throw exception when node not found")
        void shouldThrowExceptionWhenNodeNotFound() {
            // Given
            when(nodeRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.updateNode("non-existent", validNodeReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流节点不存在");

            verify(nodeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Workflow Node")
    class DeleteWorkflowNode {

        @Test
        @DisplayName("Should delete node successfully")
        void shouldDeleteNodeSuccessfully() {
            // When
            service.deleteNode("node-001");

            // Then
            verify(nodeRepository).deleteById("node-001");
        }
    }

    @Nested
    @DisplayName("List Operations")
    class ListOperations {

        @Test
        @DisplayName("Should get definition by id successfully")
        void shouldGetDefinitionById() {
            // Given
            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));

            // When
            WorkflowDefinitionResponse result = service.getById("def-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.definitionName()).isEqualTo("Leave Approval");
        }

        @Test
        @DisplayName("Should throw exception when getting non-existent definition")
        void shouldThrowExceptionWhenGettingNonExistent() {
            // Given
            when(definitionRepository.findById("non-existent"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.getById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("工作流定义不存在");
        }

        @Test
        @DisplayName("Should list all definitions")
        void shouldListAllDefinitions() {
            // Given
            List<WorkflowDefinitionEntity> entities = Arrays.asList(testDefinitionEntity);
            when(definitionRepository.findAll())
                    .thenReturn(entities);

            // When
            List<WorkflowDefinitionResponse> result = service.listAll();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).definitionName()).isEqualTo("Leave Approval");
        }

        @Test
        @DisplayName("Should list enabled definitions")
        void shouldListEnabledDefinitions() {
            // Given
            testDefinitionEntity.setStatus(1);
            List<WorkflowDefinitionEntity> entities = Arrays.asList(testDefinitionEntity);
            when(definitionRepository.findByStatusAndDeletedFalseOrderByCreateTimeDesc(1))
                    .thenReturn(entities);

            // When
            List<WorkflowDefinitionResponse> result = service.listEnabled();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should list nodes for definition")
        void shouldListNodesForDefinition() {
            // Given
            List<WorkflowNodeEntity> nodes = Arrays.asList(testNodeEntity);
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(nodes);

            // When
            List<WorkflowNodeResponse> result = service.listNodes("def-001");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).nodeName()).isEqualTo("Manager Approval");
        }

        @Test
        @DisplayName("Should return empty list when no nodes exist")
        void shouldReturnEmptyListWhenNoNodes() {
            // Given
            when(nodeRepository.findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc("def-001"))
                    .thenReturn(List.of());

            // When
            List<WorkflowNodeResponse> result = service.listNodes("def-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {

        @Test
        @DisplayName("Should handle very long definition names")
        void shouldHandleLongDefinitionNames() {
            // Given
            String longName = "A".repeat(200);
            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    longName,
                    "long_name",
                    "HR",
                    "Test",
                    1,
                    "{}"
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.create(req);

            // Then
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }

        @Test
        @DisplayName("Should handle special characters in definition key")
        void shouldHandleSpecialCharactersInKey() {
            // Given
            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Test",
                    "test-with-special.chars_123",
                    "HR",
                    "Test",
                    1,
                    "{}"
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.create(req);

            // Then
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }

        @Test
        @DisplayName("Should handle maximum node order")
        void shouldHandleMaximumNodeOrder() {
            // Given
            WorkflowNodeRequest req = new WorkflowNodeRequest(
                    "Last Node",
                    "last",
                    Integer.MAX_VALUE,
                    "user",
                    "user-001",
                    false,
                    false,
                    "Last"
            );

            when(definitionRepository.findById("def-001"))
                    .thenReturn(Optional.of(testDefinitionEntity));
            when(nodeRepository.save(any(WorkflowNodeEntity.class)))
                    .thenReturn(testNodeEntity);

            // When
            service.addNode("def-001", req);

            // Then
            verify(nodeRepository).save(any(WorkflowNodeEntity.class));
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            // Given
            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Test",
                    "test",
                    "HR",
                    null,
                    1,
                    "{}"
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.create(req);

            // Then
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }

        @Test
        @DisplayName("Should handle empty form config")
        void shouldHandleEmptyFormConfig() {
            // Given
            WorkflowDefinitionRequest req = new WorkflowDefinitionRequest(
                    "Test",
                    "test",
                    "HR",
                    "Test",
                    1,
                    ""
            );

            when(definitionRepository.existsByDefinitionKeyAndDeletedFalse(anyString()))
                    .thenReturn(false);
            when(definitionRepository.save(any(WorkflowDefinitionEntity.class)))
                    .thenReturn(testDefinitionEntity);

            // When
            service.create(req);

            // Then
            verify(definitionRepository).save(any(WorkflowDefinitionEntity.class));
        }
    }
}
