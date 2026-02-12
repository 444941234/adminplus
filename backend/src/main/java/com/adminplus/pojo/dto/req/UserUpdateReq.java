package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新用户请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public record UserUpdateReq(
        @Size(max = 50, message = "昵称长度不能超过50")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        String email,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Size(max = 255, message = "头像URL长度不能超过255")
        String avatar,

        Integer status,

        @Size(max = 50, message = "部门ID长度不能超过50")
        String deptId
) {
}