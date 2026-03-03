package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工作流定义 Repository
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, String> {

    /**
     * 根据定义键查询
     */
    Optional<WorkflowDefinitionEntity> findByDefinitionKeyAndDeletedFalse(String definitionKey);

    /**
     * 查询所有启用的工作流定义
     */
    List<WorkflowDefinitionEntity> findByStatusAndDeletedFalseOrderByCreateTimeDesc(Integer status);

    /**
     * 根据分类查询
     */
    List<WorkflowDefinitionEntity> findByCategoryAndDeletedFalseOrderByCreateTimeDesc(String category);

    /**
     * 检查定义键是否存在
     */
    boolean existsByDefinitionKeyAndDeletedFalse(String definitionKey);
}
