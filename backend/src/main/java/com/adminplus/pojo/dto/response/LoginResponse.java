package com.adminplus.pojo.dto.response;

import java.util.List;

/**
 * 登录响应
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        UserResponse user,
        List<String> permissions
) {
}