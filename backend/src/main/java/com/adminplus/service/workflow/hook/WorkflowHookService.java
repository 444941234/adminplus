package com.adminplus.service.workflow.hook;

import com.adminplus.pojo.dto.workflow.hook.HookContext;
import com.adminplus.pojo.dto.workflow.hook.HookExecutionSummary;
import com.adminplus.pojo.dto.workflow.hook.HookResult;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;

import java.util.List;
import java.util.Map;

/**
 * 工作流钩子服务接口
 * <p>
 * 提供钩子执行和管理功能
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
public interface WorkflowHookService {

    /**
     * 执行节点字段钩子（简单场景）
     *
     * @param hookPoint    钩子点
     * @param instance     工作流实例
     * @param node         当前节点
     * @param formData     表单数据
     * @param extraParams  额外参数
     * @return 执行结果列表
     */
    List<HookResult> executeNodeFieldHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 执行独立表钩子（复杂场景）
     *
     * @param hookPoint    钩子点
     * @param instance     工作流实例
     * @param node         当前节点
     * @param formData     表单数据
     * @param extraParams  额外参数
     * @return 执行结果列表
     */
    List<HookResult> executeTableHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 执行所有钩子并处理阻断逻辑
     *
     * @param hookPoint    钩子点
     * @param instance     工作流实例
     * @param node         当前节点
     * @param formData     表单数据
     * @param extraParams  额外参数
     * @return 执行汇总
     */
    HookExecutionSummary executeAllHooks(
        String hookPoint,
        WorkflowInstanceEntity instance,
        WorkflowNodeEntity node,
        Map<String, Object> formData,
        Map<String, Object> extraParams
    );

    /**
     * 获取节点配置的所有钩子点
     *
     * @param nodeId 节点ID
     * @return 钩子点列表
     */
    List<String> getConfiguredHookPoints(String nodeId);
}
