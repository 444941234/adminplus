package com.adminplus.utils;

import com.adminplus.common.exception.BizException;
import com.adminplus.common.exception.ErrorCode;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 服务层断言工具类
 * 用于业务规则校验，统一抛出 BizException
 * <p>
 * 推荐使用 ErrorCode 枚举进行断言，便于统一管理错误信息
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class ServiceAssert {

    private ServiceAssert() {}

    // ========== 使用 ErrorCode 枚举的断言方法（推荐） ==========

    /**
     * 断言条件为真（使用 ErrorCode）
     */
    public static void isTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言条件为真（使用 ErrorCode + 额外信息）
     */
    public static void isTrue(boolean condition, ErrorCode errorCode, String additionalInfo) {
        if (!condition) {
            throw new BizException(errorCode, additionalInfo);
        }
    }

    /**
     * 断言对象不为空（使用 ErrorCode）
     */
    public static void notNull(Object obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言实体不存在（使用 ErrorCode）
     */
    public static void notExists(boolean exists, ErrorCode errorCode) {
        if (exists) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言实体存在（使用 ErrorCode）
     */
    public static void exists(boolean exists, ErrorCode errorCode) {
        if (!exists) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 断言状态匹配（使用 ErrorCode）
     */
    public static void isStatus(Integer actual, Integer expected, ErrorCode errorCode) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 直接抛出业务异常（使用 ErrorCode）
     */
    public static void fail(ErrorCode errorCode) {
        throw new BizException(errorCode);
    }

    /**
     * 直接抛出业务异常（使用 ErrorCode + 额外信息）
     */
    public static void fail(ErrorCode errorCode, String additionalInfo) {
        throw new BizException(errorCode, additionalInfo);
    }

    // ========== 使用 String 消息的断言方法（兼容旧代码） ==========

    /**
     * 断言条件为真
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BizException(message);
        }
    }

    /**
     * 断言条件为真（支持动态消息）
     */
    public static void isTrue(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new BizException(messageSupplier.get());
        }
    }

    /**
     * 断言条件为真（带错误码）
     */
    public static void isTrue(boolean condition, int code, String message) {
        if (!condition) {
            throw new BizException(code, message);
        }
    }

    /**
     * 断言对象不为空
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }

    /**
     * 断言实体不存在（用于唯一性检查）
     */
    public static void notExists(boolean exists, String message) {
        if (exists) {
            throw new BizException(message);
        }
    }

    /**
     * 断言实体存在
     */
    public static void exists(boolean exists, String message) {
        if (!exists) {
            throw new BizException(message);
        }
    }

    /**
     * 断言状态匹配
     */
    public static void isStatus(Integer actual, Integer expected, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new BizException(message);
        }
    }

    /**
     * 直接抛出业务异常（用于无条件失败场景）
     */
    public static void fail(String message) {
        throw new BizException(message);
    }

    /**
     * 直接抛出业务异常（支持动态消息）
     */
    public static void fail(Supplier<String> messageSupplier) {
        throw new BizException(messageSupplier.get());
    }
}