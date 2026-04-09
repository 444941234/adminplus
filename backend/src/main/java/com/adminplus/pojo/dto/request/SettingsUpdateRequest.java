package com.adminplus.pojo.dto.request;

/**
 * 更新用户设置请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsUpdateRequest(
        Boolean notifications,
        Boolean darkMode,
        Boolean emailUpdates,
        String language
) {
}