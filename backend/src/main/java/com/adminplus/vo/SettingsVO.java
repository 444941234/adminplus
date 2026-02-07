package com.adminplus.vo;

import java.util.Map;

/**
 * 用户设置视图对象
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public record SettingsVO(
        Map<String, Object> settings
) {
}