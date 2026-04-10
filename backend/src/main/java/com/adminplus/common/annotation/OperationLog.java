package com.adminplus.common.annotation;

import com.adminplus.enums.OperationType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在 Controller 或 Service 方法上，自动记录操作日志
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // Controller 层（自动记录 IP、请求参数）
 * @OperationLog(module = "用户管理", operationType = OperationType.CREATE, description = "创建用户")
 * public ApiResponse<UserResponse> createUser(@RequestBody UserCreateRequest req) { }
 *
 * // Service 层（仅记录模块、操作类型、描述）
 * @OperationLog(module = "用户管理", operationType = OperationType.CREATE, description = "创建用户: #result.username")
 * public UserResponse createUser(UserCreateRequest req) { }
 * }</pre>
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
     * 操作类型（枚举方式，推荐）
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 操作类型（数字方式，兼容旧代码）
     * 1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他
     * @deprecated 请使用 type() 属性
     */
    @Deprecated
    int operationType() default 7;

    /**
     * 操作描述，支持 SpEL 表达式
     * Controller层: "{#user.username} 登录系统" 或 "{#username}"
     * Service层: "创建用户: #username" 或 "删除用户: #id"
     */
    String description() default "";

    /**
     * 是否在描述中包含返回结果
     * 如果为 true，可在 description 中使用 #result 引用返回值
     * 仅适用于 Service 层
     */
    boolean includeResult() default false;
}