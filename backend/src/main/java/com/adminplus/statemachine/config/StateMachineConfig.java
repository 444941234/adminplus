package com.adminplus.statemachine.config;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * 状态机配置（主入口）
 * <p>
 * 组合各子模块配置，提供完整的工作流状态机定义
 * </p>
 * <p>
 * 使用 @EnableStateMachineFactory 支持多个流程实例
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<WorkflowState, WorkflowEvent> {

    private final WorkflowStatesConfig statesConfig;
    private final WorkflowTransitionsConfig transitionsConfig;

    public StateMachineConfig(
            WorkflowStatesConfig statesConfig,
            WorkflowTransitionsConfig transitionsConfig) {
        this.statesConfig = statesConfig;
        this.transitionsConfig = transitionsConfig;
    }

    @Override
    public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states)
            throws Exception {
        statesConfig.configure(states);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions)
            throws Exception {
        transitionsConfig.configure(transitions);
    }
}
