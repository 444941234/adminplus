# 日志管理模块实施计划

## 概述

为 AdminPlus 项目添加完整的日志管理模块，支持 Elasticsearch 和数据库双存储模式（ES优先，数据库兜底）。

## 目标

1. **基础功能完善**：添加日志管理菜单、补充缺失字段、批量删除API
2. **存储策略抽象**：支持 ES + 数据库双存储，ES 未启用时自动降级到数据库
3. **日志分类查询**：操作日志、登录日志、系统日志分类管理
4. **扩展功能**：日志导出、定期清理、统计分析

---

## 任务清单

### Phase 1: 基础设施 (P0)

#### 1.1 后端配置与依赖
- [ ] 添加 Elasticsearch 依赖到 pom.xml（可选依赖）
- [ ] 创建日志存储配置类 `LogStorageProperties`
- [ ] 配置 application.yml 日志存储相关配置

#### 1.2 数据模型更新
- [ ] 更新 `LogEntity` 添加 `logType` 字段
- [ ] 更新 `LogPageVO` 添加 `method`、`params` 字段
- [ ] 创建日志统计 VO 类

#### 1.3 存储策略接口
- [ ] 创建 `LogStorageStrategy` 接口
- [ ] 实现 `DatabaseLogStorage` 数据库存储
- [ ] 实现 `ElasticsearchLogStorage` ES存储（条件装配）
- [ ] 创建 `LogStorageStrategySelector` 自动选择策略

### Phase 2: 核心功能 (P0)

#### 2.1 菜单配置
- [ ] 在 `DataInitializationRunner` 中添加日志管理菜单初始化
- [ ] 添加日志管理相关权限配置

#### 2.2 服务层更新
- [ ] 更新 `LogService` 接口添加新方法
- [ ] 更新 `LogServiceImpl` 使用存储策略
- [ ] 添加日志类型参数支持

#### 2.3 控制器更新
- [ ] 更新 `LogController` 添加日志类型筛选
- [ ] 添加批量删除 API
- [ ] 添加日志详情 API（含 method/params）

### Phase 3: 前端实现 (P1)

#### 3.1 API 层
- [ ] 更新 `frontend/src/api/log.js` 添加新 API
- [ ] 添加批量删除 API

#### 3.2 页面组件
- [ ] 更新 `Log.vue` 支持日志类型切换
- [ ] 添加日志详情对话框
- [ ] 添加批量删除功能

#### 3.3 路由配置
- [ ] 确认路由已正确配置

### Phase 4: 扩展功能 (P1-P2)

#### 4.1 日志导出
- [ ] 添加 Apache POI 依赖
- [ ] 创建 `LogExportService` 导出服务
- [ ] 添加导出 API 端点
- [ ] 前端添加导出按钮

#### 4.2 定期清理
- [ ] 创建 `LogCleanupScheduler` 定时任务
- [ ] 添加系统配置项（保留天数）
- [ ] 添加手动清理 API

#### 4.3 统计分析
- [ ] 创建 `LogStatisticsService` 统计服务
- [ ] 添加统计 API 端点
- [ ] 创建统计页面组件

### Phase 5: ES 集成 (P1)

#### 5.1 ES 配置
- [ ] 创建 ES 配置类
- [ ] 创建 ES 索引模板
- [ ] 创建 `LogDocument` ES 文档类

#### 5.2 ES Repository
- [ ] 创建 `LogElasticsearchRepository`
- [ ] 实现搜索和聚合查询

---

## 文件变更清单

### 新增文件

```
backend/src/main/java/com/adminplus/
├── common/config/
│   └── LogStorageProperties.java          # 日志存储配置
├── service/
│   ├── LogStorageStrategy.java            # 存储策略接口
│   ├── impl/
│   │   ├── DatabaseLogStorage.java        # 数据库存储实现
│   │   ├── ElasticsearchLogStorage.java   # ES存储实现
│   │   └── LogStorageStrategySelector.java # 策略选择器
│   ├── LogExportService.java              # 导出服务
│   ├── LogStatisticsService.java          # 统计服务
│   └── LogCleanupScheduler.java           # 清理定时任务
├── pojo/
│   ├── dto/
│   │   └── resp/
│   │       └── LogStatisticsResp.java     # 统计响应
│   └── document/
│       └── LogDocument.java               # ES文档类

frontend/src/
├── api/
│   └── log.js                             # 更新API
└── views/
    └── system/
        └── Log.vue                        # 更新页面
```

### 修改文件

```
backend/pom.xml                             # 添加ES依赖
backend/src/main/resources/application.yml  # 添加日志配置
backend/.../pojo/entity/LogEntity.java      # 添加logType字段
backend/.../pojo/dto/resp/LogPageVO.java    # 添加method/params
backend/.../runner/DataInitializationRunner.java # 添加菜单初始化
backend/.../service/LogService.java         # 更新接口
backend/.../service/impl/LogServiceImpl.java # 重构实现
backend/.../controller/LogController.java   # 添加新API
```

---

## 配置示例

### application.yml

```yaml
# 日志存储配置
logging:
  storage:
    # 存储模式: database | elasticsearch | auto
    mode: auto
  elasticsearch:
    enabled: false
    urls: http://localhost:9200
    index-prefix: adminplus-log
    username: elastic
    password: changeme
  cleanup:
    enabled: true
    retention-days: 90
    cron: "0 0 2 * * ?"  # 每天凌晨2点执行
```

---

## 验收标准

1. ✅ 系统管理菜单下显示"日志管理"菜单项
2. ✅ 支持操作日志、登录日志、系统日志分类查询
3. ✅ ES 未配置时自动使用数据库存储
4. ✅ ES 配置启用后自动切换到 ES 存储
5. ✅ 日志详情显示 method 和 params 字段
6. ✅ 支持批量删除日志
7. ✅ 支持导出日志为 Excel
8. ✅ 定时任务自动清理过期日志
9. ✅ 日志统计页面正常展示

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| ES 版本兼容性 | ES连接失败 | 使用 Spring Data ES 自动适配，添加降级逻辑 |
| 大量日志导出 | 内存溢出 | 使用流式导出，限制单次导出数量 |
| 定时任务并发 | 重复执行 | 添加分布式锁（Redis） |

---

## 实施顺序

1. **Phase 1** → 基础设施搭建
2. **Phase 2** → 核心功能实现
3. **Phase 3** → 前端页面开发
4. **Phase 4** → 扩展功能（可并行）
5. **Phase 5** → ES 集成（依赖 Phase 1）

预计总工作量：约 20-25 个文件变更