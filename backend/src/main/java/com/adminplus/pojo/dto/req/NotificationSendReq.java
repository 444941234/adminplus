package com.adminplus.pojo.dto.req;

import lombok.Data;

/**
 * 通知发送请求
 *
 * @author AdminPlus
 * @since 2026-04-04
 */
@Data
public class NotificationSendReq {

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
}
