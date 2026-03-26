package com.adminplus.statemachine.guards;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

/**
 * SpEL 表达式守卫
 * <p>
 * 用于条件分支判断，从消息头中读取 conditionExpression 并进行求值
 * </p>
 * <p>
 * 支持的 SpEL 变量：
 * <ul>
 *   <li>#businessData - 业务数据对象</li>
 *   <li>#instance - 流程实例对象</li>
 * </ul>
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class SpELGuard implements Guard<WorkflowState, WorkflowEvent> {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public boolean evaluate(StateContext<WorkflowState, WorkflowEvent> context) {
        // 从消息头中获取 SpEL 表达式
        Message<?> message = context.getMessage();
        if (message == null) {
            log.debug("No message in state context, allowing transition");
            return true;
        }

        String conditionExpression = message.getHeaders().get("conditionExpression", String.class);

        if (conditionExpression == null || conditionExpression.isBlank()) {
            log.debug("No condition expression provided, allowing transition");
            return true;
        }

        try {
            // 准备 SpEL 上下文
            StandardEvaluationContext spelContext = new StandardEvaluationContext();

            // 从消息头中获取业务数据和流程实例
            Object businessData = message.getHeaders().get("businessData");
            Object instance = message.getHeaders().get("instance");

            // 设置 SpEL 变量
            if (businessData != null) {
                spelContext.setVariable("businessData", businessData);
            }
            if (instance != null) {
                spelContext.setVariable("instance", instance);
            }

            // 解析并求值表达式
            Expression expression = parser.parseExpression(conditionExpression);
            Boolean result = expression.getValue(spelContext, Boolean.class);

            log.debug("SpEL expression '{}' evaluated to: {}", conditionExpression, result);
            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Failed to evaluate SpEL expression: {}", conditionExpression, e);
            // 表达式求值失败时，默认阻止转换
            return false;
        }
    }
}
