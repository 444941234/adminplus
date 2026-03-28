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

            // NOTE: 审批记录由 WorkflowStateMachineServiceImpl 在状态转换后直接管理，
            // 此 Action 仅负责日志记录。实际的审批实体通过 approvalRepository 创建。

            log.debug("Approval record for approver {} will be managed by service layer", approverId);

        } catch (Exception e) {
            log.error("Error executing CreateApprovalAction", e);
            throw new RuntimeException("Failed to create approval record", e);
        }
    }
}
