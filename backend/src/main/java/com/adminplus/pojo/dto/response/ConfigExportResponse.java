package com.adminplus.pojo.dto.response;

import java.util.List;

/**
 * 配置导出视图对象
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigExportResponse(
        String exportVersion,
        String exportTime,
        List<ExportGroup> groups
) {

    /**
     * 导出配置组信息
     */
    public record ExportGroup(
            String code,
            String name,
            String icon,
            List<ExportConfig> configs
    ) {}

    /**
     * 导出配置项信息
     */
    public record ExportConfig(
            String key,
            String name,
            String value,
            String valueType,
            String effectType,
            String description
    ) {}
}
