# AdminPlus 后端代码审计报告

**审计日期：** 2026-02-07
**审计范围：** /root/.openclaw/workspace/AdminPlus/backend/
**审计人员：** OpenClaw Subagent

---

## 执行摘要

本次审计对 AdminPlus 后端代码进行了全面检查，涵盖代码质量、安全性、异常处理、性能和配置五个方面。共发现 **20 个问题**，其中：
- **高危问题：** 6 个
- **中危问题：** 9 个
- **低危问题：** 5 个

总体而言，代码结构清晰，遵循了大部分开发规范，但在性能优化和安全防护方面存在一些需要改进的地方。

---

## 一、高危问题（6 个）

### 1.1 敏感信息硬编码 - 数据库密码明文存储

**位置：** `src/main/resources/application.yml`

**问题描述：**
```yaml
datasource:
  url: jdbc:postgresql://postgres:5432/adminplus
  username: postgres
  password: postgres  # 明文密码
```

**风险等级：** 🔴 高危

**影响：**
- 数据库密码以明文形式存储在配置文件中
- 如果配置文件泄露，攻击者可以直接访问数据库

**修复建议：**
1. 使用环境变量存储敏感信息
2. 使用 Jasypt 加密配置文件中的敏感信息
3. 在生产环境中使用密钥管理服务（如 HashiCorp Vault）

**修复示例：**
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:postgres}
```

或者使用 Jasypt：
```yaml
spring:
  datasource:
    password: ENC(encrypted_password_here)
```

---

### 1.2 JWT 密钥每次重启都会变化

**位置：** `src/main/java/com/adminplus/config/SecurityConfig.java`

**问题描述：**
```java
@Bean
public RSAKey rsaKey() throws JOSEException {
    return new RSAKeyGenerator(2048)
            .keyID("adminplus-key")
            .generate();  // 每次重启都会生成新密钥
}
```

**风险等级：** 🔴 高危

**影响：**
- 每次应用重启都会生成新的 JWT 密钥
- 导致所有已签发的 Token 立即失效
- 用户需要重新登录

**修复建议：**
1. 从环境变量读取密钥
2. 或从配置文件读取密钥（使用 Jasypt 加密）
3. 或使用密钥管理服务

**修复示例：**
```java
@Bean
public RSAKey rsaKey() throws JOSEException {
    String privateKeyPem = environment.getProperty("jwt.private-key");
    if (privateKeyPem != null) {
        // 从 PEM 文件加载
        return RSAKey.parseFromPEMEncodedObjects(privateKeyPem);
    }
    // 开发环境生成临时密钥
    return new RSAKeyGenerator(2048)
            .keyID("adminplus-key")
            .generate();
}
```

---

### 1.3 N+1 查询问题 - 用户列表查询

**位置：** `src/main/java/com/adminplus/service/impl/UserServiceImpl.java`

**问题描述：**
```java
@Override
@Transactional(readOnly = true)
public PageResultVO<UserVO> getUserList(Integer page, Integer size, String keyword) {
    var pageable = PageRequest.of(page - 1, size);
    var pageResult = userRepository.findAll(pageable);

    var records = pageResult.getContent().stream().map(user -> {
        // ❌ N+1 查询：对每个用户都执行一次查询
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleId -> roleRepository.findById(roleId).orElse(null))  // ❌ 每次都查询
                .filter(role -> role != null)
                .map(RoleEntity::getName)
                .toList();
        // ...
    }).toList();
}
```

**风险等级：** 🔴 高危

**影响：**
- 查询 100 个用户会执行 201 次数据库查询（1 次用户 + 100 次用户角色 + 100 次角色）
- 严重影响性能
- 数据库负载过高

**修复建议：**
1. 使用 `@EntityGraph` 或 `JOIN FETCH` 一次性加载关联数据
2. 使用批量查询代替循环查询
3. 使用缓存缓存角色信息

**修复示例：**
```java
// 方法 1：使用 JOIN FETCH（在 Repository 中）
@Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.deleted = false")
Page<UserEntity> findAllWithRoles(Pageable pageable);

