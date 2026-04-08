-- 检查通知表结构和数据
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'sys_notification' ORDER BY ordinal_position;

-- 查看现有通知数据
SELECT id, type, recipient_id, title, status, create_time 
FROM sys_notification WHERE deleted = false LIMIT 10;

-- 统计各用户的通知数量
SELECT recipient_id, COUNT(*) as count, 
       SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as unread_count
FROM sys_notification WHERE deleted = false 
GROUP BY recipient_id;
