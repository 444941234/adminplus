package com.adminplus.pojo.dto.workflow.hook;

import java.util.List;

/**
 * Bean 执行器配置
 *
 * @param beanName  Spring Bean 名称
 * @param methodName 方法名称
 * @param args      参数表达式列表（SpEL表达式）
 * @author AdminPlus
 * @since 2026-04-02
 */
public record BeanConfig(
    String beanName,
    String methodName,
    List<String> args
) implements HookExecutorConfig {
}