// 方法 2：批量查询角色
public PageResultVO<UserVO> getUserList(Integer page, Integer size, String keyword) {
    var pageable = PageRequest.of(page - 1, size);
    var pageResult = userRepository.findAll(pageable);

    // 批量查询所有需要的角色
    List<Long> userIds = pageResult.getContent().stream()
            .map(UserEntity::getId)
            .toList();
    List<UserRoleEntity> allUserRoles = userRoleRepository.findByUserIdIn(userIds);
    List<Long> roleIds = allUserRoles.stream()
            .map(UserRoleEntity::getRoleId)
            .distinct()
            .toList();
    List<RoleEntity> allRoles = roleRepository.findAllById(roleIds);

    // 构建映射
    Map<Long, List<String>> userRoleMap = new HashMap<>();
    // ... 构建逻辑
}
```

---

### 1.4 N+1 查询问题 - 权限查询

**位置：** `src/main/java/com/adminplus/service/impl/PermissionServiceImpl.java`

**问题描述：**
```java
@Override
@Transactional(readOnly = true)
public List<String> getUserPermissions(Long userId) {
    List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
            .map(UserRoleEntity::getRoleId)
            .toList();

    Set<Long> menuIds = roleIds.stream()
            .flatMap(roleId -> roleMenuRepository.findMenuIdByRoleId(roleId).stream())
            .collect(Collectors.toSet());

    // ❌ N+1 查询：对每个 menuId 都执行一次查询
    return menuIds.stream()
            .map(menuId -> menuRepository.findById(menuId).orElse(null))  // ❌ 每次都查询
            .filter(menu -> menu != null && menu.getPermKey() != null && !menu.getPermKey().isBlank())
            .map(MenuEntity::getPermKey)
            .collect(Collectors.toList());
}
```

**风险等级：** 🔴 高危

**影响：**
- 查询 50 个权限会执行 51 次数据库查询
- 严重影响性能

**修复建议：**
使用 `findAllById` 批量查询。

**修复示例：**
```java
@Override
@Transactional(readOnly = true)
public List<String> getUserPermissions(Long userId) {
    List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
            .map(UserRoleEntity::getRoleId)
            .toList();

    Set<Long> menuIds = roleIds.stream()
            .flatMap(roleId -> roleMenuRepository.findMenuIdByRoleId(roleId).stream())
            .collect(Collectors.toSet());

    // ✅ 批量查询
    List<MenuEntity> menus = menuRepository.findAllById(menuIds);

    return menus.stream()
            .filter(menu -> menu.getPermKey() != null && !menu.getPermKey().isBlank())
            .map(MenuEntity::getPermKey)
            .collect(Collectors.toList());
}
```

---

### 1.5 N+1 查询问题 - 字典项查询

**位置：** `src/main/java/com/adminplus/service/impl/DictServiceImpl.java`

**问题描述：**
```java
private DictItemVO toItemVO(DictItemEntity item) {
    // ❌ N+1 查询：对每个字典项都执行一次查询
    DictEntity dict = dictRepository.findById(item.getDictId())
            .orElseThrow(() -> new BizException("字典不存在"));
    
    return new DictItemVO(
            item.getId(),
            item.getDictId(),
            dict.getDictType(),
            // ...
    );
}
```

**风险等级：** 🔴 高危

**影响：**
- 查询 100 个字典项会执行 101 次数据库查询

**修复建议：**
批量查询字典信息，传递 DictEntity 而不是 DictId。

**修复示例：**
```java
public List<DictItemVO> getDictItemsByType(String dictType) {
    DictEntity dict = dictRepository.findByDictType(dictType)
            .orElseThrow(() -> new BizException("字典不存在"));

    return dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc(dict.getId(), 1).stream()
            .map(item -> toVOWithDict(item, dict))  // ✅ 传递 DictEntity
            .toList();
}

private DictItemVO toVOWithDict(DictItemEntity item, DictEntity dict) {
    return new DictItemVO(
            item.getId(),
            item.getDictId(),
            dict.getDictType(),  // ✅ 直接使用
            // ...
    );
}
```

---

### 1.6 N+1 查询问题 - 用户详情服务

**位置：** `src/main/java/com/adminplus/security/CustomUserDetailsService.java`

**问题描述：**
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

    List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
    List<String> roleCodes = userRoles.stream()
            .map(UserRoleEntity::getRoleId)
            .map(roleId -> roleRepository.findById(roleId).orElse(null))  // ❌ N+1 查询
            .filter(role -> role != null && role.getStatus() == 1)
            .map(RoleEntity::getCode)
            .collect(Collectors.toList());
    // ...
}
```

**风险等级：** 🔴 高危

**影响：**
- 每次用户登录都会执行多次查询
- 影响登录性能

**修复建议：**
使用批量查询或 JOIN FETCH。

