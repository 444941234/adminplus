-- 通知表
CREATE TABLE IF NOT EXISTS sys_notification (
    id VARCHAR(100) PRIMARY KEY,
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    recipient_id VARCHAR(100) NOT NULL COMMENT '接收人ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    related_id VARCHAR(100) COMMENT '关联业务ID',
    related_type VARCHAR(50) COMMENT '关联业务类型',
    status INT DEFAULT 0 NOT NULL COMMENT '状态: 0-未读, 1-已读',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_notification_recipient ON sys_notification(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON sys_notification(status);
CREATE INDEX IF NOT EXISTS idx_notification_type ON sys_notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON sys_notification(create_time);

-- 添加表注释
COMMENT ON TABLE sys_notification IS '系统通知表';
