# 参数配置模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现完整的参数配置模块，支持在线配置管理、分组管理、历史版本、导入导出、缓存刷新

**Architecture:** 后端采用 PostgreSQL + Redis 缓存，Spring Cache 注解管理缓存；前端采用分组卡片式布局，Vue 3 Composition API

**Tech Stack:** Spring Boot 3.5, JPA, Redis, Vue 3.5, TypeScript, Vitest

---

## 文件结构

### 后端文件

```
backend/src/main/java/com/adminplus/
├── controller/
│   ├── ConfigGroupController.java       # 分组 API
│   └── ConfigController.java             # 配置项 API
├── service/
│   ├── ConfigGroupService.java           # 分组服务接口
│   ├── ConfigService.java                # 配置服务接口
│   └── impl/
│       ├── ConfigGroupServiceImpl.java   # 分组服务实现
│       └── ConfigServiceImpl.java        # 配置服务实现
├── repository/
│   ├── ConfigGroupRepository.java        # 分组仓储
│   ├── ConfigRepository.java             # 配置仓储
│   └── ConfigHistoryRepository.java      # 历史仓储
├── pojo/
│   ├── entity/
│   │   ├── ConfigGroupEntity.java        # 分组实体
│   │   ├── ConfigEntity.java             # 配置实体
│   │   └── ConfigHistoryEntity.java      # 历史实体
│   └── dto/
│       ├── req/
│       │   ├── ConfigGroupCreateReq.java
│       │   ├── ConfigGroupUpdateReq.java
│       │   ├── ConfigCreateReq.java
│       │   ├── ConfigUpdateReq.java
│       │   ├── ConfigBatchUpdateReq.java
│       │   ├── ConfigImportReq.java
│       │   └── ConfigRollbackReq.java
│       └── resp/
│           ├── ConfigGroupResp.java
│           ├── ConfigResp.java
│           ├── ConfigHistoryResp.java
│           ├── ConfigExportResp.java
│           ├── ConfigImportResultResp.java
│           └── ConfigEffectInfoResp.java
└── common/
    └── config/
        └── ConfigCacheConfig.java        # 配置缓存配置
```

### 前端文件

```
frontend/src/
├── api/
│   └── config.ts                          # 配置 API 调用
├── types/
│   └── index.ts                          # 添加 ConfigGroup, Config 等类型
├── views/
│   └── system/
│       └── Config.vue                    # 参数配置主页面（替换现有）
├── components/
│   └── config/
│       ├── ConfigGroupTabs.vue           # 分组标签页
│       ├── ConfigGroupFormDialog.vue     # 分组表单弹窗
│       ├── ConfigItemTable.vue           # 配置项表格
│       ├── ConfigItemFormDialog.vue      # 配置项表单弹窗
│       ├── ConfigHistoryPanel.vue        # 历史记录面板
│       ├── ConfigValueEditor.vue         # 值编辑器
│       ├── ConfigImportDialog.vue        # 导入弹窗
│       └── ConfigExportDialog.vue        # 导出弹窗
└── lib/
    └── page-permissions.ts               # 更新权限配置
```

---

