package com.adminplus.statemachine.config;

import com.adminplus.statemachine.actions.CreateApprovalAction;
import com.adminplus.statemachine.actions.LogAction;
import com.adminplus.statemachine.actions.NotifyAction;
import com.adminplus.statemachine.actions.UpdateNodeAction;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.guards.SpELGuard;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.stereotype.Component;

/**
 * 工作流状态转换配置
 * <p>
 * 定义状态之间的转换关系及关联的动作和守卫
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Component
public class WorkflowTransitionsConfig {

    private final UpdateNodeAction updateNodeAction;
    private final CreateApprovalAction createApprovalAction;
    private final NotifyAction notifyAction;
    private final LogAction logAction;
    private final SpELGuard spelGuard;

    public WorkflowTransitionsConfig(
            UpdateNodeAction updateNodeAction,
            CreateApprovalAction createApprovalAction,
            NotifyAction notifyAction,
            LogAction logAction,
            SpELGuard spelGuard) {
        this.updateNodeAction = updateNodeAction;
        this.createApprovalAction = createApprovalAction;
        this.notifyAction = notifyAction;
        this.logAction = logAction;
        this.spelGuard = spelGuard;
    }

    /**
     * 配置状态转换
     *
     * @param transitions 转换配置器
     * @throws Exception 配置异常
     */
    public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions)
            throws Exception {

        transitions
                // 提交：DRAFT -> RUNNING
                .withExternal()
                .source(WorkflowState.DRAFT)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.SUBMIT)
                .action(updateNodeAction)
                .action(logAction)
                .and()

                // 同意：RUNNING -> RUNNING（下一节点）
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.APPROVE)
                .guard(spelGuard)
                .action(updateNodeAction)
                .action(createApprovalAction)
                .action(notifyAction)
                .action(logAction)
                .and()

                // 拒绝：RUNNING -> REJECTED
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.REJECTED)
                .event(WorkflowEvent.REJECT)
                .action(createApprovalAction)
                .action(notifyAction)
                .action(logAction)
                .and()

                // 取消：RUNNING -> CANCELLED
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.CANCELLED)
                .event(WorkflowEvent.CANCEL)
                .action(logAction)
                .and()

                // 退回：RUNNING -> RUNNING（上一节点）
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.ROLLBACK)
                .action(updateNodeAction)
                .action(logAction);
    }
}
