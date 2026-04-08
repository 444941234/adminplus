package com.adminplus.controller;

import com.adminplus.pojo.dto.query.UserQuery;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.req.PasswordResetReq;
import com.adminplus.pojo.dto.req.RoleAssignReq;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.UserService;
import com.adminplus.config.TestJacksonConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UserResp testUser;
    private UserCreateReq createReq;
    private UserUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        testUser = new UserResp(
                "user-001", "testuser", "Test User", "test@example.com",
                "13800138000", "avatar.jpg", 1, "1", "技术部",
                List.of(), null, null
        );
        createReq = new UserCreateReq(
                "testuser", "Password123!", "Test User", "test@example.com",
                "13800138000", null, "1"
        );
        updateReq = new UserUpdateReq(
                "Test User", "test@example.com", "13800138000", null, 1, "1"
        );
    }

    @Nested
    @DisplayName("getUserList Tests")
    class GetUserListTests {

        @Test
        @DisplayName("should return user list")
        void getUserList_ShouldReturnUserList() throws Exception {
            // Given
            PageResultResp<UserResp> pageResult = new PageResultResp<>(List.of(testUser), 1L, 1, 10);
            when(userService.getUserList(any(UserQuery.class))).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/users")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].username").value("testuser"));

            verify(userService).getUserList(any(UserQuery.class));
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user by id")
        void getUserById_ShouldReturnUser() throws Exception {
            // Given
            when(userService.getUserById("user-001")).thenReturn(testUser);

            // When & Then
            mockMvc.perform(get("/v1/sys/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));

            verify(userService).getUserById("user-001");
        }
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("should create user")
        void createUser_ShouldCreateUser() throws Exception {
            // Given
            when(userService.createUser(any(UserCreateReq.class))).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/v1/sys/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).createUser(any(UserCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user")
        void updateUser_ShouldUpdateUser() throws Exception {
            // Given
            when(userService.updateUser(anyString(), any(UserUpdateReq.class))).thenReturn(testUser);

            // When & Then
            mockMvc.perform(put("/v1/sys/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).updateUser(anyString(), any(UserUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user")
        void deleteUser_ShouldDeleteUser() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).deleteUser("user-001");
        }
    }

    @Nested
    @DisplayName("updateUserStatus Tests")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("should update user status")
        void updateUserStatus_ShouldUpdateStatus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/users/user-001/status")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).updateUserStatus("user-001", 0);
        }
    }

    @Nested
    @DisplayName("resetPassword Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("should reset password")
        void resetPassword_ShouldResetPassword() throws Exception {
            // Given
            PasswordResetReq req = new PasswordResetReq("newpassword123");

            // When & Then
            mockMvc.perform(put("/v1/sys/users/user-001/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).resetPassword("user-001", "newpassword123");
        }
    }

    @Nested
    @DisplayName("assignRoles Tests")
    class AssignRolesTests {

        @Test
        @DisplayName("should assign roles to user")
        void assignRoles_ShouldAssignRoles() throws Exception {
            // Given
            RoleAssignReq req = new RoleAssignReq(List.of("role-001", "role-002"));

            // When & Then
            mockMvc.perform(put("/v1/sys/users/user-001/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).assignRoles("user-001", List.of("role-001", "role-002"));
        }
    }

    @Nested
    @DisplayName("getUserRoleIds Tests")
    class GetUserRoleIdsTests {

        @Test
        @DisplayName("should return user role ids")
        void getUserRoleIds_ShouldReturnRoleIds() throws Exception {
            // Given
            when(userService.getUserRoleIds("user-001")).thenReturn(List.of("role-001", "role-002"));

            // When & Then
            mockMvc.perform(get("/v1/sys/users/user-001/roles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0]").value("role-001"));

            verify(userService).getUserRoleIds("user-001");
        }
    }
}