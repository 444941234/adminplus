package com.adminplus.pojo.dto.request;

import com.adminplus.enums.LogType;

/**
 * 日志条目 - 统一的日志记录参数
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 操作日志（最简）
 * logService.log(LogEntry.operation("用户管理", 1, "创建用户: admin"));
 *
 * // 操作日志（带耗时）
 * logService.log(LogEntry.operationBuilder("用户管理", 1, "查询列表")
 *     .costTime(150L)
 *     .build());
 *
 * // 登录日志
 * logService.log(LogEntry.login("admin", true, null));
 *
 * // 系统日志
 * logService.log(LogEntry.system("定时任务", "清理日志完成", null));
 * }</pre>
 *
 * @author AdminPlus
 * @since 2026-04-07
 */
public record LogEntry(
    String module,
    int logType,
    int operationType,
    String description,
    String method,
    String params,
    String ip,
    Long costTime,
    int status,
    String errorMsg,
    String username,
    boolean skipAuthCheck
) {

    // ========== 静态工厂方法 ==========

    /**
     * 创建操作日志（简单版本）
     *
     * @param module        操作模块
     * @param operationType 操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）
     * @param description   操作描述
     */
    public static LogEntry operation(String module, int operationType, String description) {
        return new LogEntry(
            module,
            LogType.OPERATION.getCode(),
            operationType,
            description,
            null,
            null,
            null,
            0L,
            1,
            null,
            null,
            false
        );
    }

    /**
     * 创建操作日志 Builder（需要额外字段时使用）
     *
     * @param module        操作模块
     * @param operationType 操作类型
     * @param description   操作描述
     */
    public static Builder operationBuilder(String module, int operationType, String description) {
        return new Builder()
            .module(module)
            .logType(LogType.OPERATION.getCode())
            .operationType(operationType)
            .description(description)
            .status(1);
    }

    /**
     * 创建登录日志
     *
     * @param username 用户名
     * @param success  是否成功
     * @param errorMsg 错误信息（失败时）
     */
    public static LogEntry login(String username, boolean success, String errorMsg) {
        return new LogEntry(
            "用户登录",
            LogType.LOGIN.getCode(),
            success ? 1 : 2,
            success ? "用户登录成功" : "用户登录失败",
            null,
            null,
            null,
            0L,
            success ? 1 : 0,
            errorMsg,
            username,
            true  // 登录日志无需认证检查
        );
    }

    /**
     * 创建系统日志
     *
     * @param module   模块名称
     * @param message  日志消息
     * @param errorMsg 错误信息（如有）
     */
    public static LogEntry system(String module, String message, String errorMsg) {
        return new LogEntry(
            module,
            LogType.SYSTEM.getCode(),
            7,  // 其他
            message,
            null,
            null,
            null,
            0L,
            errorMsg == null ? 1 : 0,
            errorMsg,
            "system",
            true  // 系统日志无需认证检查
        );
    }

    // ========== Builder ==========

    public static class Builder {
        private String module;
        private int logType = LogType.OPERATION.getCode();
        private int operationType = 7;
        private String description;
        private String method;
        private String params;
        private String ip;
        private Long costTime = 0L;
        private int status = 1;
        private String errorMsg;
        private String username;
        private boolean skipAuthCheck = false;

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder logType(int logType) {
            this.logType = logType;
            return this;
        }

        public Builder operationType(int operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder params(String params) {
            this.params = params;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder costTime(Long costTime) {
            this.costTime = costTime;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder failed(String errorMsg) {
            this.status = 0;
            this.errorMsg = errorMsg;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder skipAuthCheck() {
            this.skipAuthCheck = true;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(
                module, logType, operationType, description,
                method, params, ip, costTime, status, errorMsg,
                username, skipAuthCheck
            );
        }
    }
}