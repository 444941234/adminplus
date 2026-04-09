package com.adminplus.pojo.dto.response;

import com.adminplus.enums.LogStatus;
import com.adminplus.enums.LogType;
import com.adminplus.enums.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 日志分页响应 VO
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Schema(description = "日志分页响应")
public record LogPageResp(
        @Schema(description = "日志ID")
        String id,

        @Schema(description = "操作人用户名")
        String username,

        @Schema(description = "操作模块")
        String module,

        @Schema(description = "日志类型（1=操作日志，2=登录日志，3=系统日志）")
        Integer logType,

        @Schema(description = "操作类型")
        Integer operationType,

        @Schema(description = "操作描述")
        String description,

        @Schema(description = "请求方法")
        String method,

        @Schema(description = "请求参数")
        String params,

        @Schema(description = "请求IP")
        String ip,

        @Schema(description = "请求地点")
        String location,

        @Schema(description = "执行时长（毫秒）")
        Long costTime,

        @Schema(description = "状态（1=成功，0=失败）")
        Integer status,

        @Schema(description = "异常信息")
        String errorMsg,

        @Schema(description = "创建时间")
        Instant createTime
) {

    /**
     * 获取日志类型描述
     */
    public String getLogTypeDesc() {
        if (logType == null) {
            return "未知";
        }
        for (LogType type : LogType.values()) {
            if (type.getCode() == logType) {
                return type.getDescription();
            }
        }
        return "未知";
    }

    /**
     * 获取操作类型描述
     */
    public String getOperationTypeDesc() {
        if (operationType == null) {
            return "未知";
        }
        for (OperationType type : OperationType.values()) {
            if (type.getCode() == operationType) {
                return type.getDescription();
            }
        }
        return "未知";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        for (LogStatus logStatus : LogStatus.values()) {
            if (logStatus.getCode() == status) {
                return logStatus.getDescription();
            }
        }
        return "未知";
    }
}