package com.adminplus.pojo.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 Token 请求
 *
 * @param refreshToken 刷新令牌
 * @author AdminPlus
 * @since 2026-02-06
 */
public record RefreshTokenRequest(
        @NotBlank(message = "刷新令牌不能为空")
        String refreshToken
) {
}