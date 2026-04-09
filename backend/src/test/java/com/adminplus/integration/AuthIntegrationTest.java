package com.adminplus.integration;

import com.adminplus.base.AbstractIntegrationTest;
import com.adminplus.pojo.dto.request.UserLoginRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth integration tests
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")

class AuthIntegrationTest extends AbstractIntegrationTest {

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
    private RoleEntity testRole;
    private DeptEntity testDept;

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

        // Create test role
        testRole = new RoleEntity();
        testRole.setCode("TEST_USER");
        testRole.setName("Test User Role");
        testRole.setStatus(1);
        testRole.setSortOrder(1);
        testRole = roleRepository.save(testRole);

        // Create test user
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("Test@123456"));
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        testUser.setDeptId(testDept.getId());
        testUser = userRepository.save(testUser);

        // Assign role to user
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(testUser.getId());
        userRole.setRoleId(testRole.getId());
        userRoleRepository.save(userRole);
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class LoginTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void login_WithValidCredentials_ShouldReturnToken() throws Exception {
            UserLoginRequest loginReq = new UserLoginRequest("testuser", "Test@123456", "test-captcha-id", "1234");

            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.username").value("testuser"))
                    .andExpect(jsonPath("$.data.user.nickname").value("Test User"));
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void login_WithInvalidPassword_ShouldFail() throws Exception {
            UserLoginRequest loginReq = new UserLoginRequest("testuser", "wrongpassword", "test-captcha-id", "1234");

            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void login_WithNonExistentUser_ShouldFail() throws Exception {
            UserLoginRequest loginReq = new UserLoginRequest("nonexistent", "password", "test-captcha-id", "1234");

            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class GetCurrentUserTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getCurrentUser_WithValidToken_ShouldReturnUser() throws Exception {
            String token = generateToken(testUser.getId(), testUser.getUsername(), new String[]{"user:view"});

            mockMvc.perform(withAuth(get("/v1/auth/me"), token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("testuser"));
        }

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void getCurrentUser_WithoutAuth_ShouldFail() throws Exception {
            mockMvc.perform(get("/v1/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class LogoutTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void logout_WithValidToken_ShouldSucceed() throws Exception {
            String token = generateToken(testUser.getId(), testUser.getUsername(), new String[]{"user:view"});

            mockMvc.perform(withAuth(post("/v1/auth/logout"), token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // Verify token is blacklisted
            String pattern = "token:blacklist:" + token;
            assertThat(redisTemplate.keys("token:blacklist:*")).isNotEmpty();
        }
    }

    @Nested
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
    class RefreshTokenTests {

        @Test
@Disabled("Integration tests require Docker with TCP endpoint enabled")
@DisplayName("&")
        void refreshToken_WithValidToken_ShouldReturnNewToken() throws Exception {
            // First login to get refresh token
            UserLoginRequest loginReq = new UserLoginRequest("testuser", "Test@123456", "test-captcha-id", "1234");

            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Extract refresh token from response
            String responseBody = loginResult.getResponse().getContentAsString();
            String refreshToken = objectMapper.readTree(responseBody).path("data").path("refreshToken").asText();

            // Refresh access token
            String accessToken = generateToken(testUser.getId(), testUser.getUsername(), new String[]{"user:view"});

            mockMvc.perform(withAuth(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + refreshToken + "\"}"), accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }
    }
}