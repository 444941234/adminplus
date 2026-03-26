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

            // TODO: 在实际实现中，这里需要：
            // 1. 根据通知类型调用不同的通知服务（邮件、短信、站内信等）
            // 2. 获取接收人信息（从 UserService）
            // 3. 构建通知内容（使用模板引擎）
            // 4. 发送通知

            log.info("Would send {} notification to recipient: {}", notificationType, recipientId);
            log.debug("Notification logic to be implemented with notification services");

        } catch (Exception e) {
            log.error("Error executing NotifyAction", e);
            // 通知失败不应该影响状态转换，只记录错误
            log.warn("Notification failed but state transition will continue");
        }
    }
}
