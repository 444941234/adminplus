package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新用户设置请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsUpdateReq(
        Boolean notifications,
        Boolean darkMode,
        Boolean emailUpdates,
        @NotBlank(message = "语言不能为空")
        String language
) {
}