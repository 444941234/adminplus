package com.adminplus.repository;

import com.adminplus.pojo.entity.WorkflowApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流审批记录 Repository
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Repository
public interface WorkflowApprovalRepository extends JpaRepository<WorkflowApprovalEntity, String> {

    /**
     * 根据实例ID查询所有审批记录
     */
    List<WorkflowApprovalEntity> findByInstanceIdAndDeletedFalseOrderByCreateTimeAsc(String instanceId);

    /**
     * 根据实例ID和节点ID查询审批记录
     */
    List<WorkflowApprovalEntity> findByInstanceIdAndNodeIdAndDeletedFalse(String instanceId, String nodeId);

    /**
     * 查询待审批的记录
     */
    List<WorkflowApprovalEntity> findByApproverIdAndApprovalStatusAndDeletedFalseOrderByCreateTimeAsc(
            String approverId, String approvalStatus);

    /**
     * 根据实例ID和状态查询
     */
    List<WorkflowApprovalEntity> findByInstanceIdAndApprovalStatusAndDeletedFalse(String instanceId, String status);

    /**
     * 查询用户在某实例的审批记录
     */
    List<WorkflowApprovalEntity> findByInstanceIdAndApproverIdAndDeletedFalse(String instanceId, String approverId);

    /**
     * 统计实例的待审批数量
     */
    long countByInstanceIdAndApprovalStatusAndDeletedFalse(String instanceId, String approvalStatus);
}
