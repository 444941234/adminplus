package com.adminplus.statemachine.actions;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * 创建审批记录动作
 * <p>
 * 在状态转换时创建审批记录
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class CreateApprovalAction implements Action<WorkflowState, WorkflowEvent> {

    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        log.debug("Executing CreateApprovalAction for transition from {} to {}",
                context.getSource().getId(), context.getTarget().getId());

        try {
            Message<?> message = context.getMessage();
            if (message == null) {
                log.warn("No message in state context, skipping approval creation");
                return;
            }

            // 检查是否是回退操作
            Boolean isRollback = message.getHeaders().get("isRollback", Boolean.class);
            if (Boolean.TRUE.equals(isRollback)) {
                log.debug("Rollback detected, skipping approval creation");
                return;
            }

            // 获取审批人ID
            String approverId = message.getHeaders().get("approverId", String.class);
            if (approverId == null) {
                log.warn("No approverId in message headers, skipping approval creation");
                return;
            }

            // TODO: 在实际实现中，这里需要：
            // 1. 获取 WorkflowInstanceEntity（从扩展状态或消息头）
            // 2. 获取当前节点ID（从扩展状态）
            // 3. 创建 WorkflowApprovalEntity 并保存到数据库
            // 4. 注入 WorkflowApprovalRepository 依赖

            log.debug("Would create approval record for approver: {}", approverId);
            log.info("Approval creation logic to be implemented with WorkflowApprovalRepository");

        } catch (Exception e) {
            log.error("Error executing CreateApprovalAction", e);
            throw new RuntimeException("Failed to create approval record", e);
        }
    }
}
