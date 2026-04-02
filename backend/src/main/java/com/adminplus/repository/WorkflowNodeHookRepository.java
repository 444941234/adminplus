package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowNodeHookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流节点钩子配置 Repository
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Repository
public interface WorkflowNodeHookRepository extends JpaRepository<WorkflowNodeHookEntity, String> {

    /**
     * 根据节点ID查询所有钩子（按优先级排序）
     */
    List<WorkflowNodeHookEntity> findByNodeIdAndDeletedFalseOrderByPriorityAsc(String nodeId);

    /**
     * 根据节点ID和钩子点查询钩子
     */
    List<WorkflowNodeHookEntity> findByNodeIdAndHookPointAndDeletedFalseOrderByPriorityAsc(String nodeId, String hookPoint);

    /**
     * 根据节点ID删除所有钩子（软删除）
     */
    List<WorkflowNodeHookEntity> findByNodeIdAndDeletedFalse(String nodeId);

    /**
     * 根据钩子点查询所有钩子
     */
    List<WorkflowNodeHookEntity> findByHookPointAndDeletedFalse(String hookPoint);
}
