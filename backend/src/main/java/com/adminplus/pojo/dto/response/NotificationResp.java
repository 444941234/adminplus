package com.adminplus.pojo.dto.response;

import lombok.Data;

/**
 * 通知响应
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Data
public class NotificationResp {

    /**
     * 通知ID
     */
    private String id;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 接收人ID
     */
    private String recipientId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID
     */
    private String relatedId;

    /**
     * 关联业务类型
     */
    private String relatedType;

    /**
     * 状态: 0-未读, 1-已读
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
