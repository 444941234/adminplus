package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowAddSignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流加签记录Repository
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Repository
public interface WorkflowAddSignRepository extends JpaRepository<WorkflowAddSignEntity, String> {

    /**
     * 查询实例的所有加签记录
     */
    List<WorkflowAddSignEntity> findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(String instanceId);

    /**
     * 查询节点的加签记录
     */
    List<WorkflowAddSignEntity> findByInstanceIdAndNodeIdAndDeletedFalseOrderByCreateTimeDesc(
            String instanceId,
            String nodeId
    );

    /**
     * 查询用户发起的加签记录
     */
    @Query("SELECT a FROM WorkflowAddSignEntity a WHERE a.initiatorId = :userId AND a.deleted = false ORDER BY a.createTime DESC")
    List<WorkflowAddSignEntity> findByInitiatorId(@Param("userId") String userId);

    /**
     * 查询用户被加签的记录
     */
    @Query("SELECT a FROM WorkflowAddSignEntity a WHERE a.addUserId = :userId AND a.deleted = false ORDER BY a.createTime DESC")
    List<WorkflowAddSignEntity> findByAddUserId(@Param("userId") String userId);
}