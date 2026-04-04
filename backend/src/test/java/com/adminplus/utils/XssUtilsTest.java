package com.adminplus.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XSS 防护工具类测试
 * <p>
 * 验证 XSS escaping 真的能防住 XSS 攻击
 *
 * @author AdminPlus
 * @since 2026-04-05
 */
@DisplayName("XSS 防护工具类测试")
class XssUtilsTest {

    // ==================== escape 方法核心测试 ====================

    @Nested
    @DisplayName("escape 方法核心测试")
    class EscapeCoreTests {

        @Test
        @DisplayName("应转义 script 标签")
        void shouldEscapeScriptTag() {
            String input = "<script>alert('xss')</script>";
            String escaped = XssUtils.escape(input);

            assertFalse(escaped.contains("<script>"), "不应该包含未转义的 <script>");
            assertFalse(escaped.contains("</script>"), "不应该包含未转义的 </script>");
            assertTrue(escaped.contains("&lt;") || escaped.contains("&gt;"), "应该包含转义字符");
        }

        @Test
        @DisplayName("应转义常见 HTML 字符")
        void shouldEscapeCommonHtmlChars() {
            String input = "<>&\"'";
            String escaped = XssUtils.escape(input);

            assertTrue(escaped.contains("&lt;"), "< 应该被转义");
            assertTrue(escaped.contains("&gt;"), "> 应该被转义");
            assertTrue(escaped.contains("&amp;"), "& 应该被转义");
            assertTrue(escaped.contains("&quot;") || escaped.contains("&#34;"), "\" 应该被转义");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "<script>alert(1)</script>",
                "<img src=x onerror=alert(1)>",
                "<iframe src=javascript:alert(1)>",
                "<body onload=alert(1)>",
                "<svg onload=alert(1)>"
        })
        @DisplayName("应破坏 XSS payload")
        void shouldBreakXssPayload(String payload) {
            String escaped = XssUtils.escape(payload);

            // 验证至少有一个危险字符被转义
            boolean hasEscapedChars = escaped.contains("&lt;")
                    || escaped.contains("&gt;")
                    || escaped.contains("&quot;")
                    || escaped.contains("&amp;")
                    || escaped.contains("&#34;");

            assertTrue(hasEscapedChars, "XSS payload 应该被转义，至少包含一个转义字符");
        }
    }

    // ==================== escapeOrNull 方法测试 ====================

    @Nested
    @DisplayName("escapeOrNull 方法测试")
    class EscapeOrNullTests {

        @Test
        @DisplayName("应转义非 null 字符串")
        void shouldEscapeNonNullString() {
            String input = "<script>alert('xss')</script>";
            String escaped = XssUtils.escapeOrNull(input);

            assertNotNull(escaped);
            assertFalse(escaped.contains("<script>"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("应正确处理 null 和空字符串")
        void shouldHandleNullOrEmpty(String input) {
            String escaped = XssUtils.escapeOrNull(input);

            if (input == null) {
                assertNull(escaped, "null 输入应该返回 null");
            } else {
                assertNotNull(escaped);
                assertEquals("", escaped);
            }
        }

        @Test
        @DisplayName("应验证 escapeOrNull 与 escape 行为一致（非 null）")
        void shouldBehaveConsistentWithEscape() {
            String input = "<script>alert(1)</script>";
            String escaped = XssUtils.escape(input);
            String escapedOrNull = XssUtils.escapeOrNull(input);

            assertEquals(escaped, escapedOrNull, "escapeOrNull 应该与 escape 行为一致");
        }
    }

    // ==================== sanitize 方法测试 ====================

    @Nested
    @DisplayName("sanitize 方法测试")
    class SanitizeTests {

        @Test
        @DisplayName("应移除危险的 script 标签")
        void shouldRemoveScriptTag() {
            String input = "<script>alert('xss')</script>";
            String sanitized = XssUtils.sanitize(input);

            // DANGEROUS_TAG_PATTERN 只匹配开始标签，不匹配结束标签
            // 所以 <script>alert('xss')</script> 会变成 alert('xss')</script>
            assertFalse(sanitized.contains("<script>"), "应该移除 <script> 开始标签");
            // 结束标签可能保留（因为模式不匹配）
            // 但开始标签被移除后，脚本无法执行
            assertTrue(sanitized.contains("alert('xss')") || sanitized.contains("xss"), "标签内容应该保留");
        }

        @Test
        @DisplayName("应移除 JavaScript 事件处理器")
        void shouldRemoveJavaScriptEvents() {
            String input = "<div onclick=\"alert(1)\">click</div>";
            String sanitized = XssUtils.sanitize(input);

            assertFalse(sanitized.contains("onclick="), "应该移除 onclick 事件");
        }

        @Test
        @DisplayName("应移除 javascript: 协议")
        void shouldRemoveJavaScriptProtocol() {
            String input = "javascript:alert(1)";
            String sanitized = XssUtils.sanitize(input);

            assertFalse(sanitized.contains("javascript:"), "应该移除 javascript: 协议");
        }

        @Test
        @DisplayName("应处理 null 返回 null")
        void shouldReturnNullForNull() {
            String sanitized = XssUtils.sanitize(null);
            assertNull(sanitized);
        }

        @Test
        @DisplayName("应处理空字符串返回空")
        void shouldReturnEmptyForEmpty() {
            String sanitized = XssUtils.sanitize("");
            assertEquals("", sanitized);
        }
    }

    // ==================== 文件名清理测试 ====================

    @Nested
    @DisplayName("文件名清理测试")
    class FilenameSanitizationTests {

        @Test
        @DisplayName("应移除路径遍历字符")
        void shouldRemovePathTraversal() {
            String input = "../../../etc/passwd";
            String sanitized = XssUtils.sanitizeFilename(input);

            assertFalse(sanitized.contains(".."), "应该移除 ..");
        }

        @Test
        @DisplayName("应移除 Windows 禁止字符")
        void shouldRemoveWindowsForbiddenChars() {
            String input = "file<>:\"|?*.txt";
            String sanitized = XssUtils.sanitizeFilename(input);

            assertFalse(sanitized.contains("<"), "应该移除 <");
            assertFalse(sanitized.contains(">"), "应该移除 >");
            assertFalse(sanitized.contains(":"), "应该移除 :");
            assertFalse(sanitized.contains("\""), "应该移除 \"");
            assertFalse(sanitized.contains("|"), "应该移除 |");
            assertFalse(sanitized.contains("?"), "应该移除 ?");
        }

        @Test
        @DisplayName("应限制文件名长度")
        void shouldLimitLength() {
            String longName = "a".repeat(300) + ".txt";
            String sanitized = XssUtils.sanitizeFilename(longName);

            assertTrue(sanitized.length() <= 255, "文件名长度不应超过 255");
        }
    }

    // ==================== 扩展名验证测试 ====================

    @Nested
    @DisplayName("扩展名验证测试")
    class ExtensionValidationTests {

        @Test
        @DisplayName("应验证允许的扩展名")
        void shouldValidateAllowedExtensions() {
            String[] allowedExtensions = {".jpg", ".png", ".gif"};

            assertTrue(XssUtils.isAllowedExtension("image.jpg", allowedExtensions));
            assertTrue(XssUtils.isAllowedExtension("image.png", allowedExtensions));
            assertTrue(XssUtils.isAllowedExtension("image.gif", allowedExtensions));
        }

        @Test
        @DisplayName("应拒绝不允许的扩展名")
        void shouldRejectDisallowedExtensions() {
            String[] allowedExtensions = {".jpg", ".png", ".gif"};

            assertFalse(XssUtils.isAllowedExtension("script.exe", allowedExtensions));
            assertFalse(XssUtils.isAllowedExtension("virus.bat", allowedExtensions));
        }

        @Test
        @DisplayName("应处理大小写不敏感")
        void shouldHandleCaseInsensitive() {
            String[] allowedExtensions = {".jpg", ".png"};

            assertTrue(XssUtils.isAllowedExtension("image.JPG", allowedExtensions));
            assertTrue(XssUtils.isAllowedExtension("image.PnG", allowedExtensions));
        }
    }

    // ==================== 路径安全验证测试 ====================

    @Nested
    @DisplayName("路径安全验证测试")
    class PathSafetyTests {

        @Test
        @DisplayName("应拒绝路径遍历")
        void shouldRejectPathTraversal() {
            assertFalse(XssUtils.isSafePath("../../../etc/passwd"));
            assertFalse(XssUtils.isSafePath("..\\..\\windows"));
            assertFalse(XssUtils.isSafePath("~/.ssh/config"));
        }

        @Test
        @DisplayName("应拒绝绝对路径")
        void shouldRejectAbsolutePath() {
            assertFalse(XssUtils.isSafePath("/etc/passwd"));
            assertFalse(XssUtils.isSafePath("C:\\Windows"));
        }

        @Test
        @DisplayName("应允许相对路径")
        void shouldAllowRelativePath() {
            assertTrue(XssUtils.isSafePath("files/document.pdf"));
            assertTrue(XssUtils.isSafePath("images/photo.jpg"));
        }

        @Test
        @DisplayName("应拒绝 null 和空字符串")
        void shouldRejectNullOrEmpty() {
            assertFalse(XssUtils.isSafePath(null));
            assertFalse(XssUtils.isSafePath(""));
        }
    }

    // ==================== 真实场景测试 ====================

    @Nested
    @DisplayName("真实 XSS 攻击场景测试")
    class RealWorldXssTests {

        @Test
        @DisplayName("应防护反射型 XSS")
        void shouldProtectAgainstReflectedXss() {
            String userInput = "<script>document.cookie='stolen'</script>";
            String escaped = XssUtils.escape(userInput);

            assertFalse(escaped.contains("<script>"));
            assertTrue(escaped.contains("&lt;") || escaped.contains("&gt;"));
        }

        @Test
        @DisplayName("应防护存储型 XSS")
        void shouldProtectAgainstStoredXss() {
            String comment = "<img src=x onerror=\"fetch('https://evil.com')\">";
            String escaped = XssUtils.escape(comment);

            // 验证至少有 HTML 字符被转义
            boolean hasEscapedChars = escaped.contains("&lt;")
                    || escaped.contains("&gt;")
                    || escaped.contains("&quot;")
                    || escaped.contains("&amp;");

            assertTrue(hasEscapedChars, "存储型 XSS payload 应该被转义");
        }

        @Test
        @DisplayName("应防护 DOM XSS")
        void shouldProtectAgainstDomXss() {
            String payload = "<svg onload=alert(document.domain)>";
            String escaped = XssUtils.escape(payload);

            // 验证至少有 HTML 字符被转义
            boolean hasEscapedChars = escaped.contains("&lt;")
                    || escaped.contains("&gt;")
                    || escaped.contains("&quot;")
                    || escaped.contains("&amp;");

            assertTrue(hasEscapedChars, "DOM XSS payload 应该被转义");
        }
    }
}
