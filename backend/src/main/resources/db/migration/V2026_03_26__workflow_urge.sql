-- 工作流催办功能
-- Author: AdminPlus
-- Date: 2026-03-26

-- 创建催办记录表
CREATE TABLE IF NOT EXISTS sys_workflow_urge (
    id VARCHAR(32) PRIMARY KEY,
    instance_id VARCHAR(32) NOT NULL,
    node_id VARCHAR(32),
    node_name VARCHAR(100),
    urge_user_id VARCHAR(32) NOT NULL,
    urge_user_name VARCHAR(50),
    urge_target_id VARCHAR(32) NOT NULL,
    urge_target_name VARCHAR(50),
    urge_content VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    read_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user VARCHAR(50),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_user VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0
);

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_wf_urge_inst_id ON sys_workflow_urge(instance_id);
CREATE INDEX IF NOT EXISTS idx_wf_urge_node_id ON sys_workflow_urge(node_id);
CREATE INDEX IF NOT EXISTS idx_wf_urge_user_id ON sys_workflow_urge(urge_user_id);
CREATE INDEX IF NOT EXISTS idx_wf_urge_target_id ON sys_workflow_urge(urge_target_id);
CREATE INDEX IF NOT EXISTS idx_wf_urge_deleted ON sys_workflow_urge(deleted);
CREATE INDEX IF NOT EXISTS idx_wf_urge_is_read ON sys_workflow_urge(is_read);

-- 添加注释
COMMENT ON TABLE sys_workflow_urge IS '工作流催办记录表';
COMMENT ON COLUMN sys_workflow_urge.id IS '主键ID';
COMMENT ON COLUMN sys_workflow_urge.instance_id IS '工作流实例ID';
COMMENT ON COLUMN sys_workflow_urge.node_id IS '节点ID';
COMMENT ON COLUMN sys_workflow_urge.node_name IS '节点名称';
COMMENT ON COLUMN sys_workflow_urge.urge_user_id IS '催办人ID';
COMMENT ON COLUMN sys_workflow_urge.urge_user_name IS '催办人姓名';
COMMENT ON COLUMN sys_workflow_urge.urge_target_id IS '被催办人ID（目标审批人）';
COMMENT ON COLUMN sys_workflow_urge.urge_target_name IS '被催办人姓名';
COMMENT ON COLUMN sys_workflow_urge.urge_content IS '催办内容';
COMMENT ON COLUMN sys_workflow_urge.is_read IS '是否已读';
COMMENT ON COLUMN sys_workflow_urge.read_time IS '阅读时间';