package com.adminplus.statemachine.listener;

import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.event.WorkflowTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * 状态变更监听器
 * <p>
 * 监听状态机的状态转换事件，记录日志并发布Spring事件
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Slf4j
public class StateChangeListener extends StateMachineListenerAdapter<WorkflowState, WorkflowEvent> {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数
     *
     * @param eventPublisher Spring事件发布器，可以为null
     */
    public StateChangeListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void transition(Transition<WorkflowState, WorkflowEvent> transition) {
        State<WorkflowState, WorkflowEvent> source = transition.getSource();
        State<WorkflowState, WorkflowEvent> target = transition.getTarget();

        String sourceId = source != null ? source.getId().name() : "null";
        String targetId = target != null ? target.getId().name() : "null";

        log.info("State transition: {} -> {}", sourceId, targetId);

        if (eventPublisher != null) {
            WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                    this,
                    sourceId,
                    targetId,
                    transition.getTrigger() != null ? transition.getTrigger().getEvent().name() : "unknown",
                    null
            );
            eventPublisher.publishEvent(event);
        }
    }

    @Override
    public void transitionStarted(Transition<WorkflowState, WorkflowEvent> transition) {
        log.debug("Transition started: {} -> {}",
                transition.getSource().getId(),
                transition.getTarget().getId());
    }

    @Override
    public void transitionEnded(Transition<WorkflowState, WorkflowEvent> transition) {
        log.debug("Transition ended: {} -> {}",
                transition.getSource().getId(),
                transition.getTarget().getId());
    }

    @Override
    public void stateEntered(State<WorkflowState, WorkflowEvent> state) {
        log.debug("State entered: {}", state.getId());
    }

    @Override
    public void stateExited(State<WorkflowState, WorkflowEvent> state) {
        log.debug("State exited: {}", state.getId());
    }

    @Override
    public void stateChanged(State<WorkflowState, WorkflowEvent> from, State<WorkflowState, WorkflowEvent> to) {
        log.info("State changed from {} to {}", from.getId(), to.getId());
    }

    @Override
    public void eventNotAccepted(Message<WorkflowEvent> event) {
        log.warn("Event not accepted: {}", event.getPayload());
    }
}
