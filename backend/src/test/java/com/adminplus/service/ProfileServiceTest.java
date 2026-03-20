package com.adminplus.service;

import com.adminplus.pojo.dto.req.SettingsUpdateReq;
import com.adminplus.pojo.dto.resp.SettingsResp;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.service.impl.ProfileServiceImpl;
import com.adminplus.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
