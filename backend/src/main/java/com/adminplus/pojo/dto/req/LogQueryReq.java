package com.adminplus.pojo.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日志查询条件 DTO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Data
@Schema(description = "日志查询条件")
public class LogQueryReq {

    /**
     * 用户名（模糊查询）
     */
    @Schema(description = "用户名（模糊查询）")
    private String username;

    /**
     * 操作模块
     */
    @Schema(description = "操作模块")
    private String module;

    /**
     * 日志类型（1=操作日志，2=登录日志，3=系统日志）
     */
    @Schema(description = "日志类型")
    private Integer logType;

    /**
     * 操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）
     */
    @Schema(description = "操作类型")
    private Integer operationType;

    /**
     * 状态（1=成功，0=失败）
     */
    @Schema(description = "状态（1=成功，0=失败）")
    private Integer status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;

    /**
     * 页码（默认1）
     */
    @Schema(description = "页码")
    private Integer page = 1;

    /**
     * 每页大小（默认10）
     */
    @Schema(description = "每页大小")
    private Integer size = 10;
}