**修复示例：**
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

    List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
    List<Long> roleIds = userRoles.stream()
            .map(UserRoleEntity::getRoleId)
            .toList();

    // ✅ 批量查询
    List<RoleEntity> roles = roleRepository.findAllById(roleIds);
    List<String> roleCodes = roles.stream()
            .filter(role -> role.getStatus() == 1)
            .map(RoleEntity::getCode)
            .collect(Collectors.toList());
    // ...
}
```

---

## 二、中危问题（9 个）

### 2.1 Entity 类未使用 @Data 注解

**位置：** 所有 Entity 类（UserEntity, RoleEntity, MenuEntity 等）

**问题描述：**
```java
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class UserEntity extends BaseEntity {
    // ...
}
```

根据开发规范，Entity 应该使用 `@Data` 注解。

**风险等级：** 🟡 中危

**影响：**
- 违反开发规范
- 代码不一致

**修复建议：**
将 `@Getter` 和 `@Setter` 替换为 `@Data`。

**修复示例：**
```java
@Data
@Entity
@Table(name = "sys_user")
public class UserEntity extends BaseEntity {
    // ...
}
```

---

### 2.2 缺少 XSS 防护

**位置：** 所有 Controller 和 Service

**问题描述：**
- 没有对用户输入进行 XSS 过滤
- 用户输入直接存储到数据库或返回给前端

**风险等级：** 🟡 中危

**影响：**
- 攻击者可以通过输入恶意脚本进行 XSS 攻击
- 可能窃取用户 Cookie 或执行恶意操作

**修复建��：**
1. 使用 Spring 的 `HtmlUtils.htmlEscape()` 过滤用户输入
2. 在前端进行 XSS 防护
3. 使用 CSP（Content Security Policy）

**修复示例：**
```java
@Service
public class UserServiceImpl implements UserService {

    public UserVO createUser(UserCreateReq req) {
        // ✅ 过滤 HTML 标签
        String cleanNickname = HtmlUtils.htmlEscape(req.nickname());
        String cleanEmail = HtmlUtils.htmlEscape(req.email());
        // ...
    }
}
```

---

### 2.3 日志级别配置不当

**位置：** `src/main/resources/application.yml`

**问题描述：**
```yaml
logging:
  level:
    root: INFO
    com.adminplus: DEBUG  # 生产环境不应该使用 DEBUG
    org.springframework.security: DEBUG  # 生产环境不应该使用 DEBUG
    org.hibernate.SQL: DEBUG  # 生产环境不应该使用 DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # 生产环境不应该使用 TRACE
```

**风险等级：** 🟡 中危

**影响：**
- 生产环境会产生大量日志
- 可能泄露敏感信息（如 SQL 参数）
- 影响性能

**修复建议：**
为不同环境配置不同的日志级别。

**修复示例：**
```yaml
# application-dev.yml
logging:
  level:
    com.adminplus: DEBUG
    org.hibernate.SQL: DEBUG

# application-prod.yml
logging:
  level:
    com.adminplus: INFO
    org.hibernate.SQL: WARN
