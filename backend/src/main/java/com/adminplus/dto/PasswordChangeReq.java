package com.adminplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record PasswordChangeReq(
        @NotBlank(message = "原密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "新密码长度必须在6-20之间")
        String newPassword,

        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
) {
}