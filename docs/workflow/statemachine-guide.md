# Workflow State Machine Guide

## Overview

AdminPlus uses Spring State Machine (v4.0.0) to manage workflow state transitions, providing a robust and maintainable approach to workflow lifecycle management.

## Architecture

### State Machine Components

```
┌─────────────────────────────────────────────────────────┐
│                   Spring State Machine                   │
│                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│  │  States  │<-->│  Events  │<-->│  Guards  │          │
│  └──────────┘    └──────────┘    └──────────┘          │
│       │               │               │                 │
│       v               v               v                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│  │ Actions  │    │Listeners │    │ Persist  │          │
│  └──────────┘    └──────────┘    └──────────┘          │
└─────────────────────────────────────────────────────────┘
                            │
                            v
              ┌──────────────────────┐
              │ WorkflowInstance DB  │
              └──────────────────────┘
```

### States

The workflow state machine uses the following states:

| State | Description | Database Status |
|-------|-------------|-----------------|
| `DRAFT` | Initial state, workflow not yet submitted | `draft` |
| `RUNNING` | Workflow is active and waiting for approval | `running` |
| `APPROVED` | All approvals completed successfully | `approved` |
| `REJECTED` | Workflow rejected by an approver | `rejected` |
| `CANCELLED` | Workflow cancelled by initiator | `cancelled` |

### Events

| Event | Trigger | Valid From States | Result State |
|-------|---------|-------------------|--------------|
| `SUBMIT` | Initiator submits workflow | `DRAFT` | `RUNNING` |
| `APPROVE` | Approver approves | `RUNNING` | `RUNNING` or `APPROVED` |
| `REJECT` | Approver rejects | `RUNNING` | `REJECTED` |
| `CANCEL` | Initiator cancels | `RUNNING` | `CANCELLED` |
| `ROLLBACK` | Approver rolls back | `RUNNING` | `RUNNING` or `DRAFT` |

### Extended State

The state machine maintains extended state to track workflow context:

```java
{
  "currentNodeId": "node-123",
  "currentNodeName": "Manager Approval",
  "previousNodeId": "node-122",
  "rollbackHistory": ["node-123", "node-122"],
  "approvalCount": 2,
  "totalNodes": 3
}
```

## Configuration

### State Machine Configuration

Located in `StateMachineConfig.java`:

```java
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<WorkflowState, WorkflowEvent> {
    // State and event definitions
    // Transitions, guards, and actions
}
```

### Key Configuration Elements

1. **States**: Defined in `WorkflowState` enum
2. **Events**: Defined in `WorkflowEvent` enum
3. **Guards**: SpEL expressions for conditional transitions
4. **Actions**: Business logic executed on state changes
5. **Listeners**: State change event handlers
6. **Persister**: Database persistence for state machine context

## Usage

### Starting a Workflow

```java
@Autowired
private StateMachineFactory<WorkflowState, WorkflowEvent> stateMachineFactory;

public void startWorkflow(String instanceId) {
    StateMachine<WorkflowState, WorkflowEvent> sm =
        stateMachineFactory.getStateMachine(instanceId);

    sm.start();

    // State is now DRAFT
}
```

### Submitting a Workflow

```java
public void submitWorkflow(String instanceId) {
    StateMachine<WorkflowState, WorkflowEvent> sm =
        stateMachineFactory.getStateMachine(instanceId);

    // Send SUBMIT event
    boolean accepted = sm.sendEvent(WorkflowEvent.SUBMIT);

    if (accepted) {
        // State transitioned to RUNNING
        // Extended state updated with current node
    }
}
```

### Approving a Workflow

```java
public void approveWorkflow(String instanceId, String approverId, String comment) {
    StateMachine<WorkflowState, WorkflowEvent> sm =
        stateMachineFactory.getStateMachine(instanceId);

    // Update extended state with approver info
    sm.getExtendedState().getVariables().put("approverId", approverId);
    sm.getExtendedState().getVariables().put("comment", comment);

    // Send APPROVE event
    boolean accepted = sm.sendEvent(WorkflowEvent.APPROVE);

    if (accepted) {
        // Check if workflow completed
        if (sm.getState().getId() == WorkflowState.APPROVED) {
            // Workflow completed successfully
        } else {
            // Moved to next node
        }
    }
}
```

### Rejecting a Workflow

