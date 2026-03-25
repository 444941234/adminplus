package com.adminplus.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LogMaskingUtils 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
class LogMaskingUtilsTest {

    @Nested
    class MaskTests {
        @Test
        void mask_WithNullInput_ShouldReturnNull() {
            // When
            String result = LogMaskingUtils.mask(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void mask_WithEmptyInput_ShouldReturnEmpty() {
            // When
            String result = LogMaskingUtils.mask("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void mask_WithPassword_ShouldMaskPassword() {
            // Given
            String input = "password=admin123";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).doesNotContain("admin123");
            assertThat(result).contains("***");
        }

        @Test
        void mask_WithBearerToken_ShouldMaskToken() {
            // Given
            String input = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).contains("...");
            assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        }

        @Test
        void mask_WithIdCard_ShouldMaskIdCard() {
            // Given
            String input = "身份证号: 110101199001011234";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).contains("********");
            assertThat(result).doesNotContain("19900101");
        }

        @Test
        void mask_WithPhone_ShouldMaskPhone() {
            // Given
            String input = "手机号: 13812345678";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).contains("****");
            assertThat(result).doesNotContain("1234");
        }

        @Test
        void mask_WithEmail_ShouldMaskEmail() {
            // Given
            String input = "邮箱: testuser@example.com";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).contains("***");
            assertThat(result).doesNotContain("testuser");
        }

        @Test
        void mask_WithMultipleSensitiveFields_ShouldMaskAll() {
            // Given
            String input = "password=secret, phone: 13812345678, email: user@test.com";

            // When
            String result = LogMaskingUtils.mask(input);

            // Then
            assertThat(result).doesNotContain("secret");
            assertThat(result).doesNotContain("1234");
            assertThat(result).contains("***");
        }
    }

    @Nested
    class MaskUsernameTests {
        @Test
        void maskUsername_WithNullInput_ShouldReturnAsterisks() {
            // When
            String result = LogMaskingUtils.maskUsername(null);

            // Then
            assertThat(result).isEqualTo("***");
        }

        @Test
        void maskUsername_WithEmptyInput_ShouldReturnAsterisks() {
            // When
            String result = LogMaskingUtils.maskUsername("");

            // Then
            assertThat(result).isEqualTo("***");
        }

        @Test
        void maskUsername_WithShortUsername_ShouldReturnAsterisks() {
            // When
            String result = LogMaskingUtils.maskUsername("ab");

            // Then
            assertThat(result).isEqualTo("***");
        }

        @Test
        void maskUsername_WithNormalUsername_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskUsername("admin");

            // Then
            assertThat(result).isEqualTo("a***n");
        }

        @Test
        void maskUsername_WithLongUsername_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskUsername("administrator");

            // Then
            assertThat(result).isEqualTo("a***r");
            assertThat(result).doesNotContain("dministrato");
        }
    }

    @Nested
    class MaskPhoneTests {
        @Test
        void maskPhone_WithNullInput_ShouldReturnNull() {
            // When
            String result = LogMaskingUtils.maskPhone(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void maskPhone_WithInvalidLength_ShouldReturnOriginal() {
            // When
            String result = LogMaskingUtils.maskPhone("123456");

            // Then
            assertThat(result).isEqualTo("123456");
        }

        @Test
        void maskPhone_WithValidPhone_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskPhone("13812345678");

            // Then
            assertThat(result).isEqualTo("138****5678");
        }

        @Test
        void maskPhone_WithDifferentValidPhone_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskPhone("18800001111");

            // Then
            assertThat(result).isEqualTo("188****1111");
        }
    }

    @Nested
    class MaskEmailTests {
        @Test
        void maskEmail_WithNullInput_ShouldReturnNull() {
            // When
            String result = LogMaskingUtils.maskEmail(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void maskEmail_WithEmptyInput_ShouldReturnEmpty() {
            // When
            String result = LogMaskingUtils.maskEmail("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void maskEmail_WithValidEmail_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskEmail("testuser@example.com");

            // Then
            assertThat(result).isEqualTo("t***r@example.com");
        }

        @Test
        void maskEmail_WithShortUsername_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskEmail("ab@test.com");

            // Then
            assertThat(result).isEqualTo("***@test.com");
        }

        @Test
        void maskEmail_WithLongUsername_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskEmail("verylongusername@company.org");

            // Then
            assertThat(result).isEqualTo("v***e@company.org");
        }
    }

    @Nested
    class MaskTokenTests {
        @Test
        void maskToken_WithNullInput_ShouldReturnNull() {
            // When
            String result = LogMaskingUtils.maskToken(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void maskToken_WithEmptyInput_ShouldReturnEmpty() {
            // When
            String result = LogMaskingUtils.maskToken("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void maskToken_WithShortToken_ShouldReturnAsterisks() {
            // When
            String result = LogMaskingUtils.maskToken("shorttoken");

            // Then
            assertThat(result).isEqualTo("***");
        }

        @Test
        void maskToken_WithLongToken_ShouldMask() {
            // Given
            String token = "eyJhbGciOiJIUzI1NiJ9eyJzdWIiOiIxMjM0NTY3ODkwIn0TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";

            // When
            String result = LogMaskingUtils.maskToken(token);

            // Then
            assertThat(result).contains("...");
            assertThat(result).startsWith("eyJhbGci");
        }
    }

    @Nested
    class MaskIpTests {
        @Test
        void maskIp_WithNullInput_ShouldReturnNull() {
            // When
            String result = LogMaskingUtils.maskIp(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void maskIp_WithEmptyInput_ShouldReturnEmpty() {
            // When
            String result = LogMaskingUtils.maskIp("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void maskIp_WithValidIp_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskIp("192.168.1.100");

            // Then
            assertThat(result).isEqualTo("192.168.*.*");
        }

        @Test
        void maskIp_WithInvalidFormat_ShouldReturnOriginal() {
            // When
            String result = LogMaskingUtils.maskIp("192.168.1");

            // Then
            assertThat(result).isEqualTo("192.168.1");
        }

        @Test
        void maskIp_WithLocalhost_ShouldMask() {
            // When
            String result = LogMaskingUtils.maskIp("127.0.0.1");

            // Then
            assertThat(result).isEqualTo("127.0.*.*");
        }

        @Test
        void maskIp_WithNonNumericParts_ShouldReturnOriginal() {
            // When
            String result = LogMaskingUtils.maskIp("abc.def.ghi.jkl");

            // Then
            assertThat(result).isEqualTo("abc.def.*.*");
        }
    }
}