-- 检查用户数量
SELECT COUNT(*) as user_count FROM sys_user;

-- 检查角色数量  
SELECT COUNT(*) as role_count FROM sys_role;

-- 检查菜单数量
SELECT COUNT(*) as menu_count FROM sys_menu;

-- 检查用户-角色关联
SELECT COUNT(*) as user_role_count FROM sys_user_role;

-- 检查角色-菜单关联
SELECT COUNT(*) as role_menu_count FROM sys_role_menu;

-- 查看admin用户的角色
SELECT u.username, r.code 
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- 查看ROLE_ADMIN角色的菜单数量
SELECT r.code, COUNT(rm.menu_id) as menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
WHERE r.code = 'ROLE_ADMIN'
GROUP BY r.code;
