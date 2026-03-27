package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流节点 Repository
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNodeEntity, String> {

    /**
     * 根据定义ID查询所有节点
     */
    List<WorkflowNodeEntity> findByDefinitionIdAndDeletedFalseOrderByNodeOrderAsc(String definitionId);

    /**
     * 根据定义ID查询所有节点（按顺序）
     */
    List<WorkflowNodeEntity> findByDefinitionIdAndDeletedFalseOrderByNodeOrder(String definitionId);

    /**
     * 根据定义ID和节点编码查询
     */
    List<WorkflowNodeEntity> findByDefinitionIdAndNodeCodeAndDeletedFalse(String definitionId, String nodeCode);

    /**
     * 根据定义ID和顺序查询节点
     */
    List<WorkflowNodeEntity> findByDefinitionIdAndNodeOrderAndDeletedFalse(String definitionId, Integer nodeOrder);

    /**
     * 统计定义下的节点数量
     */
    int countByDefinitionIdAndDeletedFalse(String definitionId);
}
