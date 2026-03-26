-- =====================================================
-- 工作流状态机集成 - 数据库迁移脚本
-- =====================================================
-- 版本: V2026_03_26
-- 描述: 添加Spring State Machine支持所需的新字段和表
-- 作者: AdminPlus
-- 日期: 2026-03-26
-- =====================================================

-- =====================================================
-- 1. 创建状态机持久化表
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_state_machine (
    id VARCHAR(50) PRIMARY KEY,
    machine_id VARCHAR(100) NOT NULL COMMENT '状态机ID（通常为工作流实例ID）',
    state VARCHAR(50) NOT NULL COMMENT '当前状态',
    context TEXT COMMENT '扩展状态上下文（JSON格式）',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (machine_id)
);

CREATE INDEX idx_sm_machine_id ON sys_state_machine(machine_id);
CREATE INDEX idx_sm_state ON sys_state_machine(state);
CREATE INDEX idx_sm_deleted ON sys_state_machine(deleted);

COMMENT ON TABLE sys_state_machine IS 'Spring State Machine持久化表';

-- =====================================================
-- 2. 修改工作流实例表 - 添加状态机相关字段
-- =====================================================

ALTER TABLE sys_workflow_instance
ADD COLUMN IF NOT EXISTS business_key VARCHAR(100) COMMENT '业务键（关联业务表）';

ALTER TABLE sys_workflow_instance
ADD COLUMN IF NOT EXISTS version BIGINT COMMENT '乐观锁版本号';

ALTER TABLE sys_workflow_instance
ADD COLUMN IF NOT EXISTS node_path JSONB COMMENT '节点路径历史（JSONB格式）';

ALTER TABLE sys_workflow_instance
ADD COLUMN IF NOT EXISTS state_machine_context JSONB COMMENT '状态机上下文（JSONB格式）';

-- 为version字段添加默认值（对于已存在的记录）
ALTER TABLE sys_workflow_instance
ALTER COLUMN version SET DEFAULT 0;

-- 为已存在的记录设置初始version值
UPDATE sys_workflow_instance
SET version = 0
WHERE version IS NULL;

-- 设置version字段为NOT NULL
ALTER TABLE sys_workflow_instance
ALTER COLUMN version SET NOT NULL;

-- =====================================================
-- 3. 修改工作流节点表 - 添加条件分支字段
-- =====================================================

ALTER TABLE sys_workflow_node
ADD COLUMN IF NOT EXISTS condition_expression TEXT COMMENT '条件表达式（SpEL表达式）';

ALTER TABLE sys_workflow_node
ADD COLUMN IF NOT EXISTS next_nodes JSONB COMMENT '下一节点列表（JSONB格式）';

COMMENT ON COLUMN sys_workflow_node.condition_expression IS 'SpEL条件表达式，用于条件分支判断';
COMMENT ON COLUMN sys_workflow_node.next_nodes IS '可能的下一节点ID数组，JSONB格式';

-- =====================================================
-- 4. 修改工作流审批表 - 添加回退字段
-- =====================================================

ALTER TABLE sys_workflow_approval
ADD COLUMN IF NOT EXISTS is_rollback BOOLEAN DEFAULT FALSE COMMENT '是否为回退操作';

ALTER TABLE sys_workflow_approval
ADD COLUMN IF NOT EXISTS rollback_from_node_id VARCHAR(50) COMMENT '回退源节点ID';

ALTER TABLE sys_workflow_approval
ADD COLUMN IF NOT EXISTS rollback_from_node_name VARCHAR(100) COMMENT '回退源节点名称';

-- 为is_rollback字段设置默认值
ALTER TABLE sys_workflow_approval
ALTER COLUMN is_rollback SET DEFAULT FALSE;

-- 为已存在的记录设置初始值
UPDATE sys_workflow_approval
SET is_rollback = FALSE
WHERE is_rollback IS NULL;

-- 设置is_rollback字段为NOT NULL
ALTER TABLE sys_workflow_approval
ALTER COLUMN is_rollback SET NOT NULL;

COMMENT ON COLUMN sys_workflow_approval.is_rollback IS '标识该审批记录是否为回退操作';
COMMENT ON COLUMN sys_workflow_approval.rollback_from_node_id IS '回退操作源节点ID';
COMMENT ON COLUMN sys_workflow_approval.rollback_from_node_name IS '回退操作源节点名称';

-- =====================================================
-- 5. 创建索引以优化查询性能
-- =====================================================

-- 为business_key创建索引
CREATE INDEX IF NOT EXISTS idx_wf_inst_business_key
ON sys_workflow_instance(business_key);

-- 为node_path创建GIN索引（支持JSONB查询）
CREATE INDEX IF NOT EXISTS idx_wf_inst_node_path
ON sys_workflow_instance USING GIN(node_path);

-- 为state_machine_context创建GIN索引
CREATE INDEX IF NOT EXISTS idx_wf_inst_sm_context
ON sys_workflow_instance USING GIN(state_machine_context);

-- 为condition_expression创建索引
CREATE INDEX IF NOT EXISTS idx_wf_node_condition
ON sys_workflow_node(condition_expression);

-- 为next_nodes创建GIN索引
CREATE INDEX IF NOT EXISTS idx_wf_node_next_nodes
ON sys_workflow_node USING GIN(next_nodes);

-- =====================================================
-- 6. 数据完整性检查
-- =====================================================

-- 检查是否所有必需的列都已添加
DO $$
BEGIN
    -- 检查sys_workflow_instance表
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sys_workflow_instance'
        AND column_name IN ('business_key', 'version', 'node_path', 'state_machine_context')
    ) THEN
        RAISE EXCEPTION 'sys_workflow_instance table is missing required columns';
    END IF;

    -- 检查sys_workflow_node表
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sys_workflow_node'
        AND column_name IN ('condition_expression', 'next_nodes')
    ) THEN
        RAISE EXCEPTION 'sys_workflow_node table is missing required columns';
    END IF;

    -- 检查sys_workflow_approval表
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sys_workflow_approval'
        AND column_name IN ('is_rollback', 'rollback_from_node_id', 'rollback_from_node_name')
    ) THEN
        RAISE EXCEPTION 'sys_workflow_approval table is missing required columns';
    END IF;

    -- 检查sys_state_machine表是否存在
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'sys_state_machine'
    ) THEN
        RAISE EXCEPTION 'sys_state_machine table does not exist';
    END IF;

    RAISE NOTICE 'All database migrations completed successfully';
END $$;

-- =====================================================
-- 7. 授予必要的权限（如果需要）
-- =====================================================

-- GRANT ALL PRIVILEGES ON TABLE sys_state_machine TO adminplus;
-- GRANT ALL PRIVILEGES ON TABLE sys_workflow_instance TO adminplus;
-- GRANT ALL PRIVILEGES ON TABLE sys_workflow_node TO adminplus;
-- GRANT ALL PRIVILEGES ON TABLE sys_workflow_approval TO adminplus;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO adminplus;

-- =====================================================
-- 迁移完成
-- =====================================================