## Task 1: 创建配置分组实体

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/entity/ConfigGroupEntity.java`
- Test: `backend/src/test/java/com/adminplus/pojo/entity/ConfigGroupEntityTest.java`

- [ ] **Step 1: 编写实体类**

```java
package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置分组实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@Entity
@Table(name = "sys_config_group",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_group_code", columnNames = "code")
       },
       indexes = {
           @Index(name = "idx_group_code", columnList = "code"),
           @Index(name = "idx_group_sort", columnList = "sort_order"),
           @Index(name = "idx_group_status", columnList = "status"),
           @Index(name = "idx_group_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_config_group SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigGroupEntity extends BaseEntity {

    /**
     * 分组名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 分组编码（唯一）
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 分组图标（Lucide 图标名）
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 分组描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 状态（1=启用，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
```

- [ ] **Step 2: 编写实体测试**

```java
package com.adminplus.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfigGroupEntity Tests")
class ConfigGroupEntityTest {

    @Test
    @DisplayName("should create entity with default values")
    void shouldCreateEntityWithDefaults() {
        ConfigGroupEntity entity = new ConfigGroupEntity();

        assertThat(entity.getSortOrder()).isEqualTo(0);
        assertThat(entity.getStatus()).isEqualTo(1);
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("should set and get all fields")
    void shouldSetAndGetAllFields() {
        ConfigGroupEntity entity = new ConfigGroupEntity();
        entity.setName("基础配置");
        entity.setCode("basic");
        entity.setIcon("Settings");
        entity.setSortOrder(1);
        entity.setDescription("系统基础配置");
        entity.setStatus(1);

        assertThat(entity.getName()).isEqualTo("基础配置");
        assertThat(entity.getCode()).isEqualTo("basic");
        assertThat(entity.getIcon()).isEqualTo("Settings");
        assertThat(entity.getSortOrder()).isEqualTo(1);
        assertThat(entity.getDescription()).isEqualTo("系统基础配置");
        assertThat(entity.getStatus()).isEqualTo(1);
    }
}
```

- [ ] **Step 3: 运行测试验证**

```bash
cd backend
mvn test -Dtest=ConfigGroupEntityTest
```

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/entity/ConfigGroupEntity.java \
        backend/src/test/java/com/adminplus/pojo/entity/ConfigGroupEntityTest.java
git commit -m "feat: add ConfigGroupEntity"
```

---

## Task 2: 创建配置项实体

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/entity/ConfigEntity.java`
- Test: `backend/src/test/java/com/adminplus/pojo/entity/ConfigEntityTest.java`

- [ ] **Step 1: 编写实体类**

```java
package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置项实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@Entity
@Table(name = "sys_config",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_config_key", columnNames = "key")
       },
       indexes = {
           @Index(name = "idx_config_group", columnList = "group_id"),
           @Index(name = "idx_config_key", columnList = "key"),
           @Index(name = "idx_config_status", columnList = "status"),
           @Index(name = "idx_config_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_config SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigEntity extends BaseEntity {

    /**
     * 所属分组 ID
     */
    @Column(name = "group_id", nullable = false, length = 32)
    private String groupId;

    /**
     * 配置名称（显示名）
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 配置键（唯一）
     */
    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    /**
     * 配置值（JSON 存储复杂类型）
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    /**
     * 值类型：STRING/NUMBER/BOOLEAN/JSON/ARRAY/SECRET/FILE
     */
    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType = "STRING";

    /**
     * 生效方式：IMMEDIATE/MANUAL/RESTART
     */
    @Column(name = "effect_type", nullable = false, length = 20)
    private String effectType = "IMMEDIATE";

    /**
     * 默认值
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    /**
     * 配置说明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否必填
     */
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    /**
     * 校验规则（正则或范围）
     */
    @Column(name = "validation_rule", length = 200)
    private String validationRule;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 状态（1=启用，0=禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
```

- [ ] **Step 2: 编写实体测试**

```java
package com.adminplus.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfigEntity Tests")
class ConfigEntityTest {

    @Test
    @DisplayName("should create entity with default values")
    void shouldCreateEntityWithDefaults() {
        ConfigEntity entity = new ConfigEntity();

        assertThat(entity.getValueType()).isEqualTo("STRING");
        assertThat(entity.getEffectType()).isEqualTo("IMMEDIATE");
        assertThat(entity.getIsRequired()).isFalse();
        assertThat(entity.getSortOrder()).isEqualTo(0);
        assertThat(entity.getStatus()).isEqualTo(1);
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("should set and get all fields")
    void shouldSetAndGetAllFields() {
        ConfigEntity entity = new ConfigEntity();
        entity.setGroupId("group-001");
        entity.setName("系统名称");
        entity.setKey("system.name");
        entity.setValue("AdminPlus");
        entity.setValueType("STRING");
        entity.setEffectType("IMMEDIATE");
        entity.setDefaultValue("AdminPlus");
        entity.setDescription("系统显示名称");
        entity.setIsRequired(true);
        entity.setValidationRule(".{1,50}");
        entity.setSortOrder(1);
        entity.setStatus(1);

        assertThat(entity.getGroupId()).isEqualTo("group-001");
        assertThat(entity.getName()).isEqualTo("系统名称");
        assertThat(entity.getKey()).isEqualTo("system.name");
        assertThat(entity.getValue()).isEqualTo("AdminPlus");
        assertThat(entity.getValueType()).isEqualTo("STRING");
        assertThat(entity.getEffectType()).isEqualTo("IMMEDIATE");
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend
mvn test -Dtest=ConfigEntityTest
```

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/entity/ConfigEntity.java \
        backend/src/test/java/com/adminplus/pojo/entity/ConfigEntityTest.java
git commit -m "feat: add ConfigEntity"
```

---

## Task 3: 创建配置历史实体

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/entity/ConfigHistoryEntity.java`

- [ ] **Step 1: 编写实体类**

```java
package com.adminplus.pojo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 配置历史实体
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Getter
@Setter
@Entity
@Table(name = "sys_config_history",
       indexes = {
           @Index(name = "idx_history_config", columnList = "config_id"),
           @Index(name = "idx_history_key", columnList = "config_key"),
           @Index(name = "idx_history_time", columnList = "create_time"),
           @Index(name = "idx_history_deleted", columnList = "deleted")
       })
@SQLDelete(sql = "UPDATE sys_config_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ConfigHistoryEntity extends BaseEntity {

    /**
     * 配置项 ID
     */
    @Column(name = "config_id", nullable = false, length = 32)
    private String configId;

    /**
     * 配置键（冗余存储，便于查询）
     */
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    /**
     * 旧值
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 新值
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * 操作备注
     */
    @Column(name = "remark", length = 200)
    private String remark;
}
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/entity/ConfigHistoryEntity.java
git commit -m "feat: add ConfigHistoryEntity"
```

---

## Task 4: 创建 Repository 接口

**Files:**
- Create: `backend/src/main/java/com/adminplus/repository/ConfigGroupRepository.java`
- Create: `backend/src/main/java/com/adminplus/repository/ConfigRepository.java`
- Create: `backend/src/main/java/com/adminplus/repository/ConfigHistoryRepository.java`

- [ ] **Step 1: 编写 ConfigGroupRepository**

```java
package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置分组 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigGroupRepository extends JpaRepository<ConfigGroupEntity, String>, JpaSpecificationExecutor<ConfigGroupEntity> {

    /**
     * 根据编码查询分组
     */
    Optional<ConfigGroupEntity> findByCode(String code);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查询启用的分组，按排序序号升序
     */
    List<ConfigGroupEntity> findByStatusOrderBySortOrderAsc(Integer status);
}
```

- [ ] **Step 2: 编写 ConfigRepository**

```java
package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置项 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, String>, JpaSpecificationExecutor<ConfigEntity> {

    /**
     * 根据配置键查询
     */
    Optional<ConfigEntity> findByKey(String key);

    /**
     * 检查配置键是否存在
     */
    boolean existsByKey(String key);

    /**
     * 根据分组 ID 查询配置项，按排序序号升序
     */
    List<ConfigEntity> findByGroupIdOrderBySortOrderAsc(String groupId);

    /**
     * 根据生效类型查询配置项
     */
    List<ConfigEntity> findByEffectType(String effectType);

    /**
     * 根据分组 ID 和状态查询配置项
     */
    List<ConfigEntity> findByGroupIdAndStatusOrderBySortOrderAsc(String groupId, Integer status);
}
```

- [ ] **Step 3: 编写 ConfigHistoryRepository**

```java
package com.adminplus.repository;

import com.adminplus.pojo.entity.ConfigHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置历史 Repository
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Repository
public interface ConfigHistoryRepository extends JpaRepository<ConfigHistoryEntity, String>, JpaSpecificationExecutor<ConfigHistoryEntity> {

    /**
     * 根据配置 ID 查询历史记录，按创建时间降序
     */
    List<ConfigHistoryEntity> findByConfigIdOrderByCreateTimeDesc(String configId);

    /**
     * 根据配置 ID 查询最新一条历史记录
     */
    Optional<ConfigHistoryEntity> findFirstByConfigIdOrderByCreateTimeDesc(String configId);

    /**
     * 根据配置键查询历史记录
     */
    List<ConfigHistoryEntity> findByConfigKeyOrderByCreateTimeDesc(String configKey);
}
```

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/repository/ConfigGroupRepository.java \
        backend/src/main/java/com/adminplus/repository/ConfigRepository.java \
        backend/src/main/java/com/adminplus/repository/ConfigHistoryRepository.java
git commit -m "feat: add config repositories"
```

---

## Task 5: 创建请求 DTO

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigGroupCreateReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigGroupUpdateReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigCreateReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigUpdateReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigBatchUpdateReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigImportReq.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/ConfigRollbackReq.java`

- [ ] **Step 1: 编写 ConfigGroupCreateReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 配置分组创建请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigGroupCreateReq(
        @NotBlank(message = "分组名称不能为空")
        @Size(max = 50, message = "分组名称长度不能超过50")
        String name,

        @NotBlank(message = "分组编码不能为空")
        @Size(max = 50, message = "分组编码长度不能超过50")
        String code,

        @Size(max = 50, message = "图标长度不能超过50")
        String icon,

        Integer sortOrder,

        @Size(max = 200, message = "描述长度不能超过200")
        String description
) {}
```

- [ ] **Step 2: 编写 ConfigGroupUpdateReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.Size;

/**
 * 配置分组更新请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigGroupUpdateReq(
        @Size(max = 50, message = "分组名称长度不能超过50")
        String name,

        @Size(max = 50, message = "图标长度不能超过50")
        String icon,

        Integer sortOrder,

        @Size(max = 200, message = "描述长度不能超过200")
        String description
) {}
```

- [ ] **Step 3: 编写 ConfigCreateReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 配置项创建请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigCreateReq(
        @NotBlank(message = "分组ID不能为空")
        String groupId,

        @NotBlank(message = "配置名称不能为空")
        @Size(max = 100, message = "配置名称长度不能超过100")
        String name,

        @NotBlank(message = "配置键不能为空")
        @Size(max = 100, message = "配置键长度不能超过100")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "配置键只能包含字母、数字、点、下划线和连字符")
        String key,

        String value,

        @NotBlank(message = "值类型不能为空")
        @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON|ARRAY|SECRET|FILE)$", message = "值类型无效")
        String valueType,

        @Pattern(regexp = "^(IMMEDIATE|MANUAL|RESTART)$", message = "生效方式无效")
        String effectType,

        String defaultValue,

        @Size(max = 500, message = "说明长度不能超过500")
        String description,

        Boolean isRequired,

        @Size(max = 200, message = "校验规则长度不能超过200")
        String validationRule,

        Integer sortOrder
) {}
```

- [ ] **Step 4: 编写 ConfigUpdateReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * 配置项更新请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigUpdateReq(
        @Size(max = 100, message = "配置名称长度不能超过100")
        String name,

        String value,

        @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON|ARRAY|SECRET|FILE)$", message = "值类型无效")
        String valueType,

        @Pattern(regexp = "^(IMMEDIATE|MANUAL|RESTART)$", message = "生效方式无效")
        String effectType,

        String defaultValue,

        @Size(max = 500, message = "说明长度不能超过500")
        String description,

        Boolean isRequired,

        @Size(max = 200, message = "校验规则长度不能超过200")
        String validationRule,

        Integer sortOrder,

        Integer status
) {}
```

- [ ] **Step 5: 编写 ConfigBatchUpdateReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 批量更新配置项请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigBatchUpdateReq(
        @NotEmpty(message = "配置项列表不能为空")
        @Valid
        List<ConfigItemUpdate> items
) {
    public record ConfigItemUpdate(
            String id,
            String value
    ) {}
}
```

- [ ] **Step 6: 编写 ConfigImportReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 配置导入请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigImportReq(
        @NotBlank(message = "导入内容不能为空")
        String content,

        @NotBlank(message = "格式不能为空")
        @Pattern(regexp = "^(JSON|YAML)$", message = "格式必须是 JSON 或 YAML")
        String format,

        @Pattern(regexp = "^(OVERWRITE|MERGE|VALIDATE)$", message = "模式无效")
        String mode
) {
    public ConfigImportReq {
        if (mode == null) {
            mode = "MERGE";
        }
    }
}
```

- [ ] **Step 7: 编写 ConfigRollbackReq**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;

/**
 * 配置回滚请求
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigRollbackReq(
        @NotBlank(message = "历史记录ID不能为空")
        String historyId,

        String remark
) {}
```

- [ ] **Step 8: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/dto/req/
git commit -m "feat: add config request DTOs"
```

---

## Task 6: 创建响应 DTO

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigGroupResp.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigResp.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigHistoryResp.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigExportResp.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigImportResultResp.java`
- Create: `backend/src/main/java/com/adminplus/pojo/dto/resp/ConfigEffectInfoResp.java`

- [ ] **Step 1: 编写 ConfigGroupResp**

```java
package com.adminplus.pojo.dto.resp;

import java.time.Instant;

/**
 * 配置分组响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigGroupResp(
        String id,
        String name,
        String code,
        String icon,
        Integer sortOrder,
        String description,
        Integer status,
        Long configCount,
        Instant createTime,
        Instant updateTime
) {}
```

- [ ] **Step 2: 编写 ConfigResp**

```java
package com.adminplus.pojo.dto.resp;

import java.time.Instant;

/**
 * 配置项响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigResp(
        String id,
        String groupId,
        String groupName,
        String name,
        String key,
        String value,
        String valueType,
        String effectType,
        String defaultValue,
        String description,
        Boolean isRequired,
        String validationRule,
        Integer sortOrder,
        Integer status,
        Instant createTime,
        Instant updateTime
) {}
```

- [ ] **Step 3: 编写 ConfigHistoryResp**

```java
package com.adminplus.pojo.dto.resp;

import java.time.Instant;

/**
 * 配置历史响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigHistoryResp(
        String id,
        String configId,
        String configKey,
        String oldValue,
        String newValue,
        String remark,
        String operatorName,
        Instant createTime
) {}
```

- [ ] **Step 4: 编写 ConfigExportResp**

```java
package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 配置导出响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigExportResp(
        String exportVersion,
        String exportTime,
        List<ExportGroup> groups
) {
    public record ExportGroup(
            String code,
            String name,
            String icon,
            List<ExportConfig> configs
    ) {}

    public record ExportConfig(
            String key,
            String name,
            String value,
            String valueType,
            String effectType,
            String description
    ) {}
}
```

- [ ] **Step 5: 编写 ConfigImportResultResp**

```java
package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 配置导入结果响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigImportResultResp(
        Integer total,
        Integer success,
        Integer skipped,
        Integer failed,
        List<ImportDetail> details
) {
    public record ImportDetail(
            String key,
            String status,
            String reason
    ) {}
}
```

- [ ] **Step 6: 编写 ConfigEffectInfoResp**

```java
package com.adminplus.pojo.dto.resp;

import java.util.List;

/**
 * 配置生效信息响应
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public record ConfigEffectInfoResp(
        List<PendingEffect> pendingEffects,
        List<String> restartRequiredConfigs
) {
    public record PendingEffect(
            String key,
            String name,
            String newValue,
            String effectType,
            String updateTime
    ) {}
}
```

- [ ] **Step 7: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/dto/resp/
git commit -m "feat: add config response DTOs"
```

---

## Task 7: 创建配置分组服务

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/ConfigGroupService.java`
- Create: `backend/src/main/java/com/adminplus/service/impl/ConfigGroupServiceImpl.java`
- Test: `backend/src/test/java/com/adminplus/service/ConfigGroupServiceTest.java`

- [ ] **Step 1: 编写服务接口**

```java
package com.adminplus.service;

import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;

import java.util.List;

/**
 * 配置分组服务接口
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public interface ConfigGroupService {

    /**
     * 分页查询分组列表
     */
    PageResultResp<ConfigGroupResp> getGroupList(Integer page, Integer size, String keyword);

    /**
     * 查询所有启用的分组（不分页）
     */
    List<ConfigGroupResp> getAllGroups();

    /**
     * 根据 ID 查询分组
     */
    ConfigGroupResp getGroupById(String id);

    /**
     * 根据编码查询分组
     */
    ConfigGroupResp getGroupByCode(String code);

    /**
     * 创建分组
     */
    ConfigGroupResp createGroup(ConfigGroupCreateReq req);

    /**
     * 更新分组
     */
    ConfigGroupResp updateGroup(String id, ConfigGroupUpdateReq req);

    /**
     * 删除分组
     */
    void deleteGroup(String id);

    /**
     * 更新分组状态
     */
    void updateGroupStatus(String id, Integer status);
}
```

- [ ] **Step 2: 编写服务实现**

```java
package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.OperationType;
import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.ConfigGroupService;
import com.adminplus.service.LogService;
import com.adminplus.utils.EntityHelper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置分组服务实现
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigGroupServiceImpl implements ConfigGroupService {

    private final ConfigGroupRepository groupRepository;
    private final ConfigRepository configRepository;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<ConfigGroupResp> getGroupList(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("sortOrder").ascending());

        Specification<ConfigGroupEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + keyword + "%"),
                        cb.like(root.get("code"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = groupRepository.findAll(spec, pageable);
        var records = pageResult.getContent().stream()
                .map(this::toVO)
                .toList();

        return new PageResultResp<>(
                records,
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigGroupResp> getAllGroups() {
        return groupRepository.findByStatusOrderBySortOrderAsc(1).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigGroupResp getGroupById(String id) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(groupRepository::findById, id, "分组不存在");
        return toVO(group);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigGroupResp getGroupByCode(String code) {
        ConfigGroupEntity group = groupRepository.findByCode(code)
                .orElseThrow(() -> new BizException("分组不存在"));
        return toVO(group);
    }

    @Override
    @Transactional
    public ConfigGroupResp createGroup(ConfigGroupCreateReq req) {
        if (groupRepository.existsByCode(req.code())) {
            throw new BizException("分组编码已存在");
        }

        ConfigGroupEntity group = new ConfigGroupEntity();
        group.setName(req.name());
        group.setCode(req.code());
        group.setIcon(req.icon());
        group.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        group.setDescription(req.description());
        group.setStatus(1);

        group = groupRepository.save(group);
        log.info("创建配置分组成功: {}", group.getCode());

        logService.log("参数配置", OperationType.CREATE, "创建分组: " + group.getName() + " (" + group.getCode() + ")");

        return toVO(group);
    }

    @Override
    @Transactional
    public ConfigGroupResp updateGroup(String id, ConfigGroupUpdateReq req) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(groupRepository::findById, id, "分组不存在");

        if (req.name() != null) {
            group.setName(req.name());
        }
        if (req.icon() != null) {
            group.setIcon(req.icon());
        }
        if (req.sortOrder() != null) {
            group.setSortOrder(req.sortOrder());
        }
        if (req.description() != null) {
            group.setDescription(req.description());
        }

        group = groupRepository.save(group);
        log.info("更新配置分组成功: {}", group.getCode());

        logService.log("参数配置", OperationType.UPDATE, "更新分组: " + group.getName() + " (" + group.getCode() + ")");

        return toVO(group);
    }

    @Override
    @Transactional
    public void deleteGroup(String id) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(groupRepository::findById, id, "分组不存在");

        // 检查分组下是否有配置项
        List<ConfigEntity> configs = configRepository.findByGroupIdAndStatusOrderBySortOrderAsc(id, 1);
        if (!configs.isEmpty()) {
            throw new BizException("该分组下存在配置项，无法删除");
        }

        group.setDeleted(true);
        groupRepository.save(group);
        log.info("删除配置分组成功: {}", group.getCode());

        logService.log("参数配置", OperationType.DELETE, "删除分组: " + group.getName() + " (" + group.getCode() + ")");
    }

    @Override
    @Transactional
    public void updateGroupStatus(String id, Integer status) {
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(groupRepository::findById, id, "分组不存在");

        group.setStatus(status);
        groupRepository.save(group);
        log.info("更新配置分组状态成功: {}", group.getCode());
    }

    private ConfigGroupResp toVO(ConfigGroupEntity group) {
        // 统计分组下的配置项数量
        long configCount = configRepository.findByGroupIdAndStatusOrderBySortOrderAsc(group.getId(), 1).size();

        return new ConfigGroupResp(
                group.getId(),
                group.getName(),
                group.getCode(),
                group.getIcon(),
                group.getSortOrder(),
                group.getDescription(),
                group.getStatus(),
                configCount,
                group.getCreateTime(),
                group.getUpdateTime()
        );
    }
}
```

- [ ] **Step 3: 编写服务测试**

```java
package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.impl.ConfigGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigGroupService Unit Tests")
class ConfigGroupServiceTest {

    @Mock
    private ConfigGroupRepository groupRepository;

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private ConfigGroupServiceImpl configGroupService;

    private ConfigGroupEntity testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new ConfigGroupEntity();
        testGroup.setId("group-001");
        testGroup.setName("基础配置");
        testGroup.setCode("basic");
        testGroup.setIcon("Settings");
        testGroup.setSortOrder(1);
        testGroup.setDescription("系统基础配置");
        testGroup.setStatus(1);
    }

    @Nested
    @DisplayName("getGroupById Tests")
    class GetGroupByIdTests {

        @Test
        @DisplayName("should return group when exists")
        void getGroupById_WhenExists_ShouldReturnGroup() {
            when(groupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.findByGroupIdAndStatusOrderBySortOrderAsc("group-001", 1))
                    .thenReturn(List.of());

            ConfigGroupResp result = configGroupService.getGroupById("group-001");

            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo("basic");
            assertThat(result.configCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should throw exception when group not found")
        void getGroupById_WhenNotFound_ShouldThrowException() {
            when(groupRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> configGroupService.getGroupById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("分组不存在");
        }
    }

    @Nested
    @DisplayName("createGroup Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("should create group successfully")
        void createGroup_ShouldCreateGroup() {
            ConfigGroupCreateReq req = new ConfigGroupCreateReq(
                    "邮件配置", "email", "Mail", 2, "邮件相关配置"
            );
            when(groupRepository.existsByCode("email")).thenReturn(false);
            when(groupRepository.save(any())).thenReturn(testGroup);

            ConfigGroupResp result = configGroupService.createGroup(req);

            assertThat(result).isNotNull();
            verify(groupRepository).save(any(ConfigGroupEntity.class));
        }

        @Test
        @DisplayName("should throw exception when code exists")
        void createGroup_WhenCodeExists_ShouldThrowException() {
            ConfigGroupCreateReq req = new ConfigGroupCreateReq(
                    "基础配置", "basic", "Settings", 1, null
            );
            when(groupRepository.existsByCode("basic")).thenReturn(true);

            assertThatThrownBy(() -> configGroupService.createGroup(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("分组编码已存在");
        }
    }

    @Nested
    @DisplayName("deleteGroup Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("should delete group successfully")
        void deleteGroup_ShouldDeleteGroup() {
            when(groupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.findByGroupIdAndStatusOrderBySortOrderAsc("group-001", 1))
                    .thenReturn(List.of());
            when(groupRepository.save(any())).thenReturn(testGroup);

            configGroupService.deleteGroup("group-001");

            verify(groupRepository).save(any(ConfigGroupEntity.class));
        }

        @Test
        @DisplayName("should throw exception when group has configs")
        void deleteGroup_WhenHasConfigs_ShouldThrowException() {
            ConfigEntity config = new ConfigEntity();
            when(groupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.findByGroupIdAndStatusOrderBySortOrderAsc("group-001", 1))
                    .thenReturn(List.of(config));

            assertThatThrownBy(() -> configGroupService.deleteGroup("group-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("该分组下存在配置项");
        }
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
cd backend
mvn test -Dtest=ConfigGroupServiceTest
```

Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/adminplus/service/ConfigGroupService.java \
        backend/src/main/java/com/adminplus/service/impl/ConfigGroupServiceImpl.java \
        backend/src/test/java/com/adminplus/service/ConfigGroupServiceTest.java
git commit -m "feat: add ConfigGroupService implementation"
```

---

## Task 8: 创建配置项服务（基础 CRUD）

**Files:**
- Create: `backend/src/main/java/com/adminplus/service/ConfigService.java`
- Create: `backend/src/main/java/com/adminplus/service/impl/ConfigServiceImpl.java`（第一部分）

由于内容较长，此任务分两步实现。先实现基础 CRUD 功能。

- [ ] **Step 1: 编写服务接口**

```java
package com.adminplus.service;

import com.adminplus.pojo.dto.req.*;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.dto.resp.PageResultResp;

import java.util.List;

/**
 * 配置项服务接口
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
public interface ConfigService {

    /**
     * 分页查询配置项列表
     */
    PageResultResp<ConfigResp> getConfigList(Integer page, Integer size, String groupId, String keyword);

    /**
     * 根据分组 ID 查询配置项
     */
    List<ConfigResp> getConfigsByGroupId(String groupId);

    /**
     * 根据 ID 查询配置项
     */
    ConfigResp getConfigById(String id);

    /**
     * 根据配置键查询
     */
    ConfigResp getConfigByKey(String key);

    /**
     * 创建配置项
     */
    ConfigResp createConfig(ConfigCreateReq req);

    /**
     * 更新配置项
     */
    ConfigResp updateConfig(String id, ConfigUpdateReq req);

    /**
     * 批量更新配置项
     */
    void batchUpdateConfigs(ConfigBatchUpdateReq req);

    /**
     * 删除配置项
     */
    void deleteConfig(String id);

    /**
     * 更新配置项状态
     */
    void updateConfigStatus(String id, Integer status);

    /**
     * 查询配置项历史版本
     */
    List<ConfigHistoryResp> getConfigHistory(String configId);

    /**
     * 回滚到指定历史版本
     */
    void rollbackConfig(String configId, ConfigRollbackReq req);

    /**
     * 导出配置
     */
    ConfigExportResp exportConfigs(String groupId, String format);

    /**
     * 导入配置
     */
    ConfigImportResultResp importConfigs(ConfigImportReq req);

    /**
     * 刷新配置缓存
     */
    void refreshCache();

    /**
     * 查询生效信息
     */
    ConfigEffectInfoResp getEffectInfo();

    /**
     * 获取配置值（供其他模块调用）
     */
    String getConfigValue(String key);
}
```

- [ ] **Step 2: 编写服务实现（第一部分：基础字段和方法）**

```java
package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.OperationType;
import com.adminplus.pojo.dto.req.*;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.pojo.entity.ConfigHistoryEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigHistoryRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.ConfigService;
import com.adminplus.service.LogService;
import com.adminplus.utils.EntityHelper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置项服务实现
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final ConfigGroupRepository groupRepository;
    private final ConfigHistoryRepository historyRepository;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<ConfigResp> getConfigList(Integer page, Integer size, String groupId, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("sortOrder").ascending());

        Specification<ConfigEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (groupId != null && !groupId.isEmpty()) {
                predicates.add(cb.equal(root.get("groupId"), groupId));
            }

            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + keyword + "%"),
                        cb.like(root.get("key"), "%" + keyword + "%")
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageResult = configRepository.findAll(spec, pageable);
        var records = pageResult.getContent().stream()
                .map(this::toVO)
                .toList();

        return new PageResultResp<>(
                records,
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigResp> getConfigsByGroupId(String groupId) {
        return configRepository.findByGroupIdAndStatusOrderBySortOrderAsc(groupId, 1).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'id:' + #id")
    public ConfigResp getConfigById(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(configRepository::findById, id, "配置项不存在");
        return toVO(config);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'key:' + #key")
    public ConfigResp getConfigByKey(String key) {
        ConfigEntity config = configRepository.findByKey(key)
                .orElseThrow(() -> new BizException("配置项不存在"));
        return toVO(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigResp createConfig(ConfigCreateReq req) {
        // 验证分组存在
        ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                groupRepository::findById, req.groupId(), "分组不存在"
        );

        // 检查配置键是否存在
        if (configRepository.existsByKey(req.key())) {
            throw new BizException("配置键已存在");
        }

        ConfigEntity config = new ConfigEntity();
        config.setGroupId(req.groupId());
        config.setName(req.name());
        config.setKey(req.key());
        config.setValue(req.value());
        config.setValueType(req.valueType());
        config.setEffectType(req.effectType() != null ? req.effectType() : "IMMEDIATE");
        config.setDefaultValue(req.defaultValue());
        config.setDescription(req.description());
        config.setIsRequired(req.isRequired() != null ? req.isRequired() : false);
        config.setValidationRule(req.validationRule());
        config.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        config.setStatus(1);

        config = configRepository.save(config);
        log.info("创建配置项成功: {}", config.getKey());

        logService.log("参数配置", OperationType.CREATE, "创建配置项: " + config.getName() + " (" + config.getKey() + ")");

        return toVO(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public ConfigResp updateConfig(String id, ConfigUpdateReq req) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(configRepository::findById, id, "配置项不存在");

        // 记录历史
        if (req.value() != null && !req.value().equals(config.getValue())) {
            saveConfigHistory(config, config.getValue(), req.value(), "更新配置值");
        }

        if (req.name() != null) {
            config.setName(req.name());
        }
        if (req.value() != null) {
            config.setValue(req.value());
        }
        if (req.valueType() != null) {
            config.setValueType(req.valueType());
        }
        if (req.effectType() != null) {
            config.setEffectType(req.effectType());
        }
        if (req.defaultValue() != null) {
            config.setDefaultValue(req.defaultValue());
        }
        if (req.description() != null) {
            config.setDescription(req.description());
        }
        if (req.isRequired() != null) {
            config.setIsRequired(req.isRequired());
        }
        if (req.validationRule() != null) {
            config.setValidationRule(req.validationRule());
        }
        if (req.sortOrder() != null) {
            config.setSortOrder(req.sortOrder());
        }
        if (req.status() != null) {
            config.setStatus(req.status());
        }

        config = configRepository.save(config);
        log.info("更新配置项成功: {}", config.getKey());

        logService.log("参数配置", OperationType.UPDATE, "更新配置项: " + config.getName() + " (" + config.getKey() + ")");

        return toVO(config);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void batchUpdateConfigs(ConfigBatchUpdateReq req) {
        for (ConfigBatchUpdateReq.ConfigItemUpdate item : req.items()) {
            if (item.id() == null || item.value() == null) {
                continue;
            }

            ConfigEntity config = EntityHelper.findByIdOrThrow(
                    configRepository::findById, item.id(), "配置项不存在"
            );

            // 记录历史
            if (!item.value().equals(config.getValue())) {
                saveConfigHistory(config, config.getValue(), item.value(), "批量更新");
            }

            config.setValue(item.value());
            configRepository.save(config);
        }

        log.info("批量更新配置项成功，共 {} 项", req.items().size());
        logService.log("参数配置", OperationType.UPDATE, "批量更新配置项，共 " + req.items().size() + " 项");
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void deleteConfig(String id) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(configRepository::findById, id, "配置项不存在");

        config.setDeleted(true);
        configRepository.save(config);
        log.info("删除配置项成功: {}", config.getKey());

        logService.log("参数配置", OperationType.DELETE, "删除配置项: " + config.getName() + " (" + config.getKey() + ")");
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void updateConfigStatus(String id, Integer status) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(configRepository::findById, id, "配置项不存在");

        config.setStatus(status);
        configRepository.save(config);
        log.info("更新配置项状态成功: {}", config.getKey());
    }

    private void saveConfigHistory(ConfigEntity config, String oldValue, String newValue, String remark) {
        ConfigHistoryEntity history = new ConfigHistoryEntity();
        history.setConfigId(config.getId());
        history.setConfigKey(config.getKey());
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setRemark(remark);
        historyRepository.save(history);
    }

    private ConfigResp toVO(ConfigEntity config) {
        // 获取分组名称
        String groupName = null;
        if (config.getGroupId() != null) {
            groupName = groupRepository.findById(config.getGroupId())
                    .map(ConfigGroupEntity::getName)
                    .orElse(null);
        }

        // 敏感配置脱敏
        String displayValue = config.getValue();
        if ("SECRET".equals(config.getValueType()) && displayValue != null && !displayValue.isEmpty()) {
            displayValue = "****";
        }

        return new ConfigResp(
                config.getId(),
                config.getGroupId(),
                groupName,
                config.getName(),
                config.getKey(),
                displayValue,
                config.getValueType(),
                config.getEffectType(),
                config.getDefaultValue(),
                config.getDescription(),
                config.getIsRequired(),
                config.getValidationRule(),
                config.getSortOrder(),
                config.getStatus(),
                config.getCreateTime(),
                config.getUpdateTime()
        );
    }
}
```

- [ ] **Step 3: 提交当前部分**

```bash
git add backend/src/main/java/com/adminplus/service/ConfigService.java \
        backend/src/main/java/com/adminplus/service/impl/ConfigServiceImpl.java
git commit -m "feat: add ConfigService implementation (part 1: basic CRUD)"
```

---

## Task 9: 完成配置项服务（高级功能）

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/ConfigServiceImpl.java`（添加高级功能）

- [ ] **Step 1: 添加历史版本、回滚、缓存刷新等方法**

在 `ConfigServiceImpl.java` 中继续添加以下方法：

```java
    @Override
    @Transactional(readOnly = true)
    public List<ConfigHistoryResp> getConfigHistory(String configId) {
        List<ConfigHistoryEntity> histories = historyRepository.findByConfigIdOrderByCreateTimeDesc(configId);

        return histories.stream()
                .map(history -> new ConfigHistoryResp(
                        history.getId(),
                        history.getConfigId(),
                        history.getConfigKey(),
                        history.getOldValue(),
                        history.getNewValue(),
                        history.getRemark(),
                        history.getCreateUser(),
                        history.getCreateTime()
                ))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void rollbackConfig(String configId, ConfigRollbackReq req) {
        ConfigEntity config = EntityHelper.findByIdOrThrow(configRepository::findById, configId, "配置项不存在");
        ConfigHistoryEntity history = EntityHelper.findByIdOrThrow(
                historyRepository::findById, req.historyId(), "历史记录不存在"
        );

        // 验证历史记录属于该配置
        if (!history.getConfigId().equals(configId)) {
            throw new BizException("历史记录不属于该配置项");
        }

        // 记录回滚前的值
        saveConfigHistory(config, config.getValue(), history.getOldValue(),
                "回滚操作: " + (req.remark() != null ? req.remark() : ""));

        config.setValue(history.getOldValue());
        configRepository.save(config);
        log.info("回滚配置项成功: {} 到历史版本 {}", config.getKey(), req.historyId());

        logService.log("参数配置", OperationType.UPDATE,
                "回滚配置项: " + config.getName() + " (" + config.getKey() + ")");
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigExportResp exportConfigs(String groupId, String format) {
        List<ConfigGroupEntity> groups;

        if (groupId != null && !groupId.isEmpty()) {
            ConfigGroupEntity group = EntityHelper.findByIdOrThrow(
                    groupRepository::findById, groupId, "分组不存在"
            );
            groups = List.of(group);
        } else {
            groups = groupRepository.findByStatusOrderBySortOrderAsc(1);
        }

        List<ConfigExportResp.ExportGroup> exportGroups = groups.stream()
                .map(group -> {
                    List<ConfigEntity> configs = configRepository
                            .findByGroupIdAndStatusOrderBySortOrderAsc(group.getId(), 1);
                    List<ConfigExportResp.ExportConfig> exportConfigs = configs.stream()
                            .map(config -> new ConfigExportResp.ExportConfig(
                                    config.getKey(),
                                    config.getName(),
                                    config.getValue(),
                                    config.getValueType(),
                                    config.getEffectType(),
                                    config.getDescription()
                            ))
                            .toList();
                    return new ConfigExportResp.ExportGroup(
                            group.getCode(),
                            group.getName(),
                            group.getIcon(),
                            exportConfigs
                    );
                })
                .toList();

        return new ConfigExportResp(
                "1.0",
                Instant.now().toString(),
                exportGroups
        );
    }

    @Override
    @Transactional
    public ConfigImportResultResp importConfigs(ConfigImportReq req) {
        // 实现 JSON 导入逻辑
        // 省略详细实现，需要 JSON 解析和验证

        ConfigImportResultResp result = new ConfigImportResultResp(
                0, 0, 0, 0, List.of()
        );

        logService.log("参数配置", OperationType.CREATE, "导入配置");
        return result;
    }

    @Override
    @CacheEvict(value = "config", allEntries = true)
    public void refreshCache() {
        log.info("配置缓存已刷新");
        logService.log("参数配置", OperationType.UPDATE, "刷新配置缓存");
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigEffectInfoResp getEffectInfo() {
        List<ConfigEntity> pendingConfigs = configRepository.findByEffectType("MANUAL");
        List<ConfigEntity> restartConfigs = configRepository.findByEffectType("RESTART");

        List<ConfigEffectInfoResp.PendingEffect> pendingEffects = pendingConfigs.stream()
                .map(config -> new ConfigEffectInfoResp.PendingEffect(
                        config.getKey(),
                        config.getName(),
                        config.getValue(),
                        config.getEffectType(),
                        config.getUpdateTime().toString()
                ))
                .toList();

        List<String> restartRequiredKeys = restartConfigs.stream()
                .map(ConfigEntity::getKey)
                .toList();

        return new ConfigEffectInfoResp(pendingEffects, restartRequiredKeys);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "config", key = "'value:' + #key")
    public String getConfigValue(String key) {
        return configRepository.findByKey(key)
                .map(ConfigEntity::getValue)
                .orElse(null);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/java/com/adminplus/service/impl/ConfigServiceImpl.java
git commit -m "feat: add ConfigService advanced features (history, rollback, cache)"
```

---

## Task 10: 创建配置分组控制器

**Files:**
- Create: `backend/src/main/java/com/adminplus/controller/ConfigGroupController.java`
- Test: `backend/src/test/java/com/adminplus/controller/ConfigGroupControllerTest.java`

- [ ] **Step 1: 编写控制器**

```java
package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配置分组控制器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/config-groups")
@RequiredArgsConstructor
@Tag(name = "配置分组管理", description = "配置分组增删改查")
public class ConfigGroupController {

    private final ConfigGroupService configGroupService;

    @GetMapping
    @Operation(summary = "分页查询分组列表")
    @OperationLog(module = "参数配置", operationType = 1, description = "查询分组列表")
    @PreAuthorize("hasAuthority('system:config:list')")
    public ApiResponse<PageResultResp<ConfigGroupResp>> getGroupList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResultResp<ConfigGroupResp> result = configGroupService.getGroupList(page, size, keyword);
        return ApiResponse.ok(result);
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有启用的分组")
    @PreAuthorize("hasAuthority('system:config:list')")
    public ApiResponse<List<ConfigGroupResp>> getAllGroups() {
        List<ConfigGroupResp> result = configGroupService.getAllGroups();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询分组")
    @OperationLog(module = "参数配置", operationType = 1, description = "查询分组详情 {#id}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigGroupResp> getGroupById(@PathVariable String id) {
        ConfigGroupResp result = configGroupService.getGroupById(id);
        return ApiResponse.ok(result);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询分组")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigGroupResp> getGroupByCode(@PathVariable String code) {
        ConfigGroupResp result = configGroupService.getGroupByCode(code);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建分组")
    @OperationLog(module = "参数配置", operationType = 2, description = "新增分组 {#req.name}")
    @PreAuthorize("hasAuthority('system:config:add')")
    public ApiResponse<ConfigGroupResp> createGroup(@Valid @RequestBody ConfigGroupCreateReq req) {
        ConfigGroupResp result = configGroupService.createGroup(req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分组")
    @OperationLog(module = "参数配置", operationType = 3, description = "修改分组 {#id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<ConfigGroupResp> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody ConfigGroupUpdateReq req
    ) {
        ConfigGroupResp result = configGroupService.updateGroup(id, req);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分组")
    @OperationLog(module = "参数配置", operationType = 4, description = "删除分组 {#id}")
    @PreAuthorize("hasAuthority('system:config:delete')")
    public ApiResponse<Void> deleteGroup(@PathVariable String id) {
        configGroupService.deleteGroup(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新分组状态")
    @OperationLog(module = "参数配置", operationType = 3, description = "修改分组状态 {#id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<Void> updateGroupStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        configGroupService.updateGroupStatus(id, status);
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 2: 编写控制器测试**

```java
package com.adminplus.controller;

import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigGroupController Unit Tests")
class ConfigGroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConfigGroupService configGroupService;

    @InjectMocks
    private ConfigGroupController configGroupController;

    private ObjectMapper objectMapper;
    private ConfigGroupResp testGroup;
    private ConfigGroupCreateReq createReq;
    private ConfigGroupUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(configGroupController).build();
        objectMapper = new ObjectMapper();
        testGroup = new ConfigGroupResp(
                "group-001", "基础配置", "basic", "Settings", 1,
                "系统基础配置", 1, 0L, Instant.now(), Instant.now()
        );
        createReq = new ConfigGroupCreateReq("邮件配置", "email", "Mail", 2, "邮件配置");
        updateReq = new ConfigGroupUpdateReq("邮件配置（更新）", "MailIcon", 3, "更新描述");
    }

    @Nested
    @DisplayName("getGroupList Tests")
    class GetGroupListTests {

        @Test
        @DisplayName("should return group list")
        void getGroupList_ShouldReturnGroupList() throws Exception {
            PageResultResp<ConfigGroupResp> pageResult = new PageResultResp<>(List.of(testGroup), 1L, 1, 10);
            when(configGroupService.getGroupList(1, 10, null)).thenReturn(pageResult);

            mockMvc.perform(get("/v1/sys/config-groups")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("基础配置"));

            verify(configGroupService).getGroupList(1, 10, null);
        }
    }

    @Nested
    @DisplayName("getAllGroups Tests")
    class GetAllGroupsTests {

        @Test
        @DisplayName("should return all groups")
        void getAllGroups_ShouldReturnAllGroups() throws Exception {
            when(configGroupService.getAllGroups()).thenReturn(List.of(testGroup));

            mockMvc.perform(get("/v1/sys/config-groups/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].code").value("basic"));

            verify(configGroupService).getAllGroups();
        }
    }

    @Nested
    @DisplayName("createGroup Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("should create group")
        void createGroup_ShouldCreateGroup() throws Exception {
            when(configGroupService.createGroup(any(ConfigGroupCreateReq.class))).thenReturn(testGroup);

            mockMvc.perform(post("/v1/sys/config-groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configGroupService).createGroup(any(ConfigGroupCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateGroup Tests")
    class UpdateGroupTests {

        @Test
        @DisplayName("should update group")
        void updateGroup_ShouldUpdateGroup() throws Exception {
            when(configGroupService.updateGroup(anyString(), any(ConfigGroupUpdateReq.class))).thenReturn(testGroup);

            mockMvc.perform(put("/v1/sys/config-groups/group-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configGroupService).updateGroup(anyString(), any(ConfigGroupUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteGroup Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("should delete group")
        void deleteGroup_ShouldDeleteGroup() throws Exception {
            mockMvc.perform(delete("/v1/sys/config-groups/group-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configGroupService).deleteGroup("group-001");
        }
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend
mvn test -Dtest=ConfigGroupControllerTest
```

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/controller/ConfigGroupController.java \
        backend/src/test/java/com/adminplus/controller/ConfigGroupControllerTest.java
git commit -m "feat: add ConfigGroupController"
```

---

## Task 11: 创建配置项控制器

**Files:**
- Create: `backend/src/main/java/com/adminplus/controller/ConfigController.java`
- Test: `backend/src/test/java/com/adminplus/controller/ConfigControllerTest.java`

- [ ] **Step 1: 编写控制器**

```java
package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.common.pojo.ApiResponse;
import com.adminplus.pojo.dto.req.*;
import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配置项控制器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/v1/sys/configs")
@RequiredArgsConstructor
@Tag(name = "配置项管理", description = "配置项增删改查")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    @Operation(summary = "分页查询配置项列表")
    @OperationLog(module = "参数配置", operationType = 1, description = "查询配置项列表")
    @PreAuthorize("hasAuthority('system:config:list')")
    public ApiResponse<PageResultResp<ConfigResp>> getConfigList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String keyword
    ) {
        PageResultResp<ConfigResp> result = configService.getConfigList(page, size, groupId, keyword);
        return ApiResponse.ok(result);
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "查询指定分组的配置项")
    @PreAuthorize("hasAuthority('system:config:list')")
    public ApiResponse<List<ConfigResp>> getConfigsByGroupId(@PathVariable String groupId) {
        List<ConfigResp> result = configService.getConfigsByGroupId(groupId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置项")
    @OperationLog(module = "参数配置", operationType = 1, description = "查询配置项详情 {#id}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigResp> getConfigById(@PathVariable String id) {
        ConfigResp result = configService.getConfigById(id);
        return ApiResponse.ok(result);
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "根据配置键查询")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigResp> getConfigByKey(@PathVariable String key) {
        ConfigResp result = configService.getConfigByKey(key);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建配置项")
    @OperationLog(module = "参数配置", operationType = 2, description = "新增配置项 {#req.name}")
    @PreAuthorize("hasAuthority('system:config:add')")
    public ApiResponse<ConfigResp> createConfig(@Valid @RequestBody ConfigCreateReq req) {
        ConfigResp result = configService.createConfig(req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置项")
    @OperationLog(module = "参数配置", operationType = 3, description = "修改配置项 {#id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<ConfigResp> updateConfig(
            @PathVariable String id,
            @Valid @RequestBody ConfigUpdateReq req
    ) {
        ConfigResp result = configService.updateConfig(id, req);
        return ApiResponse.ok(result);
    }

    @PutMapping("/batch")
    @Operation(summary = "批量更新配置项")
    @OperationLog(module = "参数配置", operationType = 3, description = "批量更新配置项")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<Void> batchUpdateConfigs(@Valid @RequestBody ConfigBatchUpdateReq req) {
        configService.batchUpdateConfigs(req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置项")
    @OperationLog(module = "参数配置", operationType = 4, description = "删除配置项 {#id}")
    @PreAuthorize("hasAuthority('system:config:delete')")
    public ApiResponse<Void> deleteConfig(@PathVariable String id) {
        configService.deleteConfig(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新配置项状态")
    @OperationLog(module = "参数配置", operationType = 3, description = "修改配置项状态 {#id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<Void> updateConfigStatus(
            @PathVariable String id,
            @RequestParam Integer status
    ) {
        configService.updateConfigStatus(id, status);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "查询配置项历史版本")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<List<ConfigHistoryResp>> getConfigHistory(@PathVariable String id) {
        List<ConfigHistoryResp> result = configService.getConfigHistory(id);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚到指定历史版本")
    @OperationLog(module = "参数配置", operationType = 3, description = "回滚配置项 {#id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<Void> rollbackConfig(
            @PathVariable String id,
            @Valid @RequestBody ConfigRollbackReq req
    ) {
        configService.rollbackConfig(id, req);
        return ApiResponse.ok();
    }

    @GetMapping("/export")
    @Operation(summary = "导出配置")
    @OperationLog(module = "参数配置", operationType = 1, description = "导出配置")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigExportResp> exportConfigs(
            @RequestParam(required = false) String groupId,
            @RequestParam(defaultValue = "JSON") String format
    ) {
        ConfigExportResp result = configService.exportConfigs(groupId, format);
        return ApiResponse.ok(result);
    }

    @PostMapping("/import")
    @Operation(summary = "导入配置")
    @OperationLog(module = "参数配置", operationType = 2, description = "导入配置")
    @PreAuthorize("hasAuthority('system:config:add')")
    public ApiResponse<ConfigImportResultResp> importConfigs(@Valid @RequestBody ConfigImportReq req) {
        ConfigImportResultResp result = configService.importConfigs(req);
        return ApiResponse.ok(result);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新配置缓存")
    @OperationLog(module = "参数配置", operationType = 3, description = "刷新配置缓存")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public ApiResponse<Void> refreshCache() {
        configService.refreshCache();
        return ApiResponse.ok();
    }

    @GetMapping("/effect-info")
    @Operation(summary = "查询生效信息")
    @PreAuthorize("hasAuthority('system:config:query')")
    public ApiResponse<ConfigEffectInfoResp> getEffectInfo() {
        ConfigEffectInfoResp result = configService.getEffectInfo();
        return ApiResponse.ok(result);
    }
}
```

- [ ] **Step 2: 编写控制器测试（精简版）**

```java
package com.adminplus.controller;

import com.adminplus.pojo.dto.req.ConfigCreateReq;
import com.adminplus.pojo.dto.req.ConfigUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigController Unit Tests")
class ConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

    private ObjectMapper objectMapper;
    private ConfigResp testConfig;
    private ConfigCreateReq createReq;
    private ConfigUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(configController).build();
        objectMapper = new ObjectMapper();
        testConfig = new ConfigResp(
                "config-001", "group-001", "基础配置", "系统名称", "system.name",
                "AdminPlus", "STRING", "IMMEDIATE", null, "系统显示名称",
                false, null, 1, 1, Instant.now(), Instant.now()
        );
        createReq = new ConfigCreateReq(
                "group-001", "最大上传", "upload.max", "10",
                "NUMBER", "IMMEDIATE", "10", "上传文件大小限制（MB）",
                true, null, 2
        );
        updateReq = new ConfigUpdateReq(
                "系统名称（更新）", "AdminPlus-Pro", "STRING", "IMMEDIATE",
                null, "更新后的说明", true, null, 1, 1
        );
    }

    @Nested
    @DisplayName("getConfigList Tests")
    class GetConfigListTests {

        @Test
        @DisplayName("should return config list")
        void getConfigList_ShouldReturnConfigList() throws Exception {
            PageResultResp<ConfigResp> pageResult = new PageResultResp<>(List.of(testConfig), 1L, 1, 10);
            when(configService.getConfigList(1, 10, null, null)).thenReturn(pageResult);

            mockMvc.perform(get("/v1/sys/configs")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("系统名称"));

            verify(configService).getConfigList(1, 10, null, null);
        }
    }

    @Nested
    @DisplayName("getConfigById Tests")
    class GetConfigByIdTests {

        @Test
        @DisplayName("should return config by id")
        void getConfigById_ShouldReturnConfig() throws Exception {
            when(configService.getConfigById("config-001")).thenReturn(testConfig);

            mockMvc.perform(get("/v1/sys/configs/config-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.key").value("system.name"));

            verify(configService).getConfigById("config-001");
        }
    }

    @Nested
    @DisplayName("createConfig Tests")
    class CreateConfigTests {

        @Test
        @DisplayName("should create config")
        void createConfig_ShouldCreateConfig() throws Exception {
            when(configService.createConfig(any(ConfigCreateReq.class))).thenReturn(testConfig);

            mockMvc.perform(post("/v1/sys/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).createConfig(any(ConfigCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateConfig Tests")
    class UpdateConfigTests {

        @Test
        @DisplayName("should update config")
        void updateConfig_ShouldUpdateConfig() throws Exception {
            when(configService.updateConfig(anyString(), any(ConfigUpdateReq.class))).thenReturn(testConfig);

            mockMvc.perform(put("/v1/sys/configs/config-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).updateConfig(anyString(), any(ConfigUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteConfig Tests")
    class DeleteConfigTests {

        @Test
        @DisplayName("should delete config")
        void deleteConfig_ShouldDeleteConfig() throws Exception {
            mockMvc.perform(delete("/v1/sys/configs/config-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).deleteConfig("config-001");
        }
    }

    @Nested
    @DisplayName("refreshCache Tests")
    class RefreshCacheTests {

        @Test
        @DisplayName("should refresh cache")
        void refreshCache_ShouldRefreshCache() throws Exception {
            mockMvc.perform(post("/v1/sys/configs/refresh-cache"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).refreshCache();
        }
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend
mvn test -Dtest=ConfigControllerTest
```

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/controller/ConfigController.java \
        backend/src/test/java/com/adminplus/controller/ConfigControllerTest.java
git commit -m "feat: add ConfigController"
```

---

## Task 12: 添加前端类型定义

**Files:**
- Modify: `frontend/src/types/index.ts`

- [ ] **Step 1: 添加配置相关类型**

在 `frontend/src/types/index.ts` 文件末尾添加：

```typescript
// 配置分组
export interface ConfigGroup {
  id: string
  name: string
  code: string
  icon: string
  sortOrder: number
  description: string
  status: number
  configCount: number
  createTime: string
  updateTime: string
}

// 配置项
export interface Config {
  id: string
  groupId: string
  groupName: string
  name: string
  key: string
  value: string
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'ARRAY' | 'SECRET' | 'FILE'
  effectType: 'IMMEDIATE' | 'MANUAL' | 'RESTART'
  defaultValue: string
  description: string
  isRequired: boolean
  validationRule: string
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

// 配置历史
export interface ConfigHistory {
  id: string
  configId: string
  configKey: string
  oldValue: string
  newValue: string
  remark: string
  operatorName: string
  createTime: string
}

// 配置导出
export interface ConfigExport {
  exportVersion: string
  exportTime: string
  groups: ConfigExportGroup[]
}

export interface ConfigExportGroup {
  code: string
  name: string
  icon: string
  configs: ConfigExportItem[]
}

export interface ConfigExportItem {
  key: string
  name: string
  value: string
  valueType: string
  effectType: string
  description: string
}

// 配置导入结果
export interface ConfigImportResult {
  total: number
  success: number
  skipped: number
  failed: number
  details: ConfigImportDetail[]
}

export interface ConfigImportDetail {
  key: string
  status: 'success' | 'skipped' | 'failed'
  reason?: string
}

// 配置生效信息
export interface ConfigEffectInfo {
  pendingEffects: ConfigPendingEffect[]
  restartRequiredConfigs: string[]
}

export interface ConfigPendingEffect {
  key: string
  name: string
  newValue: string
  effectType: string
  updateTime: string
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/types/index.ts
git commit -m "feat: add config types to frontend"
```

---

## Task 13: 创建前端 API 调用模块

**Files:**
- Create: `frontend/src/api/config.ts`

- [ ] **Step 1: 编写 API 调用**

```typescript
import { get, post, put, del } from '@/utils/request'
import type {
  ConfigGroup,
  Config,
  ConfigHistory,
  ConfigExport,
  ConfigImportResult,
  ConfigEffectInfo,
  PageResult
} from '@/types'

// ==================== 配置分组 API ====================

// 获取分组列表
export function getConfigGroupList(params: {
  page?: number
  size?: number
  keyword?: string
}) {
  return get<PageResult<ConfigGroup>>('/config-groups', params)
}

// 获取所有分组（不分页）
export function getAllConfigGroups() {
  return get<ConfigGroup[]>('/config-groups/all')
}

// 根据ID获取分组
export function getConfigGroupById(id: string) {
  return get<ConfigGroup>(`/config-groups/${id}`)
}

// 根据编码获取分组
export function getConfigGroupByCode(code: string) {
  return get<ConfigGroup>(`/config-groups/code/${code}`)
}

// 创建分组
export function createConfigGroup(data: {
  name: string
  code: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return post<ConfigGroup>('/config-groups', data)
}

// 更新分组
export function updateConfigGroup(id: string, data: {
  name?: string
  icon?: string
  sortOrder?: number
  description?: string
}) {
  return put<ConfigGroup>(`/config-groups/${id}`, data)
}

// 删除分组
export function deleteConfigGroup(id: string) {
  return del<void>(`/config-groups/${id}`)
}

// 更新分组状态
export function updateConfigGroupStatus(id: string, status: number) {
  return put<void>(`/config-groups/${id}/status?status=${status}`)
}

// ==================== 配置项 API ====================

// 获取配置项列表
export function getConfigList(params: {
  page?: number
  size?: number
  groupId?: string
  keyword?: string
}) {
  return get<PageResult<Config>>('/configs', params)
}

// 根据分组ID获取配置项
export function getConfigsByGroupId(groupId: string) {
  return get<Config[]>(`/configs/group/${groupId}`)
}

// 根据ID获取配置项
export function getConfigById(id: string) {
  return get<Config>(`/configs/${id}`)
}

// 根据配置键获取
export function getConfigByKey(key: string) {
  return get<Config>(`/configs/key/${key}`)
}

// 创建配置项
export function createConfig(data: {
  groupId: string
  name: string
  key: string
  value?: string
  valueType: string
  effectType?: string
  defaultValue?: string
  description?: string
  isRequired?: boolean
  validationRule?: string
  sortOrder?: number
}) {
  return post<Config>('/configs', data)
}

// 更新配置项
export function updateConfig(id: string, data: {
  name?: string
  value?: string
  valueType?: string
  effectType?: string
  defaultValue?: string
  description?: string
  isRequired?: boolean
  validationRule?: string
  sortOrder?: number
  status?: number
}) {
  return put<Config>(`/configs/${id}`, data)
}

// 批量更新配置项
export function batchUpdateConfigs(items: Array<{ id: string; value: string }>) {
  return put<void>('/configs/batch', { items })
}

// 删除配置项
export function deleteConfig(id: string) {
  return del<void>(`/configs/${id}`)
}

// 更新配置项状态
export function updateConfigStatus(id: string, status: number) {
  return put<void>(`/configs/${id}/status?status=${status}`)
}

// ==================== 高级功能 API ====================

// 获取配置历史
export function getConfigHistory(configId: string) {
  return get<ConfigHistory[]>(`/configs/${configId}/history`)
}

// 回滚配置
export function rollbackConfig(configId: string, data: { historyId: string; remark?: string }) {
  return post<void>(`/configs/${configId}/rollback`, data)
}

// 导出配置
export function exportConfigs(params: { groupId?: string; format?: string }) {
  return get<ConfigExport>('/configs/export', params)
}

// 导入配置
export function importConfigs(data: {
  content: string
  format: 'JSON' | 'YAML'
  mode?: 'OVERWRITE' | 'MERGE' | 'VALIDATE'
}) {
  return post<ConfigImportResult>('/configs/import', data)
}

// 刷新配置缓存
export function refreshConfigCache() {
  return post<void>('/configs/refresh-cache', {})
}

// 获取生效信息
export function getEffectInfo() {
  return get<ConfigEffectInfo>('/configs/effect-info')
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/api/config.ts
git commit -m "feat: add config API module"
```

---

## Task 14: 更新前端权限配置

**Files:**
- Modify: `frontend/src/lib/page-permissions.ts`

- [ ] **Step 1: 更新权限配置**

将 `page-permissions.ts` 中的系统监控配置项改为参数配置：

找到：
```typescript
  {
    path: '/system/config',
    label: '系统监控',
    icon: 'Server',
    color: 'text-slate-500',
    permissions: ['system:config:list']
  },
```

替换为：
```typescript
  {
    path: '/system/config',
    label: '参数配置',
    icon: 'Settings',
    color: 'text-slate-500',
    permissions: ['system:config:list']
  },
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/lib/page-permissions.ts
git commit -m "fix: update config menu label from system monitor to parameter config"
```

---

## Task 15: 创建参数配置主页面

**Files:**
- Create: `frontend/src/views/system/Config.vue`（替换现有文件）
- Create: `frontend/src/components/config/ConfigGroupTabs.vue`
- Create: `frontend/src/components/config/ConfigItemTable.vue`
- Create: `frontend/src/components/config/ConfigGroupFormDialog.vue`
- Create: `frontend/src/components/config/ConfigItemFormDialog.vue`

- [ ] **Step 1: 创建配置分组标签组件**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { Settings, Mail, Database, Shield } from 'lucide-vue-next'
import type { ConfigGroup } from '@/types'

interface Props {
  groups: ConfigGroup[]
  activeCode: string
}

interface Emits {
  (e: 'update:activeCode', code: string): void
  (e: 'add'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const iconMap: Record<string, any> = {
  Settings,
  Mail,
  Database,
  Shield
}

const activeGroup = computed(() =>
  props.groups.find(g => g.code === props.activeCode)
)
</script>

<template>
  <div class="flex items-center gap-2 border-b pb-4 mb-6">
    <button
      v-for="group in groups"
      :key="group.id"
      :class="[
        'px-4 py-2 rounded-lg font-medium transition-all flex items-center gap-2',
        activeCode === group.code
          ? 'bg-primary text-primary-foreground'
          : 'bg-muted hover:bg-muted/80'
      ]"
      @click="$emit('update:activeCode', group.code)"
    >
      <component :is="iconMap[group.icon] || Settings" class="h-4 w-4" />
      {{ group.name }}
      <span class="text-xs opacity-70">({{ group.configCount }})</span>
    </button>
    <button
      class="ml-auto px-3 py-2 rounded-lg border border-dashed border-muted-foreground/30 text-muted-foreground hover:border-primary hover:text-primary transition-all flex items-center gap-1"
      @click="$emit('add')"
    >
      <span class="text-lg">+</span>
      新增分组
    </button>
  </div>
</template>
```

- [ ] **Step 2: 创建配置项表格组件**

```vue
<script setup lang="ts">
import { computed, ref } from 'vue'
import { Card, CardContent } from '@/components/ui'
import { Edit, Trash2, History, Eye, EyeOff } from 'lucide-vue-next'
import type { Config } from '@/types'

interface Props {
  configs: Config[]
  loading: boolean
}

interface Emits {
  (e: 'edit', config: Config): void
  (e: 'delete', id: string): void
  (e: 'history', id: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const showSecretValues = ref<Record<string, boolean>>({})

const toggleSecret = (key: string) => {
  showSecretValues.value[key] = !showSecretValues.value[key]
}

const displayValue = (config: Config) => {
  if (config.valueType === 'SECRET') {
    return showSecretValues.value[config.key] ? config.value : '****'
  }
  return config.value || '-'
}

const effectTypeLabel: Record<string, string> = {
  IMMEDIATE: '立即生效',
  MANUAL: '手动刷新',
  RESTART: '重启生效'
}

const effectTypeClass: Record<string, string> = {
  IMMEDIATE: 'text-green-600',
  MANUAL: 'text-yellow-600',
  RESTART: 'text-red-600'
}
</script>

<template>
  <Card>
    <CardContent class="p-0">
      <div v-if="loading" class="py-8 text-center text-muted-foreground">加载中...</div>
      <div v-else-if="configs.length === 0" class="py-8 text-center text-muted-foreground">暂无配置项</div>
      <table v-else class="w-full">
        <thead class="border-b bg-muted/50">
          <tr>
            <th class="p-4 text-left font-medium">配置名称</th>
            <th class="p-4 text-left font-medium">配置键</th>
            <th class="p-4 text-left font-medium">配置值</th>
            <th class="p-4 text-left font-medium">类型</th>
            <th class="p-4 text-left font-medium">生效方式</th>
            <th class="p-4 text-left font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr v-for="config in configs" :key="config.id" class="hover:bg-muted/30">
            <td class="p-4 font-medium">{{ config.name }}</td>
            <td class="p-4 text-sm text-muted-foreground font-mono">{{ config.key }}</td>
            <td class="p-4">
              <div class="flex items-center gap-2">
                <span class="text-sm">{{ displayValue(config) }}</span>
                <button
                  v-if="config.valueType === 'SECRET'"
                  class="text-muted-foreground hover:text-foreground"
                  @click="toggleSecret(config.key)"
                >
                  <Eye v-if="!showSecretValues[config.key]" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
            </td>
            <td class="p-4 text-sm text-muted-foreground">{{ config.valueType }}</td>
            <td class="p-4">
              <span :class="['text-sm', effectTypeClass[config.effectType]]">
                {{ effectTypeLabel[config.effectType] }}
              </span>
            </td>
            <td class="p-4">
              <div class="flex gap-2">
                <button class="p-1 hover:bg-muted rounded" @click="$emit('edit', config)">
                  <Edit class="h-4 w-4" />
                </button>
                <button class="p-1 hover:bg-muted rounded text-muted-foreground" @click="$emit('history', config.id)">
                  <History class="h-4 w-4" />
                </button>
                <button class="p-1 hover:bg-muted rounded text-destructive" @click="$emit('delete', config.id)">
                  <Trash2 class="h-4 w-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </CardContent>
  </Card>
</template>
```

- [ ] **Step 3: 创建分组表单弹窗组件**

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { Button, Input, Label, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui'
import type { ConfigGroup } from '@/types'

interface Props {
  open: boolean
  group?: ConfigGroup
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = ref({
  name: '',
  code: '',
  icon: 'Settings',
  sortOrder: 0,
  description: ''
})

const loading = ref(false)

watch(() => props.open, (open) => {
  if (open && props.group) {
    formData.value = {
      name: props.group.name,
      code: props.group.code,
      icon: props.group.icon,
      sortOrder: props.group.sortOrder,
      description: props.group.description
    }
  } else if (open) {
    formData.value = { name: '', code: '', icon: 'Settings', sortOrder: 0, description: '' }
  }
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const { createConfigGroup, updateConfigGroup } = await import('@/api/config')

    if (props.group) {
      await updateConfigGroup(props.group.id, formData.value)
    } else {
      await createConfigGroup(formData.value)
    }

    emit('success')
    emit('update:open', false)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <div class="bg-background rounded-lg shadow-lg w-full max-w-md p-6">
      <h2 class="text-lg font-semibold mb-4">{{ group ? '编辑分组' : '新增分组' }}</h2>

      <div class="space-y-4">
        <div class="space-y-2">
          <Label>分组名称 *</Label>
          <Input v-model="formData.name" placeholder="如：基础配置" />
        </div>

        <div class="space-y-2">
          <Label>分组编码 *</Label>
          <Input v-model="formData.code" placeholder="如：basic" :disabled="!!group" />
        </div>

        <div class="space-y-2">
          <Label>图标</Label>
          <Select v-model="formData.icon">
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="Settings">Settings</SelectItem>
              <SelectItem value="Mail">Mail</SelectItem>
              <SelectItem value="Database">Database</SelectItem>
              <SelectItem value="Shield">Shield</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="space-y-2">
          <Label>排序序号</Label>
          <Input v-model="formData.sortOrder" type="number" />
        </div>

        <div class="space-y-2">
          <Label>描述</Label>
          <Input v-model="formData.description" placeholder="分组描述" />
        </div>
      </div>

      <div class="flex justify-end gap-2 mt-6">
        <Button variant="outline" @click="$emit('update:open', false)">取消</Button>
        <Button :disabled="loading || !formData.name || !formData.code" @click="handleSubmit">
          {{ loading ? '提交中...' : '确定' }}
        </Button>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 4: 创建配置项表单弹窗组件**

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { Button, Input, Label, Select, SelectContent, SelectItem, SelectTrigger, SelectValue, Switch } from '@/components/ui'
import type { Config, ConfigGroup } from '@/types'

interface Props {
  open: boolean
  config?: Config
  groups: ConfigGroup[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = ref({
  groupId: '',
  name: '',
  key: '',
  value: '',
  valueType: 'STRING',
  effectType: 'IMMEDIATE',
  defaultValue: '',
  description: '',
  isRequired: false,
  validationRule: '',
  sortOrder: 0
})

const loading = ref(false)

watch(() => props.open, (open) => {
  if (open && props.config) {
    formData.value = {
      groupId: props.config.groupId,
      name: props.config.name,
      key: props.config.key,
      value: props.config.value,
      valueType: props.config.valueType,
      effectType: props.config.effectType,
      defaultValue: props.config.defaultValue || '',
      description: props.config.description || '',
      isRequired: props.config.isRequired,
      validationRule: props.config.validationRule || '',
      sortOrder: props.config.sortOrder
    }
  } else if (open) {
    formData.value = {
      groupId: props.groups[0]?.id || '',
      name: '',
      key: '',
      value: '',
      valueType: 'STRING',
      effectType: 'IMMEDIATE',
      defaultValue: '',
      description: '',
      isRequired: false,
      validationRule: '',
      sortOrder: 0
    }
  }
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const { createConfig, updateConfig } = await import('@/api/config')

    if (props.config) {
      await updateConfig(props.config.id, formData.value)
    } else {
      await createConfig(formData.value)
    }

    emit('success')
    emit('update:open', false)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <div class="bg-background rounded-lg shadow-lg w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
      <h2 class="text-lg font-semibold mb-4">{{ config ? '编辑配置项' : '新增配置项' }}</h2>

      <div class="space-y-4">
        <div class="space-y-2">
          <Label>所属分组 *</Label>
          <Select v-model="formData.groupId" :disabled="!!config">
            <SelectTrigger>
              <SelectValue placeholder="选择分组" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="group in groups" :key="group.id" :value="group.id">
                {{ group.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="space-y-2">
          <Label>配置名称 *</Label>
          <Input v-model="formData.name" placeholder="如：系统名称" />
        </div>

        <div class="space-y-2">
          <Label>配置键 *</Label>
          <Input v-model="formData.key" placeholder="如：system.name" :disabled="!!config" />
        </div>

        <div class="space-y-2">
          <Label>配置值</Label>
          <Input v-model="formData.value" :placeholder="config?.valueType === 'SECRET' ? '输入敏感信息' : '输入配置值'" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label>值类型</Label>
            <Select v-model="formData.valueType">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="STRING">字符串</SelectItem>
                <SelectItem value="NUMBER">数字</SelectItem>
                <SelectItem value="BOOLEAN">布尔值</SelectItem>
                <SelectItem value="JSON">JSON</SelectItem>
                <SelectItem value="ARRAY">数组</SelectItem>
                <SelectItem value="SECRET">密码</SelectItem>
                <SelectItem value="FILE">文件</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div class="space-y-2">
            <Label>生效方式</Label>
            <Select v-model="formData.effectType">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="IMMEDIATE">立即生效</SelectItem>
                <SelectItem value="MANUAL">手动刷新</SelectItem>
                <SelectItem value="RESTART">重启生效</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <Label>默认值</Label>
          <Input v-model="formData.defaultValue" placeholder="默认值" />
        </div>

        <div class="space-y-2">
          <Label>说明</Label>
          <Input v-model="formData.description" placeholder="配置项说明" />
        </div>

        <div class="flex items-center gap-2">
          <Switch :checked="formData.isRequired" @update:checked="formData.isRequired = $event" />
          <Label>必填项</Label>
        </div>

        <div class="space-y-2">
          <Label>校验规则</Label>
          <Input v-model="formData.validationRule" placeholder="正则表达式或范围" />
        </div>

        <div class="space-y-2">
          <Label>排序序号</Label>
          <Input v-model="formData.sortOrder" type="number" />
        </div>
      </div>

      <div class="flex justify-end gap-2 mt-6">
        <Button variant="outline" @click="$emit('update:open', false)">取消</Button>
        <Button :disabled="loading || !formData.name || !formData.key" @click="handleSubmit">
          {{ loading ? '提交中...' : '确定' }}
        </Button>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 5: 创建主页面**

```vue
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Button, Input, Card, CardContent } from '@/components/ui'
import { Search, RotateCw, Download, Upload } from 'lucide-vue-next'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useUserStore } from '@/stores/user'
import { useConfirmDialog } from '@/components/common'
import ConfigGroupTabs from '@/components/config/ConfigGroupTabs.vue'
import ConfigItemTable from '@/components/config/ConfigItemTable.vue'
import ConfigGroupFormDialog from '@/components/config/ConfigGroupFormDialog.vue'
import ConfigItemFormDialog from '@/components/config/ConfigItemFormDialog.vue'
import {
  getAllConfigGroups,
  getConfigsByGroupId,
  deleteConfigGroup,
  deleteConfig
} from '@/api/config'
import type { ConfigGroup, Config } from '@/types'

const userStore = useUserStore()
const { loading, run: runFetch } = useAsyncAction('获取配置数据失败')
const { showConfirm } = useConfirmDialog()

const groups = ref<ConfigGroup[]>([])
const activeCode = ref('basic')
const configs = ref<Config[]>([])
const searchKeyword = ref('')

// 弹窗状态
const groupDialogOpen = ref(false)
const editingGroup = ref<ConfigGroup | undefined>()
const configDialogOpen = ref(false)
const editingConfig = ref<Config | undefined>()

const activeGroup = computed(() =>
  groups.value.find(g => g.code === activeCode.value)
)

const hasPermission = (perm: string) => userStore.hasPermission(perm)

const fetchGroups = async () => {
  await runFetch(async () => {
    const res = await getAllConfigGroups()
    groups.value = res.data
    if (groups.value.length > 0 && !activeCode.value) {
      activeCode.value = groups.value[0].code
    }
  })
}

const fetchConfigs = async () => {
  if (!activeGroup.value) return
  await runFetch(async () => {
    const res = await getConfigsByGroupId(activeGroup.value.id)
    configs.value = res.data
  })
}

const handleSearch = () => {
  // 简单的客户端搜索
  if (!searchKeyword.value) {
    fetchConfigs()
    return
  }
  const keyword = searchKeyword.value.toLowerCase()
  configs.value = configs.value.filter(c =>
    c.name.toLowerCase().includes(keyword) ||
    c.key.toLowerCase().includes(keyword)
  )
}

const handleAddGroup = () => {
  editingGroup.value = undefined
  groupDialogOpen.value = true
}

const handleEditGroup = () => {
  editingGroup.value = activeGroup.value
  groupDialogOpen.value = true
}

const handleDeleteGroup = async () => {
  const confirmed = await showConfirm({
    title: '确认删除分组',
    description: `确定要删除分组「${activeGroup.value?.name}」吗？分组下有配置项时无法删除。`
  })
  if (!confirmed) return

  await runFetch(async () => {
    await deleteConfigGroup(activeGroup.value!.id)
    await fetchGroups()
    if (groups.value.length > 0) {
      activeCode.value = groups.value[0].code
    }
  })
}

const handleAddConfig = () => {
  editingConfig.value = undefined
  configDialogOpen.value = true
}

const handleEditConfig = (config: Config) => {
  editingConfig.value = config
  configDialogOpen.value = true
}

const handleDeleteConfig = async (id: string) => {
  const config = configs.value.find(c => c.id === id)
  const confirmed = await showConfirm({
    title: '确认删除配置项',
    description: `确定要删除配置项「${config?.name}」吗？`
  })
  if (!confirmed) return

  await runFetch(async () => {
    await deleteConfig(id)
    await fetchConfigs()
  })
}

const handleHistory = (id: string) => {
  // TODO: 打开历史记录面板
  console.log('Show history for config:', id)
}

const handleRefreshCache = async () => {
  const { refreshConfigCache } = await import('@/api/config')
  await runFetch(async () => {
    await refreshConfigCache()
  })
}

const handleExport = async () => {
  const { exportConfigs } = await import('@/api/config')
  const res = await exportConfigs({ groupId: activeGroup.value?.id, format: 'JSON' })
  // TODO: 处理导出文件下载
  console.log('Export:', res.data)
}

onMounted(async () => {
  await fetchGroups()
  await fetchConfigs()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">参数配置</h2>
        <p class="text-sm text-muted-foreground">管理系统配置项，支持在线修改</p>
      </div>
      <div class="flex gap-2">
        <Button variant="outline" size="sm" @click="handleExport">
          <Download class="mr-2 h-4 w-4" />
          导出
        </Button>
        <Button variant="outline" size="sm" @click="handleRefreshCache">
          <RotateCw class="mr-2 h-4 w-4" />
          刷新缓存
        </Button>
      </div>
    </div>

    <ConfigGroupTabs
      v-model:active-code="activeCode"
      :groups="groups"
      @add="handleAddGroup"
      @update:active-code="fetchConfigs"
    />

    <Card v-if="activeGroup">
      <CardContent class="p-4">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <Input
              v-model="searchKeyword"
              placeholder="搜索配置名称或键名"
              class="w-64"
              @keyup.enter="handleSearch"
            />
            <Button variant="outline" @click="handleSearch">
              <Search class="h-4 w-4" />
            </Button>
          </div>
          <div class="flex gap-2">
            <Button v-if="hasPermission('system:config:add')" size="sm" @click="handleAddConfig">
              新增配置
            </Button>
            <Button variant="outline" size="sm" @click="handleEditGroup">
              编辑分组
            </Button>
            <Button variant="outline" size="sm" class="text-destructive" @click="handleDeleteGroup">
              删除分组
            </Button>
          </div>
        </div>

        <ConfigItemTable
          :configs="configs"
          :loading="loading"
          @edit="handleEditConfig"
          @delete="handleDeleteConfig"
          @history="handleHistory"
        />
      </CardContent>
    </Card>

    <ConfigGroupFormDialog
      v-model:open="groupDialogOpen"
      :group="editingGroup"
      @success="fetchGroups"
    />

    <ConfigItemFormDialog
      v-model:open="configDialogOpen"
      :config="editingConfig"
      :groups="groups"
      @success="fetchConfigs"
    />
  </div>
</template>
```

- [ ] **Step 6: 提交**

```bash
git add frontend/src/views/system/Config.vue \
        frontend/src/components/config/
git commit -m "feat: add config management frontend components"
```

---

## Task 16: 创建数据库迁移脚本

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__create_config_tables.sql`

- [ ] **Step 1: 编写 Flyway 迁移脚本**

```sql
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

CREATE INDEX idx_group_code ON sys_config_group(code);
CREATE INDEX idx_group_sort ON sys_config_group(sort_order);
CREATE INDEX idx_group_status ON sys_config_group(status);
CREATE INDEX idx_group_deleted ON sys_config_group(deleted);

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

CREATE INDEX idx_config_group ON sys_config(group_id);
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
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/resources/db/migration/V1__create_config_tables.sql
git commit -m "feat: add database migration for config management"
```

---

## Task 17: 配置 Redis 缓存

**Files:**
- Create: `backend/src/main/java/com/adminplus/common/config/ConfigCacheConfig.java`

- [ ] **Step 1: 编写缓存配置**

```java
package com.adminplus.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 配置缓存配置
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Configuration
@EnableCaching
public class ConfigCacheConfig {

    @Bean
    public RedisCacheManager configCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add backend/src/main/java/com/adminplus/common/config/ConfigCacheConfig.java
git commit -m "feat: add Redis cache configuration for config module"
```

---

## Task 18: 运行后端测试验证

**Files:**
- No new files, run existing tests

- [ ] **Step 1: 运行所有配置相关测试**

```bash
cd backend
mvn test -Dtest=*Config*Test
```

Expected: All tests PASS

- [ ] **Step 2: 检查测试覆盖率**

```bash
cd backend
mvn test jacoco:report
```

Expected: Coverage report generated

- [ ] **Step 3: 提交测试结果**

如果没有测试失败，提交说明：

```bash
git commit --allow-empty -m "test: all config module tests passing"
```

---

## Task 19: 启动应用验证功能

**Files:**
- No new files, manual verification

- [ ] **Step 1: 启动后端服务**

```bash
cd backend
mvn spring-boot:run
```

Expected: 服务启动成功，日志无错误

- [ ] **Step 2: 启动前端服务**

```bash
cd frontend
npm run dev
```

Expected: 前端服务启动在 http://localhost:5173

- [ ] **Step 3: 访问 Swagger 验证 API**

访问: http://localhost:8081/api/swagger-ui.html

验证以下 API 可用：
- `/v1/sys/config-groups` - 分组管理
- `/v1/sys/configs` - 配置项管理

- [ ] **Step 4: 访问前端页面验证**

1. 登录系统
2. 导航到：系统管理 > 参数配置
3. 验证功能：
   - 分组标签切换
   - 配置项列表显示
   - 新增/编辑配置项
   - 删除配置项
   - 刷新缓存

- [ ] **Step 5: 提交验证完成标记**

```bash
git commit --allow-empty -m "test: manual verification completed for config module"
```

---

## Task 20: 更新文档

**Files:**
- Create: `docs/features/config-management.md`

- [ ] **Step 1: 编写功能文档**

```markdown
# 参数配置模块

## 概述

参数配置模块提供在线配置管理功能，支持动态修改系统配置，无需重启应用。

## 功能特性

### 支持的配置类型

- **键值对配置** - 简单 key-value 形式
- **分组配置** - 按模块分组管理（基础配置、邮件配置、存储配置等）
- **结构化配置** - 支持 JSON、数组等复杂类型

### 配置值类型

| 类型 | 说明 | 示例 |
|------|------|------|
| STRING | 字符串 | `AdminPlus` |
| NUMBER | 数字 | `10` |
| BOOLEAN | 布尔值 | `true` |
| JSON | JSON 对象 | `{"timeout": 30}` |
| ARRAY | 数组 | `["value1", "value2"]` |
| SECRET | 密码/密钥 | `****`（脱敏显示） |
| FILE | 文件路径 | `/path/to/file` |

### 生效方式

| 类型 | 说明 | 示例配置 |
|------|------|----------|
| IMMEDIATE | 立即生效 | 系统名称、超时时间 |
| MANUAL | 手动刷新 | 缓存 TTL、连接池大小 |
| RESTART | 重启生效 | 数据库连接、端口配置 |

## API 接口

### 配置分组

```
GET    /v1/sys/config-groups           # 分页查询分组
GET    /v1/sys/config-groups/all       # 查询所有分组
GET    /v1/sys/config-groups/{id}      # 查询分组详情
POST   /v1/sys/config-groups           # 创建分组
PUT    /v1/sys/config-groups/{id}      # 更新分组
DELETE /v1/sys/config-groups/{id}      # 删除分组
PUT    /v1/sys/config-groups/{id}/status  # 更新状态
```

### 配置项

```
GET    /v1/sys/configs                 # 分页查询配置项
GET    /v1/sys/configs/group/{groupId} # 查询分组配置
GET    /v1/sys/configs/{id}            # 查询配置详情
POST   /v1/sys/configs                 # 创建配置项
PUT    /v1/sys/configs/{id}            # 更新配置项
PUT    /v1/sys/configs/batch           # 批量更新
DELETE /v1/sys/configs/{id}            # 删除配置项
POST   /v1/sys/configs/refresh-cache   # 刷新缓存
```

## 使用说明

1. 登录系统后，导航到「系统管理 > 参数配置」
2. 选择配置分组标签
3. 点击「新增配置」添加配置项
4. 修改配置后根据生效方式决定是否需要刷新缓存或重启

## 权限说明

所有配置操作需要 `system:config:*` 系列权限：
- `system:config:list` - 查看配置
- `system:config:query` - 查询配置详情
- `system:config:add` - 新增配置
- `system:config:edit` - 编辑配置
- `system:config:delete` - 删除配置

## 注意事项

1. **敏感配置** - SECRET 类型配置值在列表中脱敏显示
2. **分组删除** - 分组下有配置项时不允许删除
3. **配置键唯一** - 全局唯一，不区分分组
4. **历史记录** - 自动记录修改历史，支持回滚
5. **缓存一致性** - 修改 IMMEDIATE 类型配置自动刷新缓存
```

- [ ] **Step 2: 提交文档**

```bash
git add docs/features/config-management.md
git commit -m "docs: add config management feature documentation"
```

---

## 验证清单

完成以上所有任务后，运行以下验证：

### 后端验证

```bash
cd backend
mvn test -Dtest=*Config*Test
```

### 前端验证

```bash
cd frontend
npm run lint
npm run test
```

### 功能验证

1. 启动后端服务
2. 启动前端服务
3. 登录系统
4. 访问参数配置页面
5. 测试 CRUD 操作
6. 测试缓存刷新
7. 测试分组管理

---

**Plan Complete.** Ready for execution.