```

---

### 2.4 缓存功能被禁用

**位置：** `src/main/java/com/adminplus/config/CacheConfig.java`

**问题描述：**
```java
@Configuration
// @EnableCaching  // ❌ 缓存被注释掉了
public class CacheConfig {
    // ...
}
```

虽然配置了缓存管理器，但 `@EnableCaching` 被注释掉了，导致所有 `@Cacheable` 注解无效。

**风险等级：** 🟡 中危

**影响：**
- 字典等频繁访问的数据没有被缓存
- 每次请求都会查询数据库
- 性能低下

**修复建议：**
启用 `@EnableCaching` 注解。

**修复示例：**
```java
@Configuration
@EnableCaching  // ✅ 启用缓存
public class CacheConfig {
    // ...
}
```

---

### 2.5 异常信息可能泄露敏感信息

**位置：** `src/main/java/com/adminplus/exception/GlobalExceptionHandler.java`

**问题描述：**
```java
@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public ApiResponse<?> handleException(Exception e) {
    log.error("系统异常", e);
    return ApiResponse.fail(500, "系统异常: " + e.getMessage());  // ❌ 可能泄露敏感信息
}
```

**风险等级：** 🟡 中危

**影响：**
- 异常信息可能包含敏感信息（如数据库结构、文件路径等）
- 可能被攻击者利用

**修复建议：**
在生产环境中返回通用错误信息，详细错误信息只记录日志。

**修复示例：**
```java
@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public ApiResponse<?> handleException(Exception e) {
    log.error("系统异常", e);
    // ✅ 生产环境返回通用信息
    String message = isProduction() ? "系统异常，请稍后重试" : e.getMessage();
    return ApiResponse.fail(500, message);
}
```

---

### 2.6 文件上传路径遍历风险

**位置：** `src/main/java/com/adminplus/service/impl/ProfileServiceImpl.java`

**问题描述：**
```java
public String uploadAvatar(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));  // ❌ 没有验证文件名
    String filename = UUID.randomUUID() + extension;
    // ...
}
```

**风险等级：** 🟡 中危

**影响：**
- 如果文件名包含 `../` 等特殊字符，可能导致路径遍历攻击
- 虽然使用了 UUID，但仍需验证文件扩展名

**修复建议：**
验证文件名和扩展名。

**修复示例：**
```java
public String uploadAvatar(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.contains("..") || originalFilename.contains("/")) {
        throw new BizException("文件名非法");
    }

    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    // 验证扩展名
    List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    if (!allowedExtensions.contains(extension.toLowerCase())) {
        throw new BizException("不支持的文件格式");
    }

    String filename = UUID.randomUUID() + extension;
    // ...
}
```

---

### 2.7 没有使用虚拟线程

**位置：** 所有 Service

**问题描述：**
- 配置文件中启用了虚拟线程，但代码中没有使用 `@Async`
- 没有利用 JDK 21 的虚拟线程特性

**风险等级：** 🟡 中危

**影响：**
- 没有充分利用 JDK 21 的新特性
- IO 密集型任务性能可能不够好

**修复建议：**
在 IO 密集型任务中使用 `@Async` 注解。

**修复示例：**
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Async
    public CompletableFuture<PageResultVO<UserVO>> getUserListAsync(Integer page, Integer size, String keyword) {
        // 使用虚拟线程处理
        return CompletableFuture.completedFuture(getUserList(page, size, keyword));
    }
}
```

---

### 2.8 缺少请求频率限制

**位置：** 所有 Controller

**问题描述：**
- 没有对 API 请求进行频率限制
- 可能被攻击者利用进行 DDoS 攻击或暴力破解

**风险等级：** 🟡 中危

**影响：**
- 可能被攻击者利用进行 DDoS 攻击
- 可能被暴力破解密码

**修复建议：**
使用 Spring Boot Starter 或 Bucket4j 实现请求频率限制。

**修复示例：**
```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @RateLimiter(name = "login", fallbackMethod = "loginFallback")
    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@Valid @RequestBody UserLoginReq req) {
        // ...
    }

    public ApiResponse<LoginResp> loginFallback(UserLoginReq req, Exception e) {
        return ApiResponse.fail(429, "请求过于频繁，请稍后再试");
    }
}
```

---

### 2.9 缺少审计日志

**位置：** 敏感操作（如删除、修改密码等）

**问题描述：**
- 虽然有 LogEntity，但没有在敏感操作时记录审计日志
- 无法追踪关键操作

**风险等级：** 🟡 中危

**影响：**
- 无法追踪关键操作
- 发生安全事件时无法追溯

**修复建议：**
在敏感操作时记录审计日志。

**修复示例：**
```java
@Service
public class UserServiceImpl implements UserService {

    @Override
    @Transactional
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setDeleted(true);
        userRepository.save(user);

        // ✅ 记录审计日志
        logService.log(
            SecurityUtils.getCurrentUserId(),
            "用户管理",
            4,  // 删除操作
            "删除用户: " + user.getUsername()
        );
    }
}
```

---

## 三、低危问题（5 个）

### 3.1 缺少代码注释

**位置：** 部分方法和复杂逻辑

**问题描述：**
- 部分复杂逻辑缺少注释
- 部分方法缺少 JavaDoc 注释

**风险���级：** 🟢 低危

**影响：**
- 代码可读性降低
- 维护困难

**修复建议：**
为复杂逻辑和公共方法添加注释。

---

### 3.2 魔法数字

**位置：** 多处代码

**问题描述：**
```java
if (user.getStatus() == 1) {  // ❌ 魔法数字
    // ...
}
```

**风险等级：** 🟢 低危

**影响：**
- 代码可读性降低
- 容易出错

**修复建议：**
使用常量或枚举代替魔法数字。

**修复示例：**
```java
public interface UserStatus {
    int DISABLED = 0;
    int ENABLED = 1;
}

if (user.getStatus() == UserStatus.ENABLED) {  // ✅ 使用常量
    // ...
}
```

---

### 3.3 缺少单元测试

