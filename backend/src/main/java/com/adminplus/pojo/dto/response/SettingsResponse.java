package com.adminplus.pojo.dto.response;

/**
 * 用户设置视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsResponse(
        boolean notifications,
        boolean darkMode,
        boolean emailUpdates,
        String language
) {
}