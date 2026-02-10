package com.adminplus.service;

import com.adminplus.dto.UserLoginReq;
import com.adminplus.dto.UserLoginResp;
import com.adminplus.entity.UserEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.exception.BizException;
import com.adminplus.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务集成测试
 */
class AuthServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 清理并准备测试数据
        userRepository.deleteAll();

        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setNickname("测试用户");
        user.setStatus(1);
        userRepository.save(user);
    }

    @Test
    void testLogin_Success() {
        // Given
        UserLoginReq loginReq = new UserLoginReq("testuser", "password123");

        // When
        UserLoginResp response = authService.login(loginReq);

        // Then
        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals("testuser", response.username());
        assertTrue(response.expiresIn() > 0);
    }

    @Test
    void testLogin_InvalidUsername() {
        // Given
        UserLoginReq loginReq = new UserLoginReq("nonexistent", "password123");

        // When & Then
        assertThrows(BizException.class, 
            () -> authService.login(loginReq));
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        UserLoginReq loginReq = new UserLoginReq("testuser", "wrongpassword");

        // When & Then
        assertThrows(BizException.class, 
            () -> authService.login(loginReq));
    }

    @Test
    void testLogin_UserDisabled() {
        // Given
        UserEntity user = new UserEntity();
        user.setUsername("disableduser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setNickname("禁用用户");
        user.setStatus(0); // 禁用状态
        userRepository.save(user);

        UserLoginReq loginReq = new UserLoginReq("disableduser", "password123");

        // When & Then
        assertThrows(BizException.class, 
            () -> authService.login(loginReq));
    }
}