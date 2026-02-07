# 个人中心 API 开发总结

## 已完成的工作

### 1. 创建的文件

#### 工具类
- **SecurityUtils.java** - 安全工具类，用于获取当前登录用户信息

#### 数据访问层
- **ProfileRepository.java** - 继承 JpaRepository，提供用户数据访问方法

#### 服务层
- **ProfileService.java** - 服务接口，定义个人中心业务方法
- **ProfileServiceImpl.java** - 服务实现，包含：
  - 获取当前用户信息
  - 更新用户信息
  - 修改密码（包含密码验证逻辑）
  - 上传头像（包含文件类型和大小验证）
  - 获取/更新用户设置

#### 控制器
- **ProfileController.java** - REST API 控制器，提供 6 个接口

#### 数据传输对象 (DTO)
- **ProfileUpdateReq.java** - 更新个人资料请求
- **PasswordChangeReq.java** - 修改密码请求
- **SettingsUpdateReq.java** - 更新设置请求

#### 视图对象 (VO)
- **ProfileVO.java** - 个人资料响应
- **SettingsVO.java** - 用户设置响应
- **AvatarUploadResp.java** - 头像上传响应

### 2. 实现的接口

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /api/profile | 获取当前用户信息 |
| PUT | /api/profile | 更新当前用户信息 |
| POST | /api/profile/password | 修改密码 |
| POST | /api/profile/avatar | 上传头像 |
| GET | /api/profile/settings | 获取用户设置 |
| PUT | /api/profile/settings | 更新用户设置 |

### 3. 核心功能

#### 密码修改
- 验证原密码是否正确
- 验证新密码和确认密码是否一致
- 验证新密码不能与原密码相同
- 使用 BCrypt 加密存储新密码

#### 头像上传
- 支持格式：JPG、PNG、GIF、WebP
- 文件大小限制：最大 2MB
- 按日期自动创建目录：`uploads/avatars/{yyyy}/{MM}/{dd}/`
- 生成唯一文件名（UUID）
- 上传成功后自动更新用户头像

#### 用户设置
- 使用 PostgreSQL JSONB 类型存储
- 支持任意键值对配置
- 更新时合并设置（未提供的键保持不变）
- 可用于存储主题、语言、布局偏好等

### 4. 安全特性

- 所有接口都需要 JWT 认证
- 通过 SecurityUtils 获取当前登录用户
- 密码使用 BCrypt 加密
- 参数校验使用 Jakarta Validation

### 5. 遵循的开发规范

- **命名规范**：使用项目统一的命名风格
- **代码风格**：
  - 使用 Lombok 简化代码
  - 使用 Record 定义 DTO/VO
  - 使用 @RequiredArgsConstructor 依赖注入
  - 使用 @Slf4j 日志记录
- **异常处理**：使用 BizException 和 GlobalExceptionHandler
- **事务管理**：查询使用 `@Transactional(readOnly = true)`，更新使用 `@Transactional`
- **API 文档**：使用 Swagger 注解（@Operation, @Tag）

### 6. 技术栈

- Spring Boot 3.5
- JDK 21
- Spring Data JPA
- PostgreSQL
- Lombok
- Jakarta Validation

## 文档

- **PROFILE_API.md** - 详细的 API 接口文档，包含请求/响应示例

## 验证清单

- [x] 创建所有必要的类文件
- [x] 实现所有 6 个接口
- [x] 添加参数校验
- [x] 添加异常处理
- [x] 遵循项目代码规范
- [x] 编写 API 文档

## 后续建议

1. **单元测试**：为 ProfileService 编写单元测试
2. **集成测试**：使用 MockMvc 测试 API 接口
3. **静态资源配置**：配置 Spring MVC 提供上传文件的访问
4. **文件清理**：实现定期清理未使用的头像文件
5. **头像裁剪**：考虑添加图片裁剪功能
6. **日志增强**：添加更��细的操作日志

## 注意事项

1. 头像文件保存在项目根目录的 `uploads/avatars/` 下，需要确保该目录有写入权限
2. 静态资源访问需要配置 Spring MVC 的资源映射
3. 用户设置使用 JSONB 类型，确保 PostgreSQL 数据库支持
4. 密码修改操作建议记录日志，用于安全审计

## 快速测试

使用 curl 测试接口：

```bash
# 1. 获取用户信息
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/profile

# 2. 更新用户信息
curl -X PUT -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"新昵称"}' \
  http://localhost:8080/api/profile

# 3. 修改密码
curl -X POST -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"old","newPassword":"new","confirmPassword":"new"}' \
  http://localhost:8080/api/profile/password

# 4. 上传头像
curl -X POST -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/avatar.jpg" \
  http://localhost:8080/api/profile/avatar

# 5. 获取设置
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/profile/settings

# 6. 更新设置
curl -X PUT -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"settings":{"theme":"dark"}}' \
  http://localhost:8080/api/profile/settings
```