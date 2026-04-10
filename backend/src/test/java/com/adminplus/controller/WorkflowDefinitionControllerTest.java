package com.adminplus.controller;

import com.adminplus.pojo.dto.request.WorkflowDefinitionRequest;
import com.adminplus.pojo.dto.request.WorkflowNodeRequest;
import com.adminplus.pojo.dto.response.WorkflowDefinitionResponse;
import com.adminplus.pojo.dto.response.WorkflowNodeResponse;
import com.adminplus.service.WorkflowDefinitionService;
import com.adminplus.config.TestJacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WorkflowDefinitionController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowDefinitionController Unit Tests")
class WorkflowDefinitionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowDefinitionService definitionService;

    @InjectMocks
    private WorkflowDefinitionController definitionController;

    private ObjectMapper objectMapper;
    private WorkflowDefinitionResponse testDefinition;
    private WorkflowDefinitionRequest definitionReq;
    private WorkflowNodeResponse testNode;
    private WorkflowNodeRequest nodeReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(definitionController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        testDefinition = new WorkflowDefinitionResponse(
                "def-001", "请假审批", "leave_approval",
                "请假类别", "请假审批流程", 1, 1, null, 0, Instant.now(), Instant.now()
        );
        definitionReq = WorkflowDefinitionRequest.builder()
                .definitionName("请假审批")
                .definitionKey("leave_approval")
                .category("请假类别")
                .description("请假审批流程")
                .status(1)
                .build();
        testNode = new WorkflowNodeResponse(
                "node-001", "def-001", "部门主管审批", "node_001",
                1, "user", "user-001", false, true, null, Instant.now()
        );
        nodeReq = WorkflowNodeRequest.builder()
                .nodeName("部门主管审批")
                .nodeCode("node_001")
                .nodeOrder(1)
                .approverType("user")
                .approverId("user-001")
                .isCounterSign(false)
                .autoPassSameUser(true)
                .build();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("should create workflow definition")
        void create_ShouldCreateDefinition() throws Exception {
            // Given
            when(definitionService.create(any(WorkflowDefinitionRequest.class))).thenReturn(testDefinition);

            // When & Then
            mockMvc.perform(post("/workflow/definitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(definitionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(definitionService).create(any(WorkflowDefinitionRequest.class));
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("should update workflow definition")
        void update_ShouldUpdateDefinition() throws Exception {
            // Given
            when(definitionService.update(anyString(), any(WorkflowDefinitionRequest.class))).thenReturn(testDefinition);

            // When & Then
            mockMvc.perform(put("/workflow/definitions/def-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(definitionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(definitionService).update(anyString(), any(WorkflowDefinitionRequest.class));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete workflow definition")
        void delete_ShouldDeleteDefinition() throws Exception {
            // When & Then
            mockMvc.perform(delete("/workflow/definitions/def-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(definitionService).delete("def-001");
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("should return workflow definition by id")
        void getById_ShouldReturnDefinition() throws Exception {
            // Given
            when(definitionService.getById("def-001")).thenReturn(testDefinition);

            // When & Then
            mockMvc.perform(get("/workflow/definitions/def-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.definitionName").value("请假审批"));

            verify(definitionService).getById("def-001");
        }
    }

    @Nested
    @DisplayName("listAll Tests")
    class ListAllTests {

        @Test
        @DisplayName("should return all workflow definitions")
        void listAll_ShouldReturnAll() throws Exception {
            // Given
            when(definitionService.listAll()).thenReturn(List.of(testDefinition));

            // When & Then
            mockMvc.perform(get("/workflow/definitions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].definitionName").value("请假审批"));

            verify(definitionService).listAll();
        }
    }

    @Nested
    @DisplayName("addNode Tests")
    class AddNodeTests {

        @Test
        @DisplayName("should add workflow node")
        void addNode_ShouldAddNode() throws Exception {
            // Given
            when(definitionService.addNode(anyString(), any(WorkflowNodeRequest.class))).thenReturn(testNode);

            // When & Then
            mockMvc.perform(post("/workflow/definitions/def-001/nodes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nodeReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(definitionService).addNode(anyString(), any(WorkflowNodeRequest.class));
        }
    }

    @Nested
    @DisplayName("listNodes Tests")
    class ListNodesTests {

        @Test
        @DisplayName("should return workflow nodes")
        void listNodes_ShouldReturnNodes() throws Exception {
            // Given
            when(definitionService.listNodes("def-001")).thenReturn(List.of(testNode));

            // When & Then
            mockMvc.perform(get("/workflow/definitions/def-001/nodes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].nodeName").value("部门主管审批"));

            verify(definitionService).listNodes("def-001");
        }
    }
}