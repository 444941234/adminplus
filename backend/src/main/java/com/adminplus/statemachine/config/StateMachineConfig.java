package com.adminplus.statemachine.config;

import com.adminplus.statemachine.actions.CreateApprovalAction;
import com.adminplus.statemachine.actions.LogAction;
import com.adminplus.statemachine.actions.NotifyAction;
import com.adminplus.statemachine.actions.UpdateNodeAction;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.guards.SpELGuard;
import com.adminplus.statemachine.listener.StateChangeListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;

/**
 * 状态机配置
 * <p>
 * 配置工作流状态机的状态、转换、动作和监听器
 * </p>
 * <p>
 * 使用 @EnableStateMachineFactory 而不是 @EnableStateMachine，
 * 以支持多个流程实例
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<WorkflowState, WorkflowEvent> {

    private final StateMachinePersister<WorkflowState, WorkflowEvent, String> persister;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数注入依赖
     *
     * @param persister       状态机持久化器
     * @param eventPublisher  Spring事件发布器
     */
    public StateMachineConfig(
            StateMachinePersister<WorkflowState, WorkflowEvent, String> persister,
            ApplicationEventPublisher eventPublisher) {
        this.persister = persister;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 配置状态
     */
    @Override
    public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states)
            throws Exception {

        states
                .withStates()
                .initial(WorkflowState.DRAFT)
                .states(EnumSet.allOf(WorkflowState.class))
                // 定义终端状态
                .end(WorkflowState.APPROVED)
                .end(WorkflowState.REJECTED)
                .end(WorkflowState.CANCELLED);
    }

    /**
     * 配置状态转换
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions)
            throws Exception {

        transitions
                // 提交：DRAFT -> RUNNING
                .withExternal()
                .source(WorkflowState.DRAFT)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.SUBMIT)
                .action(updateNodeAction())
                .action(logAction())
                .and()

                // 同意：RUNNING -> RUNNING（下一节点）
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.APPROVE)
                .guard(spelGuard())
                .action(updateNodeAction())
                .action(createApprovalAction())
                .action(notifyAction())
                .action(logAction())
                .and()

                // 拒绝：RUNNING -> REJECTED
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.REJECTED)
                .event(WorkflowEvent.REJECT)
                .action(createApprovalAction())
                .action(notifyAction())
                .action(logAction())
                .and()

                // 取消：RUNNING -> CANCELLED
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.CANCELLED)
                .event(WorkflowEvent.CANCEL)
                .action(logAction())
                .and()

                // 退回：RUNNING -> RUNNING（上一节点）
                .withExternal()
                .source(WorkflowState.RUNNING)
                .target(WorkflowState.RUNNING)
                .event(WorkflowEvent.ROLLBACK)
                .action(updateNodeAction())
                .action(logAction());
    }

    /**
     * SpEL守卫Bean
     * 用于条件分支判断（MVP 2使用）
     */
    @Bean
    public SpELGuard spelGuard() {
        return new SpELGuard();
    }

    /**
     * 更新节点动作Bean
     */
    @Bean
    public UpdateNodeAction updateNodeAction() {
        return new UpdateNodeAction();
    }

    /**
     * 创建审批记录动作Bean
     */
    @Bean
    public CreateApprovalAction createApprovalAction() {
        return new CreateApprovalAction();
    }

    /**
     * 通知动作Bean
     */
    @Bean
    public NotifyAction notifyAction() {
        return new NotifyAction();
    }

    /**
     * 日志动作Bean
     */
    @Bean
    public LogAction logAction() {
        return new LogAction();
    }

    /**
     * 状态变更监听器Bean
     */
    @Bean
    public StateChangeListener stateChangeListener() {
        return new StateChangeListener(eventPublisher);
    }
}
