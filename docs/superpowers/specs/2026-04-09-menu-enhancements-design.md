# 菜单管理人性化操作增强设计

**日期**: 2026-04-09
**状态**: 设计中

## 概述

为菜单管理模块添加人性化操作功能，提升用户操作效率。包括：全部展开/收起、菜单复制、快速创建菜单。

## 功能设计

### 1. 全部展开/收起

**位置**: 工具栏搜索区域右侧

**交互**:
- 点击"全部展开"按钮 → 展开所有有子节点的菜单
- 点击"全部收起"按钮 → 收起所有菜单

**技术实现**:
- 复用 `useTreeData` 的 `expandAll()` 和 `collapseAll()` 方法
- 图标: `ChevronsDown` (展开), `ChevronsUp` (收起)

---

### 2. 菜单复制

**位置**: 操作列新增复制按钮

**交互流程**:
1. 点击复制按钮
2. 弹出对话框选择目标父级菜单
3. 确认后创建副本，名称自动添加" (副本)"后缀
4. 刷新列表

**复制规则**:
- 复制菜单本身，不包含子菜单（简化实现）
- 名称格式: `{原名称} (副本)`
- 排序值: 目标父级下最大值 + 10
- 其他字段: 保持原值

**API**:
```typescript
POST /api/sys/menus/{id}/copy
Body: { targetParentId: string }
```

---

### 3. 快速创建

**位置**: 工具栏"新增菜单"按钮右侧

**交互流程**:
1. 点击"快速创建"按钮
2. 弹出简化对话框
3. 填写必填项后创建
4. 创建成功后保持对话框打开，可连续创建

**对话框字段**:
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| 父级菜单 | 下拉 | 是 | 顶级 | 选择父级 |
| 菜单类型 | 下拉 | 是 | 目录 | 目录/菜单/按钮 |
| 菜单名称 | 输入 | 是 | - | 菜单显示名称 |

**自动填充值**:
- 排序: 0
- 可见: 显示
- 状态: 正常

---

## 组件设计

### ExpandCollapseButtons

```typescript
// 位置: frontend/src/components/menu/ExpandCollapseButtons.vue
interface Props {
  disabled?: boolean
}

interface Emits {
  (e: 'expandAll'): void
  (e: 'collapseAll'): void
}
```

### CopyMenuDialog

```typescript
// 位置: frontend/src/components/menu/CopyMenuDialog.vue
interface Props {
  open: boolean
  menuId: string
  menuName: string
  parentOptions: Array<{ id: string; label: string }>
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm', targetParentId: string): void
}
```

### QuickCreateDialog

```typescript
// 位置: frontend/src/components/menu/QuickCreateDialog.vue
interface Props {
  open: boolean
  parentOptions: Array<{ id: string; label: string }>
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm', data: QuickCreateData): void
}

interface QuickCreateData {
  parentId: string
  type: string
  name: string
}
```

---

## API 设计

### 前端 API

```typescript
// frontend/src/api/menu.ts 新增

// 复制菜单
export function copyMenu(id: string, targetParentId: string) {
  return post<Menu>(`/sys/menus/${id}/copy`, { targetParentId })
}
```

### 后端 API

```java
// backend/src/main/java/com/adminplus/controller/MenuController.java 新增

@PostMapping("/{id}/copy")
@PreAuthorize("hasAuthority('menu:add')")
public ApiResponse<Menu> copyMenu(
    @PathVariable String id,
    @RequestBody @Valid CopyMenuRequest request
) {
    Menu copied = menuService.copyMenu(id, request.getTargetParentId());
    return ApiResponse.success(copied);
}
```

```java
// backend/src/main/java/com/adminplus/dto/CopyMenuRequest.java 新增

public record CopyMenuRequest(
    @NotBlank(message = "目标父级ID不能为空")
    String targetParentId
) {}
```

```java
// backend/src/main/java/com/adminplus/service/MenuService.java 新增

/**
 * 复制菜单到指定父级
 * @param id 原菜单ID
 * @param targetParentId 目标父级ID，null表示顶级
 * @return 复制后的菜单
 */
Menu copyMenu(String id, String targetParentId);
```

---

## 数据库

无需变更，复用现有表结构。

---

## 权限

复制操作使用 `menu:add` 权限，与新增菜单一致。

---

## 测试计划

### 前端测试
- [ ] 全部展开/收起按钮功能测试
- [ ] 复制对话框打开/关闭测试
- [ ] 快速创建对话框连续创建测试
- [ ] 按钮权限控制测试

### 后端测试
- [ ] 复制菜单到不同父级
- [ ] 复制时名称后缀生成
- [ ] 复制不存在的菜单
- [ ] 复制到自己的子级（应拒绝）

---

## 实施步骤

1. 后端实现复制接口
2. 前端添加 API 调用
3. 创建三个新组件
4. Menu.vue 集成新功能
5. 测试验证
