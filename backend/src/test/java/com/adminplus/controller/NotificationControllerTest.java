package com.adminplus.controller;

import com.adminplus.pojo.dto.req.NotificationSendReq;
import com.adminplus.pojo.dto.resp.NotificationResp;
import com.adminplus.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NotificationController Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private NotificationResp mockNotification;
    private NotificationSendReq mockRequest;

    @BeforeEach
    void setUp() {
        mockNotification = new NotificationResp();
        mockNotification.setId("notif-001");
        mockNotification.setType("workflow_approve");
        mockNotification.setRecipientId("user-001");
        mockNotification.setTitle("测试通知");
        mockNotification.setContent("这是一条测试通知");
        mockNotification.setRelatedId("workflow-001");
        mockNotification.setRelatedType("workflow");
        mockNotification.setStatus(0);

        mockRequest = new NotificationSendReq();
        mockRequest.setType("workflow_approve");
        mockRequest.setRecipientId("user-001");
        mockRequest.setTitle("测试通知");
        mockRequest.setContent("这是一条测试通知");
        mockRequest.setRelatedId("workflow-001");
        mockRequest.setRelatedType("workflow");
    }

    @Test
    @WithMockUser(username = "admin")
    void testGetMyNotifications() throws Exception {
        Page<NotificationResp> emptyPage = new PageImpl<>(Collections.emptyList());

        when(notificationService.getUserNotifications(eq("admin"), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/v1/notifications")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(notificationService).getUserNotifications(eq("admin"), any(), any());
    }

    @Test
    @WithMockUser(username = "admin")
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount("admin")).thenReturn(5L);

        mockMvc.perform(get("/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService).getUnreadCount("admin");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"notification:send"})
    void testSendNotification() throws Exception {
        when(notificationService.sendNotification(any(NotificationSendReq.class)))
                .thenReturn(mockNotification);

        mockMvc.perform(post("/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"workflow_approve\",\"recipientId\":\"user-001\",\"title\":\"测试通知\",\"content\":\"这是一条测试通知\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("notif-001"))
                .andExpect(jsonPath("$.data.title").value("测试通知"));

        verify(notificationService).sendNotification(any(NotificationSendReq.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"notification:send"})
    void testSendBatchNotification() throws Exception {
        doNothing().when(notificationService).sendBatchNotification(anyList(), any(NotificationSendReq.class));

        mockMvc.perform(post("/v1/notifications/batch")
                        .param("recipientIds", "user-001,user-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"workflow_approve\",\"recipientId\":\"user-001\",\"title\":\"测试通知\",\"content\":\"这是一条测试通知\"}"))
                .andExpect(status().isOk());

        verify(notificationService).sendBatchNotification(anyList(), any(NotificationSendReq.class));
    }

    @Test
    @WithMockUser(username = "admin")
    void testMarkAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(eq("notif-001"), eq("admin"));

        mockMvc.perform(put("/v1/notifications/notif-001/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(eq("notif-001"), eq("admin"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testMarkAllAsRead() throws Exception {
        when(notificationService.markAllAsRead("admin")).thenReturn(10);

        mockMvc.perform(put("/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));

        verify(notificationService).markAllAsRead("admin");
    }

    @Test
    @WithMockUser(username = "admin")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(eq("notif-001"), eq("admin"));

        mockMvc.perform(delete("/v1/notifications/notif-001"))
                .andExpect(status().isOk());

        verify(notificationService).deleteNotification(eq("notif-001"), eq("admin"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testGetMyNotifications_WithStatus() throws Exception {
        Page<NotificationResp> emptyPage = new PageImpl<>(Collections.emptyList());

        when(notificationService.getUserNotifications(eq("admin"), eq(0), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/v1/notifications")
                        .param("status", "0")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(notificationService).getUserNotifications(eq("admin"), eq(0), any());
    }

    @Test
    void testGetMyNotifications_WithoutAuth() throws Exception {
        mockMvc.perform(get("/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

}
