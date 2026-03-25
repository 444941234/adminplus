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
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user successfully")
        void deleteUser_ShouldDeleteUser() {
            // Given
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenReturn(testUser);

            // When
            userService.deleteUser("user-001");

            // Then
            verify(userRepository).save(any(UserEntity.class));
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
            when(userRepository.existsById("user-001")).thenReturn(true);
            when(roleRepository.existsById("role-001")).thenReturn(true);
            doNothing().when(userRoleRepository).deleteByUserId("user-001");
            when(userRoleRepository.saveAll(any())).thenReturn(List.of());
            when(userRepository.findById("user-001")).thenReturn(Optional.of(testUser));
            when(roleRepository.findAllById(any())).thenReturn(List.of(testRole));

            // When
            userService.assignRoles("user-001", roleIds);

            // Then
            verify(userRoleRepository).deleteByUserId("user-001");
            verify(userRoleRepository).saveAll(any());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void assignRoles_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.existsById("non-existent")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.assignRoles("non-existent", List.of()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void assignRoles_WhenRoleNotFound_ShouldThrowException() {
            // Given
            when(userRepository.existsById("user-001")).thenReturn(true);
            when(roleRepository.existsById("non-existent-role")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.assignRoles("user-001", List.of("non-existent-role")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
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
            when(userRepository.existsById("user-001")).thenReturn(true);
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
            when(userRepository.existsById("non-existent")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.getUserRoleIds("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在");
        }
    }
}