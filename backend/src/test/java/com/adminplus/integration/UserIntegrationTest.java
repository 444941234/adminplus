package com.adminplus.integration;

import com.adminplus.base.AbstractIntegrationTest;
import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.dto.request.UserUpdateRequest;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User integration tests
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")

class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private DeptRepository deptRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private UserEntity testUser;
    private UserEntity adminUser;
    private RoleEntity testRole;
    private RoleEntity adminRole;
    private DeptEntity testDept;
    private String adminToken;

    @BeforeEach
    void setUpTestData() {
        // Clear Redis cache
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Create test department
        testDept = new DeptEntity();
        testDept.setName("Test Department");
        testDept.setCode("TEST_DEPT");
        testDept.setStatus(1);
        testDept.setSortOrder(1);
        testDept = deptRepository.save(testDept);

        // Create admin role with all permissions
        adminRole = new RoleEntity();
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator");
        adminRole.setStatus(1);
        adminRole.setSortOrder(1);
        adminRole = roleRepository.save(adminRole);

        // Create test role
        testRole = new RoleEntity();
        testRole.setCode("USER");
        testRole.setName("User");
        testRole.setStatus(1);
        testRole.setSortOrder(2);
        testRole = roleRepository.save(testRole);

        // Create admin user
        adminUser = new UserEntity();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("Admin@123456"));
        adminUser.setNickname("Administrator");
        adminUser.setEmail("admin@example.com");
        adminUser.setStatus(1);
        adminUser.setDeptId(testDept.getId());
        adminUser = userRepository.save(adminUser);

        // Assign admin role to admin user
        UserRoleEntity adminUserRole = new UserRoleEntity();
        adminUserRole.setUserId(adminUser.getId());
        adminUserRole.setRoleId(adminRole.getId());
        userRoleRepository.save(adminUserRole);

        // Create test user
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("Test@123456"));
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setDeptId(testDept.getId());
        testUser = userRepository.save(testUser);

        // Assign test role to test user
        UserRoleEntity testUserRole = new UserRoleEntity();
        testUserRole.setUserId(testUser.getId());
        testUserRole.setRoleId(testRole.getId());
        userRoleRepository.save(testUserRole);

        // Generate admin token with all permissions
        adminToken = generateToken(adminUser.getId(), adminUser.getUsername(),
                new String[]{"user:query", "user:add", "user:edit", "user:delete", "user:assign"});
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class UserCrudTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void createUser_WithValidData_ShouldSucceed() throws Exception {
            UserCreateRequest req = new UserCreateRequest(
                    "newuser",
                    "NewUser@123456",
                    "New User",
                    "newuser@example.com",
                    "13900139000",
                    null,
                    testDept.getId()
            );

            mockMvc.perform(withAuth(post("/v1/sys/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("newuser"))
                    .andExpect(jsonPath("$.data.nickname").value("New User"));

            // Verify user exists in database
            assertThat(userRepository.findByUsername("newuser")).isPresent();
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void createUser_WithExistingUsername_ShouldFail() throws Exception {
            UserCreateRequest req = new UserCreateRequest(
                    "testuser", // Existing username
                    "NewUser@123456",
                    "New User",
                    "newuser@example.com",
                    null, null, null
            );

            mockMvc.perform(withAuth(post("/v1/sys/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)), adminToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getUserById_WithValidId_ShouldReturnUser() throws Exception {
            mockMvc.perform(withAuth(get("/v1/sys/users/" + testUser.getId()), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void updateUser_WithValidData_ShouldSucceed() throws Exception {
            UserUpdateRequest req = new UserUpdateRequest(
                    "Updated Nickname",
                    "updated@example.com",
                    null,
                    null,
                    1,
                    null
            );

            mockMvc.perform(withAuth(put("/v1/sys/users/" + testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.nickname").value("Updated Nickname"));

            // Verify update in database
            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getNickname()).isEqualTo("Updated Nickname");
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void deleteUser_WithValidId_ShouldSucceed() throws Exception {
            mockMvc.perform(withAuth(delete("/v1/sys/users/" + testUser.getId()), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // Verify soft delete
            UserEntity deleted = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(deleted.getDeleted()).isTrue();
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class UserStatusTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void updateUserStatus_ShouldSucceed() throws Exception {
            mockMvc.perform(withAuth(put("/v1/sys/users/" + testUser.getId() + "/status")
                            .param("status", "0"), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // Verify status change
            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(0);
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class PasswordResetTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void resetPassword_WithValidData_ShouldSucceed() throws Exception {
            String newPassword = "NewPassword@123";

            mockMvc.perform(withAuth(put("/v1/sys/users/" + testUser.getId() + "/password")
                            .param("password", newPassword), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // Verify password was changed
            UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class RoleAssignmentTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void assignRoles_ShouldSucceed() throws Exception {
            List<String> roleIds = List.of(adminRole.getId(), testRole.getId());

            mockMvc.perform(withAuth(put("/v1/sys/users/" + testUser.getId() + "/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roleIds)), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // Verify role assignment
            List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(testUser.getId());
            assertThat(userRoles).hasSize(2);
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getUserRoleIds_ShouldReturnRoleIds() throws Exception {
            mockMvc.perform(withAuth(get("/v1/sys/users/" + testUser.getId() + "/roles"), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class AuthorizationTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void withoutAuth_ShouldFail() throws Exception {
            mockMvc.perform(get("/v1/sys/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void withoutPermission_ShouldFail() throws Exception {
            // Create user without user:query permission
            String userToken = generateToken(testUser.getId(), testUser.getUsername(), new String[]{"other:permission"});

            mockMvc.perform(withAuth(get("/v1/sys/users"), userToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class UserListTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getUserList_ShouldReturnPaginatedList() throws Exception {
            mockMvc.perform(withAuth(get("/v1/sys/users")
                            .param("page", "1")
                            .param("size", "10"), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").isNumber());
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getUserList_WithKeyword_ShouldFilter() throws Exception {
            mockMvc.perform(withAuth(get("/v1/sys/users")
                            .param("keyword", "test"), adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}