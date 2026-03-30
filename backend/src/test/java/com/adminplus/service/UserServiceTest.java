package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

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
    private LogService logService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUser;
    private RoleEntity testRole;
    private DeptEntity testDept;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-001");
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13812345678");
        testUser.setStatus(1);
        testUser.setDeptId("dept-001");

        testRole = new RoleEntity();
        testRole.setId("role-001");
        testRole.setCode("ROLE_USER");
        testRole.setName("User");

        testDept = new DeptEntity();
        testDept.setId("dept-001");
        testDept.setName("IT Department");
    }

    private UserRoleEntity createUserRoleEntity(String userId, String roleId) {
        UserRoleEntity entity = new UserRoleEntity();
        entity.setUserId(userId);
        entity.setRoleId(roleId);
        return entity;
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when exists")
        void getUserById_WhenExists_ShouldReturnUser() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of());
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));

            // When
            UserResp result = userService.getUserById("user-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserById_WhenNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("getUserByUsername Tests")
    class GetUserByUsernameTests {

        @Test
        @DisplayName("should return user when exists")
        void getUserByUsername_WhenExists_ShouldReturnUser() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When
            UserEntity result = userService.getUserByUsername("testuser");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserByUsername_WhenNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("should throw exception when username exists")
        void createUser_WhenUsernameExists_ShouldThrowException() {
            // Given
            UserCreateReq req = new UserCreateReq(
                    "testuser", "StrongP@ss123", "Test", "test@test.com",
                    "13800000000", null, null
            );
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户名已存在");
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void createUser_WhenDeptNotFound_ShouldThrowException() {
            // Given
            UserCreateReq req = new UserCreateReq(
                    "newuser", "StrongP@ss123", "New User", "new@test.com",
                    "13800000001", null, "non-existent-dept"
            );
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(deptRepository.existsById("non-existent-dept")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("部门不存在");
        }

        @Test
        @DisplayName("should throw exception when password is too weak")
        void createUser_WithWeakPassword_ShouldThrowException() {
            // Given - 密码强度验证已移到 Service 层
            UserCreateReq req = new UserCreateReq(
                    "newuser", "weak", "New User", "new@test.com",
                    "13800000001", null, null
            );
            when(userRepository.existsByUsername("newuser")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("密码");
        }

        @Test
        @DisplayName("should create user successfully with valid data")
        void createUser_WithValidData_ShouldSucceed() {
            // Given
            UserCreateReq req = new UserCreateReq(
                    "newuser", "StrongP@ss123", "New User", "new@test.com",
                    "13800000001", null, null
            );
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded-password");
            when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
                UserEntity user = inv.getArgument(0);
                user.setId("new-user-id");
                return user;
            });
            doNothing().when(logService).log(any(), anyInt(), any());

            // When
            UserResp result = userService.createUser(req);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("newuser");
            verify(userRepository).save(any(UserEntity.class));
            verify(logService).log(any(), anyInt(), any());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user successfully")
        void deleteUser_ShouldDeleteUser() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).delete(any(UserEntity.class));

            // When
            userService.deleteUser("user-001");

            // Then
            verify(userRepository).delete(any(UserEntity.class));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void deleteUser_WhenNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("updateUserStatus Tests")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("should update status successfully")
        void updateUserStatus_ShouldUpdateStatus() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenReturn(testUser);

            // When
            userService.updateUserStatus("user-001", 0);

            // Then
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void updateUserStatus_WhenNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUserStatus("non-existent", 0))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("assignRoles Tests")
    class AssignRolesTests {

        @Test
        @DisplayName("should assign roles successfully")
        void assignRoles_ShouldAssignRoles() {
            // Given
            List<String> roleIds = List.of("role-001");
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(roleRepository.findAllById(roleIds)).thenReturn(List.of(testRole));
            // Current roles (will be removed)
            when(userRoleRepository.findByUserId("user-001")).thenReturn(
                List.of(createUserRoleEntity("user-001", "existing-role"))
            );
            doNothing().when(userRoleRepository).deleteByUserIdAndRoleIdIn("user-001", Set.of("existing-role"));
            when(userRoleRepository.saveAll(any())).thenReturn(List.of());

            // When
            userService.assignRoles("user-001", roleIds);

            // Then
            verify(userRoleRepository).deleteByUserIdAndRoleIdIn("user-001", Set.of("existing-role"));
            verify(userRoleRepository).saveAll(any());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void assignRoles_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.assignRoles("non-existent", List.of()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void assignRoles_WhenRoleNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(roleRepository.findAllById(List.of("non-existent-role"))).thenReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> userService.assignRoles("user-001", List.of("non-existent-role")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }

        @Test
        @DisplayName("should clear all roles when empty list provided")
        void assignRoles_WithEmptyList_ShouldClearRoles() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            // Current roles (will be removed)
            when(userRoleRepository.findByUserId("user-001")).thenReturn(
                List.of(createUserRoleEntity("user-001", "existing-role"))
            );
            doNothing().when(userRoleRepository).deleteByUserIdAndRoleIdIn("user-001", Set.of("existing-role"));

            // When
            userService.assignRoles("user-001", List.of());

            // Then
            verify(userRoleRepository).deleteByUserIdAndRoleIdIn("user-001", Set.of("existing-role"));
            verify(userRoleRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("resetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("should reset password successfully")
        void resetPassword_ShouldSucceed() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(any())).thenReturn("encoded-password");
            when(userRepository.save(any())).thenReturn(testUser);
            doNothing().when(logService).log(any(), anyInt(), any());

            // When
            userService.resetPassword("user-001", "NewStrongP@ss123");

            // Then
            verify(userRepository).save(any(UserEntity.class));
            verify(logService).log(any(), anyInt(), any());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void resetPassword_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword("non-existent", "NewStrongP@ss123"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("should throw exception when new password is weak")
        void resetPassword_WithWeakPassword_ShouldThrowException() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.resetPassword("user-001", "123"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("密码");
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user successfully")
        void updateUser_ShouldUpdateUser() {
            // Given
            UserUpdateReq req = new UserUpdateReq(
                    "Updated Nick", "updated@test.com", "13900000000", null, 1, null
            );
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenReturn(testUser);
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));

            // When
            UserResp result = userService.updateUser("user-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void updateUser_WhenNotFound_ShouldThrowException() {
            // Given
            UserUpdateReq req = new UserUpdateReq(
                    "Updated", null, null, null, null, null
            );
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser("non-existent", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void updateUser_WithInvalidDept_ShouldThrowException() {
            // Given
            UserUpdateReq req = new UserUpdateReq(
                    null, null, null, null, null, "invalid-dept"
            );
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(deptRepository.existsById("invalid-dept")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.updateUser("user-001", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("部门不存在");
        }
    }

    @Nested
    @DisplayName("getUserRoleIds Tests")
    class GetUserRoleIdsTests {

        @Test
        @DisplayName("should return role IDs for user")
        void getUserRoleIds_ShouldReturnRoleIds() {
            // Given
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId("user-001");
            userRole.setRoleId("role-001");
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(userRole));

            // When
            List<String> result = userService.getUserRoleIds("user-001");

            // Then
            assertThat(result).containsExactly("role-001");
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserRoleIds_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserRoleIds("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }
}