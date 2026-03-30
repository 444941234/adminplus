-- 配置分组表
CREATE TABLE sys_config_group (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(50),
    sort_order INT NOT NULL DEFAULT 0,
    description VARCHAR(200),
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user VARCHAR(32) NOT NULL,
    update_user VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_config_group_code ON sys_config_group(code);
CREATE INDEX idx_config_group_sort ON sys_config_group(sort_order);
CREATE INDEX idx_config_group_status ON sys_config_group(status);
CREATE INDEX idx_config_group_deleted ON sys_config_group(deleted);

-- 配置项表
CREATE TABLE sys_config (
    id VARCHAR(32) PRIMARY KEY,
    group_id VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    effect_type VARCHAR(20) NOT NULL DEFAULT 'IMMEDIATE',
    default_value TEXT,
    description TEXT,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    validation_rule VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user VARCHAR(32) NOT NULL,
    update_user VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_config_group FOREIGN KEY (group_id) REFERENCES sys_config_group(id)
);

CREATE INDEX idx_config_group_id ON sys_config(group_id);
CREATE INDEX idx_config_key ON sys_config(key);
CREATE INDEX idx_config_status ON sys_config(status);
CREATE INDEX idx_config_deleted ON sys_config(deleted);

-- 配置历史表
CREATE TABLE sys_config_history (
    id VARCHAR(32) PRIMARY KEY,
    config_id VARCHAR(32) NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    remark VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user VARCHAR(32) NOT NULL,
    update_user VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_history_config FOREIGN KEY (config_id) REFERENCES sys_config(id)
);

CREATE INDEX idx_history_config ON sys_config_history(config_id);
CREATE INDEX idx_history_key ON sys_config_history(config_key);
CREATE INDEX idx_history_time ON sys_config_history(create_time);
CREATE INDEX idx_history_deleted ON sys_config_history(deleted);

-- 插入默认分组
INSERT INTO sys_config_group (id, name, code, icon, sort_order, description, status, create_user, update_user)
VALUES
('1', '基础配置', 'basic', 'Settings', 1, '系统基础配置', 1, 'system', 'system'),
('2', '邮件配置', 'email', 'Mail', 2, '邮件服务器配置', 1, 'system', 'system'),
('3', '存储配置', 'storage', 'Database', 3, '文件存储配置', 1, 'system', 'system');

-- 插入默认配置项
INSERT INTO sys_config (id, group_id, name, key, value, value_type, effect_type, description, status, create_user, update_user)
VALUES
('1', '1', '系统名称', 'system.name', 'AdminPlus', 'STRING', 'IMMEDIATE', '系统显示名称', 1, 'system', 'system'),
('2', '1', '上传文件大小限制', 'upload.maxSize', '10', 'NUMBER', 'IMMEDIATE', '上传文件大小限制（MB）', 1, 'system', 'system');
