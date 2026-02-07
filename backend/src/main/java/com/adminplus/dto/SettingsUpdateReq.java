package com.adminplus.dto;

import java.util.Map;

/**
 * 更新用户设置请求 DTO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsUpdateReq(
        Map<String, Object> settings
) {
}