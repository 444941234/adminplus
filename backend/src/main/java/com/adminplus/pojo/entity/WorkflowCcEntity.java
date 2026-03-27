package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * 工作流抄送记录实体
 * <p>
 * 记录工作流中的抄送通知
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_cc",
       indexes = {
           @Index(name = "idx_wf_cc_inst_id", columnList = "instance_id"),
           @Index(name = "idx_wf_cc_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_cc_user_id", columnList = "user_id"),
           @Index(name = "idx_wf_cc_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_cc SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowCcEntity extends BaseEntity {

    /**
     * 工作流实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    /**
     * 节点ID
     */
    @Column(name = "node_id")
    private String nodeId;

    /**
     * 节点名称
     */
    @Column(name = "node_name", length = 100)
    private String nodeName;

    /**
     * 被抄送人ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 被抄送人姓名
     */
    @Column(name = "user_name", length = 50)
    private String userName;

    /**
     * 抄送类型（start=发起时, approve=审批通过, reject=审批拒绝, rollback=回退）
     */
    @Column(name = "cc_type", nullable = false, length = 20)
    private String ccType;

    /**
     * 抄送内容/原因
     */
    @Column(name = "cc_content", length = 500)
    private String ccContent;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private Instant readTime;

    /**
     * 是否已读
     */
    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.readTime = Instant.now();
    }
}
