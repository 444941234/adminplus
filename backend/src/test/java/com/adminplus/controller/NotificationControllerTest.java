package com.adminplus.controller;

import com.adminplus.pojo.dto.query.NotificationQuery;
import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.pojo.dto.response.NotificationResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NotificationController Tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse mockNotification;
    private NotificationSendRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        mockNotification = new NotificationResponse();
        mockNotification.setId("notif-001");
        mockNotification.setType("workflow_approve");
        mockNotification.setRecipientId("user-001");
        mockNotification.setTitle("测试通知");
        mockNotification.setContent("这是一条测试通知");
        mockNotification.setRelatedId("workflow-001");
        mockNotification.setRelatedType("workflow");
        mockNotification.setStatus(0);

        mockRequest = new NotificationSendRequest();
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
        PageResultResponse<NotificationResponse> emptyPage = new PageResultResponse<>(Collections.emptyList(), 0L, 1, 20);

        when(notificationService.getUserNotifications(eq("admin"), any(NotificationQuery.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/notifications")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(notificationService).getUserNotifications(eq("admin"), any(NotificationQuery.class));
    }

    @Test
    @WithMockUser(username = "admin")
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount("admin")).thenReturn(5L);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService).getUnreadCount("admin");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"notification:send"})
    void testSendNotification() throws Exception {
        when(notificationService.sendNotification(any(NotificationSendRequest.class)))
                .thenReturn(mockNotification);

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"workflow_approve\",\"recipientId\":\"user-001\",\"title\":\"测试通知\",\"content\":\"这是一条测试通知\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("notif-001"))
                .andExpect(jsonPath("$.data.title").value("测试通知"));

        verify(notificationService).sendNotification(any(NotificationSendRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"notification:send"})
    void testSendBatchNotification() throws Exception {
        doNothing().when(notificationService).sendBatchNotification(anyList(), any(NotificationSendRequest.class));

        mockMvc.perform(post("/notifications/batch")
                        .param("recipientIds", "user-001,user-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"workflow_approve\",\"recipientId\":\"user-001\",\"title\":\"测试通知\",\"content\":\"这是一条测试通知\"}"))
                .andExpect(status().isOk());

        verify(notificationService).sendBatchNotification(anyList(), any(NotificationSendRequest.class));
    }

    @Test
    @WithMockUser(username = "admin")
    void testMarkAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(eq("notif-001"), eq("admin"));

        mockMvc.perform(put("/notifications/notif-001/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(eq("notif-001"), eq("admin"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testMarkAllAsRead() throws Exception {
        when(notificationService.markAllAsRead("admin")).thenReturn(10);

        mockMvc.perform(put("/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));

        verify(notificationService).markAllAsRead("admin");
    }

    @Test
    @WithMockUser(username = "admin")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(eq("notif-001"), eq("admin"));

        mockMvc.perform(delete("/notifications/notif-001"))
                .andExpect(status().isOk());

        verify(notificationService).deleteNotification(eq("notif-001"), eq("admin"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testGetMyNotifications_WithStatus() throws Exception {
        PageResultResponse<NotificationResponse> emptyPage = new PageResultResponse<>(Collections.emptyList(), 0L, 1, 20);

        when(notificationService.getUserNotifications(eq("admin"), any(NotificationQuery.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/notifications")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(notificationService).getUserNotifications(eq("admin"), any(NotificationQuery.class));
    }

    @Test
    void testGetMyNotifications_WithoutAuth() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized());
    }

}
