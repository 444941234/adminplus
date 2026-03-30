---
name: 参数配置模块设计
description: 参数配置模块完整功能设计，支持在线配置管理、分组管理、历史版本、导入导出、缓存刷新
type: project
---

# 参数配置模块设计

## 概述

参数配置模块用于支持在线配置管理，允许用户通过 Web 界面动态管理系统配置，无需修改配置文件或重启应用。

**Why:** 当前 Config.vue 页面实际展示的是系统监控功能，而非参数配置功能。需要一个完整的参数配置模块来支持灵活的在线配置管理。

**How to apply:** 后端实现配置存储、缓存、历史版本；前端实现分组卡片式界面，支持 CRUD、导入导出、缓存刷新。

---

## 功能需求

### 配置类型支持

1. **键值对配置** - 简单 key-value 形式
2. **分组配置** - 按模块分组管理
3. **结构化配置** - 支持 JSON、数组等复杂类型

### 配置值类型

| 类型 | 说明 | 编辑组件 |
|------|------|----------|
| STRING | 字符串 | 文本输入框 |
| NUMBER | 数字 | 数字输入框 |
| BOOLEAN | 布尔值 | 开关切换 |
| JSON | JSON 对象 | JSON 编辑器 |
| ARRAY | 数组 | 动态列表编辑器 |
| SECRET | 密码/密钥 | 密码输入框（脱敏显示） |
| FILE | 文件路径 | 文件上传组件 |

### 操作功能

- 基础 CRUD（新增、编辑、删除、查看）
- 搜索过滤（按名称/键名/分组筛选）
- 配置分组管理（创建/编辑/删除分组）
- 配置导入导出（JSON/YAML 格式）
- 配置历史版本（记录修改历史，可回滚）
- 配置缓存刷新（实时刷新应用缓存）

### 生效方式（分级生效）

| 生效类型 | 说明 |
|----------|------|
| IMMEDIATE | 立即生效 - 修改后实时刷新缓存 |
| MANUAL | 手动刷新 - 需调用刷新接口 |
| RESTART | 重启生效 - 应用重启后加载 |

### 权限控制

统一权限 `system:config:*` 管理所有配置项操作。

---

## 技术方案

**方案 A：数据库存储 + Redis 缓存**

- PostgreSQL 存储配置数据（配置项、分组、历史版本）
- Redis 作为配置缓存层，支持快速读取和实时刷新
- 使用 Spring Cache 注解简化缓存操作

---

## 数据库设计

### 配置分组表 `sys_config_group`

继承 `BaseEntity`（id, createTime, updateTime, createUser, updateUser, deleted）。

扩展字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| name | VARCHAR(50) | 分组名称 |
| code | VARCHAR(50) | 分组编码（唯一） |
| icon | VARCHAR(50) | 分组图标（Lucide 图标名） |
| sort_order | INT | 排序序号 |
| description | VARCHAR(200) | 分组描述 |
| status | INT | 状态：0禁用 1启用 |

### 配置项表 `sys_config`

继承 `BaseEntity`。

扩展字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| group_id | VARCHAR(32) | 所属分组 ID |
| name | VARCHAR(100) | 配置名称（显示名） |
| key | VARCHAR(100) | 配置键（唯一） |
| value | TEXT | 配置值（JSON 存储复杂类型） |
| value_type | VARCHAR(20) | 值类型：STRING/NUMBER/BOOLEAN/JSON/ARRAY/SECRET/FILE |
| effect_type | VARCHAR(20) | 生效方式：IMMEDIATE/MANUAL/RESTART |
| default_value | TEXT | 默认值 |
| description | VARCHAR(500) | 配置说明 |
| is_required | BOOLEAN | 是否必填 |
| validation_rule | VARCHAR(200) | 校验规则（正则或范围） |
| sort_order | INT | 排序序号 |
| status | INT | 状态：0禁用 1启用 |

### 配置历史表 `sys_config_history`

继承 `BaseEntity`。

