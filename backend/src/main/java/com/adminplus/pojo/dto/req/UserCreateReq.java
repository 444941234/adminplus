package com.adminplus.pojo.dto.req;

import com.adminplus.utils.PasswordUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建用户请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record UserCreateReq(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 20, message = "密码长度必须在8-20之间")
        String password,

        @Size(max = 50, message = "昵称长度不能超过50")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        String email,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Size(max = 255, message = "头像URL长度不能超过255")
        String avatar,

        @Size(max = 50, message = "部门ID长度不能超过50")
        String deptId
) {
    public UserCreateReq {
        // 自定义验证：密码强度
        if (password != null && !PasswordUtils.isStrongPassword(password)) {
            throw new IllegalArgumentException(PasswordUtils.getPasswordStrengthHint(password));
        }
    }
}