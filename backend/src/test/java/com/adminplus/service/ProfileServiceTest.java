package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.SettingsResp;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.service.impl.ProfileServiceImpl;
import com.adminplus.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProfileService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-20
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("test-user-id");
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setStatus(1);

        // Initialize settings with default values
        Map<String, Object> settings = new HashMap<>();
        settings.put("notifications", true);
        settings.put("darkMode", false);
        settings.put("emailUpdates", true);
        settings.put("language", "zh-CN");
        testUser.setSettings(settings);
    }

    @Test
    void testGetSettings_WithExistingSettings() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
            when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));

            // When
            SettingsResp settings = profileService.getSettings();

            // Then
            assertNotNull(settings);
            assertTrue(settings.notifications());
            assertFalse(settings.darkMode());
            assertTrue(settings.emailUpdates());
            assertEquals("zh-CN", settings.language());
        }
    }

    @Test
    void testGetSettings_WithNullSettings() {
        // Given
        testUser.setSettings(null);
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
            when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));

            // When
            SettingsResp settings = profileService.getSettings();

            // Then
            assertNotNull(settings);
            assertTrue(settings.notifications());
            assertFalse(settings.darkMode());
            assertTrue(settings.emailUpdates());
            assertEquals("zh-CN", settings.language());
        }
    }

    @Test
    void testUpdateSettings_AllFields() {
        // Given
        SettingsUpdateReq req = new SettingsUpdateReq(
                false,  // notifications
                true,   // darkMode
                false,  // emailUpdates
                "en-US" // language
        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
            when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(profileRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            SettingsResp settings = profileService.updateSettings(req);

            // Then
            assertNotNull(settings);
            assertFalse(settings.notifications());
            assertTrue(settings.darkMode());
            assertFalse(settings.emailUpdates());
            assertEquals("en-US", settings.language());

            // Verify save was called
            verify(profileRepository, times(1)).save(any(UserEntity.class));
        }
    }

    @Test
    void testUpdateSettings_PartialFields() {
        // Given
        SettingsUpdateReq req = new SettingsUpdateReq(
                null,   // notifications (not updating)
                true,   // darkMode
                null,   // emailUpdates (not updating)
                null    // language (not updating)
        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
            when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(profileRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            SettingsResp settings = profileService.updateSettings(req);

            // Then
            assertNotNull(settings);
            // notifications should remain true (original value)
            assertTrue(settings.notifications());
            // darkMode should be updated to true
            assertTrue(settings.darkMode());
            // emailUpdates should remain true (original value)
            assertTrue(settings.emailUpdates());
            // language should remain zh-CN (original value)
            assertEquals("zh-CN", settings.language());
        }
    }

    @Test
    void testUpdateSettings_InitializeNullSettings() {
        // Given
        testUser.setSettings(null);
        SettingsUpdateReq req = new SettingsUpdateReq(
                false,
                true,
                true,
                "en-US"
        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
            when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(profileRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
                UserEntity user = invocation.getArgument(0);
                testUser.setSettings(user.getSettings());
                return testUser;
            });

            // When
            SettingsResp settings = profileService.updateSettings(req);

            // Then
            assertNotNull(settings);
            assertFalse(settings.notifications());
            assertTrue(settings.darkMode());
            assertTrue(settings.emailUpdates());
            assertEquals("en-US", settings.language());

            // Verify settings were initialized and saved
            assertNotNull(testUser.getSettings());
            verify(profileRepository, times(1)).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("修改密码测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("应成功修改密码")
        void shouldChangePassword() {
            // Given
            PasswordChangeReq req = new PasswordChangeReq("oldPass123!@", "NewPass123!@#", "NewPass123!@#");

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
                when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("oldPass123!@", "encoded-password")).thenReturn(true);
                when(passwordEncoder.encode("NewPass123!@#")).thenReturn("new-encoded-password");
                when(profileRepository.save(any(UserEntity.class))).thenReturn(testUser);

                // When
                profileService.changePassword(req);

                // Then
                verify(passwordEncoder).matches("oldPass123!@", "encoded-password");
                verify(passwordEncoder).encode("NewPass123!@#");
                verify(profileRepository).save(any(UserEntity.class));
            }
        }

        @Test
        @DisplayName("新密码和确认密码不一致时应抛出异常")
        void shouldThrowWhenPasswordsDoNotMatch() {
            // Given
            PasswordChangeReq req = new PasswordChangeReq("oldPass123!@", "NewPass123!@#1", "DifferentPass!@#");

            // When & Then
            BizException exception = assertThrows(BizException.class, () -> profileService.changePassword(req));
            assertEquals("新密码和确认密码不一致", exception.getMessage());
        }

        @Test
        @DisplayName("新密码与原密码相同时应抛出异常")
        void shouldThrowWhenNewPasswordSameAsOld() {
            // Given
            PasswordChangeReq req = new PasswordChangeReq("samePass123!@#", "samePass123!@#", "samePass123!@#");

            // When & Then
            BizException exception = assertThrows(BizException.class, () -> profileService.changePassword(req));
            assertEquals("新密码不能与原密码相同", exception.getMessage());
        }

        @Test
        @DisplayName("原密码错误时应抛出异常")
        void shouldThrowWhenOldPasswordIncorrect() {
            // Given
            PasswordChangeReq req = new PasswordChangeReq("wrongPass123!@", "NewPass123!@#1", "NewPass123!@#1");

            try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
                securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
                when(profileRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("wrongPass123!@", "encoded-password")).thenReturn(false);

                // When & Then
                BizException exception = assertThrows(BizException.class, () -> profileService.changePassword(req));
                assertEquals("原密码错误", exception.getMessage());
            }
        }

        @Test
        @DisplayName("新密码强度不足时应抛出异常")
        void shouldThrowWhenPasswordTooWeak() {
            // Given - password too short (less than 12 chars)
            PasswordChangeReq req = new PasswordChangeReq("oldPass123!@", "weak123!", "weak123!");

            // When & Then
            assertThrows(BizException.class, () -> profileService.changePassword(req));
        }
    }
}
