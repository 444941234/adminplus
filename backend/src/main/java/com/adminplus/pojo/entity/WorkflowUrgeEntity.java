package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * 工作流催办记录实体
 * <p>
 * 记录工作流的催办操作
 * </p>
 *
 * @author AdminPlus
 * @since 2026-03-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_workflow_urge",
       indexes = {
           @Index(name = "idx_wf_urge_inst_id", columnList = "instance_id"),
           @Index(name = "idx_wf_urge_node_id", columnList = "node_id"),
           @Index(name = "idx_wf_urge_user_id", columnList = "urge_user_id"),
           @Index(name = "idx_wf_urge_target_id", columnList = "urge_target_id"),
           @Index(name = "idx_wf_urge_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_workflow_urge SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class WorkflowUrgeEntity extends BaseEntity {

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
     * 催办人ID
     */
    @Column(name = "urge_user_id", nullable = false)
    private String urgeUserId;

    /**
     * 催办人姓名
     */
    @Column(name = "urge_user_name", length = 50)
    private String urgeUserName;

    /**
     * 被催办人ID（目标审批人）
     */
    @Column(name = "urge_target_id", nullable = false)
    private String urgeTargetId;

    /**
     * 被催办人姓名
     */
    @Column(name = "urge_target_name", length = 50)
    private String urgeTargetName;

    /**
     * 催办内容
     */
    @Column(name = "urge_content", length = 500)
    private String urgeContent;

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