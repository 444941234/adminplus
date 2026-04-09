package com.adminplus.controller;

import com.adminplus.pojo.dto.response.WorkflowHookLogResponse;
import com.adminplus.service.WorkflowHookLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WorkflowHookLogController 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowHookLogController Unit Tests")
class WorkflowHookLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowHookLogService hookLogService;

    @InjectMocks
    private WorkflowHookLogController hookLogController;

    private WorkflowHookLogResponse testLogResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hookLogController).build();

        testLogResponse = new WorkflowHookLogResponse(
                "log-001",
                "instance-001",
                "node-001",
                "hook-001",
                null,
                "PRE_APPROVE",
                "spel",
                null,
                true,
                "SUCCESS",
                "校验通过",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Nested
    @DisplayName("getHookLogs Tests")
    class GetHookLogsTests {

        @Test
        @DisplayName("should return logs by instance id")
        void listByInstanceId_Success() throws Exception {
            when(hookLogService.listByInstanceId("instance-001"))
                .thenReturn(List.of(testLogResponse));

            mockMvc.perform(get("/v1/workflow/hook-logs/instance/instance-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("log-001"));

            verify(hookLogService).listByInstanceId("instance-001");
        }

        @Test
        @DisplayName("should return logs by instance id and hook point")
        void listByInstanceIdAndHookPoint_Success() throws Exception {
            when(hookLogService.listByInstanceIdAndHookPoint("instance-001", "PRE_APPROVE"))
                .thenReturn(List.of(testLogResponse));

            mockMvc.perform(get("/v1/workflow/hook-logs/instance/instance-001/PRE_APPROVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hookPoint").value("PRE_APPROVE"));

            verify(hookLogService).listByInstanceIdAndHookPoint("instance-001", "PRE_APPROVE");
        }

        @Test
        @DisplayName("should return logs by instance id and node id")
        void listByInstanceIdAndNodeId_Success() throws Exception {
            when(hookLogService.listByInstanceIdAndNodeId("instance-001", "node-001"))
                .thenReturn(List.of(testLogResponse));

            mockMvc.perform(get("/v1/workflow/hook-logs/instance/instance-001/node/node-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nodeId").value("node-001"));

            verify(hookLogService).listByInstanceIdAndNodeId("instance-001", "node-001");
        }

        @Test
        @DisplayName("should return logs by hook id")
        void listByHookId_Success() throws Exception {
            when(hookLogService.listByHookId("hook-001"))
                .thenReturn(List.of(testLogResponse));

            mockMvc.perform(get("/v1/workflow/hook-logs/hook/hook-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hookId").value("hook-001"));

            verify(hookLogService).listByHookId("hook-001");
        }

        @Test
        @DisplayName("should return log by id")
        void getById_Success() throws Exception {
            when(hookLogService.getById("log-001")).thenReturn(testLogResponse);

            mockMvc.perform(get("/v1/workflow/hook-logs/log-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("log-001"));

            verify(hookLogService).getById("log-001");
        }

        @Test
        @DisplayName("should return error when log not found")
        void getById_NotFound() throws Exception {
            when(hookLogService.getById("non-existent"))
                .thenThrow(new com.adminplus.common.exception.BizException("日志不存在"));

            mockMvc.perform(get("/v1/workflow/hook-logs/non-existent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
        }
    }
}