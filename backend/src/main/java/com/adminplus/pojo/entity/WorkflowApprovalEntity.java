package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 工作流审批记录实体
 * <p>
 * 记录每个节点的审批历史
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_approval",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_wf_appr_inst_node_user", columnNames = {"instance_id", "node_id", "approver_id"})
       },
       indexes = {
           @Index(name = "idx_wf_appr_inst_id", columnList = "instance_id"),
           @Index(name = "idx_wf_appr_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_appr_user_id", columnList = "approver_id"),
           @Index(name = "idx_wf_appr_status", columnList = "approval_status"),
           @Index(name = "idx_wf_appr_deleted", columnList = "deleted"),
           // 复合索引：用于查询用户待审批记录（最常用的查询场景）
           @Index(name = "idx_wf_appr_pending", columnList = "approver_id, approval_status, instance_id")
       })
@SQLDelete(sql = "UPDATE sys_workflow_approval SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowApprovalEntity extends BaseEntity {

    /**
     * 工作流实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    /**
     * 节点ID
     */
    @Column(name = "node_id", nullable = false)
    private String nodeId;

    /**
     * 节点名称
     */
    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    /**
     * 审批人ID
     */
    @Column(name = "approver_id", nullable = false)
    private String approverId;

    /**
     * 审批人姓名
     */
    @Column(name = "approver_name", length = 50)
    private String approverName;

    /**
     * 审批状态（pending=待审批, approved=已同意, rejected=已拒绝, transferred=已转审, delegated=已代理）
     */
    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "pending";

    /**
     * 审批意见
     */
    @Column(name = "comment", length = 500)
    private String comment;

    /**
     * 审批附件（JSON数组，存储文件ID列表）
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private java.time.Instant approvalTime;

    /**
     * 是否为回退操作
     */
    @Column(name = "is_rollback")
    private Boolean isRollback = false;

    /**
     * 回退源节点ID
     */
    @Column(name = "rollback_from_node_id", length = 50)
    private String rollbackFromNodeId;

    /**
     * 回退源节点名称
     */
    @Column(name = "rollback_from_node_name", length = 100)
    private String rollbackFromNodeName;

    /**
     * 是否为待审批
     */
    public boolean isPending() {
        return "pending".equals(this.approvalStatus);
    }

    /**
     * 是否已同意
     */
    public boolean isApproved() {
        return "approved".equals(this.approvalStatus);
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return "rejected".equals(this.approvalStatus);
    }

    /**
     * 是否已处理（非pending状态）
     */
    public boolean isProcessed() {
        return !isPending();
    }
}
