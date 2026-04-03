-- ====================================================================
-- 工作流钩子日志表索引优化
-- ====================================================================
-- 创建日期: 2026-04-03
-- 描述: 添加 deleted 索引优化软删除查询性能
-- ====================================================================

-- 添加 deleted 索引以优化清理任务的软删除查询
CREATE INDEX IF NOT EXISTS idx_wf_hook_log_deleted ON sys_workflow_hook_log(deleted);

-- 添加复合索引以优化按时间和删除状态查询
CREATE INDEX IF NOT EXISTS idx_wf_hook_log_time_deleted ON sys_workflow_hook_log(create_time, deleted);