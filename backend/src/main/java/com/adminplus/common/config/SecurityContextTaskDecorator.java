package com.adminplus.common.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SecurityContext 任务装饰器
 * 将主线程的 SecurityContext 传递到异步线程
 *
 * <p>解决 @Async 方法中 SecurityContext 丢失的问题</p>
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Component
public class SecurityContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 获取当前线程的 SecurityContext
        var securityContext = SecurityContextHolder.getContext();

        // 返回一个新的 Runnable，在执行前设置 SecurityContext
        return new DelegatingSecurityContextRunnable(runnable, securityContext);
    }
}
