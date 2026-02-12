package com.adminplus.common.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应结果封装
 *
 * @param <T> 数据类型
 * @author AdminPlus
 * @since 2026-02-06
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        Integer code,
        String message,
        T data,
        Long timestamp
) {

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ok("操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data, System.currentTimeMillis());
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(500, message, null, System.currentTimeMillis());
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}