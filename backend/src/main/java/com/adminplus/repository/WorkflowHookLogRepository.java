package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowHookLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流钩子执行日志 Repository
 *
 * @author AdminPlus
 * @since 2026-04-02
 */
@Repository
public interface WorkflowHookLogRepository extends JpaRepository<WorkflowHookLogEntity, String> {

    /**
     * 根据实例ID查询所有日志
     */
    List<WorkflowHookLogEntity> findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(String instanceId);

    /**
     * 根据实例ID和钩子点查询日志
     */
    List<WorkflowHookLogEntity> findByInstanceIdAndHookPointAndDeletedFalseOrderByCreateTimeDesc(String instanceId, String hookPoint);

    /**
     * 根据实例ID和节点ID查询日志
     */
    List<WorkflowHookLogEntity> findByInstanceIdAndNodeIdAndDeletedFalseOrderByCreateTimeDesc(String instanceId, String nodeId);

    /**
     * 根据钩子配置ID查询日志
     */
    List<WorkflowHookLogEntity> findByHookIdAndDeletedFalseOrderByCreateTimeDesc(String hookId);

    /**
     * 统计实例的日志数量
     */
    long countByInstanceIdAndDeletedFalse(String instanceId);
}
