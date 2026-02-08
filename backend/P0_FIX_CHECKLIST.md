# AdminPlus 后端 P0 安全问题修复清单

**修复日期：** 2026-02-09
**项目路径：** /root/.openclaw/workspace/AdminPlus/backend/

---

## 修复清单

### ✅ P0-1: 生产环境 SQL 日志泄露敏感信息

- [x] 修改 `application.yml`
  - [x] 将 `show-sql` 默认值改为 `false`
  - [x] 将 `format_sql` 默认值改为 `false`
  - [x] 将 `use_sql_comments` 默认值改为 `false`

- [x] 修改 `application-dev.yml`
  - [x] 添加开发环境 JPA 配置
  - [x] 设置 `show-sql: true`
  - [x] 设置 `format_sql: true`
  - [x] 设置 `use_sql_comments: true`

- [x] 修改 `application-prod.yml`
  - [x] 添加生产环境 JPA 配置
  - [x] 设置 `show-sql: false`
  - [x] 设置 `format_sql: false`
  - [x] 设置 `use_sql_comments: false`

**验证方法：**
```bash
# 开发环境
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
# 检查日志是否输出 SQL 语句

# 生产环境
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# 检查日志是否不输出 SQL 语句
```

---

### ✅ P0-2: JWT 密钥管理不当

- [x] 修改 `SecurityConfig.java` 中的 `rsaKey()` 方法
  - [x] 移除包含密钥信息的警告日志
  - [x] 只记录密钥长度，不记录密钥内容
  - [x] 生产环境：从环境变量 `JWT_SECRET` 读取密钥
  - [x] 生产环境：验证密钥长度（至少 2048 位）
  - [x] 开发环境：优先从配置文件 `jwt.dev-secret` 读取固定密钥
  - [x] 开发环境：如果没有固定密钥，生成临时密钥
  - [x] 添加详细的注释，说明安全要求

- [x] 修改 `application.yml`
  - [x] 添加 `jwt.dev-secret` 配置项

**验证方法：**
```bash
# 开发环境（临时密钥）
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
# 检查日志：应该输出 "开发环境：使用临时生成的 JWT 密钥，密钥长度：2048 位"
# 检查日志：不应该输出密钥内容

# 开发环境（固定密钥）
export JWT_DEV_SECRET='{"kty":"RSA",...}'
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
# 检查日志：应该输出 "开发环境：使用配置文件中的 JWT 密钥，密钥长度：2048 位"

# 生产环境（未配置密钥）
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# 检查日志：应该抛出异常 "生产环境必须配置 JWT 密钥！"

# 生产环境（已配置密钥）
export JWT_SECRET='{"kty":"RSA",...}'
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# 检查日志：应该输出 "JWT 密钥已从环境变量加载，密钥长度：2048 位"
# 检查日志：不应该输出密钥内容
```

---

### ✅ P0-3: CSRF 保护配置不完整

- [x] 修改 `SecurityConfig.java` 中的 `filterChain()` 方法
  - [x] 添加 `security.jwt.use-cookie` 配置项
  - [x] 根据 JWT 存储方式动态配置 CSRF 保护
  - [x] Cookie 模式：启用 CSRF 保护
  - [x] Bearer Token 模式：禁用 CSRF 保护
  - [x] 登录/注册端点始终忽略 CSRF
  - [x] 添加详细的注释，说明 CSRF 保护策略

- [x] 修改 `application.yml`
  - [x] 添加 `spring.security.jwt.use-cookie` 配置项（默认为 `false`）

- [x] 修改 `application-prod.yml`
  - [x] 添加 `spring.security.jwt.use-cookie` 配置项

**验证方法：**
```bash
# Bearer Token 模式
export SECURITY_JWT_USE_COOKIE=false
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# 检查日志：应该输出 "CSRF 保护已禁用（Bearer Token 模式）"

# Cookie 模式
export SECURITY_JWT_USE_COOKIE=true
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
# 检查日志：应该输出 "CSRF 保护已启用（Cookie 存储 JWT 模式）"
```

---

### ✅ P0-4: 密码强度验证不一致

- [x] 修改 `UserServiceImpl.java` 中的 `resetPassword()` 方法
  - [x] 添加密码强度验证
  - [x] 使用 `PasswordUtils.isStrongPassword()` 方法
  - [x] 如果密码不符合强度要求，抛出异常
  - [x] 确保与 `createUser()` 方法使用相同的验证规则

