-- 工作流加签/转办功能
-- Author: AdminPlus
-- Date: 2026-03-26

-- 创建加签记录表
CREATE TABLE IF NOT EXISTS sys_workflow_add_sign (
    id VARCHAR(32) PRIMARY KEY,
    instance_id VARCHAR(32) NOT NULL,
    node_id VARCHAR(32) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    initiator_id VARCHAR(32) NOT NULL,
    initiator_name VARCHAR(50),
    add_user_id VARCHAR(32) NOT NULL,
    add_user_name VARCHAR(50),
    add_type VARCHAR(20) NOT NULL,
    add_reason VARCHAR(500),
    original_approver_id VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user VARCHAR(50),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_user VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0
);

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_wf_add_sign_inst_id ON sys_workflow_add_sign(instance_id);
CREATE INDEX IF NOT EXISTS idx_wf_add_sign_node_id ON sys_workflow_add_sign(node_id);
CREATE INDEX IF NOT EXISTS idx_wf_add_sign_user_id ON sys_workflow_add_sign(add_user_id);
CREATE INDEX IF NOT EXISTS idx_wf_add_sign_deleted ON sys_workflow_add_sign(deleted);

-- 添加注释
COMMENT ON TABLE sys_workflow_add_sign IS '工作流加签记录表';
COMMENT ON COLUMN sys_workflow_add_sign.id IS '主键ID';
COMMENT ON COLUMN sys_workflow_add_sign.instance_id IS '工作流实例ID';
COMMENT ON COLUMN sys_workflow_add_sign.node_id IS '节点ID';
COMMENT ON COLUMN sys_workflow_add_sign.node_name IS '节点名称';
COMMENT ON COLUMN sys_workflow_add_sign.initiator_id IS '加签发起人ID';
COMMENT ON COLUMN sys_workflow_add_sign.initiator_name IS '加签发起人姓名';
COMMENT ON COLUMN sys_workflow_add_sign.add_user_id IS '被加签人ID';
COMMENT ON COLUMN sys_workflow_add_sign.add_user_name IS '被加签人姓名';
COMMENT ON COLUMN sys_workflow_add_sign.add_type IS '加签类型（before=前加签, after=后加签）';
COMMENT ON COLUMN sys_workflow_add_sign.add_reason IS '加签原因';
COMMENT ON COLUMN sys_workflow_add_sign.original_approver_id IS '原始审批人ID（转办时使用）';

-- 更新审批表，增加转办状态
ALTER TABLE sys_workflow_approval
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'pending';

COMMENT ON COLUMN sys_workflow_approval.approval_status IS '审批状态（pending=待审批, approved=已同意, rejected=已拒绝, transferred=已转审, delegated=已代理）';