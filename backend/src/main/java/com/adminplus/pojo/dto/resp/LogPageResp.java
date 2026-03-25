package com.adminplus.pojo.dto.resp;

import com.adminplus.constants.LogStatus;
import com.adminplus.constants.LogType;
import com.adminplus.constants.OperationType;
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
        return switch (logType) {
            case LogType.OPERATION -> "操作日志";
            case LogType.LOGIN -> "登录日志";
            case LogType.SYSTEM -> "系统日志";
            default -> "未知";
        };
    }

    /**
     * 获取操作类型描述
     */
    public String getOperationTypeDesc() {
        if (operationType == null) {
            return "未知";
        }
        return switch (operationType) {
            case OperationType.QUERY -> "查询";
            case OperationType.CREATE -> "新增";
            case OperationType.UPDATE -> "修改";
            case OperationType.DELETE -> "删除";
            case OperationType.EXPORT -> "导出";
            case OperationType.IMPORT -> "导入";
            case OperationType.OTHER -> "其他";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case LogStatus.SUCCESS -> "成功";
            case LogStatus.FAILED -> "失败";
            default -> "未知";
        };
    }
}