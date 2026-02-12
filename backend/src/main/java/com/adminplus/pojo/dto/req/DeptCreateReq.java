package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 部门创建请求
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Builder
public record DeptCreateReq(
        @NotNull(message = "父部门ID不能为空")
        String parentId,

        @NotBlank(message = "部门名称不能为空")
        @Size(max = 50, message = "部门名称长度不能超过50")
        String name,

        @Size(max = 30, message = "部门编码长度不能超过30")
        String code,

        @Size(max = 30, message = "负责人长度不能超过30")
        String leader,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Size(max = 50, message = "邮箱长度不能超过50")
        String email,

        @NotNull(message = "排序不能为空")
        Integer sortOrder,

        @NotNull(message = "状态不能为空")
        Integer status
) {
}