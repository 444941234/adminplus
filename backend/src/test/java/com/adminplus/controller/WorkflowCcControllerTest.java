package com.adminplus.controller;

import com.adminplus.pojo.dto.resp.WorkflowCcResp;
import com.adminplus.service.WorkflowCcService;
import com.adminplus.utils.SecurityUtils;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WorkflowCcController Tests
 *
 * @author AdminPlus
 * @since 2026-03-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowCcController Tests")
class WorkflowCcControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowCcService ccService;

    @InjectMocks
    private WorkflowCcController ccController;

    private ObjectMapper objectMapper;
    private WorkflowCcResp testCcRecord;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ccController).build();
        objectMapper = new ObjectMapper();
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("user-001");

        testCcRecord = new WorkflowCcResp(
                "cc-001", "inst-001", "node-001", "Manager Approval",
                "user-002", "李四", "approve", "审批通过抄送",
                false, null, Instant.now()
        );
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Nested
    @DisplayName("Get My CC Records Tests")
    class GetMyCcRecordsTests {

        @Test
        @DisplayName("should return my cc records")
        void shouldReturnMyCcRecords() throws Exception {
            when(ccService.getUserCcRecords(anyString())).thenReturn(Arrays.asList(testCcRecord));

            mockMvc.perform(get("/v1/workflow/cc/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].id").value("cc-001"));

            verify(ccService).getUserCcRecords(anyString());
        }
    }

    @Nested
    @DisplayName("Get Unread CC Records Tests")
    class GetUnreadCcRecordsTests {

        @Test
        @DisplayName("should return unread cc records")
        void shouldReturnUnreadCcRecords() throws Exception {
            when(ccService.getUnreadCcRecords(anyString())).thenReturn(Arrays.asList(testCcRecord));

            mockMvc.perform(get("/v1/workflow/cc/my/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].isRead").value(false));

            verify(ccService).getUnreadCcRecords(anyString());
        }

        @Test
        @DisplayName("should count unread cc records")
        void shouldCountUnreadCcRecords() throws Exception {
            when(ccService.countUnreadCcRecords(anyString())).thenReturn(3L);

            mockMvc.perform(get("/v1/workflow/cc/my/unread/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(3));

            verify(ccService).countUnreadCcRecords(anyString());
        }
    }

    @Nested
    @DisplayName("Get Instance CC Records Tests")
    class GetInstanceCcRecordsTests {

        @Test
        @DisplayName("should return instance cc records")
        void shouldReturnInstanceCcRecords() throws Exception {
            when(ccService.getInstanceCcRecords("inst-001")).thenReturn(Arrays.asList(testCcRecord));

            mockMvc.perform(get("/v1/workflow/cc/instance/inst-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].instanceId").value("inst-001"));

            verify(ccService).getInstanceCcRecords("inst-001");
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark single cc record as read")
        void shouldMarkSingleAsRead() throws Exception {
            mockMvc.perform(put("/v1/workflow/cc/cc-001/read"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(ccService).markAsRead("cc-001");
        }

        @Test
        @DisplayName("should mark multiple cc records as read")
        void shouldMarkMultipleAsRead() throws Exception {
            List<String> ccIds = Arrays.asList("cc-001", "cc-002");

            mockMvc.perform(put("/v1/workflow/cc/read-batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ccIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(ccService).markAsReadBatch(anyList());
        }
    }
}