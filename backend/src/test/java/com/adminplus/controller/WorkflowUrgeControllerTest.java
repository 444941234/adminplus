package com.adminplus.controller;

import com.adminplus.pojo.dto.req.UrgeActionReq;
import com.adminplus.pojo.dto.resp.WorkflowUrgeResp;
import com.adminplus.service.WorkflowUrgeService;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.config.TestJacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WorkflowUrgeController Tests
 *
 * @author AdminPlus
 * @since 2026-03-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowUrgeController Tests")
class WorkflowUrgeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowUrgeService urgeService;

    @InjectMocks
    private WorkflowUrgeController urgeController;

    private ObjectMapper objectMapper;
    private WorkflowUrgeResp testUrgeRecord;
    private UrgeActionReq urgeActionReq;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(urgeController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("user-001");

        testUrgeRecord = new WorkflowUrgeResp(
                "urge-001", "inst-001", "node-001", "Manager Approval",
                "user-001", "张三", "user-002", "李四",
                "请尽快审批", false, null, Instant.now()
        );

        urgeActionReq = new UrgeActionReq("请尽快处理审批", "user-002");
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Nested
    @DisplayName("Urge Workflow Tests")
    class UrgeWorkflowTests {

        @Test
        @DisplayName("should urge workflow successfully")
        void shouldUrgeWorkflow() throws Exception {
            mockMvc.perform(post("/v1/workflow/urge/inst-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urgeActionReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(urgeService).urgeWorkflow(anyString(), any(UrgeActionReq.class));
        }
    }

    @Nested
    @DisplayName("Get Received Urge Records Tests")
    class GetReceivedUrgeRecordsTests {

        @Test
        @DisplayName("should return received urge records")
        void shouldReturnReceivedUrgeRecords() throws Exception {
            when(urgeService.getReceivedUrgeRecords(anyString())).thenReturn(Arrays.asList(testUrgeRecord));

            mockMvc.perform(get("/v1/workflow/urge/received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].id").value("urge-001"));

            verify(urgeService).getReceivedUrgeRecords(anyString());
        }
    }

    @Nested
    @DisplayName("Get Sent Urge Records Tests")
    class GetSentUrgeRecordsTests {

        @Test
        @DisplayName("should return sent urge records")
        void shouldReturnSentUrgeRecords() throws Exception {
            when(urgeService.getSentUrgeRecords(anyString())).thenReturn(Arrays.asList(testUrgeRecord));

            mockMvc.perform(get("/v1/workflow/urge/sent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].id").value("urge-001"));

            verify(urgeService).getSentUrgeRecords(anyString());
        }
    }

    @Nested
    @DisplayName("Get Unread Urge Records Tests")
    class GetUnreadUrgeRecordsTests {

        @Test
        @DisplayName("should return unread urge records")
        void shouldReturnUnreadUrgeRecords() throws Exception {
            when(urgeService.getUnreadUrgeRecords(anyString())).thenReturn(Arrays.asList(testUrgeRecord));

            mockMvc.perform(get("/v1/workflow/urge/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].isRead").value(false));

            verify(urgeService).getUnreadUrgeRecords(anyString());
        }

        @Test
        @DisplayName("should count unread urge records")
        void shouldCountUnreadUrgeRecords() throws Exception {
            when(urgeService.countUnreadUrgeRecords(anyString())).thenReturn(5L);

            mockMvc.perform(get("/v1/workflow/urge/unread/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(urgeService).countUnreadUrgeRecords(anyString());
        }
    }

    @Nested
    @DisplayName("Get Instance Urge Records Tests")
    class GetInstanceUrgeRecordsTests {

        @Test
        @DisplayName("should return instance urge records")
        void shouldReturnInstanceUrgeRecords() throws Exception {
            when(urgeService.getInstanceUrgeRecords("inst-001")).thenReturn(Arrays.asList(testUrgeRecord));

            mockMvc.perform(get("/v1/workflow/urge/instance/inst-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].instanceId").value("inst-001"));

            verify(urgeService).getInstanceUrgeRecords("inst-001");
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark single urge record as read")
        void shouldMarkSingleAsRead() throws Exception {
            mockMvc.perform(put("/v1/workflow/urge/urge-001/read"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(urgeService).markAsRead("urge-001");
        }

        @Test
        @DisplayName("should mark multiple urge records as read")
        void shouldMarkMultipleAsRead() throws Exception {
            List<String> urgeIds = Arrays.asList("urge-001", "urge-002");

            mockMvc.perform(put("/v1/workflow/urge/read-batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urgeIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(urgeService).markAsReadBatch(anyList());
        }
    }
}