**验证方法：**
```bash
# 测试弱密码
curl -X POST http://localhost:8081/api/v1/users/1/reset-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"newPassword": "123456"}'
# 预期结果：返回错误 "密码长度至少8位；密码必须包含数字；密码必须包含小写字母；密码必须包含大写字母；密码必须包含特殊字符"

# 测试强密码
curl -X POST http://localhost:8081/api/v1/users/1/reset-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"newPassword": "MyP@ss123"}'
# 预期结果：成功重置密码
```

---

### ✅ P0-5: 敏感信息未脱敏

- [x] 创建 `LogMaskingUtils.java`
  - [x] 提供统一的敏感信息脱��方法
  - [x] 支持密码、Token、身份证号、手机号、邮箱等敏感信息脱敏
  - [x] 使用正则表达式匹配和替换
  - [x] 添加详细的注释和使用示例

- [x] 创建 `LogMaskingConverter.java`
  - [x] 继承 `MessageConverter`
  - [x] 自动脱敏日志消息
  - [x] 支持多种敏感信息类型的脱敏
  - [x] 脱敏 SQL 语句中的敏感值

- [x] 修改 `logback-spring.xml`
  - [x] 注册日志脱敏转换器
  - [x] 在所有日志配置中使用 `%maskedMsg` 替代 `%msg`
  - [x] 开发环境和生产环境都启用脱敏

**验证方法：**
```bash
# 启动应用程序
mvn spring-boot:run

# 测试登录失败（用户名脱敏）
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong","captchaId":"test","captchaCode":"1234"}'
# 检查日志：应该输出 "用户登录失败: username=a***n"（而不是 "admin"）

# 测试密码脱敏
# 在代码中记录包含密码的日志
log.info("用户登录: username={}, password={}", username, password);
# 检查日志：应该输出 "用户登录: username=a***n, password=***"（而不是实际密码）

# 测试手机号脱敏
log.info("用户手机号: {}", "13800138000");
# 检查日志：应该输出 "用户手机号: 138****8000"（而不是 "13800138000"）

# 测试邮箱脱敏
log.info("用户邮箱: {}", "test@example.com");
# 检查日志：应该输出 "用户邮箱: t***t@example.com"（而不是 "test@example.com"）
```

---

## 文件变更清单

### 新增文件

- [x] `src/main/java/com/adminplus/utils/LogMaskingUtils.java` (3731 bytes)
- [x] `src/main/java/com/adminplus/logging/LogMaskingConverter.java` (3348 bytes)

### 修改文件

- [x] `src/main/resources/application.yml`
  - [x] 修改 JPA 配置（默认关闭 SQL 日志）
  - [x] 添加 JWT 存储方式配置
  - [x] 添加 `jwt.dev-secret` 配置项

- [x] `src/main/resources/application-dev.yml`
  - [x] 添加开发环境 JPA 配置（启用 SQL 日志）

- [x] `src/main/resources/application-prod.yml`
  - [x] 添加生产环境 JPA 配置（关闭 SQL 日志）
  - [x] 添加生产环境安全配置

- [x] `src/main/resources/logback-spring.xml`
  - [x] 注册日志脱敏转换器
  - [x] 所有日志配置使用脱敏转换器

- [x] `src/main/java/com/adminplus/config/SecurityConfig.java`
  - [x] 修改 JWT 密钥生成逻辑（移除密钥信息日志）
  - [x] 修改 CSRF 保护配置（根据 JWT 存储方式动态配置）

- [x] `src/main/java/com/adminplus/service/impl/UserServiceImpl.java`
  - [x] 修改 `resetPassword()` 方法（添加密码强度验证）

---

## 环境变量配置清单

### 生产环境必须配置的环境变量

- [x] `JWT_SECRET`：JWT 密钥（RSA 2048 位或以上）
- [x] `CORS_ALLOWED_ORIGINS`：CORS 允许的域名（逗号分隔）
- [x] `SECURITY_JWT_USE_COOKIE`：JWT 存储方式（`false` 或 `true`）
- [x] `DB_URL`：数据库连接 URL
- [x] `DB_USERNAME`：数据库用户名
- [x] `DB_PASSWORD`：数据库密码
- [x] `VIRUS_SCAN_ENABLED`：病毒扫描是否启用（`true` 或 `false`）
- [x] `CLAMAV_HOST`：ClamAV 主机地址
- [x] `CLAMAV_PORT`：ClamAV 端口号

### 开发环境可选的环境变量

- [x] `JWT_DEV_SECRET`：开发环境固定密钥（可选，用于测试）
- [x] `CORS_ALLOWED_ORIGINS`：CORS 允许的域名（默认为 `http://localhost:5173`）

