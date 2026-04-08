package com.adminplus.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 配置查询条件请求类
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Schema(description = "配置查询条件")
public record ConfigQuery(
        @Schema(description = "页码", example = "1")
        Integer page,

        @Schema(description = "每页大小", example = "10")
        Integer size,

        @Schema(description = "配置组ID", example = "1")
        String groupId,

        @Schema(description = "关键字搜索", example = "timeout")
        String keyword,

        @Schema(description = "配置状态（1=启用，0=禁用）", example = "1")
        Integer status
) implements PageQuery {
    public ConfigQuery {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100;
    }

    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    public String getGroupId() { return groupId; }
    public String getKeyword() { return keyword; }
    public Integer getStatus() { return status; }
}
