# AdminPlus Design Patterns Guide

本文档总结了 AdminPlus 系统中使用的设计模式及其应用场景。

## 目录

1. [创建型模式](#创建型模式)
2. [结构型模式](#结构型模式)
3. [行为型模式](#行为型模式)
4. [架构模式](#架构模式)

---

## 创建型模式

### 工厂模式 (Factory Pattern)

**应用位置**: `DataInitializationRunner`

```java
// Spring 自动发现所有 DataInitializer Bean，按 order 排序执行
@Service
@RequiredArgsConstructor
public class DataInitializationRunner implements CommandLineRunner {
    private final List<DataInitializer> dataInitializers;

    @Override
    public void run(String... args) {
        dataInitializers.stream()
            .sorted(Comparator.comparingInt(DataInitializer::getOrder))
            .forEach(DataInitializer::initialize);
    }
}
```

**优点**:
- 开闭原则：新增初始化器只需实现接口，无需修改主类
- 单一职责：每个初始化器只负责一个领域

---

### 建造者模式 (Builder Pattern)

**应用位置**: Lombok `@Builder` 注解

```java
@Builder
public record LoginReq(
    String username,
    String password,
    String captchaCode
) {}
```

**优点**:
- 链式调用，代码可读性强
- 适用于多参数构造

---

## 结构型模式

### 适配器模式 (Adapter Pattern)

**应用位置**: `JwtAuthenticationConverter`

```java
@Component
public class JwtAuthenticationConverter {
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }
}
```

**优点**:
- 将 JWT 令牌适配为 Spring Security 认证对象
- 解耦 JWT 解析与权限提取

---

### 门面模式 (Facade Pattern)

**应用位置**: Service 层

```java
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeptRepository deptRepository;
    // 统一协调多个 Repository 完成业务逻辑
}
```

**优点**:
- 简化 Controller 调用
- 隐藏内部复杂性

---

### 代理模式 (Proxy Pattern)

**应用位置**: Spring AOP

```java
@Aspect
@Component
public class LogAspect {
    @Around("@annotation(OperationLog)")
    public Object aroundOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 前置：记录开始时间
        // 执行：joinPoint.proceed()
        // 后置：记录日志
    }
}
```

**优点**:
- 横切关注点分离
- 不侵入业务代码

---

## 行为型模式

### 策略模式 (Strategy Pattern)

**应用位置**: `DataInitializer` 初始化器

```java
public interface DataInitializer {
    int getOrder();
    String getName();
    void initialize();
}

@Component
public class DepartmentInitializer implements DataInitializer {
    @Override
    public void initialize() { /* 部门初始化逻辑 */ }
}

@Component
public class RoleInitializer implements DataInitializer {
    @Override
    public void initialize() { /* 角色初始化逻辑 */ }
}
```

**优点**:
- 算法可独立变化
- 新增策略不影响现有代码

---

### 模板方法模式 (Template Method Pattern)

**应用位置**: `LogAspect`

```java
private Object executeWithLogging(ProceedingJoinPoint joinPoint, LogContext context) {
    // 1. 记录开始时间
    long startTime = System.currentTimeMillis();

    // 2. 获取请求信息
    HttpServletRequest request = getRequest();
    String ip = getClientIp(request);

    // 3. 执行业务方法
    Object result = joinPoint.proceed();

    // 4. 异步保存日志
    saveLogAsync(context, startTime, ip, true, null);

    return result;
}
```

**优点**:
- 定义算法骨架，子类可扩展
- 复用公共逻辑

---

### 观察者模式 (Observer Pattern)

**应用位置**: Token 刷新订阅机制

```typescript
// 前端 API 拦截器
const refreshSubscribers: Array<(token: string) => void> = []

function subscribeTokenRefresh(callback: (token: string) => void) {
    refreshSubscribers.push(callback)
}

function onTokenRefreshed(token: string) {
    refreshSubscribers.forEach(cb => cb(token))
    refreshSubscribers.length = 0
}
```

**优点**:
- 多个请求可等待同一 Token 刷新
- 避免重复刷新

---

### 责任链模式 (Chain of Responsibility)

**应用位置**: Spring Security Filter Chain

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .addFilterBefore(tokenBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
        .cors(cors -> cors.configurationSource(corsConfig))
        .headers(headers -> headers.contentSecurityPolicy(...));
    return http.build();
}
```

**优点**:
- 请求依次经过多个处理器
- 灵活配置过滤器顺序

---

### 拦截器模式 (Interceptor Pattern)

**应用位置**: Axios 请求拦截器

```typescript
// 请求拦截器
instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

// 响应拦截器
instance.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response?.status === 401) {
            // Token 过期处理
        }
        return Promise.reject(error)
    }
)
```

**优点**:
- 统一处理认证、错误、重试
- 不侵入业务代码

---

## 架构模式

### 分层架构 (Layered Architecture)

```
┌─────────────────────────────────────────┐
│              Controller 层               │
│         (REST API 端点, 权限检查)          │
├─────────────────────────────────────────┤
│              Service 层                  │
│         (业务逻辑, 事务管理)               │
├─────────────────────────────────────────┤
│            Repository 层                 │
│         (数据访问, JPA 封装)              │
├─────────────────────────────────────────┤
│              Entity 层                   │
│         (领域模型, ORM 映射)              │
└─────────────────────────────────────────┘
```

---

### Repository 模式

```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByDeletedFalse();
}
```

**优点**:
- 数据访问逻辑封装
- 业务层无需关心持久化细节

---

### 组合模式 (Composite Pattern)

**应用位置**: 树形结构实体

```java
@MappedSuperclass
public abstract class TreeEntity<E extends TreeEntity<E>> extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    protected E parent;

    @OneToMany(mappedBy = "parent")
    protected List<E> children = new ArrayList<>();
}
```

**优点**:
- 统一处理树形结构（部门、菜单、字典项）
- 支持递归操作

---

## 前端设计模式

### Composables 模式 (组合式函数)

```typescript
// useAsyncAction.ts - 统一异步操作处理
export function useAsyncAction(defaultError = '操作失败') {
    const loading = ref(false)

    const run = async <T>(fn: () => Promise<T>, options?: Options): Promise<T | undefined> => {
        loading.value = true
        try {
            const result = await fn()
            if (options?.successMessage) toast.success(options.successMessage)
            return result
        } catch (error) {
            toast.error(error instanceof Error ? error.message : defaultError)
            return undefined
        } finally {
            loading.value = false
        }
    }

    return { loading, run }
}
```

**应用场景**:
- `useAuth` - 认证状态管理
- `usePermission` - 权限检查
- `useCRUD` - CRUD 操作封装
- `useTreeData` - 树形数据管理
- `useApiInterceptors` - API 拦截器配置

---

## 模式选择指南

| 场景 | 推荐模式 |
|------|----------|
| 需要支持多种算法/策略 | 策略模式 |
| 需要统一处理横切关注点 | 代理模式、拦截器模式 |
| 需要解耦事件发送与接收 | 观察者模式 |
| 需要处理树形结构 | 组合模式 |
| 需要构建复杂对象 | 建造者模式 |
| 需要隐藏系统复杂性 | 门面模式 |
| 需要统一数据访问接口 | Repository 模式 |
| 需要复用算法骨架 | 模板方法模式 |

---

## 代码优化记录

### 2026-03-30 优化

| 文件 | 优化前 | 优化后 | 模式 |
|------|--------|--------|------|
| `DataInitializationRunner.java` | 891 行 | 53 行 | 策略模式 |
| `SecurityConfig.java` | 443 行 | 133 行 | 单一职责 |
| `StateMachineConfig.java` | 176 行 | 46 行 | 模块化 |

### 新增模块

- `JwtSecurityConfig.java` - JWT 配置
- `CorsSecurityConfig.java` - CORS 配置
- `SecurityConstants.java` - 安全常量
- `DepartmentInitializer.java` - 部门初始化
- `RoleInitializer.java` - 角色初始化
- `MenuInitializer.java` - 菜单初始化
- `UserInitializer.java` - 用户初始化
- `DictInitializer.java` - 字典初始化
- `useApiInterceptors.ts` - API 拦截器

---

## 参考资料

- [Design Patterns: Elements of Reusable Object-Oriented Software](https://en.wikipedia.org/wiki/Design_Patterns)
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/reference/)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)