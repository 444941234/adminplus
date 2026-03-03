package com.adminplus.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordUtils 测试类
 * <p>
 * 测试密码强度验证、密码强度描述和密码强度提示信息功能
 * <p>
 * 测试策略：
 * - 强密码测试：验证符合所有要求的密码
 * - 弱密码测试：验证不符合要求的密码
 * - 边界条件测试：验证边界值
 * - 空值测试：验证空值和null处理
 * - 提示信息测试：验证密码强度提示信息的准确性
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@DisplayName("密码工具类测试")
class PasswordUtilsTest {

    // ==================== 强密码测试 ====================

    @Nested
    @DisplayName("强密码验证测试")
    class StrongPasswordTests {

        @Test
        @DisplayName("应验证包含所有要素的强密码 - 大小写字母+数字+特殊字符+12位以上")
        void shouldVerifyStrongPasswordWithAllRequirements() {
            // 包含所有要素的最小长度强密码 (12位)
            String strongPassword = "Aa1!aaaa1234";
            assertTrue(PasswordUtils.isStrongPassword(strongPassword),
                    "12位密码包含大小写字母、数字和特殊字符应被识别为强密码");

            // 更长的强密码
            String longerStrongPassword = "StrongP@ssw0rd";
            assertTrue(PasswordUtils.isStrongPassword(longerStrongPassword),
                    "更长且符合所有要素的密码应被识别为强密码");

            // 包含多个特殊字符的强密码
            String multipleSpecialChars = "Aa1!@#$%12345";
            assertTrue(PasswordUtils.isStrongPassword(multipleSpecialChars),
                    "包含多个特殊字符的密码应被识别为强密码");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password123!@", // 标准强密码 (12位)
                "P@ssw0rd12345",  // 12位强密码
                "Secure#Pass2023", // 长强密码
                "MyP@ss123456",   // 包含常见特殊字符
                "A1b2C3d!eFgH",   // 符合所有要求
                "Test@12345678",  // 常见格式
                "Admin#2023XYZ",  // 包含数字和字母
                "Welcome$123Abc", // 长密码
                "Xy9*uuuu1234",   // 12位边界
                "Abc123!@#def"    // 多个特殊字符
        })
        @DisplayName("应验证各种有效的强密码格式")
        void shouldVerifyVariousStrongPasswordFormats(String password) {
            assertTrue(PasswordUtils.isStrongPassword(password),
                    String.format("密码 '%s' 应被识别为强密码", password));
        }

