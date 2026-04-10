package com.adminplus.utils;

import com.adminplus.common.exception.BizException;
import com.adminplus.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ServiceAssert 断言工具测试
 *
 * @author AdminPlus
 * @since 2026-04-11
 */
@DisplayName("ServiceAssert 测试")
class ServiceAssertTest {

    @Nested
    @DisplayName("使用 ErrorCode 的断言方法")
    class ErrorCodeAssertTests {

        @Test
        @DisplayName("isTrue 条件为真时不抛异常")
        void isTrueShouldNotThrowWhenConditionIsTrue() {
            ServiceAssert.isTrue(true, ErrorCode.USER_NOT_FOUND);
            // 不抛异常即通过
        }

        @Test
        @DisplayName("isTrue 条件为假时抛出 BizException")
        void isTrueShouldThrowWhenConditionIsFalse() {
            assertThatThrownBy(() -> ServiceAssert.isTrue(false, ErrorCode.USER_NOT_FOUND))
                    .isInstanceOf(BizException.class)
                    .hasMessage("用户不存在")
                    .extracting("code").isEqualTo(200101);
        }

        @Test
        @DisplayName("notNull 对象不为空时不抛异常")
        void notNullShouldNotThrowWhenObjectNotNull() {
            ServiceAssert.notNull("test", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("notNull 对象为空时抛出 BizException")
        void notNullShouldThrowWhenObjectIsNull() {
            assertThatThrownBy(() -> ServiceAssert.notNull(null, ErrorCode.ROLE_NOT_FOUND))
                    .isInstanceOf(BizException.class)
                    .hasMessage("角色不存在");
        }

        @Test
        @DisplayName("notExists 不存在时不抛异常")
        void notExistsShouldNotThrowWhenNotExists() {
            ServiceAssert.notExists(false, ErrorCode.USER_USERNAME_EXISTS);
        }

        @Test
        @DisplayName("notExists 存在时抛出 BizException")
        void notExistsShouldThrowWhenExists() {
            assertThatThrownBy(() -> ServiceAssert.notExists(true, ErrorCode.USER_USERNAME_EXISTS))
                    .isInstanceOf(BizException.class)
                    .hasMessage("用户名已存在");
        }

        @Test
        @DisplayName("exists 存在时不抛异常")
        void existsShouldNotThrowWhenExists() {
            ServiceAssert.exists(true, ErrorCode.DEPT_NOT_FOUND);
        }

        @Test
        @DisplayName("exists 不存在时抛出 BizException")
        void existsShouldThrowWhenNotExists() {
            assertThatThrownBy(() -> ServiceAssert.exists(false, ErrorCode.DEPT_NOT_FOUND))
                    .isInstanceOf(BizException.class)
                    .hasMessage("部门不存在");
        }

        @Test
        @DisplayName("fail 直接抛出 BizException")
        void failShouldThrowBizException() {
            assertThatThrownBy(() -> ServiceAssert.fail(ErrorCode.AUTH_PERMISSION_DENIED))
                    .isInstanceOf(BizException.class);
        }

        @Test
        @DisplayName("fail 带额外信息抛出 BizException")
        void failWithAdditionalInfoShouldThrowBizException() {
            assertThatThrownBy(() -> ServiceAssert.fail(ErrorCode.USER_NOT_FOUND, "userId=123"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("用户不存在")
                    .hasMessageContaining("userId=123");
        }
    }

    @Nested
    @DisplayName("使用 String 消息的断言方法（兼容旧代码）")
    class StringMessageAssertTests {

        @Test
        @DisplayName("isTrue 条件为假时使用字符串消息")
        void isTrueWithStringMessageShouldThrow() {
            assertThatThrownBy(() -> ServiceAssert.isTrue(false, "自定义错误消息"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("自定义错误消息")
                    .extracting("code").isEqualTo(400);
        }

        @Test
        @DisplayName("isTrue 带错误码使用字符串消息")
        void isTrueWithCodeAndStringMessageShouldThrow() {
            assertThatThrownBy(() -> ServiceAssert.isTrue(false, 404, "资源不存在"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("资源不存在")
                    .extracting("code").isEqualTo(404);
        }

        @Test
        @DisplayName("fail 使用字符串消息")
        void failWithStringMessageShouldThrow() {
            assertThatThrownBy(() -> ServiceAssert.fail("操作失败"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("操作失败");
        }
    }

    @Nested
    @DisplayName("BizException ErrorCode 支持")
    class BizExceptionErrorCodeTests {

        @Test
        @DisplayName("BizException 应持有 ErrorCode")
        void bizExceptionShouldHoldErrorCode() {
            BizException exception = new BizException(ErrorCode.USER_NOT_FOUND);
            assertThat(exception.hasStandardErrorCode()).isTrue();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            assertThat(exception.getCode()).isEqualTo(200101);
        }

        @Test
        @DisplayName("使用字符串消息的 BizException 不持有 ErrorCode")
        void bizExceptionWithStringMessageShouldNotHoldErrorCode() {
            BizException exception = new BizException("自定义消息");
            assertThat(exception.hasStandardErrorCode()).isFalse();
            assertThat(exception.getErrorCode()).isNull();
        }
    }
}