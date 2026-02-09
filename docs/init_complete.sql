-- ================================================================================
-- AdminPlus 完整数据库初始化脚本
-- PostgreSQL 16+
--
-- 说明：
-- 1. 创建数据库和所有表结构
-- 2. 插入初始数据（管理员账号、角色、菜单、部门等）
-- 3. 创建索引和触发器
--
-- 执行方式：
-- docker exec -i adminplus-postgres psql -U postgres < docs/init_complete.sql
-- ================================================================================

-- ================================================================================
-- 第一部分：创建数据库
-- ================================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS adminplus;

-- 切换到 adminplus 数据库
\c adminplus;

-- ================================================================================
-- 第二部分：删除已存在的表（按依赖关系倒序）
-- ================================================================================

DROP TABLE IF EXISTS sys_role_menu CASCADE;
DROP TABLE IF EXISTS sys_user_role CASCADE;
DROP TABLE IF EXISTS sys_dict_item CASCADE;
DROP TABLE IF EXISTS sys_dict CASCADE;
DROP TABLE IF EXISTS sys_log CASCADE;
DROP TABLE IF EXISTS sys_dept CASCADE;
DROP TABLE IF EXISTS sys_menu CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

-- ================================================================================
-- 第三部分：创建表结构
-- ================================================================================

-- 用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT,
    email TEXT,
    phone TEXT,
    avatar TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    settings JSONB,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    data_scope INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 菜单表
CREATE TABLE sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    type INTEGER NOT NULL,
    name TEXT NOT NULL,
    path TEXT,
    component TEXT,
    perm_key TEXT,
    icon TEXT,
    sort_order INTEGER,
    visible INTEGER NOT NULL DEFAULT 1,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 部门表
CREATE TABLE sys_dept (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    name TEXT NOT NULL,
    code TEXT,
    leader TEXT,
    phone TEXT,
    email TEXT,
    sort_order INTEGER,
    status INTEGER NOT NULL DEFAULT 1,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 用户-角色关联表
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE(user_id, role_id)
);

-- 角色-菜单关联表
CREATE TABLE sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE(role_id, menu_id)
);

