package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowUrgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流催办记录Repository
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Repository
public interface WorkflowUrgeRepository extends JpaRepository<WorkflowUrgeEntity, String> {

    /**
     * 查询实例的所有催办记录
     */
    List<WorkflowUrgeEntity> findByInstanceIdAndDeletedFalseOrderByCreateTimeDesc(String instanceId);

    /**
     * 查询用户收到的催办记录
     */
    @Query("SELECT u FROM WorkflowUrgeEntity u WHERE u.urgeTargetId = :userId AND u.deleted = false ORDER BY u.createTime DESC")
    List<WorkflowUrgeEntity> findByUrgeTargetId(@Param("userId") String userId);

    /**
     * 查询用户发送的催办记录
     */
    @Query("SELECT u FROM WorkflowUrgeEntity u WHERE u.urgeUserId = :userId AND u.deleted = false ORDER BY u.createTime DESC")
    List<WorkflowUrgeEntity> findByUrgeUserId(@Param("userId") String userId);

    /**
     * 查询用户的未读催办记录
     */
    @Query("SELECT u FROM WorkflowUrgeEntity u WHERE u.urgeTargetId = :userId AND u.isRead = false AND u.deleted = false ORDER BY u.createTime DESC")
    List<WorkflowUrgeEntity> findUnreadByUrgeTargetId(@Param("userId") String userId);

    /**
     * 统计用户未读催办数量
     */
    @Query("SELECT COUNT(u) FROM WorkflowUrgeEntity u WHERE u.urgeTargetId = :userId AND u.isRead = false AND u.deleted = false")
    long countUnreadByUrgeTargetId(@Param("userId") String userId);
}