扩展字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| config_id | VARCHAR(32) | 配置项 ID |
| config_key | VARCHAR(100) | 配置键（冗余存储） |
| old_value | TEXT | 旧值 |
| new_value | TEXT | 新值 |
| remark | VARCHAR(200) | 操作备注 |

---

## 后端架构

### Entity 类

```java
// ConfigGroupEntity.java
@Entity
@Table(name = "sys_config_group",
       uniqueConstraints = @UniqueConstraint(name = "uk_group_code", columnNames = "code"))
@SQLDelete(sql = "UPDATE sys_config_group SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigGroupEntity extends BaseEntity { ... }

// ConfigEntity.java
@Entity
@Table(name = "sys_config",
       uniqueConstraints = @UniqueConstraint(name = "uk_config_key", columnNames = "key"))
@SQLDelete(sql = "UPDATE sys_config SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigEntity extends BaseEntity { ... }

// ConfigHistoryEntity.java
@Entity
@Table(name = "sys_config_history")
@SQLDelete(sql = "UPDATE sys_config_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigHistoryEntity extends BaseEntity { ... }
```

### Repository 类

```java
@Repository
public interface ConfigGroupRepository extends JpaRepository<ConfigGroupEntity, String>, JpaSpecificationExecutor<ConfigGroupEntity> {
    Optional<ConfigGroupEntity> findByCode(String code);
    boolean existsByCode(String code);
    List<ConfigGroupEntity> findByStatusOrderBySortOrderAsc(Integer status);
}

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, String>, JpaSpecificationExecutor<ConfigEntity> {
    Optional<ConfigEntity> findByKey(String key);
    boolean existsByKey(String key);
    List<ConfigEntity> findByGroupIdOrderBySortOrderAsc(String groupId);
    List<ConfigEntity> findByEffectType(String effectType);
}

@Repository
public interface ConfigHistoryRepository extends JpaRepository<ConfigHistoryEntity, String>, JpaSpecificationExecutor<ConfigHistoryEntity> {
    List<ConfigHistoryEntity> findByConfigIdOrderByCreateTimeDesc(String configId);
    Optional<ConfigHistoryEntity> findFirstByConfigIdOrderByCreateTimeDesc(String configId);
}
```

### DTO 类

**请求 DTO：**
- `ConfigGroupCreateReq` - 创建分组
- `ConfigGroupUpdateReq` - 更新分组
- `ConfigCreateReq` - 创建配置项
- `ConfigUpdateReq` - 更新配置项
- `ConfigBatchUpdateReq` - 批量更新配置项
- `ConfigImportReq` - 导入配置
- `ConfigRollbackReq` - 回滚请求

**响应 DTO：**
- `ConfigGroupResp` - 分组响应（含配置项数量）
- `ConfigResp` - 配置项响应
- `ConfigHistoryResp` - 配置历史记录
- `ConfigExportResp` - 导出响应
- `ConfigImportResultResp` - 导入结果响应
- `ConfigEffectInfoResp` - 生效信息响应

### Service 类

```java
public interface ConfigGroupService {
    PageResultResp<ConfigGroupResp> getGroupList(Integer page, Integer size, String keyword);
    List<ConfigGroupResp> getAllGroups();
    ConfigGroupResp getGroupById(String id);
    ConfigGroupResp getGroupByCode(String code);
    ConfigGroupResp createGroup(ConfigGroupCreateReq req);
    ConfigGroupResp updateGroup(String id, ConfigGroupUpdateReq req);
    void deleteGroup(String id);
    void updateGroupStatus(String id, Integer status);
}

public interface ConfigService {
    PageResultResp<ConfigResp> getConfigList(Integer page, Integer size, String groupId, String keyword);
    List<ConfigResp> getConfigsByGroupId(String groupId);
    ConfigResp getConfigById(String id);
    ConfigResp getConfigByKey(String key);
    ConfigResp createConfig(ConfigCreateReq req);
    ConfigResp updateConfig(String id, ConfigUpdateReq req);
    void batchUpdateConfigs(ConfigBatchUpdateReq req);
    void deleteConfig(String id);
    void updateConfigStatus(String id, Integer status);
    List<ConfigHistoryResp> getConfigHistory(String configId);
    void rollbackConfig(String configId, String historyId);
    ConfigExportResp exportConfigs(String groupId, String format);
    ConfigImportResultResp importConfigs(ConfigImportReq req);
    void refreshCache();
    ConfigEffectInfoResp getEffectInfo();
    String getConfigValue(String key); // 获取配置值（供其他模块调用）
}
```

