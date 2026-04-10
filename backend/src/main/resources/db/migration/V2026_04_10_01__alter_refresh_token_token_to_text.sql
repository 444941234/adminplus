-- 修改 refresh_token 表的 token 字段类型为 TEXT，支持更长的 JWT Token
ALTER TABLE sys_refresh_token ALTER COLUMN token TYPE TEXT;

-- 删除旧的索引（TEXT 类型不支持 B-tree 索引）
DROP INDEX IF EXISTS idx_refresh_token_token;

-- 使用 hash 索引替代（TEXT 类型支持）
CREATE INDEX IF NOT EXISTS idx_refresh_token_token_hash ON sys_refresh_token USING hash (token);
