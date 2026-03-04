package com.adminplus.common.annotation;

import java.lang.annotation.*;

/**
 * 登录日志注解
 * 标记在登录方法上，自动记录登录日志
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginLog {

    /**
     * 登录类型：1=登录，2=登出
     */
    int type() default 1;

    /**
     * 描述
     */
    String description() default "";
}