```java
public void rejectWorkflow(String instanceId, String reason) {
    StateMachine<WorkflowState, WorkflowEvent> sm =
        stateMachineFactory.getStateMachine(instanceId);

    sm.getExtendedState().getVariables().put("rejectReason", reason);

    boolean accepted = sm.sendEvent(WorkflowEvent.REJECT);

    if (accepted) {
        // State transitioned to REJECTED
        // Workflow instance updated in database
    }
}
```

### Rolling Back a Workflow

```java
public void rollbackWorkflow(String instanceId, String targetNodeId, String reason) {
    StateMachine<WorkflowState, WorkflowEvent> sm =
        stateMachineFactory.getStateMachine(instanceId);

    sm.getExtendedState().getVariables().put("rollbackTarget", targetNodeId);
    sm.getExtendedState().getVariables().put("rollbackReason", reason);

    boolean accepted = sm.sendEvent(WorkflowEvent.ROLLBACK);

    if (accepted) {
        // State transitioned to RUNNING or DRAFT
        // Extended state updated with new current node
    }
}
```

## Guards

Guards are SpEL expressions that determine if a transition is allowed:

### Approve Guard

```java
@Override
public void configure(StateMachineStateConfigurer<WorkflowState, WorkflowEvent> states)
        throws Exception {
    states
        .withStates()
        .initial(WorkflowState.DRAFT)
        .states(EnumSet.allOf(WorkflowState.class));
}

@Override
public void configure(StateMachineTransitionConfigurer<WorkflowState, WorkflowEvent> transitions)
        throws Exception {
    transitions
        .withExternal()
            .source(WorkflowState.RUNNING)
            .target(WorkflowState.APPROVED)
            .event(WorkflowEvent.APPROVE)
            .guard(approveGuard)
            .action(approveAction)
        .and()
        .withExternal()
            .source(WorkflowState.RUNNING)
            .target(WorkflowState.RUNNING)
            .event(WorkflowEvent.APPROVE)
            .guard(approveToNextNodeGuard)
            .action(moveToNextNodeAction);
}
```

### SpEL Guard Examples

```java
// Check if this is the last node
@Bean
public Guard<WorkflowState, WorkflowEvent> approveGuard() {
    return context -> {
        String currentNodeId = (String) context.getExtendedState()
            .getVariables().get("currentNodeId");
        String totalNodes = (String) context.getExtendedState()
            .getVariables().get("totalNodes");

        return isLastNode(currentNodeId, totalNodes);
    };
}

// Check if approval is allowed
@Bean
public Guard<WorkflowState, WorkflowEvent> approvalAllowedGuard() {
    return context -> {
        String approverId = (String) context.getExtendedState()
            .getVariables().get("approverId");
        String currentNodeId = (String) context.getExtendedState()
            .getVariables().get("currentNodeId");

        return isApprovalAllowed(approverId, currentNodeId);
    };
}
```

## Actions

Actions execute business logic during state transitions:

### Approve Action

```java
@Bean
public Action<WorkflowState, WorkflowEvent> approveAction() {
    return context -> {
        String instanceId = context.getStateMachine().getId();
        String approverId = (String) context.getExtendedState()
            .getVariables().get("approverId");
        String comment = (String) context.getExtendedState()
            .getVariables().get("comment");

        // Update workflow instance
        workflowService.completeWorkflow(instanceId);

        // Update approval record
        approvalService.recordApproval(instanceId, approverId, comment);

        // Clear extended state
        context.getExtendedState().getVariables().clear();
    };
}
```

### Rollback Action

```java
@Bean
public Action<WorkflowState, WorkflowEvent> rollbackAction() {
    return context -> {
        String instanceId = context.getStateMachine().getId();
        String targetNodeId = (String) context.getExtendedState()
            .getVariables().get("rollbackTarget");
        String reason = (String) context.getExtendedState()
            .getVariables().get("rollbackReason");

        // Update current node in extended state
        context.getExtendedState().getVariables()
            .put("currentNodeId", targetNodeId);

        // Record rollback in database
        approvalService.recordRollback(instanceId, targetNodeId, reason);

        // Update workflow instance
        workflowService.rollbackToNode(instanceId, targetNodeId);
    };
}
```

## Listeners

Listeners respond to state machine events:

