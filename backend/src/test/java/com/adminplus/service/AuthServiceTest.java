package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.UserLoginRequest;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private UserService userService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LogService logService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity testUser;
    private RoleEntity testRole;
    private UserLoginRequest loginReq;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-001");
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);

        testRole = new RoleEntity();
        testRole.setId("role-001");
        testRole.setCode("ROLE_USER");
        testRole.setName("User");
        testRole.setStatus(1);

        loginReq = new UserLoginRequest("testuser", "password", "captcha-id", "ABCD");
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("should throw exception when captcha is invalid")
        void login_WithInvalidCaptcha_ShouldThrowException() {
            // Given - 验证码错误或已过期
            when(captchaService.validateCaptcha("captcha-id", "ABCD")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("should throw exception when captcha is expired")
        void login_WithExpiredCaptcha_ShouldThrowException() {
            // Given - 验证码已过期（captchaService 返回 false）
            when(captchaService.validateCaptcha("captcha-id", "ABCD")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("验证码错误或已过期");
        }

        @Test
        @DisplayName("should throw exception when authentication fails")
        void login_WithInvalidCredentials_ShouldThrowException() {
            // Given
            when(captchaService.validateCaptcha("captcha-id", "ABCD")).thenReturn(true);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new AuthenticationException("Invalid credentials") {});

            // When & Then
            assertThatThrownBy(() -> authService.login(loginReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户名或密码错误");
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user info by id")
        void getUserById_ShouldReturnUserInfo() {
            // Given
            UserResponse userResponse = new UserResponse(
                "user-001", "testuser", "Test User",
                "test@example.com", "13800000000", null,
                1, null, null, List.of("管理员"),
                null, null
            );

            when(userService.getUserById("user-001")).thenReturn(userResponse);

            // When
            UserResponse result = authService.getUserById("user-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("getUserPermissions Tests")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("should return user permissions by id")
        void getUserPermissions_ShouldReturnPermissions() {
            // Given
            when(permissionService.getUserPermissions("user-001")).thenReturn(List.of("user:view", "user:edit"));

            // When
            List<String> result = authService.getUserPermissions("user-001");

            // Then
            assertThat(result).containsExactly("user:view", "user:edit");
        }
    }

    @Nested
    @DisplayName("refreshAccessToken Tests")
    class RefreshAccessTokenTests {

        @Test
        @DisplayName("should delegate to refreshTokenService")
        void refreshAccessToken_ShouldDelegateToRefreshTokenService() {
            // Given
            when(refreshTokenService.refreshAccessToken("refresh-token")).thenReturn("new-access-token");

            // When
            String result = authService.refreshAccessToken("refresh-token");

            // Then
            assertThat(result).isEqualTo("new-access-token");
        }
    }
}