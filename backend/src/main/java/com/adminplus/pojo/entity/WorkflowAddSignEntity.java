package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * 工作流加签记录实体
 * <p>
 * 记录工作流的加签操作（前加签、后加签）
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_add_sign",
       indexes = {
           @Index(name = "idx_wf_add_sign_inst_id", columnList = "instance_id"),
           @Index(name = "idx_wf_add_sign_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_add_sign_user_id", columnList = "add_user_id"),
           @Index(name = "idx_wf_add_sign_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_add_sign SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class WorkflowAddSignEntity extends BaseEntity {

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
     * 加签发起人ID
     */
    @Column(name = "initiator_id", nullable = false)
    private String initiatorId;

    /**
     * 加签发起人姓名
     */
    @Column(name = "initiator_name", length = 50)
    private String initiatorName;

    /**
     * 被加签人ID
     */
    @Column(name = "add_user_id", nullable = false)
    private String addUserId;

    /**
     * 被加签人姓名
     */
    @Column(name = "add_user_name", length = 50)
    private String addUserName;

    /**
     * 加签类型（before=前加签, after=后加签）
     */
    @Column(name = "add_type", nullable = false, length = 20)
    private String addType;

    /**
     * 加签原因
     */
    @Column(name = "add_reason", length = 500)
    private String addReason;

    /**
     * 原始审批人ID（转办时使用）
     */
    @Column(name = "original_approver_id", length = 50)
    private String originalApproverId;
}