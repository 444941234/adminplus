# 个人中心 API 接口文档

## 概述

本文档描述了 AdminPlus 后端个人中心模块的 API 接口。

## 技术栈

- Spring Boot 3.5
- JDK 21
- Spring Data JPA
- PostgreSQL

## 接口列表

### 1. 获取用户信息

**接口地址**: `GET /api/profile`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.jpg",
    "status": 1,
    "createTime": "2026-02-06T10:00:00Z",
    "updateTime": "2026-02-07T10:00:00Z"
  },
  "timestamp": 1738944000000
}
```

### 2. 更新用户信息

**接口地址**: `PUT /api/profile`

**请求参数**:
```json
{
  "nickname": "新昵称",
  "email": "new@example.com",
  "phone": "13900139000",
  "avatar": "https://example.com/new-avatar.jpg"
}
```

**参数说明**:
- `nickname`: 昵称（可选，最大50字符）
- `email`: 邮箱（可选，需符合邮箱格式，最大100字符）
- `phone`: 手机号（可选，需符合手机号格式）
- `avatar`: 头像URL（可选，最大255字符）

**响应示例**: 同获取用户信息

### 3. 修改密码

**接口地址**: `POST /api/profile/password`

**请求参数**:
```json
{
  "oldPassword": "oldPassword123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

**参数说明**:
- `oldPassword`: 原密码（必填）
- `newPassword`: 新密码（必填，6-20字符）
- `confirmPassword`: 确认密码（必填）

**验证规则**:
1. 新密码和确认密码必须一致
2. 新密码不能与原密码相同
3. 原密码必须正确

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "timestamp": 1738944000000
}
```

### 4. 上传头像

**接口地址**: `POST /api/profile/avatar`

**请求参数**:
- `file`: 图片文件（multipart/form-data）

**文件要求**:
- 支持格式：JPG、PNG、GIF、WebP
- 文件大小：最大 2MB

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "url": "/uploads/avatars/2026/02/07/uuid.jpg"
  },
  "timestamp": 1738944000000
}
```

**说明**: 上传成功后会自动更新用户头像。

### 5. 获取用户设置

**接口地址**: `GET /api/profile/settings`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "settings": {
      "theme": "dark",
      "language": "zh-CN",
      "sidebarCollapsed": false
    }
  },
  "timestamp": 1738944000000
}
```

### 6. 更新用户设置

**接口地址**: `PUT /api/profile/settings`

**请求参数**:
```json
{
  "settings": {
    "theme": "light",
    "language": "en-US"
  }
}
```

**参数说明**:
- `settings`: 设置对象（JSON格式，支持任意键值对）

**响应示例**: 同获取用户设置

**说明**: 新设置会与现有设置合并，不会覆盖未提供的键。

## 异��处理

所有接口统一使用以下错误响应格式：

```json
{
  "code": 400,
  "message": "错误信息",
  "timestamp": 1738944000000
}
```

**常见错误码**:
- `400`: 参数校验失败
- `401`: 认证失败
- `403`: 无权访问
- `500`: 业务异常或系统错误

## 认证方式

所有接口都需要 JWT 认证，请求头需包含：

```
Authorization: Bearer {token}
```

## 文件结构

```
com.adminplus
├── controller
│   └── ProfileController.java          # 控制器
├── service
│   ├── ProfileService.java             # 服务接口
│   └── impl
│       └── ProfileServiceImpl.java     # 服务实现
├── repository
│   └── ProfileRepository.java          # 数据访问层
├── dto
│   ├── ProfileUpdateReq.java           # 更新个人资料请求
│   ├── PasswordChangeReq.java          # 修改密码请求
│   └── SettingsUpdateReq.java          # 更新设置请求
├── vo
│   ├── ProfileVO.java                  # 个人资料响应
│   ├── SettingsVO.java                 # 设置响应
│   └── AvatarUploadResp.java           # 头像上传响应
└── utils
    └── SecurityUtils.java              # 安全工具类（获取当前用户）
```

## 开发规范

1. **命名规范**:
   - Controller: `XxxController`
   - Service: `XxxService` / `XxxServiceImpl`
   - Repository: `XxxRepository`
   - DTO: `XxxReq` / `XxxResp`
   - VO: `XxxVO`

2. **代码风格**:
   - 使用 Lombok 简化代码
   - 使用 Record 定义 DTO 和 VO
   - 使用 @RequiredArgsConstructor 进行依赖注入
   - 使用 @Slf4j 进行日志记录

3. **异常处理**:
   - 使用 BizException 抛出业务异常
   - 使用 GlobalExceptionHandler 统一处理异常

4. **事务管理**:
   - 查询操作使用 `@Transactional(readOnly = true)`
   - 更新操作使用 `@Transactional`

5. **参数校验**:
   - 使用 Jakarta Validation 注解
   - 在 Controller 方法参数上添加 `@Valid` 注解

## 测试建议

1. 使用 Postman 或 Swagger UI 测试接口
2. 测试各种边界条件和异常情况
3. 验证文件上传功能
4. 测试并发场景下的数据一致性

## 注意事项

1. 头像文件保存在 `uploads/avatars/{yyyy}/{MM}/{dd}/` 目录下
2. 用户设置使用 PostgreSQL 的 JSONB 类型存储
3. 密码使用 BCrypt 加密存储
4. 所有接口通过 SecurityUtils 获取当前登录用户