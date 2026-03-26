package com.adminplus.statemachine.actions;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.extendedstate.WorkflowExtendedState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.StateMachine;

/**
 * 更新节点动作
 * <p>
 * 在状态转换时更新扩展状态中的节点信息
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class UpdateNodeAction implements Action<WorkflowState, WorkflowEvent> {

    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        log.debug("Executing UpdateNodeAction for transition from {} to {}",
                context.getSource().getId(), context.getTarget().getId());

        try {
            // 获取状态机实例
            StateMachine<WorkflowState, WorkflowEvent> stateMachine = context.getStateMachine();
            if (stateMachine == null) {
                log.warn("No state machine in context, skipping node update");
                return;
            }

            // 获取消息头中的目标节点ID
            Message<?> message = context.getMessage();
            if (message != null && message.getHeaders().containsKey("targetNodeId")) {
                String targetNodeId = message.getHeaders().get("targetNodeId", String.class);
                WorkflowExtendedState.updateCurrentNode(stateMachine, targetNodeId);
                log.debug("Updated current node ID to: {}", targetNodeId);
            } else {
                log.debug("No targetNodeId in message headers, skipping node update");
            }

            // 注意：扩展状态的修改会通过 StateMachinePersister 持久化到数据库
            // 不需要在这里手动保存

        } catch (Exception e) {
            log.error("Error executing UpdateNodeAction", e);
            throw new RuntimeException("Failed to update node in extended state", e);
        }
    }
}
