package com.adminplus.pojo.dto.workflow.hook;

/**
 * 钩子执行结果
 * <p>
 * 统一的钩子执行返回结构
 * </p>
 *
 * @param success        是否成功
 * @param code           结果码
 * @param message        结果消息
 * @param data           附加数据（可选）
 * @param executionTime  执行耗时（毫秒）
 * @param retryAttempts  实际重试次数
 * @author AdminPlus
 * @since 2026-04-02
 */
public record HookResult(
    boolean success,
    String code,
    String message,
    Object data,
    Long executionTime,
    Integer retryAttempts
) {

    public HookResult(boolean success, String code, String message) {
        this(success, code, message, null, null, 0);
    }

    public HookResult(boolean success, String code, String message, Object data) {
        this(success, code, message, data, null, 0);
    }

    public HookResult withExecutionTime(Long time) {
        return new HookResult(success, code, message, data, time, retryAttempts);
    }

    public HookResult withRetryAttempts(Integer attempts) {
        return new HookResult(success, code, message, data, executionTime, attempts);
    }

    /**
     * 成功结果
     */
    public static HookResult ok() {
        return new HookResult(true, "SUCCESS", "执行成功");
    }

    public static HookResult ok(String message) {
        return new HookResult(true, "SUCCESS", message);
    }

    public static HookResult ok(String code, String message) {
        return new HookResult(true, code, message);
    }

    public static HookResult ok(String code, String message, Object data) {
        return new HookResult(true, code, message, data);
    }

    /**
     * 失败结果
     */
    public static HookResult fail(String message) {
        return new HookResult(false, "FAILED", message);
    }

    public static HookResult fail(String code, String message) {
        return new HookResult(false, code, message);
    }

    public static HookResult fail(String code, String message, Object data) {
        return new HookResult(false, code, message, data);
    }
}
