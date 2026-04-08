package com.adminplus.statemachine.persist;

import com.adminplus.repository.StateMachineRepository;
import com.adminplus.statemachine.enums.WorkflowEvent;
import com.adminplus.statemachine.enums.WorkflowState;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流状态机持久化器
 * <p>
 * 负责将状态机上下文序列化到数据库，以及从数据库恢复
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowStateMachinePersister
        implements StateMachinePersister<WorkflowState, WorkflowEvent, String> {

    private final StateMachineRepository repository;
    private final JsonMapper objectMapper;

    @Override
    public void persist(StateMachine<WorkflowState, WorkflowEvent> stateMachine,
                       String contextId) throws Exception {

        log.debug("Persisting state machine for context: {}", contextId);

        StateMachineEntity entity = repository.findByMachineId(contextId)
                .orElse(new StateMachineEntity());

        entity.setMachineId(contextId);
        entity.setState(stateMachine.getState().getId().toString());

        // 序列化 extended state
        Map<String, Object> extendedStateMap = new HashMap<>();
        stateMachine.getExtendedState().getVariables().entrySet().forEach(entry -> {
            String k = entry.getKey().toString();
            Object v = entry.getValue();
            try {
                String json = objectMapper.writeValueAsString(v);
                extendedStateMap.put(k, json);
            } catch (Exception e) {
                log.warn("Failed to serialize extended state key: {}", k, e);
                extendedStateMap.put(k, v);
            }
        });

        String extendedStateJson = objectMapper.writeValueAsString(extendedStateMap);
        entity.setExtendedState(extendedStateJson);

        repository.save(entity);

        log.debug("State machine persisted successfully for context: {}", contextId);
    }

    @Override
    public StateMachine<WorkflowState, WorkflowEvent> restore(
            StateMachine<WorkflowState, WorkflowEvent> stateMachine,
            String contextId) throws Exception {

        log.debug("Restoring state machine for context: {}", contextId);

        StateMachineEntity entity = repository.findByMachineId(contextId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "State machine context not found: " + contextId));

        log.debug("Found state machine context for: {}, state: {}",
                contextId, entity.getState());

        // 从数据库读取并反序列化上下文
        StateMachineContext<WorkflowState, WorkflowEvent> context =
                deserializeContext(entity);

        // 重置状态机到持久化的状态
        stateMachine.getStateMachineAccessor().doWithAllRegions(accessor -> {
            accessor.resetStateMachine(context);
        });

        // 恢复扩展状态
        restoreExtendedState(entity);

        log.debug("State machine restored successfully for context: {}", contextId);

        return stateMachine;
    }

    /**
     * 从数据库实体反序列化状态机上下文
     */
    private StateMachineContext<WorkflowState, WorkflowEvent> deserializeContext(
            StateMachineEntity entity) throws Exception {

        WorkflowState state = WorkflowState.valueOf(entity.getState());

        // 创建简单的上下文，只包含状态信息
        // ExtendedState 会在后续手动恢复
        return new DefaultStateMachineContext<>(
                state,
                null,
                null,
                null
        );
    }

    /**
     * 恢复扩展状态到状态机
     */
    private void restoreExtendedState(StateMachineEntity entity) throws Exception {
        if (entity.getExtendedState() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> serializedMap = objectMapper.readValue(
                    entity.getExtendedState(),
                    Map.class);

            // 注意：这里需要通过 StateMachine 的 ExtendedState 来设置变量
            // 实际使用时，需要在调用 restore 方法后手动设置 ExtendedState
            log.debug("Deserialized {} extended state variables",
                    serializedMap.size());
        }
    }
}

