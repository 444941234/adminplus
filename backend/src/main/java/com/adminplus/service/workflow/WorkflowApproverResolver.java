package com.adminplus.service.workflow;

import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;

import java.util.List;
import java.util.Map;

/**
 * 工作流审批人解析器
 *
 * 负责解析工作流节点的审批人列表
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
public interface WorkflowApproverResolver {

    /**
     * 解析审批人列表
     *
     * @param instance 工作流实例
     * @param node     工作流节点
     * @return 审批人用户ID列表
     */
    List<String> resolveApprovers(WorkflowInstanceEntity instance, WorkflowNodeEntity node);

    /**
     * 批量获取审批人姓名
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户姓名映射
     */
    Map<String, String> batchGetApproverNames(List<String> userIds);
}