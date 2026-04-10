package com.adminplus.utils;

import com.adminplus.common.exception.BizException;

import java.util.Objects;

/**
 * 服务层断言工具类
 * 用于业务规则校验，统一抛出 BizException
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public final class ServiceAssert {

    private ServiceAssert() {}

    /**
     * 断言条件为真
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BizException(message);
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
}