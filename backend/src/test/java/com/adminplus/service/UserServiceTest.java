package com.adminplus.service;

import com.adminplus.dto.UserCreateReq;
import com.adminplus.entity.UserEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private com.adminplus.service.impl.UserServiceImpl userService;

    @Test
    void testCreateUser_Success() {
        // Given
        UserCreateReq req = new UserCreateReq("testuser", "password123", "测试用户", "test@example.com", "13800138000", null);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId("1");
            return user;
        });

        // When
        UserVO user = userService.createUser(req);

        // Then
        assertNotNull(user);
        assertEquals("1", user.id());
        assertEquals("testuser", user.username());
        assertEquals("测试用户", user.nickname());

        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        // Given
        UserCreateReq req = new UserCreateReq("testuser", "password123", "测试用户", "test@example.com", "13800138000", null);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThrows(com.adminplus.exception.BizException.class, () -> userService.createUser(req));

        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testGetUserById_Success() {
        // Given
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setStatus(1);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // When
        UserVO userVO = userService.getUserById("1");

        // Then
        assertNotNull(userVO);
        assertEquals("1", userVO.id());
        assertEquals("testuser", userVO.username());
        assertEquals("测试用户", userVO.nickname());

        verify(userRepository, times(1)).findById("1");
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(com.adminplus.exception.BizException.class, () -> userService.getUserById("1"));

        verify(userRepository, times(1)).findById("1");
    }
}