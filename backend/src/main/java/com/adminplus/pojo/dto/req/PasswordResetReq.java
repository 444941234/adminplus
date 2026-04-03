package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求 DTO (管理员重置其他用户密码)
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public record PasswordResetReq(
        @NotBlank(message = "新密码不能为空")
        @Size(min = 12, max = 128, message = "新密码长度必须在12-128之间")
        String password
) {
}
