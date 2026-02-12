package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record UserLoginReq(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password,

        @NotBlank(message = "验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "验证码不能为空")
        String captchaCode
) {
}