---

## API 接口设计

### 配置分组 API (`/v1/sys/config-groups`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/` | 查询分组列表（分页） | `system:config:list` |
| GET | `/all` | 查询所有分组（不分页） | `system:config:list` |
| GET | `/{id}` | 查询分组详情 | `system:config:query` |
| GET | `/code/{code}` | 根据编码查询分组 | `system:config:query` |
| POST | `/` | 创建分组 | `system:config:add` |
| PUT | `/{id}` | 更新分组 | `system:config:edit` |
| DELETE | `/{id}` | 删除分组 | `system:config:delete` |
| PUT | `/{id}/status` | 更新分组状态 | `system:config:edit` |

### 配置项 API (`/v1/sys/configs`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/` | 查询配置项列表（分页） | `system:config:list` |
| GET | `/group/{groupId}` | 查询指定分组的配置项 | `system:config:list` |
| GET | `/{id}` | 查询配置项详情 | `system:config:query` |
| GET | `/key/{key}` | 根据配置键查询 | `system:config:query` |
| POST | `/` | 创建配置项 | `system:config:add` |
| PUT | `/{id}` | 更新配置项 | `system:config:edit` |
| PUT | `/batch` | 批量更新配置项 | `system:config:edit` |
| DELETE | `/{id}` | 删除配置项 | `system:config:delete` |
| PUT | `/{id}/status` | 更新配置项状态 | `system:config:edit` |

### 高级功能 API (`/v1/sys/configs`)

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/{id}/history` | 查询配置项历史版本 | `system:config:query` |
| POST | `/{id}/rollback` | 回滚到指定历史版本 | `system:config:edit` |
| GET | `/export` | 导出配置 | `system:config:query` |
| POST | `/import` | 导入配置 | `system:config:add` |
| POST | `/refresh-cache` | 刷新配置缓存 | `system:config:edit` |
| GET | `/effect-info` | 查询生效信息 | `system:config:query` |

---

## 前端设计

### 页面布局

分组卡片式布局：
- 顶部：分组标签页切换（横向标签）
- 中部：当前分组的配置项列表表格
- 底部/侧边：配置历史面板
- 弹窗：配置项编辑、导入导出

### 组件结构

**页面组件：**
- `Config.vue` - 参数配置主页面（替换现有系统监控内容）

**子组件：**
- `ConfigGroupTabs.vue` - 分组标签页
- `ConfigGroupFormDialog.vue` - 分组新增/编辑弹窗
- `ConfigItemTable.vue` - 配置项表格
- `ConfigItemFormDialog.vue` - 配置项新增/编辑弹窗
- `ConfigHistoryPanel.vue` - 配置历史面板
- `ConfigValueEditor.vue` - 配置值编辑器（按类型动态渲染）
- `ConfigImportDialog.vue` - 配置导入弹窗
- `ConfigExportDialog.vue` - 配置导出弹窗

### API 调用模块

`frontend/src/api/config.ts`：
- `getConfigGroupList()`
- `getAllConfigGroups()`
- `getConfigGroupById()`
- `createConfigGroup()`
- `updateConfigGroup()`
- `deleteConfigGroup()`
- `getConfigList()`
- `getConfigsByGroupId()`
- `getConfigById()`
- `getConfigByKey()`
- `createConfig()`
- `updateConfig()`
- `batchUpdateConfigs()`
- `deleteConfig()`
- `getConfigHistory()`
- `rollbackConfig()`
- `exportConfigs()`
- `importConfigs()`
- `refreshConfigCache()`
- `getEffectInfo()`

---

## 缓存与生效机制

### Redis 缓存结构

```
缓存命名空间：config

