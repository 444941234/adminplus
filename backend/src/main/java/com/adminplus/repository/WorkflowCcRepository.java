package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowCcEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流抄送记录Repository
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Repository
public interface WorkflowCcRepository extends JpaRepository<WorkflowCcEntity, String> {

    /**
     * 查询实例的所有抄送记录
     */
    List<WorkflowCcEntity> findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(String instanceId);

    /**
     * 查询用户的未读抄送记录
     */
    @Query("SELECT cc FROM WorkflowCcEntity cc WHERE cc.userId = :userId AND cc.isRead = false AND cc.deleted = false ORDER BY cc.createTime DESC")
    List<WorkflowCcEntity> findUnreadByUserId(@Param("userId") String userId);

    /**
     * 统计用户未读抄送数量
     */
    @Query("SELECT COUNT(cc) FROM WorkflowCcEntity cc WHERE cc.userId = :userId AND cc.isRead = false AND cc.deleted = false")
    long countUnreadByUserId(@Param("userId") String userId);

    /**
     * 查询用户的所有抄送记录
     */
    @Query("SELECT cc FROM WorkflowCcEntity cc WHERE cc.userId = :userId AND cc.deleted = false ORDER BY cc.createTime DESC")
    List<WorkflowCcEntity> findByUserId(@Param("userId") String userId);
}
