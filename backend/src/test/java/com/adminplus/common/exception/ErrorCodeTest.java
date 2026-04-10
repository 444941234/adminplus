package com.adminplus.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErrorCode 枚举测试
 *
 * @author AdminPlus
 * @since 2026-04-11
 */
@DisplayName("ErrorCode 测试")
class ErrorCodeTest {

    @Test
    @DisplayName("应该返回正确的错误码")
    void shouldReturnCorrectCode() {
        assertThat(ErrorCode.USER_NOT_FOUND.getCode()).isEqualTo(200101);
        assertThat(ErrorCode.AUTH_TOKEN_EXPIRED.getCode()).isEqualTo(210102);
        assertThat(ErrorCode.UNKNOWN_ERROR.getCode()).isEqualTo(100001);
    }

    @Test
    @DisplayName("应该返回正确的错误消息")
    void shouldReturnCorrectMessage() {
        assertThat(ErrorCode.USER_NOT_FOUND.getMessage()).isEqualTo("用户不存在");
        assertThat(ErrorCode.AUTH_CAPTCHA_WRONG.getMessage()).isEqualTo("验证码错误");
        assertThat(ErrorCode.UNKNOWN_ERROR.getMessage()).isEqualTo("系统异常");
    }

    @Test
    @DisplayName("应该根据错误码查找枚举")
    void shouldFindErrorCodeByCode() {
        assertThat(ErrorCode.fromCode(200101)).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(ErrorCode.fromCode(210102)).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED);
        assertThat(ErrorCode.fromCode(999999)).isEqualTo(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    @DisplayName("未知错误码应返回 UNKNOWN_ERROR")
    void shouldReturnUnknownErrorForUnknownCode() {
        assertThat(ErrorCode.fromCode(-1)).isEqualTo(ErrorCode.UNKNOWN_ERROR);
        assertThat(ErrorCode.fromCode(0)).isEqualTo(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    @DisplayName("所有错误码应该是唯一的")
    void allCodesShouldBeUnique() {
        var codes = java.util.stream.Stream.of(ErrorCode.values())
                .map(ErrorCode::getCode)
                .toList();
        var uniqueCodes = new java.util.HashSet<>(codes);
        assertThat(uniqueCodes.size()).isEqualTo(codes.size());
    }
}