package com.adminplus.service.workflow.hook.impl;

import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutorConfig;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.dto.workflow.hook.SpELConfig;
import com.adminplus.service.workflow.hook.HookExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * SpEL 钩子执行器
 * <p>
 * 支持在钩子中使用 SpEL 表达式进行校验和执行
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Component
@Slf4j
public class SpELHookExecutor implements HookExecutor {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        SpELConfig spelConfig = (SpELConfig) config;

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("instance", context.instance());
        evalContext.setVariable("node", context.node());
        evalContext.setVariable("formData", context.formData());
        evalContext.setVariable("operation", context.operation());
        evalContext.setVariable("operatorId", context.operatorId());
        evalContext.setVariable("operatorName", context.operatorName());
        evalContext.setVariable("extraParams", context.extraParams());

        try {
            Expression expr = parser.parseExpression(spelConfig.expression());
            Object result = expr.getValue(evalContext);

            if (result instanceof HookResult hookResult) {
                return hookResult;
            }
            if (result instanceof Boolean bool) {
                return new HookResult(bool,
                    bool ? "SUCCESS" : "VALIDATION_FAILED",
                    bool ? "校验通过" : spelConfig.failureMessage() != null ? spelConfig.failureMessage() : "校验失败");
            }
            return new HookResult(true, "SUCCESS", "执行成功", result);

        } catch (Exception e) {
            log.error("SpEL钩子执行失败: expression={}", spelConfig.expression(), e);
            return new HookResult(false, "EXECUTION_ERROR", "执行失败: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "spel";
    }
}