Key 设计：
- config:all               → Map<key, ConfigEntity>（所有配置）
- config:group:{groupId}   → List<ConfigEntity>（分组配置）
- config:item:{key}        → ConfigEntity（单个配置）
- config:effect:pending    → List<PendingEffect>（待生效列表）
```

### 缓存策略

| 操作 | 缓存行为 |
|------|----------|
| 查询配置 | 先查缓存，不存在则查数据库并写入 |
| 更新 IMMEDIATE 配置 | 更新数据库 + 立即刷新缓存 |
| 更新 MANUAL 配置 | 更新数据库 + 记录待生效 |
| 更新 RESTART 配置 | 更新数据库 + 记录待生效 |
| 刷新缓存 | 清除所有缓存 + 重新加载 |
| 应用启动 | 加载所有配置到缓存 + 处理待生效 |

### 待生效配置存储

```json
config:effect:pending = [
  {
    "key": "database.pool.size",
    "newValue": "20",
    "effectType": "RESTART",
    "updateTime": "2026-03-30T10:00:00Z"
  }
]
```

---

## 导入导出设计

### 导出格式

**JSON 格式：**
```json
{
  "exportVersion": "1.0",
  "exportTime": "2026-03-30T10:00:00Z",
  "groups": [
    {
      "code": "basic",
      "name": "基础配置",
      "icon": "Settings",
      "configs": [
        {
          "key": "system.name",
          "name": "系统名称",
          "value": "AdminPlus",
          "valueType": "STRING",
          "effectType": "IMMEDIATE",
          "description": "系统显示名称"
        }
      ]
    }
  ]
}
```

**YAML 格式：**
```yaml
exportVersion: "1.0"
exportTime: "2026-03-30T10:00:00Z"
groups:
  - code: basic
    name: 基础配置
    icon: Settings
    configs:
      - key: system.name
        name: 系统名称
        value: AdminPlus
        valueType: STRING
        effectType: IMMEDIATE
```

### 导入模式

| 模式 | 说明 |
|------|------|
| OVERWRITE | 同名配置直接覆盖 |
| MERGE | 同名配置跳过，新增配置追加 |
| VALIDATE | 仅校验格式，不实际导入 |

### 导入结果

```json
{
  "total": 20,
  "success": 18,
  "skipped": 1,
  "failed": 1,
  "details": [
    { "key": "system.name", "status": "success" },
    { "key": "upload.max", "status": "skipped", "reason": "配置已存在" },
    { "key": "invalid.key", "status": "failed", "reason": "值类型不匹配" }
  ]
}
```

---

## 测试策略

### 后端测试

- `ConfigGroupServiceTest` - 分组 CRUD 单元测试
- `ConfigServiceTest` - 配置项 CRUD 单元测试
- `ConfigCacheTest` - 缓存读写单元测试
- `ConfigImportExportTest` - 导入导出单元测试
- `ConfigEffectTest` - 生效机制单元测试
- `ConfigControllerIntegrationTest` - API 集成测试

### 前端测试

- `ConfigGroupTabs.test.ts` - 分组切换测试
- `ConfigItemTable.test.ts` - 配置表格渲染测试
- `ConfigValueEditor.test.ts` - 各类型编辑器测试
- `ConfigImportDialog.test.ts` - 导入弹窗测试

---

## 注意事项

1. **敏感配置显示**：SECRET 类型配置值在列表中脱敏显示（如 `****`），编辑时显示完整值
2. **分组删除约束**：分组下有配置项时不允许删除
3. **配置键唯一性**：全局唯一，不区分分组
4. **历史记录保留**：默认保留最近 50 条历史记录，可配置
5. **缓存一致性**：多实例部署时通过 Redis 共享缓存，刷新操作影响所有实例