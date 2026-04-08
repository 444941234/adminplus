package com.adminplus.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 通知查询条件请求类
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Schema(description = "通知查询条件")
public record NotificationQuery(
        @Schema(description = "页码", example = "1")
        Integer page,

        @Schema(description = "每页大小", example = "20")
        Integer size,

        @Schema(description = "通知状态（1=未读，2=已读，3=已删除）", example = "1")
        Integer status,

        @Schema(description = "通知类型（1=系统通知，2=审批通知，3=@通知）", example = "1")
        Integer type
) implements PageQuery {
    public NotificationQuery {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100;
    }

    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    public Integer getStatus() { return status; }
    public Integer getType() { return type; }
}
