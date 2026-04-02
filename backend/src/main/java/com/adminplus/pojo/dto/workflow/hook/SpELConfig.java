package com.adminplus.pojo.dto.workflow.hook;

/**
 * SpEL 执行器配置
 *
 * @param expression      SpEL 表达式
 * @param failureMessage  失败时的提示消息
 * @author AdminPlus
 * @since 2026-04-02
 */
public record SpELConfig(
    String expression,
    String failureMessage
) implements HookExecutorConfig {
}
