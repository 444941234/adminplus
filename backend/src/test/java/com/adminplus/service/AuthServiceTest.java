package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.UserLoginReq;
import com.adminplus.pojo.dto.resp.LoginResp;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.List;
import java.util.Optional;

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

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity testUser;
    private RoleEntity testRole;
    private UserLoginReq loginReq;

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

        loginReq = new UserLoginReq("testuser", "password", "captcha-id", "ABCD");
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("should throw exception when captcha is invalid")
        void login_WithInvalidCaptcha_ShouldThrowException() {
            // Given
            when(captchaService.validateCaptcha("captcha-id", "ABCD")).thenReturn(false);
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("captcha:captcha-id")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> authService.login(loginReq))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("验证码");
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
    @DisplayName("getCurrentUser Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return current user info")
        void getCurrentUser_ShouldReturnUserInfo() {
            // Given
            UserResp userResp = new UserResp(
                "user-001", "testuser", "Test User",
                "test@example.com", "13800000000", null,
                1, null, null, List.of("管理员"),
                null, null
            );

            when(userService.getUserRespByUsername("testuser")).thenReturn(userResp);

            // When
            UserResp result = authService.getCurrentUser("testuser");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("getCurrentUserPermissions Tests")
    class GetCurrentUserPermissionsTests {

        @Test
        @DisplayName("should return user permissions")
        void getCurrentUserPermissions_ShouldReturnPermissions() {
            // Given
            when(userService.getUserByUsername("testuser")).thenReturn(testUser);
            when(permissionService.getUserPermissions("user-001")).thenReturn(List.of("user:view", "user:edit"));

            // When
            List<String> result = authService.getCurrentUserPermissions("testuser");

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