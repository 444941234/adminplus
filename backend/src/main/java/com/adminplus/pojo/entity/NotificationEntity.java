package com.adminplus.pojo.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 通知实体
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Data
@Entity
@Table(name = "sys_notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 100)
    private String id;

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
    @Column(name = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private java.time.LocalDateTime updateTime;
}
