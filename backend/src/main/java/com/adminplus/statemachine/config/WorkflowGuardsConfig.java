package com.adminplus.statemachine.config;

import com.adminplus.statemachine.guards.SpELGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流守卫 Bean 配置
 * <p>
 * 定义状态机转换的守卫（条件判断）
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Configuration
public class WorkflowGuardsConfig {

    @Bean
    public SpELGuard spelGuard() {
        return new SpELGuard();
    }
}