-- 字典表
CREATE TABLE sys_dict (
    id BIGSERIAL PRIMARY KEY,
    dict_name TEXT NOT NULL,
    dict_type TEXT NOT NULL UNIQUE,
    status INTEGER NOT NULL DEFAULT 1,
    remark TEXT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 字典项表
CREATE TABLE sys_dict_item (
    id BIGSERIAL PRIMARY KEY,
    dict_id BIGINT NOT NULL,
    label TEXT NOT NULL,
    value TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    remark TEXT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES sys_dict(id) ON DELETE CASCADE
);

-- 操作日志表
CREATE TABLE sys_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username TEXT,
    module TEXT,
    operation_type INTEGER,
    description TEXT,
    method TEXT,
    params TEXT,
    ip TEXT,
    location TEXT,
    browser TEXT,
    os TEXT,
    cost_time BIGINT,
    status INTEGER,
    error_msg TEXT,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- ================================================================================
-- 第四部分：创建索引
-- ================================================================================

CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_status ON sys_user(status);

CREATE INDEX idx_sys_role_code ON sys_role(code);

CREATE INDEX idx_sys_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX idx_sys_menu_type ON sys_menu(type);

CREATE INDEX idx_sys_dept_parent_id ON sys_dept(parent_id);
CREATE INDEX idx_sys_dept_name ON sys_dept(name);
CREATE INDEX idx_sys_dept_code ON sys_dept(code);
CREATE INDEX idx_sys_dept_status ON sys_dept(status);

CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

CREATE INDEX idx_sys_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id ON sys_role_menu(menu_id);

CREATE INDEX idx_sys_dict_type ON sys_dict(dict_type);
CREATE INDEX idx_sys_dict_status ON sys_dict(status);

CREATE INDEX idx_sys_dict_item_dict_id ON sys_dict_item(dict_id);
CREATE INDEX idx_sys_dict_item_value ON sys_dict_item(value);

CREATE INDEX idx_sys_log_user_id ON sys_log(user_id);
CREATE INDEX idx_sys_log_module ON sys_log(module);
CREATE INDEX idx_sys_log_operation_type ON sys_log(operation_type);
CREATE INDEX idx_sys_log_create_time ON sys_log(create_time);

-- ================================================================================
-- 第五部分：创建更新时间触发器
-- ================================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为所有表添加触发器
CREATE TRIGGER update_sys_user_updated_at BEFORE UPDATE ON sys_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_role_updated_at BEFORE UPDATE ON sys_role
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_menu_updated_at BEFORE UPDATE ON sys_menu
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_dept_updated_at BEFORE UPDATE ON sys_dept
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_dict_updated_at BEFORE UPDATE ON sys_dict
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_dict_item_updated_at BEFORE UPDATE ON sys_dict_item
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sys_log_updated_at BEFORE UPDATE ON sys_log
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================================
-- 第六部分：插入初始数据
-- ================================================================================

-- 插入管理员角色
INSERT INTO sys_role (code, name, description, data_scope, status, sort_order)
VALUES ('ROLE_ADMIN', '超级管理员', '拥有所有权限', 1, 1, 1);

-- 插入管理员用户（密码: admin123，BCrypt 加密）
INSERT INTO sys_user (username, password, nickname, email, phone, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'admin@adminplus.com', '13800138000', 1);

-- 关联管理员用户和管理员角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN';

-- 插入菜单数据
INSERT INTO sys_menu (parent_id, type, name, path, component, perm_key, icon, sort_order, visible, status) VALUES
-- 系统管理模块
(NULL, 0, '系统管理', '/system', NULL, NULL, 'Setting', 1, 1, 1),

-- 用户管理
((SELECT id FROM sys_menu WHERE name = '系统管理'), 1, '用户管理', '/system/user', 'system/user/index', NULL, 'User', 1, 1, 1),
((SELECT id FROM sys_menu WHERE name = '用户管理'), 2, '新增用户', NULL, NULL, 'user:add', NULL, 1, 0, 1),
((SELECT id FROM sys_menu WHERE name = '用户管理'), 2, '编辑用户', NULL, NULL, 'user:edit', NULL, 2, 0, 1),
((SELECT id FROM sys_menu WHERE name = '用户管理'), 2, '删除用户', NULL, NULL, 'user:delete', NULL, 3, 0, 1),
((SELECT id FROM sys_menu WHERE name = '用户管理'), 2, '分配角色', NULL, NULL, 'user:assign', NULL, 4, 0, 1),
((SELECT id FROM sys_menu WHERE name = '用户管理'), 2, '重置密码', NULL, NULL, 'user:reset', NULL, 5, 0, 1),

-- 角色管理
((SELECT id FROM sys_menu WHERE name = '系统管理'), 1, '角色管理', '/system/role', 'system/role/index', NULL, 'UserFilled', 2, 1, 1),
((SELECT id FROM sys_menu WHERE name = '角色管理'), 2, '新增角色', NULL, NULL, 'role:add', NULL, 1, 0, 1),
((SELECT id FROM sys_menu WHERE name = '角色管理'), 2, '编辑角色', NULL, NULL, 'role:edit', NULL, 2, 0, 1),
((SELECT id FROM sys_menu WHERE name = '角色管理'), 2, '删除角色', NULL, NULL, 'role:delete', NULL, 3, 0, 1),
((SELECT id FROM sys_menu WHERE name = '角色管理'), 2, '分配权限', NULL, NULL, 'role:assign', NULL, 4, 0, 1),

-- 菜单管理
((SELECT id FROM sys_menu WHERE name = '系统管理'), 1, '菜单管理', '/system/menu', 'system/menu/index', NULL, 'Menu', 3, 1, 1),
((SELECT id FROM sys_menu WHERE name = '菜单管理'), 2, '新增菜单', NULL, NULL, 'menu:add', NULL, 1, 0, 1),
((SELECT id FROM sys_menu WHERE name = '菜单管理'), 2, '编辑菜单', NULL, NULL, 'menu:edit', NULL, 2, 0, 1),
((SELECT id FROM sys_menu WHERE name = '菜单管理'), 2, '删除菜单', NULL, NULL, 'menu:delete', NULL, 3, 0, 1),

-- 字典管理
((SELECT id FROM sys_menu WHERE name = '系统管理'), 1, '字典管理', '/system/dict', 'system/dict/index', NULL, 'Document', 4, 1, 1),
((SELECT id FROM sys_menu WHERE name = '字典管理'), 2, '新增字典', NULL, NULL, 'dict:add', NULL, 1, 0, 1),
((SELECT id FROM sys_menu WHERE name = '字典管理'), 2, '编辑字典', NULL, NULL, 'dict:edit', NULL, 2, 0, 1),
((SELECT id FROM sys_menu WHERE name = '字典管理'), 2, '删除字典', NULL, NULL, 'dict:delete', NULL, 3, 0, 1),

-- 部门管理
((SELECT id FROM sys_menu WHERE name = '系统管理'), 1, '部门管理', '/system/dept', 'system/dept/index', NULL, 'OfficeBuilding', 5, 1, 1),
((SELECT id FROM sys_menu WHERE name = '部门管理'), 2, '新增部门', NULL, NULL, 'dept:add', NULL, 1, 0, 1),
((SELECT id FROM sys_menu WHERE name = '部门管理'), 2, '编辑部门', NULL, NULL, 'dept:edit', NULL, 2, 0, 1),
((SELECT id FROM sys_menu WHERE name = '部门管理'), 2, '删除部门', NULL, NULL, 'dept:delete', NULL, 3, 0, 1),
((SELECT id FROM sys_menu WHERE name = '部门管理'), 2, '查询部门', NULL, NULL, 'dept:query', NULL, 4, 0, 1),
((SELECT id FROM sys_menu WHERE name = '部门管理'), 2, '部门列表', NULL, NULL, 'dept:list', NULL, 5, 0, 1);

-- 为管理员角色分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r, sys_menu m
WHERE r.code = 'ROLE_ADMIN';

-- 插入部门数据
INSERT INTO sys_dept (parent_id, name, code, leader, phone, email, sort_order, status) VALUES
(NULL, 'AdminPlus 总部', 'HQ', '张三', '010-12345678', 'hq@adminplus.com', 1, 1),
(1, '技术研发部', 'RD', '李四', '010-12345679', 'rd@adminplus.com', 1, 1),
(1, '市场运营部', 'MK', '王五', '010-12345680', 'mk@adminplus.com', 2, 1),
(2, '后端开发组', 'BE', '赵六', '010-12345681', 'be@adminplus.com', 1, 1),
(2, '前端开发组', 'FE', '钱七', '010-12345682', 'fe@adminplus.com', 2, 1),
(3, '市场推广组', 'MP', '孙八', '010-12345683', 'mp@adminplus.com', 1, 1),
(3, '客户服务组', 'CS', '周九', '010-12345684', 'cs@adminplus.com', 2, 1);

-- ================================================================================
-- 第七部分：添加表和字段注释
-- ================================================================================

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT ON TABLE sys_role_menu IS '角色-菜单关联表';
COMMENT ON TABLE sys_dict IS '字典表';
COMMENT ON TABLE sys_dict_item IS '字典项表';
COMMENT ON TABLE sys_log IS '操作日志表';

COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码（BCrypt 加密）';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.avatar IS '头像';
COMMENT ON COLUMN sys_user.status IS '状态（1=正常，0=禁用）';
COMMENT ON COLUMN sys_user.settings IS '配置（JSONB 类型）';

COMMENT ON COLUMN sys_role.code IS '角色编码（如 ROLE_ADMIN）';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.description IS '描述';
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围（1=全部，2=本部门，3=本部门及以下，4=仅本人）';
COMMENT ON COLUMN sys_role.status IS '状态（1=正常，0=禁用）';
COMMENT ON COLUMN sys_role.sort_order IS '排序';

COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.type IS '类型（0=目录，1=菜单，2=按钮）';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.perm_key IS '权限标识符（如 user:add）';
COMMENT ON COLUMN sys_menu.icon IS '图标';
COMMENT ON COLUMN sys_menu.sort_order IS '排序';
COMMENT ON COLUMN sys_menu.visible IS '是否可见（1=显示，0=隐藏）';
COMMENT ON COLUMN sys_menu.status IS '状态（1=正常，0=��用）';

COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.name IS '部门名称';
COMMENT ON COLUMN sys_dept.code IS '部门编码';
COMMENT ON COLUMN sys_dept.leader IS '负责人';
COMMENT ON COLUMN sys_dept.phone IS '联系电话';
COMMENT ON COLUMN sys_dept.email IS '邮箱';
COMMENT ON COLUMN sys_dept.sort_order IS '排序';
COMMENT ON COLUMN sys_dept.status IS '状态（1=正常，0=禁用）';

COMMENT ON COLUMN sys_dict.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict.dict_type IS '字典类型（唯一标识）';
COMMENT ON COLUMN sys_dict.status IS '状态（1=正常，0=禁用）';
COMMENT ON COLUMN sys_dict.remark IS '备注';

COMMENT ON COLUMN sys_dict_item.dict_id IS '字典ID';
COMMENT ON COLUMN sys_dict_item.label IS '字典标签';
COMMENT ON COLUMN sys_dict_item.value IS '字典值';
COMMENT ON COLUMN sys_dict_item.sort_order IS '排序';
COMMENT ON COLUMN sys_dict_item.status IS '状态（1=正常，0=禁用）';
COMMENT ON COLUMN sys_dict_item.remark IS '备注';

COMMENT ON COLUMN sys_log.user_id IS '操作人ID';
COMMENT ON COLUMN sys_log.username IS '操作人用户名';
COMMENT ON COLUMN sys_log.module IS '操作模块';
COMMENT ON COLUMN sys_log.operation_type IS '操作类型（1=查询，2=新增，3=修改，4=删除，5=导出，6=导入，7=其他）';
COMMENT ON COLUMN sys_log.description IS '操作描述';
COMMENT ON COLUMN sys_log.method IS '请求方法';
COMMENT ON COLUMN sys_log.params IS '请求参数';
COMMENT ON COLUMN sys_log.ip IS '请求IP';
COMMENT ON COLUMN sys_log.location IS '请求地点';
COMMENT ON COLUMN sys_log.browser IS '浏览器类型';
COMMENT ON COLUMN sys_log.os IS '操作系统';
COMMENT ON COLUMN sys_log.cost_time IS '执行时长（毫秒）';
COMMENT ON COLUMN sys_log.status IS '状态（1=成功，0=失败）';
COMMENT ON COLUMN sys_log.error_msg IS '异常信息';

-- ================================================================================
-- 第八部分：验证数据
-- ================================================================================

-- 显示初始化结果
SELECT '========================================' AS info;
SELECT '数据库初始化完成！' AS info;
SELECT '========================================' AS info;
SELECT '数据统计：' AS info;
SELECT '  用户数: ' || COUNT(*) AS info FROM sys_user;
SELECT '  角色数: ' || COUNT(*) AS info FROM sys_role;
SELECT '  菜单数: ' || COUNT(*) AS info FROM sys_menu;
SELECT '  部门数: ' || COUNT(*) AS info FROM sys_dept;
SELECT '默认账号：' AS info;
SELECT '  用户名: admin' AS info;
SELECT '  密码: admin123' AS info;
SELECT '  角色: 超级管理员' AS info;
SELECT '========================================' AS info;