package com.adminplus.common.aspect;

import com.adminplus.common.annotation.Auditable;
import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 审计日志切面
 * 拦截带有 @Auditable 注解的方法，自动记录审计日志
 *
 * <p>支持 SpEL 表达式解析：</p>
 * <ul>
 *   <li>方法参数：#paramName</li>
 *   <li>返回结果：#result (需设置 includeResult = true)</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final LogService logService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String description = resolveDescription(
                auditable.description(),
                joinPoint,
                auditable.includeResult() ? result : null
            );

            logService.log(LogEntry.operation(
                auditable.module(),
                auditable.operationType().getCode(),
                description
            ));
        } catch (Exception e) {
            log.warn("审计日志记录失败: {}", e.getMessage());
        }
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void logFailure(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        if (ex instanceof BizException) {
            try {
                String description = resolveDescription(auditable.description(), joinPoint, null);
                logService.log(LogEntry.operation(
                    auditable.module(),
                    auditable.operationType().getCode(),
                    description + " [失败: " + ex.getMessage() + "]"
                ));
            } catch (Exception e) {
                log.warn("审计日志记录失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 解析描述中的 SpEL 表达式
     *
     * @param template  描述模板
     * @param joinPoint 切点
     * @param result    方法返回结果（可为 null）
     * @return 解析后的描述
     */
    private String resolveDescription(String template, JoinPoint joinPoint, Object result) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        if (!template.contains("#")) {
            return template;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        if (result != null) {
            context.setVariable("result", result);
        }

        try {
            return parser.parseExpression(template, new TemplateParserContext())
                .getValue(context, String.class);
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: {}, 模板: {}", e.getMessage(), template);
            return template;
        }
    }
}