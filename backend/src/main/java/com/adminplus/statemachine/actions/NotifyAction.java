package com.adminplus.statemachine.actions;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * 通知动作
 * <p>
 * 在状态转换时发送通知（邮件、短信等）
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class NotifyAction implements Action<WorkflowState, WorkflowEvent> {

    @Override
    public void execute(StateContext<WorkflowState, WorkflowEvent> context) {
        log.debug("Executing NotifyAction for transition from {} to {}",
                context.getSource().getId(), context.getTarget().getId());

        try {
            Message<?> message = context.getMessage();
            if (message == null) {
                log.warn("No message in state context, skipping notification");
                return;
            }

            // 获取通知类型和接收人
            String notificationType = message.getHeaders().get("notificationType", String.class);
            String recipientId = message.getHeaders().get("recipientId", String.class);

            if (notificationType == null || recipientId == null) {
                log.debug("No notificationType or recipientId in message headers, skipping notification");
                return;
            }

            // NOTE: 通知逻辑待通知服务基础设施就绪后实现
            log.info("{} notification queued for recipient: {}", notificationType, recipientId);

        } catch (Exception e) {
            log.error("Error executing NotifyAction", e);
            // 通知失败不应该影响状态转换，只记录错误
            log.warn("Notification failed but state transition will continue");
        }
    }
}
