package com.adminplus.controller;

import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.service.ProfileService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProfileController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Unit Tests")
class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    private ObjectMapper objectMapper;
    private ProfileResp testProfile;
    private ProfileUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
        objectMapper = new ObjectMapper();
        testProfile = new ProfileResp(
                "user-001", "testuser", "Test User", "test@example.com",
                "13800138000", "avatar.jpg", 1, "技术部",
                List.of("管理员"), Instant.now(), Instant.now()
        );
        updateReq = new ProfileUpdateReq("Test User", "test@example.com", "13800138000", "avatar.jpg");
    }

    @Nested
    @DisplayName("getProfile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("should return profile")
        void getProfile_ShouldReturnProfile() throws Exception {
            // Given
            when(profileService.getCurrentUserProfile()).thenReturn(testProfile);

            // When & Then
            mockMvc.perform(get("/v1/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(profileService).getCurrentUserProfile();
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("should update profile")
        void updateProfile_ShouldUpdateProfile() throws Exception {
            // Given
            when(profileService.updateCurrentProfile(any(ProfileUpdateReq.class))).thenReturn(testProfile);

            // When & Then
            mockMvc.perform(put("/v1/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(profileService).updateCurrentProfile(any(ProfileUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("should change password")
        void changePassword_ShouldChangePassword() throws Exception {
            // Given
            PasswordChangeReq req = new PasswordChangeReq("OldPass123!", "NewTestPass123!", "NewTestPass123!");

            // When & Then
            mockMvc.perform(post("/v1/profile/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(profileService).changePassword(any(PasswordChangeReq.class));
        }
    }
}