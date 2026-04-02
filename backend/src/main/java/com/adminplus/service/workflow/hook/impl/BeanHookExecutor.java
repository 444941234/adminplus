package com.adminplus.service.workflow.hook.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.workflow.hook.BeanConfig;
import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutorConfig;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.service.workflow.hook.HookExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Bean 钩子执行器
 * <p>
 * 支持调用 Spring Bean 方法作为钩子
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BeanHookExecutor implements HookExecutor {

    private final ApplicationContext applicationContext;

    @Override
    public HookResult execute(HookExecutorConfig config, HookContext context) {
        BeanConfig beanConfig = (BeanConfig) config;

        try {
            Object bean = applicationContext.getBean(beanConfig.beanName());
            Method method = findMethod(bean.getClass(), beanConfig.methodName(), beanConfig.args().size());

            Object[] args = resolveArgs(beanConfig.args(), context);
            Object result = method.invoke(bean, args);

            return wrapResult(result, beanConfig);

        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            log.error("Bean不存在: beanName={}", beanConfig.beanName());
            return new HookResult(false, "BEAN_NOT_FOUND", "Bean不存在: " + beanConfig.beanName());
        } catch (NoSuchMethodException e) {
            log.error("方法不存在: beanName={}, methodName={}", beanConfig.beanName(), beanConfig.methodName());
            return new HookResult(false, "METHOD_NOT_FOUND", "方法不存在: " + beanConfig.methodName());
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BizException biz) {
                return new HookResult(false, String.valueOf(biz.getCode()), biz.getMessage());
            }
            log.error("Bean方法执行异常: beanName={}, methodName={}", beanConfig.beanName(), beanConfig.methodName(), cause);
            return new HookResult(false, "EXECUTION_ERROR", "执行失败: " + cause.getMessage());
        } catch (Exception e) {
            log.error("Bean钩子执行失败: beanName={}, methodName={}", beanConfig.beanName(), beanConfig.methodName(), e);
            return new HookResult(false, "EXECUTION_ERROR", "执行失败: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "bean";
    }

    private Method findMethod(Class<?> beanClass, String methodName, int argCount) throws NoSuchMethodException {
        for (Method method : beanClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
                return method;
            }
        }
        throw new NoSuchMethodException("Method not found: " + methodName + " with " + argCount + " parameters");
    }

    private Object[] resolveArgs(List<String> argExpressions, HookContext context) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        evalContext.setVariable("instance", context.instance());
        evalContext.setVariable("node", context.node());
        evalContext.setVariable("formData", context.formData());
        evalContext.setVariable("operation", context.operation());
        evalContext.setVariable("operatorId", context.operatorId());
        evalContext.setVariable("operatorName", context.operatorName());
        evalContext.setVariable("extraParams", context.extraParams());

        return argExpressions.stream()
            .map(expr -> parser.parseExpression(expr).getValue(evalContext))
            .toArray();
    }

    private HookResult wrapResult(Object result, BeanConfig config) {
        if (result instanceof HookResult hookResult) {
            return hookResult;
        }
        if (result instanceof Boolean bool) {
            return new HookResult(bool,
                bool ? "SUCCESS" : "VALIDATION_FAILED",
                bool ? "执行成功" : "校验失败");
        }
        return new HookResult(true, "SUCCESS", "执行成功", result);
    }
}
