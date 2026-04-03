-- ====================================================================
-- 工作流节点钩子系统 - 数据库迁移脚本
-- ====================================================================
-- 创建日期: 2026-04-02
-- 描述: 添加工作流节点钩子配置和执行日志表，扩展节点表字段
-- ====================================================================

-- 1. 扩展节点表字段（简单钩子 - SpEL表达式）
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_submit_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_approve_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_reject_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_rollback_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_cancel_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_withdraw_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS pre_add_sign_validate TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_submit_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_approve_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_reject_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_rollback_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_cancel_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_withdraw_action TEXT;
ALTER TABLE sys_workflow_node ADD COLUMN IF NOT EXISTS post_add_sign_action TEXT;

-- 2. 创建钩子配置表（复杂钩子）
CREATE TABLE IF NOT EXISTS sys_workflow_node_hook (
    id VARCHAR(50) PRIMARY KEY,
    node_id VARCHAR(50) NOT NULL,
    hook_point VARCHAR(30) NOT NULL,
    hook_type VARCHAR(20) NOT NULL,
    executor_type VARCHAR(20) NOT NULL,
    executor_config TEXT,
    async_execution BOOLEAN NOT NULL DEFAULT FALSE,
    block_on_failure BOOLEAN NOT NULL DEFAULT TRUE,
    failure_message VARCHAR(500),
    priority INTEGER NOT NULL DEFAULT 0,
    condition_expression TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    retry_interval INTEGER DEFAULT 1000,
    hook_name VARCHAR(100),
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_wf_hook_node_id ON sys_workflow_node_hook(node_id);
CREATE INDEX IF NOT EXISTS idx_wf_hook_point ON sys_workflow_node_hook(hook_point);
CREATE INDEX IF NOT EXISTS idx_wf_hook_deleted ON sys_workflow_node_hook(deleted);

-- 3. 创建钩子日志表
CREATE TABLE IF NOT EXISTS sys_workflow_hook_log (
    id VARCHAR(50) PRIMARY KEY,
    instance_id VARCHAR(50) NOT NULL,
    node_id VARCHAR(50),
    hook_id VARCHAR(50),
    hook_source VARCHAR(20),
    hook_point VARCHAR(30) NOT NULL,
    executor_type VARCHAR(20),
    executor_config TEXT,
    success BOOLEAN NOT NULL,
    result_code VARCHAR(50),
    result_message TEXT,
    execution_time INTEGER,
    retry_attempts INTEGER,
    async BOOLEAN,
    operator_id VARCHAR(50),
    operator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_wf_hook_log_instance ON sys_workflow_hook_log(instance_id);
CREATE INDEX IF NOT EXISTS idx_wf_hook_log_node ON sys_workflow_hook_log(node_id);
CREATE INDEX IF NOT EXISTS idx_wf_hook_log_point ON sys_workflow_hook_log(hook_point);
CREATE INDEX IF NOT EXISTS idx_wf_hook_log_time ON sys_workflow_hook_log(create_time);

-- 4. 添加注释
COMMENT ON COLUMN sys_workflow_node.pre_submit_validate IS '提交前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_approve_validate IS '同意前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_reject_validate IS '拒绝前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_rollback_validate IS '退回前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_cancel_validate IS '取消前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_withdraw_validate IS '撤回前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.pre_add_sign_validate IS '加签前校验（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_submit_action IS '提交后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_approve_action IS '同意后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_reject_action IS '拒绝后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_rollback_action IS '退回后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_cancel_action IS '取消后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_withdraw_action IS '撤回后执行（SpEL表达式）';
COMMENT ON COLUMN sys_workflow_node.post_add_sign_action IS '加签后执行（SpEL表达式）';

COMMENT ON TABLE sys_workflow_node_hook IS '工作流节点钩子配置表';
COMMENT ON TABLE sys_workflow_hook_log IS '工作流钩子执行日志表';
