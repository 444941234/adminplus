package com.adminplus.controller;

import com.adminplus.pojo.dto.request.ApprovalActionRequest;
import com.adminplus.pojo.dto.request.WorkflowStartRequest;
import com.adminplus.pojo.dto.response.WorkflowDetailResponse;
import com.adminplus.pojo.dto.response.WorkflowInstanceResponse;
import com.adminplus.service.WorkflowInstanceService;
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
 * WorkflowInstanceController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowInstanceController Unit Tests")
class WorkflowInstanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowInstanceService instanceService;

    @InjectMocks
    private WorkflowInstanceController instanceController;

    private ObjectMapper objectMapper;
    private WorkflowInstanceResponse testInstance;
    private WorkflowStartRequest startReq;
    private ApprovalActionRequest actionReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instanceController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        testInstance = new WorkflowInstanceResponse(
                "inst-001", "def-001", "请假审批", "user-001", "发起人",
                "dept-001", null, "请假申请", null, null, null,
                "pending", Instant.now(), null, null, Instant.now(),
                true, true, false, false, true, false, false
        );
        startReq = WorkflowStartRequest.builder()
                .definitionId("def-001")
                .title("请假申请")
                .build();
        actionReq = ApprovalActionRequest.builder()
                .comment("同意")
                .build();
    }

    @Nested
    @DisplayName("createDraft Tests")
    class CreateDraftTests {

        @Test
        @DisplayName("should create draft")
        void createDraft_ShouldCreateDraft() throws Exception {
            // Given
            when(instanceService.createDraft(any(WorkflowStartRequest.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/draft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(startReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).createDraft(any(WorkflowStartRequest.class));
        }
    }

    @Nested
    @DisplayName("submit Tests")
    class SubmitTests {

        @Test
        @DisplayName("should submit workflow")
        void submit_ShouldSubmitWorkflow() throws Exception {
            // Given
            when(instanceService.submit("inst-001", null)).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/submit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).submit("inst-001", null);
        }
    }

    @Nested
    @DisplayName("start Tests")
    class StartTests {

        @Test
        @DisplayName("should start workflow")
        void start_ShouldStartWorkflow() throws Exception {
            // Given
            when(instanceService.start(any(WorkflowStartRequest.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(startReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).start(any(WorkflowStartRequest.class));
        }
    }

    @Nested
    @DisplayName("getDetail Tests")
    class GetDetailTests {

        @Test
        @DisplayName("should return workflow detail")
        void getDetail_ShouldReturnDetail() throws Exception {
            // Given
            WorkflowDetailResponse detail = new WorkflowDetailResponse(
                    testInstance, List.of(), List.of(), null, true,
                    null, null, List.of(), List.of(), null
            );
            when(instanceService.getDetail("inst-001")).thenReturn(detail);

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/inst-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.instance.title").value("请假申请"));

            verify(instanceService).getDetail("inst-001");
        }
    }

    @Nested
    @DisplayName("getMyWorkflows Tests")
    class GetMyWorkflowsTests {

        @Test
        @DisplayName("should return my workflows")
        void getMyWorkflows_ShouldReturnWorkflows() throws Exception {
            // Given
            when(instanceService.getMyWorkflows(null)).thenReturn(List.of(testInstance));

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].title").value("请假申请"));

            verify(instanceService).getMyWorkflows(null);
        }
    }

    @Nested
    @DisplayName("getPendingApprovals Tests")
    class GetPendingApprovalsTests {

        @Test
        @DisplayName("should return pending approvals")
        void getPendingApprovals_ShouldReturnApprovals() throws Exception {
            // Given
            when(instanceService.getPendingApprovals()).thenReturn(List.of(testInstance));

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].title").value("请假申请"));

            verify(instanceService).getPendingApprovals();
        }
    }

    @Nested
    @DisplayName("countPendingApprovals Tests")
    class CountPendingApprovalsTests {

        @Test
        @DisplayName("should return pending count")
        void countPendingApprovals_ShouldReturnCount() throws Exception {
            // Given
            when(instanceService.countPendingApprovals()).thenReturn(5L);

            // When & Then
            mockMvc.perform(get("/v1/workflow/instances/pending/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(instanceService).countPendingApprovals();
        }
    }

    @Nested
    @DisplayName("approve Tests")
    class ApproveTests {

        @Test
        @DisplayName("should approve workflow")
        void approve_ShouldApproveWorkflow() throws Exception {
            // Given
            when(instanceService.approve(anyString(), any(ApprovalActionRequest.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/approve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).approve(anyString(), any(ApprovalActionRequest.class));
        }
    }

    @Nested
    @DisplayName("reject Tests")
    class RejectTests {

        @Test
        @DisplayName("should reject workflow")
        void reject_ShouldRejectWorkflow() throws Exception {
            // Given
            when(instanceService.reject(anyString(), any(ApprovalActionRequest.class))).thenReturn(testInstance);

            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).reject(anyString(), any(ApprovalActionRequest.class));
        }
    }

    @Nested
    @DisplayName("cancel Tests")
    class CancelTests {

        @Test
        @DisplayName("should cancel workflow")
        void cancel_ShouldCancelWorkflow() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).cancel("inst-001");
        }
    }

    @Nested
    @DisplayName("withdraw Tests")
    class WithdrawTests {

        @Test
        @DisplayName("should withdraw workflow")
        void withdraw_ShouldWithdrawWorkflow() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/workflow/instances/inst-001/withdraw"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(instanceService).withdraw("inst-001");
        }
    }
}