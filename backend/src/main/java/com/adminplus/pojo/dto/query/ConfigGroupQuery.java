package com.adminplus.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 配置组查询条件请求类
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Schema(description = "配置组查询条件")
public record ConfigGroupQuery(
        @Schema(description = "页码", example = "1")
        Integer page,

        @Schema(description = "每页大小", example = "10")
        Integer size,

        @Schema(description = "关键字搜索", example = "系统配置")
        String keyword
) implements PageQuery {
    public ConfigGroupQuery {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 10000) size = 10000;  // Allow up to 10000 for export functionality
    }

    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    public String getKeyword() { return keyword; }
}