        @Test
        @DisplayName("应验证包含不同特殊字符的强密码")
        void shouldVerifyStrongPasswordWithDifferentSpecialCharacters() {
            // 测试各种特殊字符 - 使用12字符最小长度
            assertTrue(PasswordUtils.isStrongPassword("Aa1!aaaa1234"), "感叹号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1@aaaa1234"), "@符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1#aaaa1234"), "#符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1$aaaa1234"), "$符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1%aaaa1234"), "%符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1^aaaa1234"), "^符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1&aaaa1234"), "&符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1*aaaa1234"), "*符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1(aaaa1234"), "左括号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1)aaaa1234"), "右括号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1_aaaa1234"), "下划线");
            assertTrue(PasswordUtils.isStrongPassword("Aa1+aaaa1234"), "+符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1-aaaa1234"), "-符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1=aaaa1234"), "=符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1[aaaa1234"), "[符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1]aaaa1234"), "]符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1{aaaa1234"), "{符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1}aaaa1234"), "}符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1'aaaa1234"), "单引号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1\"aaaa1234"), "双引号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1|aaaa1234"), "|符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1,aaaa1234"), ",符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1.aaaa1234"), ".符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1<aaaa1234"), "<符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1>aaaa1234"), ">符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1/aaaa1234"), "/符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1?aaaa1234"), "?符号");
            assertTrue(PasswordUtils.isStrongPassword("Aa1\\aaaa1234"), "反斜杠");
        }
    }

    // ==================== 弱密码测试 ====================

    @Nested
    @DisplayName("弱密码验证测试")
    class WeakPasswordTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "password",      // 只有小写字母
                "PASSWORD",      // 只有大写字母
                "12345678",      // 只有数字
                "!@#$%^&*",      // 只有特殊字符
                "Pass123",       // 6位，缺少特殊字符
                "PASS123!",      // 缺少小写字母
                "pass123!",      // 缺少大写字母
                "Password!",     // 缺少数字
                "P@ssw0d",       // 7位
                "Aa1!",          // 4位
                "abc",           // 3位
                "Ab1!",          // 4位
                "A1!aaaa",       // 缺少小写字母
                "a1!aaaa",       // 缺少大写字母
                "Aa!aaaa",       // 缺少数字
                "Aa1aaaa",       // 缺少特殊字符
        })
        @DisplayName("应拒绝不符合要求的弱密码")
        void shouldRejectWeakPasswords(String password) {
            assertFalse(PasswordUtils.isStrongPassword(password),
                    String.format("密码 '%s' 不应被识别为强密码", password));
        }

        @Test
        @DisplayName("应拒绝缺少数字的密码")
        void shouldRejectPasswordWithoutDigits() {
            String noDigitPassword = "Password!";
            assertFalse(PasswordUtils.isStrongPassword(noDigitPassword),
                    "缺少数字的密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应拒绝缺少小写字母的密码")
        void shouldRejectPasswordWithoutLowerCase() {
            String noLowerCasePassword = "PASSWORD1!";
            assertFalse(PasswordUtils.isStrongPassword(noLowerCasePassword),
                    "缺少小写字母的密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应拒绝缺少大写字母的密码")
        void shouldRejectPasswordWithoutUpperCase() {
            String noUpperCasePassword = "password1!";
            assertFalse(PasswordUtils.isStrongPassword(noUpperCasePassword),
                    "缺少大写字母的密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应拒绝缺少特殊字符的密码")
        void shouldRejectPasswordWithoutSpecialChar() {
            String noSpecialCharPassword = "Password1";
            assertFalse(PasswordUtils.isStrongPassword(noSpecialCharPassword),
                    "缺少特殊字符的密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应拒绝长度不足12位的密码")
        void shouldRejectPasswordWithInsufficientLength() {
            String shortPassword = "Aa1!aaaaa";
            assertFalse(PasswordUtils.isStrongPassword(shortPassword),
                    "长度不足12位的密码不应被识别为强密码");
        }
    }

    // ==================== 中等密码测试 ====================

    @Nested
    @DisplayName("中等密码验证测试")
    class MediumPasswordTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "123456",        // 6位数字
                "abcdef",        // 6位小写字母
                "ABCDEF",        // 6位大写字母
                "Abc123",        // 6位混合
                "Pass123",       // 7位
                "Password1",     // 9位
                "VeryLongPassword123",  // 长密码
        })
        @DisplayName("应验证符合长度要求的中等密码")
        void shouldVerifyMediumPasswords(String password) {
            assertTrue(PasswordUtils.isMediumPassword(password),
                    String.format("密码 '%s' 应被识别为中等密码", password));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345",         // 5位
                "abcde",         // 5位
                "Abc12",         // 5位
                "A1!",           // 3位
                "a",             // 1位
        })
        @DisplayName("应拒绝长度不足6位的密码")
        void shouldRejectShortPasswordsForMedium(String password) {
            assertFalse(PasswordUtils.isMediumPassword(password),
                    String.format("密码 '%s' 不应被识别为中等密码", password));
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @Test
        @DisplayName("应验证恰好12位的强密码")
        void shouldVerifyExactly12CharactersStrongPassword() {
            String exact12Chars = "Aa1!aaaa1234";
            assertTrue(PasswordUtils.isStrongPassword(exact12Chars),
                    "恰好12位且符合所有要求的密码应为强密码");
        }

        @Test
        @DisplayName("应拒绝11位密码")
        void shouldReject11CharactersPassword() {
            String password11Chars = "Aa1!aaa1234";
            assertFalse(PasswordUtils.isStrongPassword(password11Chars),
                    "11位密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应验证恰好6位的中等密码")
        void shouldVerifyExactly6CharactersMediumPassword() {
            String exact6Chars = "123456";
            assertTrue(PasswordUtils.isMediumPassword(exact6Chars),
                    "恰好6位的密码应为中等密码");
        }

        @Test
        @DisplayName("应拒绝5位密码")
        void shouldReject5CharactersPassword() {
            String password5Chars = "12345";
            assertFalse(PasswordUtils.isMediumPassword(password5Chars),
                    "5位密码不应被识别为中等密码");
        }

        @Test
        @DisplayName("应验证包含最小字符集的12位密码")
        void shouldVerifyPasswordWithMinimumCharacterSet() {
            // 最小字符集：1个大写、1个小写、1个数字、1个特殊字符 + 8个任意字符 = 12位
            String minCharSet = "Aa1!bbbb1234";
            assertTrue(PasswordUtils.isStrongPassword(minCharSet),
                    "包含最小字符集的12位密码应为强密码");
        }

        @Test
        @DisplayName("应验证最大长度密码")
        void shouldVerifyMaximumLengthPassword() {
            // 最大128字符
            String maxPassword = "Aa1!" + "a".repeat(124);
            assertTrue(PasswordUtils.isStrongPassword(maxPassword),
                    "恰好128位且符合所有要求的密码应为强密码");
        }

        @Test
        @DisplayName("应拒绝超过最大长度的密码")
        void shouldRejectPasswordOverMaximumLength() {
            String tooLongPassword = "Aa1!" + "a".repeat(130);
            assertFalse(PasswordUtils.isStrongPassword(tooLongPassword),
                    "超过128位的密码不应被识别为强密码");
        }

        @Test
        @DisplayName("应验证只有1个特殊字符的密码")
        void shouldVerifyPasswordWithSingleSpecialChar() {
            String singleSpecialChar = "Aa1aaaa!1234";
            assertTrue(PasswordUtils.isStrongPassword(singleSpecialChar),
                    "只有1个特殊字符的密码应为强密码");
        }

        @Test
        @DisplayName("应验证只有1个数字的密码")
        void shouldVerifyPasswordWithSingleDigit() {
            String singleDigit = "Aa!aaaaa1234";
            assertTrue(PasswordUtils.isStrongPassword(singleDigit),
                    "只有1个数字的密码应为强密码");
        }

        @Test
        @DisplayName("应验证只有1个大写字母的密码")
        void shouldVerifyPasswordWithSingleUpperCase() {
            String singleUpperCase = "1a!aaaaa1234A";
            assertTrue(PasswordUtils.isStrongPassword(singleUpperCase),
                    "只有1个大写字母的密码应为强密码");
        }

        @Test
        @DisplayName("应验证只有1个小写字母的密码")
        void shouldVerifyPasswordWithSingleLowerCase() {
            String singleLowerCase = "1A!aaaaa1234";
            assertTrue(PasswordUtils.isStrongPassword(singleLowerCase),
                    "只有1个小写字母的密码应为强密码");
        }
    }

    // ==================== 空值测试 ====================

    @Nested
    @DisplayName("空值和null测试")
    class NullAndEmptyTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("应拒绝null和空字符串作为强密码")
        void shouldRejectNullOrEmptyForStrongPassword(String password) {
            assertFalse(PasswordUtils.isStrongPassword(password),
                    "null或空字符串不应被识别为强密码");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("应拒绝null和空字符串作为中等密码")
        void shouldRejectNullOrEmptyForMediumPassword(String password) {
            assertFalse(PasswordUtils.isMediumPassword(password),
                    "null或空字符串不应被识别为中等密码");
        }

        @Test
        @DisplayName("应返回空密码错误信息")
        void shouldReturnErrorMessageForEmptyPassword() {
            assertEquals("密码不能为空", PasswordUtils.getPasswordStrength(""));
            assertEquals("密码不能为空", PasswordUtils.getPasswordStrength(null));
            // getPasswordStrengthHint 现在返回 int 错误代码
            assertEquals(1, PasswordUtils.getPasswordStrengthHint(""));
            assertEquals(1, PasswordUtils.getPasswordStrengthHint(null));
        }

        @Test
        @DisplayName("应拒绝空白字符串")
        void shouldRejectBlankStrings() {
            String blankPassword = "   ";
            assertFalse(PasswordUtils.isStrongPassword(blankPassword),
                    "空白字符串不应被识别为强密码");
            // 空白字符串长度为3，不是中等密码（最小6位）
            assertFalse(PasswordUtils.isMediumPassword(blankPassword),
                    "空白字符串（3个空格）不应为中等密码");
            assertEquals("弱", PasswordUtils.getPasswordStrength(blankPassword));
        }
    }

    // ==================== 密码强度描述测试 ====================

    @Nested
    @DisplayName("密码强度描述测试")
    class PasswordStrengthDescriptionTests {

        @Test
        @DisplayName("应正确描述强密码")
        void shouldDescribeStrongPassword() {
            String strongPassword = "StrongP@ssw0rd";
            assertEquals("强", PasswordUtils.getPasswordStrength(strongPassword));
        }

        @Test
        @DisplayName("应正确描述中等密码")
        void shouldDescribeMediumPassword() {
            String mediumPassword = "Pass123";
            assertEquals("中", PasswordUtils.getPasswordStrength(mediumPassword));
        }

        @Test
        @DisplayName("应正确描述弱密码")
        void shouldDescribeWeakPassword() {
            String weakPassword = "123";
            assertEquals("弱", PasswordUtils.getPasswordStrength(weakPassword));
        }

        @Test
        @DisplayName("应正确描述6位中等密码")
        void shouldDescribe6CharMediumPassword() {
            String password = "123456";
            assertEquals("中", PasswordUtils.getPasswordStrength(password),
                    "6位密码应被描述为中等强度");
        }

        @Test
        @DisplayName("应正确描述7位符合部分要求的密码")
        void shouldDescribe7CharPartialRequirementPassword() {
            String password = "Pass123";  // 7位，缺少特殊字符
            assertEquals("中", PasswordUtils.getPasswordStrength(password),
                    "7位但缺少特殊字符的密码应被描述为中等强度");
        }
    }

    // ==================== 密码强度提示信息测试 ====================

    @Nested
    @DisplayName("密码强度提示信息测试")
    class PasswordStrengthHintTests {

        @Test
        @DisplayName("应返回符合要求密码的成功提示")
        void shouldReturnSuccessHintForValidPassword() {
            String validPassword = "StrongP@ssw0rd";
            assertEquals(0, PasswordUtils.getPasswordStrengthHint(validPassword));
        }

        @Test
        @DisplayName("应返回长度不足的提示")
        void shouldReturnHintForShortPassword() {
            String shortPassword = "Aa1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(shortPassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertTrue(hint.contains("密码长度至少12位"),
                    "提示应包含长度要求信息");
        }

        @Test
        @DisplayName("应返回缺少数字的提示")
        void shouldReturnHintForMissingDigit() {
            String noDigitPassword = "Password!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noDigitPassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertTrue(hint.contains("密码必须包含数字"),
                    "提示应包含缺少数字的信息");
        }

        @Test
        @DisplayName("应返回缺少小写字母的提示")
        void shouldReturnHintForMissingLowerCase() {
            String noLowerCasePassword = "PASSWORD1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noLowerCasePassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertTrue(hint.contains("密码必须包含小写字母"),
                    "提示应包含缺少小写字母的信息");
        }

        @Test
        @DisplayName("应返回缺少大写字母的提示")
        void shouldReturnHintForMissingUpperCase() {
            String noUpperCasePassword = "password1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noUpperCasePassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertTrue(hint.contains("密码必须包含大写字母"),
                    "提示应包含缺少大写字母的信息");
        }

        @Test
        @DisplayName("应返回缺少特殊字符的提示")
        void shouldReturnHintForMissingSpecialChar() {
            String noSpecialCharPassword = "Password1";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noSpecialCharPassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertTrue(hint.contains("密码必须包含特殊字符"),
                    "提示应包含缺少特殊字符的信息");
        }

        @Test
        @DisplayName("应返回多个缺失要求的提示")
        void shouldReturnMultipleHintsForMultipleMissingRequirements() {
            String weakPassword = "pass";  // 缺少大写、数字、特殊字符、长度不足
            int errorCode = PasswordUtils.getPasswordStrengthHint(weakPassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);

            assertTrue(hint.contains("密码长度至少12位"),
                    "提示应包含长度要求");
            assertTrue(hint.contains("密码必须包含数字"),
                    "提示应包含数字要求");
            assertTrue(hint.contains("密码必须包含大写字母"),
                    "提示应包含大写字母要求");
            assertTrue(hint.contains("密码必须包含特殊字符"),
                    "提示应包含特殊字符要求");
        }

        @Test
        @DisplayName("应验证提示信息不以分号结尾")
        void shouldVerifyHintDoesNotEndWithSemicolon() {
            String invalidPassword = "pass";
            int errorCode = PasswordUtils.getPasswordStrengthHint(invalidPassword);
            String hint = PasswordUtils.getErrorMessage(errorCode);
            assertFalse(hint.endsWith(";"),
                    "提示信息不应以分号结尾");
        }

        @Test
        @DisplayName("应验证所有要求都缺失时的提示")
        void shouldVerifyHintWhenAllRequirementsMissing() {
            String allMissing = "a";  // 只有1个小写字母
            int errorCode = PasswordUtils.getPasswordStrengthHint(allMissing);
            String hint = PasswordUtils.getErrorMessage(errorCode);

            // 应该包含除了小写字母外的所有要求
            assertTrue(hint.contains("密码长度至少12位"));
            assertTrue(hint.contains("密码必须包含数字"));
            assertTrue(hint.contains("密码必须包含大写字母"));
            assertTrue(hint.contains("密码必须包含特殊字符"));

            // 不应包含小写字母要求（因为已有）
            assertFalse(hint.contains("密码必须包含小写字母"));
        }
    }

    // ==================== 特殊场景测试 ====================

    @Nested
    @DisplayName("特殊场景测试")
    class SpecialScenarioTests {

        @Test
        @DisplayName("应验证包含中文的密码")
        void shouldVerifyPasswordWithChineseCharacters() {
            // 包含中文的密码（虽然有中文，但包含所有必需元素）
            String chinesePassword = "Aa1!中文密码很安全ab";
            assertTrue(PasswordUtils.isStrongPassword(chinesePassword),
                    "包含中文但符合所有其他要求的密码应为强密码");
        }

        @Test
        @DisplayName("应验证包含空格的密码")
        void shouldVerifyPasswordWithSpaces() {
            String spacePassword = "Aa1! aaaa1234bb";
            assertTrue(PasswordUtils.isStrongPassword(spacePassword),
                    "包含空格但符合所有要求的密码应为强密码");
        }

        @Test
        @DisplayName("应验证只有数字和字母的密码不为强密码")
        void shouldVerifyAlphanumericPasswordIsNotStrong() {
            String alphanumericPassword = "Password12345";
            assertFalse(PasswordUtils.isStrongPassword(alphanumericPassword),
                    "只有数字和字母的密码不应为强密码");
            assertEquals("中", PasswordUtils.getPasswordStrength(alphanumericPassword),
                    "只有数字和字母的长密码应为中等强度");
        }

        @Test
        @DisplayName("应验证连续相同字符的密码")
        void shouldVerifyPasswordWithRepeatedCharacters() {
            String repeatedPassword = "Aa1!aaaaaaaa";
            assertTrue(PasswordUtils.isStrongPassword(repeatedPassword),
                    "包含重复字符但符合所有要求的密码应为强密码");
        }

        @Test
        @DisplayName("应验证常用模式密码仍需符合要求")
        void shouldVerifyCommonPatternPasswordsMustMeetRequirements() {
            // 常见键盘模式 (12位)
            String qwertyPassword = "Qwerty1!1234";
            assertTrue(PasswordUtils.isStrongPassword(qwertyPassword),
                    "即使是常见模式，符合要求的密码仍应为强密码");

            // 数字序列 (12位)
            String sequentialPassword = "Aa1!1234567890";
            assertTrue(PasswordUtils.isStrongPassword(sequentialPassword),
                    "包含数字序列但符合要求的密码应为强密码");
        }

        @Test
        @DisplayName("应验证边界情况：只有特殊字符和数字")
        void shouldVerifyPasswordWithOnlySpecialCharsAndDigits() {
            String specialAndDigits = "1234!@#$";
            assertFalse(PasswordUtils.isStrongPassword(specialAndDigits),
                    "只有特殊字符和数字的密码不应为强密码");
        }

        @Test
        @DisplayName("应验证边界情况：只有特殊字符和字母")
        void shouldVerifyPasswordWithOnlySpecialCharsAndLetters() {
            String specialAndLetters = "Password!";
            assertFalse(PasswordUtils.isStrongPassword(specialAndLetters),
                    "只有特殊字符和字母的密码不应为强密码");
        }
    }

    // ==================== 方法源测试 ====================

    @Nested
    @DisplayName("参数化测试 - 方法源")
    class MethodSourceTests {

        static Stream<TestPassword> providePasswordTestCases() {
            return Stream.of(
                    // 强密码用例 (12位以上)
                    new TestPassword("StrongP@ssw0rd", true, true, "强"),
                    new TestPassword("Aa1!aaaa1234", true, true, "强"),
                    new TestPassword("Password123!@", true, true, "强"),

                    // 中等密码用例
                    new TestPassword("Pass123", false, true, "中"),
                    new TestPassword("123456", false, true, "中"),
                    new TestPassword("abcdef", false, true, "中"),

                    // 弱密码用例
                    new TestPassword("123", false, false, "弱"),
                    new TestPassword("abc", false, false, "弱"),
                    new TestPassword("Ab1!", false, false, "弱")
            );
        }

        @ParameterizedTest
        @MethodSource("providePasswordTestCases")
        @DisplayName("应验证密码强度综合测试用例")
        void shouldVerifyPasswordStrengthComprehensive(TestPassword testCase) {
            assertEquals(testCase.isStrong, PasswordUtils.isStrongPassword(testCase.password),
                    String.format("密码 '%s' 的强密码验证结果不匹配", testCase.password));
            assertEquals(testCase.isMedium, PasswordUtils.isMediumPassword(testCase.password),
                    String.format("密码 '%s' 的中等密码验证结果不匹配", testCase.password));
            assertEquals(testCase.strength, PasswordUtils.getPasswordStrength(testCase.password),
                    String.format("密码 '%s' 的强度描述不匹配", testCase.password));
        }

        // 测试用例数据类
        static class TestPassword {
            final String password;
            final boolean isStrong;
            final boolean isMedium;
            final String strength;

            TestPassword(String password, boolean isStrong, boolean isMedium, String strength) {
                this.password = password;
                this.isStrong = isStrong;
                this.isMedium = isMedium;
                this.strength = strength;
            }
        }
    }

    // ==================== 性能测试 ====================

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("应快速验证密码强度")
        void shouldVerifyPasswordStrengthQuickly() {
            String password = "StrongP@ssw0rd";
            long startTime = System.nanoTime();

            for (int i = 0; i < 10000; i++) {
                PasswordUtils.isStrongPassword(password);
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // 10000次验证应在100ms内完成
            assertTrue(duration < 100_000_000,
                    String.format("10000次密码验证耗时 %d ns，超过100ms", duration));
        }

        @Test
        @DisplayName("应快速生成密码强度提示")
        void shouldGeneratePasswordHintQuickly() {
            String password = "pass";  // 弱密码，会生成所有错误代码
            long startTime = System.nanoTime();

            for (int i = 0; i < 10000; i++) {
                PasswordUtils.getPasswordStrengthHint(password);
                PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(password));
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // 10000次提示生成应在100ms内完成
            assertTrue(duration < 100_000_000,
                    String.format("10000次提示生成耗时 %d ns，超过100ms", duration));
        }
    }

    // ==================== 新功能测试 - 错误代码和错误消息 ====================

    @Nested
    @DisplayName("错误代码和错误消息测试")
    class ErrorMessageTests {

        @Test
        @DisplayName("应验证无错误时返回成功消息")
        void shouldReturnSuccessMessageForNoError() {
            assertEquals("密码强度符合要求",
                    PasswordUtils.getErrorMessage(0));
        }

        @Test
        @DisplayName("应验证空密码错误代码")
        void shouldReturnErrorCodeForEmptyPassword() {
            assertEquals(1, PasswordUtils.getPasswordStrengthHint(""));
            assertEquals(1, PasswordUtils.getPasswordStrengthHint(null));
        }

        @Test
        @DisplayName("应验证长度不足错误代码")
        void shouldReturnErrorCodeForShortPassword() {
            String shortPassword = "Aa1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(shortPassword);

            assertTrue((errorCode & 0x01) != 0,
                    "长度不足应设置0x01位");
        }

        @Test
        @DisplayName("应验证长度超限错误代码")
        void shouldReturnErrorCodeForOverMaxLengthPassword() {
            String base = "Aa1!";
            String padding = "a".repeat(125);
            String tooLongPassword = base + padding;

            int errorCode = PasswordUtils.getPasswordStrengthHint(tooLongPassword);

            assertTrue((errorCode & 0x02) != 0,
                    "长度超限应设置0x02位");
        }

        @Test
        @DisplayName("应验证缺少数字错误代码")
        void shouldReturnErrorCodeForMissingDigit() {
            String noDigitPassword = "Password!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noDigitPassword);

            assertTrue((errorCode & 0x04) != 0,
                    "缺少数字应设置0x04位");
        }

        @Test
        @DisplayName("应验证缺少小写字母错误代码")
        void shouldReturnErrorCodeForMissingLowerCase() {
            String noLowerCasePassword = "PASSWORD1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noLowerCasePassword);

            assertTrue((errorCode & 0x08) != 0,
                    "缺少小写字母应设置0x08位");
        }

        @Test
        @DisplayName("应验证缺少大写字母错误代码")
        void shouldReturnErrorCodeForMissingUpperCase() {
            String noUpperCasePassword = "password1!";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noUpperCasePassword);

            assertTrue((errorCode & 0x10) != 0,
                    "缺少大写字母应设置0x10位");
        }

        @Test
        @DisplayName("应验证缺少特殊字符错误代码")
        void shouldReturnErrorCodeForMissingSpecialChar() {
            String noSpecialCharPassword = "Password1";
            int errorCode = PasswordUtils.getPasswordStrengthHint(noSpecialCharPassword);

            assertTrue((errorCode & 0x20) != 0,
                    "缺少特殊字符应设置0x20位");
        }

        @Test
        @DisplayName("应验证复合错误代码")
        void shouldReturnCompositeErrorCode() {
            // 只有1个小写字母 - 缺少长度、数字、大写、特殊字符
            String weakPassword = "a";
            int errorCode = PasswordUtils.getPasswordStrengthHint(weakPassword);

            assertTrue((errorCode & 0x01) != 0, "应设置长度不足位");
            assertTrue((errorCode & 0x04) != 0, "应设置缺少数字位");
            assertTrue((errorCode & 0x10) != 0, "应设置缺少大写字母位");
            assertTrue((errorCode & 0x20) != 0, "应设置缺少特殊字符位");
            assertFalse((errorCode & 0x08) != 0, "不应设置缺少小写字母位");
        }

        @Test
        @DisplayName("应验证错误消息格式正确")
        void shouldVerifyErrorMessageFormat() {
            int errorCode = 0x01 | 0x04 | 0x10; // 长度不足+缺少数字+缺少大写
            String message = PasswordUtils.getErrorMessage(errorCode);

            assertTrue(message.contains("密码长度至少12位"));
            assertTrue(message.contains("密码必须包含数字"));
            assertTrue(message.contains("密码必须包含大写字母"));
            assertFalse(message.endsWith(";"), "消息不应以分号结尾");
        }

        @Test
        @DisplayName("应验证128位最大长度限制")
        void shouldVerify128MaxLengthLimit() {
            // 测试恰好128位
            String maxPassword = "Aa1!" + "a".repeat(124);
            assertEquals(128, maxPassword.length());
            assertTrue(PasswordUtils.isStrongPassword(maxPassword));

            // 测试129位（超过限制）
            String tooLongPassword = "Aa1!" + "a".repeat(125);
            assertEquals(129, tooLongPassword.length());
            assertFalse(PasswordUtils.isStrongPassword(tooLongPassword));
        }
    }
}
