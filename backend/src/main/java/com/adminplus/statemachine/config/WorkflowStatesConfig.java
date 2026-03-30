package com.adminplus.statemachine.config;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * 工作流状态配置
 * <p>
 * 定义工作流的所有状态和终端状态
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Component
public class WorkflowStatesConfig {

    /**
     * 配置状态
     *
     * @param states 状态配置器
     * @throws Exception 配置异常
     */
    public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states)
            throws Exception {

        states
                .withStates()
                .initial(WorkflowState.DRAFT)
                .states(EnumSet.allOf(WorkflowState.class))
                // 终端状态
                .end(WorkflowState.APPROVED)
                .end(WorkflowState.REJECTED)
                .end(WorkflowState.CANCELLED);
    }
}
