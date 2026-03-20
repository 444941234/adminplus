package com.adminplus.pojo.dto.resp;

/**
 * 用户设置视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsResp(
        boolean notifications,
        boolean darkMode,
        boolean emailUpdates,
        String language
) {
}