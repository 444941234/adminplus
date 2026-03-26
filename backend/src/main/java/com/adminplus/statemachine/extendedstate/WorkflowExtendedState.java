package com.adminplus.statemachine.extendedstate;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import com.adminplus.statemachine.enums.WorkflowState;
import com.adminplus.statemachine.enums.WorkflowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流扩展状态操作助手
 * <p>
 * 提供对 StateMachine ExtendedState 的类型安全操作
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public class WorkflowExtendedState {

    private static final String VAR_CURRENT_NODE_ID = "currentNodeId";
    private static final String VAR_NODE_PATH = "nodePath";
    private static final String VAR_BUSINESS_DATA = "businessData";
    private static final String VAR_INSTANCE_ID = "instanceId";

    /**
     * Private constructor to prevent instantiation
     */
    private WorkflowExtendedState() {
        // Utility class - prevent instantiation
    }

    /**
     * 更新当前节点
     *
     * @param sm    状态机实例
     * @param nodeId 节点ID
     */
    public static void updateCurrentNode(
            StateMachine<WorkflowState, WorkflowEvent> sm,
            String nodeId) {

        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }

        sm.getExtendedState().getVariables().put(VAR_CURRENT_NODE_ID, nodeId);

        // 更新节点路径
        List<String> path = getCurrentNodePath(sm);
        if (path == null) {
            path = new ArrayList<>();
            sm.getExtendedState().getVariables().put(VAR_NODE_PATH, path);
        }
        if (!path.contains(nodeId)) {
            path.add(nodeId);
        }
    }

    /**
     * 获取当前节点ID
     *
     * @param sm 状态机实例
     * @return 当前节点ID，可能为null
     */
    public static String getCurrentNodeId(
            StateMachine<WorkflowState, WorkflowEvent> sm) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        return (String) sm.getExtendedState().getVariables().get(VAR_CURRENT_NODE_ID);
    }

    /**
     * 获取节点路径历史
     *
     * @param sm 状态机实例
     * @return 节点路径列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getCurrentNodePath(
            StateMachine<WorkflowState, WorkflowEvent> sm) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        return (List<String>) sm.getExtendedState().getVariables().get(VAR_NODE_PATH);
    }

    /**
     * 获取上一个节点ID
     *
     * @param sm 状态机实例
     * @return 上一个节点ID，如果是首节点则返回null
     */
    public static String getPreviousNodeId(
            StateMachine<WorkflowState, WorkflowEvent> sm) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        List<String> path = getCurrentNodePath(sm);
        if (path != null && path.size() > 1) {
            return path.get(path.size() - 2);
        }
        return null;
    }

    /**
     * 设置业务数据
     *
     * @param sm          状态机实例
     * @param businessData 业务数据Map
     */
    public static void setBusinessData(
            StateMachine<WorkflowState, WorkflowEvent> sm,
            Object businessData) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        sm.getExtendedState().getVariables().put(VAR_BUSINESS_DATA, businessData);
    }

    /**
     * 获取业务数据
     *
     * @param sm 状态机实例
     * @return 业务数据
     */
    public static Object getBusinessData(
            StateMachine<WorkflowState, WorkflowEvent> sm) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        return sm.getExtendedState().getVariables().get(VAR_BUSINESS_DATA);
    }

    /**
     * 设置流程实例ID
     *
     * @param sm         状态机实例
     * @param instanceId 流程实例ID
     */
    public static void setInstanceId(
            StateMachine<WorkflowState, WorkflowEvent> sm,
            String instanceId) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        sm.getExtendedState().getVariables().put(VAR_INSTANCE_ID, instanceId);
    }

    /**
     * 获取流程实例ID
     *
     * @param sm 状态机实例
     * @return 流程实例ID
     */
    public static String getInstanceId(
            StateMachine<WorkflowState, WorkflowEvent> sm) {
        if (sm == null) {
            throw new IllegalArgumentException("StateMachine cannot be null");
        }
        return (String) sm.getExtendedState().getVariables().get(VAR_INSTANCE_ID);
    }
}
