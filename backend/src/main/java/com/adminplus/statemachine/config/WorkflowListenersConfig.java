package com.adminplus.statemachine.config;

import com.adminplus.statemachine.listener.StateChangeListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流监听器 Bean 配置
 * <p>
 * 定义状态机事件监听器
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Configuration
public class WorkflowListenersConfig {

    @Bean
    public StateChangeListener stateChangeListener(ApplicationEventPublisher eventPublisher) {
        return new StateChangeListener(eventPublisher);
    }
}