---

## 测试清单

### 单元测试

- [ ] 测试 `LogMaskingUtils` 的所有脱敏方法
- [ ] 测试 `LogMaskingConverter` 的脱敏功能
- [ ] 测试 `SecurityConfig.rsaKey()` 的密钥生成逻辑
- [ ] 测试 `SecurityConfig.filterChain()` 的 CSRF 保护配置
- [ ] 测试 `UserServiceImpl.resetPassword()` 的密码强度验证

### 集成测试

- [ ] 测试开发环境 SQL 日志输出
- [ ] 测试生产环境 SQL 日志不输出
- [ ] 测试开发环境 JWT 密钥生成（临时密钥）
- [ ] 测试开发环境 JWT 密钥生成（固定密钥）
- [ ] 测试生产环境 JWT 密钥加载（环境变量）
- [ ] 测试生产环境 JWT 密钥未配置（抛出异常）
- [ ] 测试 Bearer Token 模式 CSRF 保护禁用
- [ ] 测试 Cookie 模式 CSRF 保护启用
- [ ] 测试弱密码重置（拒绝）
- [ ] 测试强密码重置（接受）
- [ ] 测试日志脱敏（密码）
- [ ] 测试日志脱敏（Token）
- [ ] 测试日志脱敏（手机号）
- [ ] 测试日志脱敏（邮箱）

### 安全测试

- [ ] 测试 SQL 注入防护
- [ ] 测试 XSS 防护
- [ ] 测试 CSRF 防护（Cookie 模式）
- [ ] 测试密码强度验证
- [ ] 测试日志脱敏
- [ ] 测试 JWT Token 安全性

---

## 部署清单

### 开发环境部署

- [ ] 配置 `SPRING_PROFILES_ACTIVE=dev`
- [ ] 配置可选的环境变量（`JWT_DEV_SECRET`、`CORS_ALLOWED_ORIGINS`）
- [ ] 启动应用程序
- [ ] 验证 SQL 日志输出
- [ ] 验证 JWT 密钥生成
- [ ] 验证 CSRF 保护配置
- [ ] 验证密码强度验证
- [ ] 验证日志脱敏

### 生产环境部署

- [ ] 配置 `SPRING_PROFILES_ACTIVE=prod`
- [ ] 配置所有必须的环境变量
- [ ] 启动应用程序
- [ ] 验证 SQL 日志不输出
- [ ] 验证 JWT 密钥加载
- [ ] 验证 CSRF 保护配置
- [ ] 验证密码强度验证
- [ ] 验证日志脱敏
- [ ] 验证 CORS 配置
- [ ] 验证病毒扫描配置

---

## 文档清单

- [x] `P0_SECURITY_FIX_SUMMARY.md`：修复总结文档
- [x] `P0_FIX_CHECKLIST.md`：修复清单文档
- [x] `LogMaskingUtils.java`：日志脱敏工具类文档（注释）
- [x] `LogMaskingConverter.java`：日志脱敏转换器文档（注释）
- [x] `SecurityConfig.java`：安全配置文档（注释）
- [x] `UserServiceImpl.java`：用户服务文档（注释）

---

## 代码审查清单

### 代码质量

- [ ] 所有修改都遵循了项目开发规范
- [ ] 所有修改都使用了 Spring Boot 最佳实践
- [ ] 所有修改都不影响现有功能
- [ ] 所有修改都添加了适当的注释
- [ ] 所有修改都经过了测试验证

### 安全性

- [ ] 所有敏感信息都进行了脱敏处理
- [ ] 所有密码都使用了强密码验证
- [ ] 所有密钥都使用了安全的管理方式
- [ ] 所有日志都进行了脱敏处理
- [ ] 所有配置都符合安全最佳实践

### 可维护性

- [ ] 所有代码都添加了详细的注释
- [ ] 所有配置都使用了环境变量
- [ ] 所有修改都记录在文档中
- [ ] 所有修改都经过了代码审查

---

## 总结

✅ **P0-1: 生产环境 SQL 日志泄露敏感信息** - 已修复
✅ **P0-2: JWT 密钥管理不当** - 已修复
✅ **P0-3: CSRF 保护配置不完整** - 已修复
✅ **P0-4: 密码强度验证不一致** - 已修复
✅ **P0-5: 敏感信息未脱敏** - 已修复

**修复状态：** ✅ 完成
**测试状态：** ⏳ 待测试
**部署状态：** ⏳ 待部署

---

**修复完成日期：** 2026-02-09
**修复人员：** OpenClaw Subagent