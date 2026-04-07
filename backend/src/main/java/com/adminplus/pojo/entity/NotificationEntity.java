package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 通知实体
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "sys_notification",
       indexes = {
           @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
           @Index(name = "idx_notification_type", columnList = "type"),
           @Index(name = "idx_notification_status", columnList = "status"),
           @Index(name = "idx_notification_related", columnList = "related_id"),
           @Index(name = "idx_notification_create_time", columnList = "create_time"),
           @Index(name = "idx_notification_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_notification SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class NotificationEntity extends BaseEntity {

    /**
     * 通知类型
     * 如: workflow_approve, workflow_reject, system_notice
     */
    @Column(name = "type", length = 50)
    private String type;

    /**
     * 接收人ID
     */
    @Column(name = "recipient_id", length = 100)
    private String recipientId;

    /**
     * 通知标题
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 关联业务ID
     */
    @Column(name = "related_id", length = 100)
    private String relatedId;

    /**
     * 关联业务类型
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * 状态: 0-未读, 1-已读
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
}
