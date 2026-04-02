package com.adminplus.pojo.dto.workflow.hook;

import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;

import java.util.Map;

/**
 * 钩子执行上下文
 * <p>
 * 包含钩子执行所需的所有上下文信息
 * </p>
 *
 * @param instance     工作流实例
 * @param node         当前节点
 * @param formData     表单数据
 * @param operation    操作类型
 * @param operatorId   操作人ID
 * @param operatorName 操作人姓名
 * @param extraParams  额外参数
 * @author AdminPlus
 * @since 2026-04-02
 */
public record HookContext(
    WorkflowInstanceEntity instance,
    WorkflowNodeEntity node,
    Map<String, Object> formData,
    String operation,
    String operatorId,
    String operatorName,
    Map<String, Object> extraParams
) {
}
