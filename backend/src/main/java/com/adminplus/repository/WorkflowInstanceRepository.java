package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 工作流实例 Repository
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstanceEntity, String> {

    /**
     * 查询用户发起的工作流
     */
    List<WorkflowInstanceEntity> findByUserIdAndDeletedFalseOrderBySubmitTimeDesc(String userId);

    /**
     * 根据状态查询工作流实例
     */
    List<WorkflowInstanceEntity> findByUserIdAndStatusAndDeletedFalseOrderBySubmitTimeDesc(String userId, String status);

    /**
     * 查询运行中的工作流实例（当前节点需要特定用户审批）
     */
    @Query("SELECT DISTINCT i FROM WorkflowInstanceEntity i " +
           "JOIN WorkflowApprovalEntity a ON a.instanceId = i.id " +
           "WHERE i.status = 'running' " +
           "AND a.approverId = :approverId " +
           "AND a.approvalStatus = 'pending' " +
           "AND i.deleted = false " +
           "ORDER BY i.submitTime DESC")
    List<WorkflowInstanceEntity> findPendingApprovalsByUser(String approverId);

    /**
     * 根据定义ID查询实例
     */
    List<WorkflowInstanceEntity> findByDefinitionIdAndDeletedFalseOrderBySubmitTimeDesc(String definitionId);

    /**
     * 统计用户发起的工作流数量
     */
    long countByUserIdAndDeletedFalse(String userId);

    /**
     * 统计待审批数量
     */
    @Query("SELECT COUNT(DISTINCT i) FROM WorkflowInstanceEntity i " +
           "JOIN WorkflowApprovalEntity a ON a.instanceId = i.id " +
           "WHERE i.status = 'running' " +
           "AND a.approverId = :approverId " +
           "AND a.approvalStatus = 'pending' " +
           "AND i.deleted = false")
    long countPendingApprovalsByUser(String approverId);

    /**
     * 悲观锁查询工作流实例（用于状态机操作）
     *
     * @param instanceId 工作流实例ID
     * @return 工作流实例（悲观锁）
     */
    @Query("SELECT i FROM WorkflowInstanceEntity i WHERE i.id = :instanceId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WorkflowInstanceEntity> findByIdForUpdate(String instanceId);
}
