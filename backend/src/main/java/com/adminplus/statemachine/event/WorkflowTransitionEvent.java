package com.adminplus.statemachine.event;

import org.springframework.context.ApplicationEvent;

/**
 * 工作流状态转换事件
 * <p>
 * 当状态机发生状态转换时发布，其他组件可监听此事件执行副作用
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-29
 */
public class WorkflowTransitionEvent extends ApplicationEvent {

    private final String sourceState;
    private final String targetState;
    private final String event;
    private final String machineId;

    public WorkflowTransitionEvent(Object source, String sourceState, String targetState,
                                   String event, String machineId) {
        super(source);
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.event = event;
        this.machineId = machineId;
    }

    public String getSourceState() {
        return sourceState;
    }

    public String getTargetState() {
        return targetState;
    }

    public String getEvent() {
        return event;
    }

    public String getMachineId() {
        return machineId;
    }

    @Override
    public String toString() {
        return "WorkflowTransitionEvent{sourceState='%s', targetState='%s', event='%s', machineId='%s'}"
                .formatted(sourceState, targetState, event, machineId);
    }
}
