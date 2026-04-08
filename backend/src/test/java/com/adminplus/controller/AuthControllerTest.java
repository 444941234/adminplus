package com.adminplus.controller;

import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.UserLoginReq;
import com.adminplus.pojo.dto.resp.LoginResp;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.service.AuthService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private UserLoginReq loginReq;
    private LoginResp loginResp;
    private UserResp userResp;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        loginReq = new UserLoginReq("testuser", "password", "captcha-id", "ABCD");
        userResp = new UserResp(
                "user-001", "testuser", "Test User", "test@example.com",
                "13800138000", "avatar.jpg", 1, "1", "技术部",
                List.of(), null, null
        );
        loginResp = new LoginResp("access-token", "refresh-token", "Bearer", userResp, List.of("user:view"));
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("should return login response on successful login")
        void login_ShouldReturnLoginResponse() throws Exception {
            // Given
            when(authService.login(any(UserLoginReq.class))).thenReturn(loginResp);

            // When & Then
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

            verify(authService).login(any(UserLoginReq.class));
        }
    }

    
    @Nested
    @DisplayName("logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("should logout successfully")
        void logout_ShouldSucceed() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(authService).logout();
        }
    }

    @Nested
    @DisplayName("refreshAccessToken Tests")
    class RefreshAccessTokenTests {

        @Test
        @DisplayName("should refresh access token")
        void refreshAccessToken_ShouldReturnNewToken() throws Exception {
            // Given
            when(authService.refreshAccessToken(anyString())).thenReturn("new-access-token");

            // When & Then
            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"refresh-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("new-access-token"));
        }
    }
}