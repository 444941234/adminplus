package com.adminplus.controller;

import com.adminplus.pojo.dto.req.ApprovalActionReq;
import com.adminplus.pojo.dto.req.AddSignReq;
import com.adminplus.pojo.dto.req.AddSignReq.AddSignType;
import com.adminplus.pojo.dto.resp.WorkflowAddSignResp;
import com.adminplus.pojo.dto.resp.WorkflowInstanceResp;
import com.adminplus.pojo.dto.resp.WorkflowNodeResp;
import com.adminplus.service.WorkflowInstanceService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WorkflowInstanceController rollback and add-sign tests
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowInstanceController Rollback and AddSign Tests")
class WorkflowInstanceControllerRollbackTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowInstanceService instanceService;

    @InjectMocks
    private WorkflowInstanceController instanceController;

    private ObjectMapper objectMapper;
    private WorkflowInstanceResp testInstance;
    private ApprovalActionReq actionReq;
    private AddSignReq addSignReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instanceController).build();
        objectMapper = new ObjectMapper();

        testInstance = new WorkflowInstanceResp(
                "inst-001", "def-001", "请假审批", "user-001", "发起人",
                "dept-001", "研发部", "请假申请", null, null, null, "running",
                java.time.Instant.now(), null, null, java.time.Instant.now(), true, true,
                false, false, true, false, false
        );

        actionReq = ApprovalActionReq.builder()
                .comment("回退原因")
                .targetNodeId("node-001")
                .build();

        addSignReq = new AddSignReq("user-002", AddSignType.BEFORE, "需要更多人审核");
    }

    @Nested
    @DisplayName("rollback Tests")
    class RollbackTests {

        @Test
        @DisplayName("should rollback workflow with target node")
        void rollback_ShouldRollbackWithTargetNode() throws Exception {
            // Given
            when(instanceService.rollback(anyString(), any(ApprovalActionReq.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/rollback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).rollback("inst-001", actionReq);
        }

        @Test
        @DisplayName("should rollback workflow without target node")
        void rollback_ShouldRollbackWithoutTargetNode() throws Exception {
            // Given
            ApprovalActionReq reqWithoutTarget = ApprovalActionReq.builder()
                    .comment("回退原因")
                    .build();
            when(instanceService.rollback(anyString(), any(ApprovalActionReq.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/rollback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqWithoutTarget)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).rollback("inst-001", reqWithoutTarget);
        }
    }

    @Nested
    @DisplayName("getRollbackableNodes Tests")
    class GetRollbackableNodesTests {

        @Test
        @DisplayName("should return rollbackable nodes")
        void getRollbackableNodes_ShouldReturnNodes() throws Exception {
            // Given
            WorkflowNodeResp node = new WorkflowNodeResp(
                    "node-001", "def-001", "部门经理审批", "manager_approval",
                    1, "user", "user-001", false, false, "审批", null
            );
            when(instanceService.getRollbackableNodes("inst-001")).thenReturn(List.of(node));

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/inst-001/rollbackable-nodes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].nodeName").value("部门经理审批"));

            verify(instanceService).getRollbackableNodes("inst-001");
        }

        @Test
        @DisplayName("should return empty list when no rollbackable nodes")
        void getRollbackableNodes_ShouldReturnEmptyList() throws Exception {
            // Given
            when(instanceService.getRollbackableNodes("inst-001")).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/inst-001/rollbackable-nodes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(instanceService).getRollbackableNodes("inst-001");
        }
    }

    @Nested
    @DisplayName("addSign Tests")
    class AddSignTests {

        @Test
        @DisplayName("should perform before add-sign")
        void addSign_ShouldPerformBeforeAddSign() throws Exception {
            // Given
            WorkflowAddSignResp addSignResp = new WorkflowAddSignResp(
                    "add-sign-001", "inst-001", "node-001", "当前节点",
                    "user-001", "发起人", "user-002", "被加签人",
                    "before", "需要更多人审核", null, java.time.Instant.now()
            );
            when(instanceService.addSign(anyString(), any(AddSignReq.class))).thenReturn(addSignResp);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/add-sign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addSignReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.addType").value("before"));

            verify(instanceService).addSign("inst-001", addSignReq);
        }

        @Test
        @DisplayName("should perform after add-sign")
        void addSign_ShouldPerformAfterAddSign() throws Exception {
            // Given
            AddSignReq afterReq = new AddSignReq("user-002", AddSignType.AFTER, "补充审批");
            WorkflowAddSignResp addSignResp = new WorkflowAddSignResp(
                    "add-sign-001", "inst-001", "node-001", "当前节点",
                    "user-001", "发起人", "user-002", "被加签人",
                    "after", "补充审批", null, java.time.Instant.now()
            );
            when(instanceService.addSign(anyString(), any(AddSignReq.class))).thenReturn(addSignResp);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/add-sign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(afterReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.addType").value("after"));

            verify(instanceService).addSign("inst-001", afterReq);
        }

        @Test
        @DisplayName("should perform transfer")
        void addSign_ShouldPerformTransfer() throws Exception {
            // Given
            AddSignReq transferReq = new AddSignReq("user-002", AddSignType.TRANSFER, "暂时无法审批");
            WorkflowAddSignResp addSignResp = new WorkflowAddSignResp(
                    "add-sign-001", "inst-001", "node-001", "当前节点",
                    "user-001", "发起人", "user-002", "被转办人",
                    "transfer", "暂时无法审批", "user-001", java.time.Instant.now()
            );
            when(instanceService.addSign(anyString(), any(AddSignReq.class))).thenReturn(addSignResp);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/add-sign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transferReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.addType").value("transfer"))
                    .andExpect(jsonPath("$.data.originalApproverId").value("user-001"));

            verify(instanceService).addSign("inst-001", transferReq);
        }
    }

    @Nested
    @DisplayName("getAddSignRecords Tests")
    class GetAddSignRecordsTests {

        @Test
        @DisplayName("should return add-sign records")
        void getAddSignRecords_ShouldReturnRecords() throws Exception {
            // Given
            WorkflowAddSignResp record = new WorkflowAddSignResp(
                    "add-sign-001", "inst-001", "node-001", "当前节点",
                    "user-001", "发起人", "user-002", "被加签人",
                    "before", "需要更多人审核", null, java.time.Instant.now()
            );
            when(instanceService.getAddSignRecords("inst-001")).thenReturn(List.of(record));

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/inst-001/add-sign-records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].addType").value("before"));

            verify(instanceService).getAddSignRecords("inst-001");
        }

        @Test
        @DisplayName("should return empty list when no add-sign records")
        void getAddSignRecords_ShouldReturnEmptyList() throws Exception {
            // Given
            when(instanceService.getAddSignRecords("inst-001")).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/inst-001/add-sign-records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(instanceService).getAddSignRecords("inst-001");
        }
    }
}