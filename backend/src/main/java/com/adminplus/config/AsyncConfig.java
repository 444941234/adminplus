package com.adminplus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务配置
 * 使用 JDK 21 虚拟线程
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置异步任务执行器（使用虚拟线程）
     * JDK 21 的虚拟线程适合 IO 密集型任务
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        log.info("初始化虚拟线程执行器");
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}