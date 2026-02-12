package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 登录响应
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record LoginResp(
        String token,
        String refreshToken,
        String tokenType,
        UserResp user,
        List<String> permissions
) {
}