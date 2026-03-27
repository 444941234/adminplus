-- 工作流抄送功能
-- Author: AdminPlus
-- Date: 2026-03-26

-- 1. 添加抄送人字段到工作流节点表
ALTER TABLE sys_workflow_node
ADD COLUMN IF NOT EXISTS cc_user_ids TEXT;

COMMENT ON COLUMN sys_workflow_node.cc_user_ids IS '抄送人ID列表（JSON字符串，存储用户ID数组）';

ALTER TABLE sys_workflow_node
ADD COLUMN IF NOT EXISTS cc_role_ids TEXT;

COMMENT ON COLUMN sys_workflow_node.cc_role_ids IS '抄送角色ID列表（JSON字符串，存储角色ID数组）';

-- 2. 创建抄送记录表
CREATE TABLE IF NOT EXISTS sys_workflow_cc (
    id VARCHAR(32) PRIMARY KEY,
    instance_id VARCHAR(32) NOT NULL,
    node_id VARCHAR(32),
    node_name VARCHAR(100),
    user_id VARCHAR(32) NOT NULL,
    user_name VARCHAR(50),
    cc_type VARCHAR(20) NOT NULL,
    cc_content VARCHAR(500),
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
CREATE INDEX IF NOT EXISTS idx_wf_cc_inst_id ON sys_workflow_cc(instance_id);
CREATE INDEX IF NOT EXISTS idx_wf_cc_node_id ON sys_workflow_cc(node_id);
CREATE INDEX IF NOT EXISTS idx_wf_cc_user_id ON sys_workflow_cc(user_id);
CREATE INDEX IF NOT EXISTS idx_wf_cc_deleted ON sys_workflow_cc(deleted);
CREATE INDEX IF NOT EXISTS idx_wf_cc_is_read ON sys_workflow_cc(is_read);

-- 添加注释
COMMENT ON TABLE sys_workflow_cc IS '工作流抄送记录表';
COMMENT ON COLUMN sys_workflow_cc.id IS '主键ID';
COMMENT ON COLUMN sys_workflow_cc.instance_id IS '工作流实例ID';
COMMENT ON COLUMN sys_workflow_cc.node_id IS '节点ID';
COMMENT ON COLUMN sys_workflow_cc.node_name IS '节点名称';
COMMENT ON COLUMN sys_workflow_cc.user_id IS '被抄送人ID';
COMMENT ON COLUMN sys_workflow_cc.user_name IS '被抄送人姓名';
COMMENT ON COLUMN sys_workflow_cc.cc_type IS '抄送类型（start=发起时, approve=审批通过, reject=审批拒绝, rollback=回退）';
COMMENT ON COLUMN sys_workflow_cc.cc_content IS '抄送内容/原因';
COMMENT ON COLUMN sys_workflow_cc.is_read IS '是否已读';
COMMENT ON COLUMN sys_workflow_cc.read_time IS '阅读时间';
