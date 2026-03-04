package com.adminplus.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在 Controller 方法上，自动记录操作日志
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     * 1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他
     */
    int operationType() default 7;

    /**
     * 操作描述，支持 SpEL 表达式
     * 例如："{#user.username} 登录系统"
     */
    String description() default "";
}