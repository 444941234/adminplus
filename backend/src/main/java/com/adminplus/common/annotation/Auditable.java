package com.adminplus.common.annotation;

import com.adminplus.enums.OperationType;

import java.lang.annotation.*;

/**
 * 可审计操作注解
 * 标记在 Service 方法上，自动记录审计日志
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @Auditable(module = "用户管理", operationType = OperationType.CREATE, description = "创建用户: #username")
 * public UserResponse createUser(UserCreateRequest req) {
 *     // No manual logService.log() needed
 * }
 * }</pre>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型
     */
    OperationType operationType();

    /**
     * 操作描述，支持 SpEL 表达式
     * 例如："创建用户: #username"、"删除用户: #userId"、"修改状态: #result.status"
     */
    String description();

    /**
     * 是否在描述中包含返回结果
     * 如果为 true，可在 description 中使用 #result 引用返回值
     */
    boolean includeResult() default false;
}