**位置：** 所有 Service 和 Controller

**问题描述：**
- 没有（或很少）单元测试
- 代码质量无法保证

**风险等级：** 🟢 低危

**影响：**
- 代码质量无法保证
- 重构风险高

**修复建议：**
为核心业务逻辑添加单元测试。

---

### 3.4 缺少 API 版本控制

**位置：** 所有 Controller

**问题描述：**
- API 路径没有版本号
- 未来升级 API 时可能影响现有客户端

**风险等级：** 🟢 低危

**影响：**
- API 升级困难
- 可能影响现有客户端

**修复建议：**
为 API 添加版本控制。

**修复示例：**
```java
@RestController
@RequestMapping("/api/v1/sys/users")  // ✅ 添加版本号
@RequiredArgsConstructor
public class UserController {
    // ...
}
```

---

### 3.5 缺少健康检查优化

**位置：** `src/main/resources/application.yml`

**问题描述：**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

健康检查过于简单，没有检查数据库连接等关键依赖。

**风险等级：** 🟢 低危

**影响：**
- 健康检查不够准确
- 可能无法及时发现服务问题

**修复建议：**
配置自定义健康检查。

**修复示例：**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 检查数据库连接
        // 检查 Redis 连接
        // ...
        return Health.up().build();
    }
}
```

---

## 四、代码质量评估

### 4.1 符合开发规范的情况

| 规范项 | 符合程度 | 说明 |
|--------|----------|------|
| DTO 使用 record | ✅ 完全符合 | 所有 DTO 都使用了 record 类型 |
| VO 使用 record | ✅ 完全符合 | 所有 VO 都使用了 record 类型 |
| Entity 使用 Lombok | ⚠️ 部分符合 | 使用了 @Getter/@Setter 而非 @Data |
| 方法命名 | ✅ 完全符合 | 遵循小驼峰命名规范 |
| 包结构 | ✅ 完全符合 | 遵循标准包结构 |
| API 响应格式 | ✅ 完全符合 | 使用 ApiResponse 统一封装 |
| 权限控制 | ✅ 完全符合 | 使用 @PreAuthorize 注解 |
| 异常处理 | ✅ 完全符合 | 使用 @RestControllerAdvice 统一处理 |

### 4.2 代码优点

1. **代码结构清晰**：包结构合理，职责分明
2. **使用现代技术栈**：JDK 21、Spring Boot 3.5、record 类型
3. **安全性较好**：使用 BCrypt 加密密码，参数化查询防止 SQL 注入
4. **统一异常处理**：使用 @RestControllerAdvice 统一处理异常
5. **权限控制完善**：使用 @PreAuthorize 进行方法级权限控制
6. **参数校验**：使用 @Valid 进行参数校验

### 4.3 需要改进的地方

1. **性能优化**：解决 N+1 查询问题
2. **安全加固**：添加 XSS 防护、请求频率限制
3. **配置优化**：敏感信息加密、日志级别调整
4. **缓存启用**：启用缓存功能
5. **测试覆盖**：添加单元测试

---

## 五、修复优先级建议

### 第一优先级（立即修复）

1. ✅ 修复 N+1 查询问题（6 个）
2. ✅ 加密敏感信息（数据库密码）
3. ✅ 修复 JWT 密钥问题

### 第二优先级（本周内修复）

4. ✅ 启用缓存功能
5. ✅ 添加 XSS 防护
6. ✅ 调整日志级别
7. ✅ 添加请求频率限制

### 第三优先级（本月内修复）

8. ✅ 修复 Entity 注解问题
9. ✅ 添加审计日志
10. ✅ 优化异常信息
11. ✅ 添加文件上传验证

### 第四优先级（有时间时修复）

12. ✅ 使用虚拟线程
13. ✅ 添加代码注释
14. ✅ 添加单元测试
15. ✅ 添加 API 版本控制
16. ✅ 优化健康检查

---

## 六、总结

AdminPlus 后端代码整体质量良好，遵循了大部分开发规范，使用了现代技术栈。但在性能优化和安全防护方面存在一些需要改进的地方。

主要问题集中在：
1. **性能问题**：N+1 查询问题严重，需要立即修复
2. **配置问题**：敏感信息未加密，JWT 密钥配置不当
3. **安全防护**：缺少 XSS 防护和请求频率限制

建议按照优先级逐步修复这些问题，以提升系统的性能和安全性。

---

**审计完成时间：** 2026-02-07
**下次审计建议时间：** 修复完成后重新审计