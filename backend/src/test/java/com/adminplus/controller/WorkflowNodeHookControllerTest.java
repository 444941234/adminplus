package com.adminplus.controller;

import com.adminplus.pojo.dto.workflow.hook.WorkflowNodeHookRequest;
import com.adminplus.pojo.dto.response.WorkflowNodeHookResponse;
import com.adminplus.service.WorkflowNodeHookService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WorkflowNodeHookController 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowNodeHookController Unit Tests")
class WorkflowNodeHookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowNodeHookService hookService;

    @InjectMocks
    private WorkflowNodeHookController hookController;

    private ObjectMapper objectMapper;
    private WorkflowNodeHookResponse testHookResponse;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(hookController)
                .setValidator(validator)
                .build();
        objectMapper = TestJacksonConfig.createObjectMapper();

        testHookResponse = new WorkflowNodeHookResponse(
                "hook-001",
                "node-001",
                "PRE_APPROVE",
                "validate",
                "spel",
                "{\"expression\":\"#formData.amount > 100\"}",
                false,
                true,
                null,
                0,
                null,
                0,
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
    @DisplayName("createHook Tests")
    class CreateHookTests {

        @Test
        @DisplayName("should create hook successfully")
        void createHook_Success() throws Exception {
            when(hookService.create(any(WorkflowNodeHookRequest.class))).thenReturn(testHookResponse);

            mockMvc.perform(post("/v1/workflow/hooks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "nodeId": "node-001",
                            "hookPoint": "PRE_APPROVE",
                            "hookType": "validate",
                            "executorType": "spel",
                            "executorConfig": "{\\"expression\\":\\"#formData.amount > 100\\"}",
                            "asyncExecution": false,
                            "blockOnFailure": true,
                            "priority": 0,
                            "retryCount": 0
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("hook-001"));

            verify(hookService).create(any(WorkflowNodeHookRequest.class));
        }
    }

    @Nested
    @DisplayName("getHook Tests")
    class GetHookTests {

        @Test
        @DisplayName("should return hook by id")
        void getHookById_Success() throws Exception {
            when(hookService.getById("hook-001")).thenReturn(testHookResponse);

            mockMvc.perform(get("/v1/workflow/hooks/hook-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("hook-001"));

            verify(hookService).getById("hook-001");
        }

        @Test
        @DisplayName("should return error when hook not found")
        void getHookById_NotFound() throws Exception {
            when(hookService.getById("non-existent"))
                .thenThrow(new com.adminplus.common.exception.BizException("钩子配置不存在"));

            mockMvc.perform(get("/v1/workflow/hooks/non-existent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
        }

        @Test
        @DisplayName("should return hooks by node id")
        void listByNodeId_Success() throws Exception {
            when(hookService.listByNodeId("node-001"))
                .thenReturn(List.of(testHookResponse));

            mockMvc.perform(get("/v1/workflow/hooks/node/node-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("hook-001"));

            verify(hookService).listByNodeId("node-001");
        }

        @Test
        @DisplayName("should return hooks by node id and hook point")
        void listByNodeIdAndHookPoint_Success() throws Exception {
            when(hookService.listByNodeIdAndHookPoint("node-001", "PRE_APPROVE"))
                .thenReturn(List.of(testHookResponse));

            mockMvc.perform(get("/v1/workflow/hooks/node/node-001/PRE_APPROVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hookPoint").value("PRE_APPROVE"));

            verify(hookService).listByNodeIdAndHookPoint("node-001", "PRE_APPROVE");
        }
    }

    @Nested
    @DisplayName("updateHook Tests")
    class UpdateHookTests {

        @Test
        @DisplayName("should update hook successfully")
        void updateHook_Success() throws Exception {
            when(hookService.update(any(String.class), any(WorkflowNodeHookRequest.class))).thenReturn(testHookResponse);

            mockMvc.perform(put("/v1/workflow/hooks/hook-001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "nodeId": "node-001",
                            "hookPoint": "PRE_APPROVE",
                            "hookType": "validate",
                            "executorType": "spel",
                            "executorConfig": "{\\"expression\\":\\"#formData.amount > 200\\"}",
                            "asyncExecution": false,
                            "blockOnFailure": true,
                            "priority": 0,
                            "retryCount": 0
                        }
                        """))
                .andExpect(status().isOk());

            verify(hookService).update(any(String.class), any(WorkflowNodeHookRequest.class));
        }
    }

    @Nested
    @DisplayName("deleteHook Tests")
    class DeleteHookTests {

        @Test
        @DisplayName("should delete hook successfully")
        void deleteHook_Success() throws Exception {
            mockMvc.perform(delete("/v1/workflow/hooks/hook-001"))
                .andExpect(status().isOk());

            verify(hookService).delete("hook-001");
        }
    }
}