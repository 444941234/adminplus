package com.adminplus.statemachine.config;

import com.adminplus.statemachine.actions.CreateApprovalAction;
import com.adminplus.statemachine.actions.LogAction;
import com.adminplus.statemachine.actions.NotifyAction;
import com.adminplus.statemachine.actions.UpdateNodeAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流动作 Bean 配置
 * <p>
 * 定义状态机转换过程中的动作 Bean
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Configuration
public class WorkflowActionsConfig {

    @Bean
    public UpdateNodeAction updateNodeAction() {
        return new UpdateNodeAction();
    }

    @Bean
    public CreateApprovalAction createApprovalAction() {
        return new CreateApprovalAction();
    }

    @Bean
    public NotifyAction notifyAction() {
        return new NotifyAction();
    }

    @Bean
    public LogAction logAction() {
        return new LogAction();
    }
}
