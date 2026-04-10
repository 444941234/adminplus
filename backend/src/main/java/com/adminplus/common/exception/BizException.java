package com.adminplus.common.exception;

/**
 * 业务异常（支持错误码）
 * <p>
 * 推荐使用方式：
 * - 使用 ErrorCode 枚举：throw new BizException(ErrorCode.USER_NOT_FOUND)
 * - 直接消息：throw new BizException("用户不存在")（默认 code=400）
 * - 自定义 code：throw new BizException(404, "资源不存在")
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
public class BizException extends RuntimeException {

    private final Integer code;
    private final ErrorCode errorCode;

    /**
     * 使用 ErrorCode 枚举创建异常
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    /**
     * 使用 ErrorCode 枚举创建异常（带额外信息）
     */
    public BizException(ErrorCode errorCode, String additionalInfo) {
        super(errorCode.getMessage() + ": " + additionalInfo);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    /**
     * 直接使用消息（默认 code=400）
     */
    public BizException(String message) {
        super(message);
        this.code = 400;
        this.errorCode = null;
    }

    /**
     * 自定义 code 和消息
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }

    /**
     * 带原因的异常
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.errorCode = null;
    }

    /**
     * 带原因和 code 的异常
     */
    public BizException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorCode = null;
    }

    /**
     * 带 ErrorCode 和原因的异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public Integer getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 是否使用标准错误码
     */
    public boolean hasStandardErrorCode() {
        return errorCode != null;
    }
}