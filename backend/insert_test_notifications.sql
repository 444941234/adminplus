-- 插入测试通知数据
-- 首先获取 admin 用户的 ID
DO $$
DECLARE
    v_admin_id VARCHAR(100);
    v_notification_id VARCHAR(100);
BEGIN
    -- 获取 admin 用户 ID
    SELECT id INTO v_admin_id FROM sys_user WHERE username = 'admin' AND deleted = false LIMIT 1;
    
    IF v_admin_id IS NOT NULL THEN
        -- 删除旧的测试通知
        DELETE FROM sys_notification WHERE recipient_id = v_admin_id AND type LIKE 'test_%';
        
        -- 插入 5 条测试通知（3 条未读，2 条已读）
        FOR i IN 1..3 LOOP
            v_notification_id := 'test_notify_' || v_admin_id || '_' || i;
            INSERT INTO sys_notification (id, type, recipient_id, title, content, status, create_time, update_time, deleted)
            VALUES (
                v_notification_id,
                'workflow_approve',
                v_admin_id,
                '测试通知 ' || i,
                '这是一条测试通知的内容，用于验证通知中心功能。',
                0,  -- 未读
                CURRENT_TIMESTAMP - (i || ' minutes')::interval,
                CURRENT_TIMESTAMP,
                false
            );
        END LOOP;
        
        FOR i IN 4..5 LOOP
            v_notification_id := 'test_notify_' || v_admin_id || '_' || i;
            INSERT INTO sys_notification (id, type, recipient_id, title, content, status, create_time, update_time, deleted)
            VALUES (
                v_notification_id,
                'workflow_cc',
                v_admin_id,
                '已读测试通知 ' || i,
                '这是一条已读的测试通知。',
                1,  -- 已读
                CURRENT_TIMESTAMP - (i || ' hours')::interval,
                CURRENT_TIMESTAMP,
                false
            );
        END LOOP;
        
        RAISE NOTICE '已插入 5 条测试通知给用户 %', v_admin_id;
    ELSE
        RAISE NOTICE '未找到 admin 用户';
    END IF;
END $$;

-- 查询插入的结果
SELECT recipient_id, COUNT(*) as total, 
       SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as unread
FROM sys_notification WHERE type LIKE 'test_%' AND deleted = false
GROUP BY recipient_id;
