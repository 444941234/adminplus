package com.adminplus.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static java.lang.Thread.ofVirtual;

/**
 * 异步任务配置
 * 使用 JDK 21 虚拟线程，并传递 SecurityContext 到异步线程
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

    private final SecurityContextTaskDecorator securityContextTaskDecorator;

    /**
     * 配置异步任务执行器
     * 使用虚拟线程池 + SecurityContext 传递装饰器
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        log.info("初始化虚拟线程执行器（带 SecurityContext 传递）");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 使用虚拟线程工厂（JDK 21+）
        executor.setThreadFactory(ofVirtual().factory());

        // 核心配置
        executor.setCorePoolSize(1);  // 虚拟线程模式下，核心线程数设为 1
        executor.setMaxPoolSize(Integer.MAX_VALUE);  // 虚拟线程支持无限扩展
        executor.setQueueCapacity(100);  // 任务队列容量
        executor.setThreadNamePrefix("async-");

        // 关键：设置 TaskDecorator 传递 SecurityContext
        executor.setTaskDecorator(securityContextTaskDecorator);

        // 拒绝策略：调用者运行（防止任务丢失）
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * 钩子异步执行器
     * 用于异步执行钩子逻辑
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        log.info("初始化钩子异步执行器");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心配置
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-hook-");

        // 拒绝策略：调用者运行（防止任务丢失）
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}