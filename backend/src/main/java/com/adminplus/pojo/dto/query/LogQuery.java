package com.adminplus.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 日志查询条件请求类
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Schema(description = "日志查询条件")
public record LogQuery(
        @Schema(description = "页码", example = "1")
        Integer page,

        @Schema(description = "每页大小", example = "10")
        Integer size,

        @Schema(description = "用户名（模糊查询）")
        String username,

        @Schema(description = "操作模块")
        String module,

        @Schema(description = "日志类型（1=操作日志，2=登录日志，3=系统日志）")
        Integer logType,

        @Schema(description = "操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）")
        Integer operationType,

        @Schema(description = "状态（1=成功，0=失败）")
        Integer status,

        @Schema(description = "开始时间")
        String startTime,

        @Schema(description = "结束时间")
        String endTime
) implements PageQuery {
    public LogQuery {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 10000) size = 10000;  // Allow up to 10000 for export functionality
    }

    public Integer getPage() { return page; }
    public Integer getSize() { return size; }

    // 字段 accessor 方法（record 自动生成 public accessor，但为了兼容性保留）
    public String username() { return username; }
    public String module() { return module; }
    public Integer logType() { return logType; }
    public Integer operationType() { return operationType; }
    public Integer status() { return status; }
    public String startTime() { return startTime; }
    public String endTime() { return endTime; }

    // Getter 方法（为了兼容现有代码）
    public String getUsername() { return username; }
    public String getModule() { return module; }
    public Integer getLogType() { return logType; }
    public Integer getOperationType() { return operationType; }
    public Integer getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    /**
     * 创建新的查询对象，重置分页参数
     */
    public LogQuery withPagination(int page, int size) {
        return new LogQuery(page, size, username, module, logType, operationType, status, startTime, endTime);
    }
}
