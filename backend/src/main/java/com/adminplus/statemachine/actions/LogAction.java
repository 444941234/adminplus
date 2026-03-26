package com.adminplus.statemachine.actions;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.extendedstate.WorkflowExtendedState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.time.LocalDateTime;

/**
 * 日志记录动作
 * <p>
 * 记录状态转换的详细日志
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class LogAction implements Action<WorkflowState, WorkflowEvent> {

    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        log.info("=== State Transition Log ===");
        log.info("Timestamp: {}", LocalDateTime.now());
        log.info("Source State: {}", context.getSource().getId());
        log.info("Target State: {}", context.getTarget().getId());
        log.info("Event: {}", context.getEvent());

        try {
            // 获取状态机实例
            var stateMachine = context.getStateMachine();
            if (stateMachine != null) {
                // 获取扩展状态信息
                String currentNodeId = WorkflowExtendedState.getCurrentNodeId(stateMachine);
                String instanceId = WorkflowExtendedState.getInstanceId(stateMachine);

                log.info("Current Node ID: {}", currentNodeId);
                log.info("Workflow Instance ID: {}", instanceId);
            }

            // 获取消息信息
            Message<?> message = context.getMessage();
            if (message != null) {
                log.info("Message Headers: {}", message.getHeaders());

                // 提取关键信息
                String userId = message.getHeaders().get("userId", String.class);
                String comment = message.getHeaders().get("comment", String.class);

                if (userId != null) {
                    log.info("User ID: {}", userId);
                }
                if (comment != null) {
                    log.info("Comment: {}", comment);
                }
            }

            log.info("========================");

        } catch (Exception e) {
            log.error("Error executing LogAction", e);
            // 日志记录失败不应该影响状态转换
        }
    }
}