```java
@Component
public class WorkflowStateChangeListener extends StateMachineListenerAdapter<WorkflowState, WorkflowEvent> {

    @Override
    public void stateChanged(State<WorkflowState, WorkflowEvent> from,
                            State<WorkflowState, WorkflowEvent> to) {
        log.info("State changed from {} to {}", from.getId(), to.getId());

        // Update database
        // Send notifications
        // Audit logging
    }

    @Override
    public void transitionEnded(Transition<WorkflowState, WorkflowEvent> transition) {
        log.info("Transition ended: {} -> {} with event {}",
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent());
    }
}
```

## Persistence

### State Machine Persister

The `WorkflowStateMachinePersister` handles state machine persistence:

```java
@Component
public class WorkflowStateMachinePersister
        extends AbstractStateMachinePersister<WorkflowState, WorkflowEvent, String> {

    @Autowired
    private StateMachineRepository stateMachineRepository;

    @Override
    public void persist(StateMachineContext<WorkflowState, WorkflowEvent> context,
                       String machineId) {
        // Serialize and save to database
    }

    @Override
    public StateMachineContext<WorkflowState, WorkflowEvent> read(String machineId)
            throws Exception {
        // Load from database and deserialize
    }
}
```

### Database Table

```sql
CREATE TABLE spring_state_machine_context (
    id VARCHAR(100) PRIMARY KEY,
    machine_id VARCHAR(100) UNIQUE NOT NULL,
    state VARCHAR(50) NOT NULL,
    extended_state JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
```

## Testing

### Unit Tests

```java
@SpringBootTest
class WorkflowStateMachineTest {

    @Autowired
    private StateMachineFactory<WorkflowState, WorkflowEvent> factory;

    @Test
    void shouldTransitionFromDraftToRunning() {
        StateMachine<WorkflowState, WorkflowEvent> sm =
            factory.getStateMachine("test-id");

        sm.start();
        boolean accepted = sm.sendEvent(WorkflowEvent.SUBMIT);

        assertTrue(accepted);
        assertEquals(WorkflowState.RUNNING, sm.getState().getId());
    }
}
```

### Integration Tests

See `WorkflowStateMachineIntegrationTest.java` for comprehensive integration tests.

## Frontend Integration

### Flow Visualization

The `WorkflowVisualizer.vue` component provides interactive workflow visualization:

```vue
<template>
  <WorkflowVisualizer
    :definition-id="definitionId"
    :readonly="true"
    @node-click="handleNodeClick"
  />
</template>

<script setup>
import WorkflowVisualizer from '@/views/workflow/WorkflowVisualizer.vue'

function handleNodeClick(node) {
  console.log('Node clicked:', node)
}
</script>
```

### State Display

```typescript
const statusLabels = {
  draft: '草稿',
  running: '审批中',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消'
}
```

## Best Practices

1. **Always use state machine for state transitions** - Don't directly update workflow status in database
2. **Validate transitions with guards** - Ensure business rules are enforced
3. **Use actions for side effects** - Keep actions focused and testable
4. **Log all state changes** - Maintain audit trail
5. **Handle failures gracefully** - Use transactions and rollback mechanisms
6. **Test thoroughly** - Unit test guards and actions, integration test full flows
7. **Monitor state machine performance** - Track state transition times

## Troubleshooting

### Common Issues

**Issue**: State machine not persisting
- **Solution**: Check database connection and persister configuration

**Issue**: Event not accepted
- **Solution**: Verify guard conditions and current state

**Issue**: Extended state lost after restart
- **Solution**: Ensure persister is properly configured and called

**Issue**: Multiple state machines conflicting
- **Solution**: Use unique machine IDs (workflow instance IDs)

## Migration Guide

### From String-Based States

Old approach:
```java
instance.setStatus("running");
```

New approach:
```java
StateMachine<WorkflowState, WorkflowEvent> sm =
    stateMachineFactory.getStateMachine(instance.getId());
sm.sendEvent(WorkflowEvent.SUBMIT);
```

### Database Migration

See migration script: `V5__add_state_machine_support.sql`

## References

- [Spring State Machine Documentation](https://docs.spring.io/spring-statemachine/docs/current/reference/)
- AdminPlus Backend: `backend/src/main/java/com/adminplus/statemachine/`
- AdminPlus Frontend: `frontend/src/views/workflow/WorkflowVisualizer.vue`

## Support

For questions or issues, contact the development team or create an issue in the project repository.
