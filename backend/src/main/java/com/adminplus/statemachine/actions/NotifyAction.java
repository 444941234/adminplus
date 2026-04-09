package com.adminplus.statemachine.actions;

import com.adminplus.pojo.dto.request.NotificationSendRequest;
import com.adminplus.service.NotificationService;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * 通知动作
 * <p>
 * 在状态转换时发送通知（站内信、邮件、短信等）
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
@RequiredArgsConstructor
public class NotifyAction implements Action<WorkflowState, WorkflowEvent> {

    private final NotificationService notificationService;

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
            String title = message.getHeaders().get("notificationTitle", String.class);
            String content = message.getHeaders().get("notificationContent", String.class);
            String relatedId = message.getHeaders().get("relatedId", String.class);
            String relatedType = message.getHeaders().get("relatedType", String.class);

            if (notificationType == null || recipientId == null) {
                log.debug("No notificationType or recipientId in message headers, skipping notification");
                return;
            }

            // 发送通知
            NotificationSendRequest req = new NotificationSendRequest();
            req.setType(notificationType);
            req.setRecipientId(recipientId);
            req.setTitle(title != null ? title : getDefaultTitle(notificationType));
            req.setContent(content != null ? content : getDefaultContent(notificationType, context));
            req.setRelatedId(relatedId);
            req.setRelatedType(relatedType != null ? relatedType : "workflow");

            notificationService.sendNotification(req);

            log.info("Notification sent successfully: type={}, recipientId={}", notificationType, recipientId);

        } catch (Exception e) {
            log.error("Error executing NotifyAction", e);
            // 通知失败不应该影响状态转换，只记录错误
            log.warn("Notification failed but state transition will continue");
        }
    }

    private String getDefaultTitle(String notificationType) {
        return switch (notificationType) {
            case "workflow_approve" -> "审批通过通知";
            case "workflow_reject" -> "审批驳回通知";
            case "workflow_submit" -> "流程提交通知";
            case "workflow_cancel" -> "流程取消通知";
            case "workflow_rollback" -> "流程回退通知";
            case "workflow_cc" -> "抄送通知";
            case "workflow_urge" -> "催办通知";
            default -> "系统通知";
        };
    }

    private String getDefaultContent(String notificationType, StateContext<WorkflowState, WorkflowEvent> context) {
        WorkflowState targetState = context.getTarget().getId();
        return switch (notificationType) {
            case "workflow_approve" -> "您的申请已通过审批。";
            case "workflow_reject" -> "您的申请已被驳回。";
            case "workflow_submit" -> "您的流程已提交，等待审批。";
            case "workflow_cancel" -> "流程已取消。";
            case "workflow_rollback" -> "流程已回退，请重新处理。";
            case "workflow_cc" -> "您被抄送了一个流程。";
            case "workflow_urge" -> "有人催办您处理的流程。";
            default -> "您有一个新的系统通知。";
        };
    }
}
