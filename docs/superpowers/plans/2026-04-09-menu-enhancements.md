# 菜单管理人性化操作增强实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为菜单管理模块添加全部展开/收起、菜单复制、快速创建功能

**Architecture:** 前端复用现有 `useTreeData` composable，后端新增复制接口。前后端分离开发，前端组件化设计。

**Tech Stack:** Vue 3, TypeScript, Spring Boot 3.5, JPA

---

## File Structure

```
backend/
  src/main/java/com/adminplus/
    controller/
      MenuController.java          [MODIFY] 新增复制接口
    dto/req/
      CopyMenuRequest.java         [CREATE] 复制菜单请求DTO
    service/
      MenuService.java             [MODIFY] 新增复制方法签名
    service/impl/
      MenuServiceImpl.java         [MODIFY] 实现复制逻辑

frontend/
  src/
    api/menu.ts                    [MODIFY] 新增复制API
    components/menu/
      CopyMenuDialog.vue           [CREATE] 复制菜单对话框
      QuickCreateDialog.vue        [CREATE] 快速创建对话框
    views/system/
      Menu.vue                     [MODIFY] 集成新功能
```

---

## Task 1: 后端 - 创建复制菜单请求DTO

**Files:**
- Create: `backend/src/main/java/com/adminplus/pojo/dto/req/CopyMenuRequest.java`

- [ ] **Step 1: 创建 CopyMenuRequest record 类**

```java
package com.adminplus.pojo.dto.req;

import jakarta.validation.constraints.NotBlank;

/**
 * 复制菜单请求 DTO
 *
 * @author AdminPlus
 * @since 2026-04-09
 */
public record CopyMenuRequest(

        @NotBlank(message = "目标父级ID不能为空")
        String targetParentId
) {
}
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

Expected: 无错误输出

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/adminplus/pojo/dto/req/CopyMenuRequest.java
git commit -m "feat: add CopyMenuRequest DTO"
```

---

## Task 2: 后端 - Service接口新增复制方法

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/MenuService.java`

- [ ] **Step 1: 在 MenuService 接口添加 copyMenu 方法签名**

在文件末尾、最后一个方法后添加：

```java
    /**
     * 复制菜单到指定父级
     *
     * @param id 原菜单ID
     * @param targetParentId 目标父级ID，"0"表示顶级
     * @return 复制后的菜单
     */
    MenuResp copyMenu(String id, String targetParentId);
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

Expected: 编译失败，提示 `MenuServiceImpl` 需要实现该方法

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/adminplus/service/MenuService.java
git commit -m "feat: add copyMenu method signature to MenuService"
```

---

## Task 3: 后端 - ServiceImpl实现复制逻辑

**Files:**
- Modify: `backend/src/main/java/com/adminplus/service/impl/MenuServiceImpl.java`

- [ ] **Step 1: 在 MenuServiceImpl 类中添加 copyMenu 实现**

在文件末尾、`toResp` 方法之前添加：

```java
    @Override
    @Transactional
    @CacheEvict(value = {"userMenus", "menuTree", "allPermissions"}, allEntries = true)
    public MenuResp copyMenu(String id, String targetParentId) {
        // 获取原菜单
        MenuEntity sourceMenu = EntityHelper.findByIdOrThrow(menuRepository::findById, id, "菜单不存在");

        // 不能复制到自己的子孙节点（防止循环引用）
        if (!targetParentId.equals("0") && isChildMenu(id, targetParentId)) {
            throw new BizException("不能将菜单复制到自己的子菜单下");
        }

        // 创建副本
        MenuEntity copiedMenu = new MenuEntity();
        copiedMenu.setType(sourceMenu.getType());
        copiedMenu.setName(sourceMenu.getName() + " (副本)");
        copiedMenu.setPath(sourceMenu.getPath());
        copiedMenu.setComponent(sourceMenu.getComponent());
        copiedMenu.setPermKey(sourceMenu.getPermKey());
        copiedMenu.setIcon(sourceMenu.getIcon());
        copiedMenu.setVisible(sourceMenu.getVisible());
        copiedMenu.setStatus(sourceMenu.getStatus());

        // 计算排序值：目标父级下最大值 + 10
        int maxSortOrder = 0;
        if (targetParentId.equals("0")) {
            // 顶级菜单最大排序值
            List<MenuEntity> topMenus = menuRepository.findAllByOrderBySortOrderAsc();
            maxSortOrder = topMenus.stream()
                    .filter(m -> m.getParent() == null)
                    .mapToInt(MenuEntity::getSortOrder)
                    .max()
                    .orElse(0);
        } else {
            // 指定父级下最大排序值
            MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, targetParentId, "父菜单不存在");
            maxSortOrder = parent.getChildren().stream()
                    .mapToInt(MenuEntity::getSortOrder)
                    .max()
                    .orElse(0);
        }
        copiedMenu.setSortOrder(maxSortOrder + 10);

        // 设置父菜单关系
        if (!targetParentId.equals("0")) {
            MenuEntity parent = EntityHelper.findByIdOrThrow(menuRepository::findById, targetParentId, "父菜单不存在");
            copiedMenu.setParent(parent);
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            copiedMenu.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            copiedMenu.setAncestors("0,");
        }

        copiedMenu = menuRepository.save(copiedMenu);

        // 记录审计日志
        logService.log(LogEntry.operation("菜单管理", OperationType.CREATE.getCode(),
                "复制菜单: " + sourceMenu.getName() + " -> " + copiedMenu.getName()));

        return toResp(copiedMenu);
    }
```

- [ ] **Step 2: 验证编译**

```bash
cd backend && mvn compile -q
```

Expected: 无错误输出

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/adminplus/service/impl/MenuServiceImpl.java
git commit -m "feat: implement copyMenu logic in MenuServiceImpl"
```

---

## Task 4: 后端 - Controller新增复制接口

**Files:**
- Modify: `backend/src/main/java/com/adminplus/controller/MenuController.java`

- [ ] **Step 1: 在类开头 import 部分添加 CopyMenuRequest 导入**

在 `MenuUpdateReq` 导入后添加：

```java
import com.adminplus.pojo.dto.req.CopyMenuRequest;
```

- [ ] **Step 2: 在 Controller 类中添加 copyMenu 端点**

在 `getUserMenuTree` 方法后、类结束前添加：

```java
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制菜单")
    @OperationLog(module = "菜单管理", operationType = 2, description = "复制菜单 {#id}")
    @PreAuthorize("hasAuthority('menu:add')")
    public ApiResponse<MenuResp> copyMenu(
            @PathVariable String id,
            @Valid @RequestBody CopyMenuRequest request) {
        MenuResp menu = menuService.copyMenu(id, request.targetParentId());
        return ApiResponse.ok(menu);
    }
```

- [ ] **Step 3: 验证编译**

```bash
cd backend && mvn compile -q
```

Expected: 无错误输出

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/adminplus/controller/MenuController.java
git commit -m "feat: add copyMenu endpoint to MenuController"
```

---

## Task 5: 后端 - 复制功能单元测试

**Files:**
- Modify: `backend/src/test/java/com/adminplus/service/MenuServiceTest.java`

- [ ] **Step 1: 添加复制功能测试嵌套类**

在文件末尾、最后一个 `}` 前添加：

```java
    @Nested
    @DisplayName("copyMenu Tests")
    class CopyMenuTests {

        @Test
        @DisplayName("should copy menu to top level when targetParentId is 0")
        void copyMenu_WhenTargetIsTopLevel_ShouldCopySuccessfully() {
            // Given
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(parentMenu, testMenu));
            when(menuRepository.save(any(MenuEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MenuResp result = menuService.copyMenu("menu-001", "0");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Menu (副本)");
            assertThat(result.parentId()).isEqualTo("0");
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("should throw exception when copying to child menu")
        void copyMenu_WhenTargetIsChild_ShouldThrowException() {
            // Given - setup a child relationship
            MenuEntity childMenu = new MenuEntity();
            childMenu.setId("child-001");
            childMenu.setParent(testMenu);
            testMenu.setAncestors("0,parent-001,");

            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findById("child-001")).thenReturn(Optional.of(childMenu));

            // When & Then
            assertThatThrownBy(() -> menuService.copyMenu("menu-001", "child-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不能将菜单复制到自己的子菜单下");
        }

        @Test
        @DisplayName("should copy to specified parent when targetParentId is provided")
        void copyMenu_WhenTargetIsProvided_ShouldCopyToParent() {
            // Given
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findById("parent-001")).thenReturn(Optional.of(parentMenu));
            when(menuRepository.save(any(MenuEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MenuResp result = menuService.copyMenu("menu-001", "parent-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Menu (副本)");
            assertThat(result.parentId()).isEqualTo("parent-001");
            verify(menuRepository).save(any(MenuEntity.class));
        }
    }
```

- [ ] **Step 2: 运行测试**

```bash
cd backend && mvn test -Dtest=MenuServiceTest\$CopyMenuTests -q
```

Expected: 3 个测试全部通过

- [ ] **Step 3: 提交**

```bash
git add backend/src/test/java/com/adminplus/service/MenuServiceTest.java
git commit -m "test: add copyMenu unit tests"
```

---

## Task 6: 前端 - 添加复制API

**Files:**
- Modify: `frontend/src/api/menu.ts`

- [ ] **Step 1: 在文件末尾添加 copyMenu 函数**

在 `batchDelete` 函数后添加：

```typescript
// 复制菜单
export function copyMenu(id: string, targetParentId: string) {
  return post<Menu>(`/sys/menus/${id}/copy`, { targetParentId })
}
```

- [ ] **Step 2: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/api/menu.ts
git commit -m "feat: add copyMenu API function"
```

---

## Task 7: 前端 - 创建复制菜单对话框组件

**Files:**
- Create: `frontend/src/components/menu/CopyMenuDialog.vue`

- [ ] **Step 1: 创建 CopyMenuDialog.vue 组件**

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Button } from '@/components/ui/button'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { copyMenu } from '@/api'
import { toast } from 'vue-sonner'

interface ParentOption {
  id: string
  label: string
}

interface Props {
  open: boolean
  menuId: string
  menuName: string
  parentOptions: ParentOption[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { loading, run } = useAsyncAction('复制菜单失败')
const selectedParentId = ref<string>('0')

const dialogOpen = ref(false)

watch(() => props.open, (val) => {
  dialogOpen.value = val
  if (val) {
    selectedParentId.value = '0'
  }
})

watch(dialogOpen, (val) => {
  emit('update:open', val)
})

const handleConfirm = () => {
  run(async () => {
    await copyMenu(props.menuId, selectedParentId.value)
  }, {
    successMessage: '菜单复制成功',
    onSuccess: () => {
      dialogOpen.value = false
      emit('confirm')
    }
  })
}
</script>

<template>
  <Dialog v-model:open="dialogOpen">
    <DialogContent class="sm:max-w-[420px]">
      <DialogHeader>
        <DialogTitle>复制菜单</DialogTitle>
        <DialogDescription>
          选择目标位置，将「{{ menuName }}」复制为新菜单
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <div class="space-y-2">
          <Label>目标父级</Label>
          <Select v-model="selectedParentId">
            <SelectTrigger>
              <SelectValue placeholder="请选择目标父级" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="option in parentOptions"
                :key="option.id"
                :value="option.id"
              >
                {{ option.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="dialogOpen = false"
        >
          取消
        </Button>
        <Button
          :disabled="loading"
          @click="handleConfirm"
        >
          确认复制
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
```

- [ ] **Step 2: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/components/menu/CopyMenuDialog.vue
git commit -m "feat: add CopyMenuDialog component"
```

---

## Task 8: 前端 - 创建快速创建对话框组件

**Files:**
- Create: `frontend/src/components/menu/QuickCreateDialog.vue`

- [ ] **Step 1: 创建 QuickCreateDialog.vue 组件**

```vue
<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { createMenu } from '@/api'
import { toast } from 'vue-sonner'
import { useDict } from '@/composables/useDict'

interface ParentOption {
  id: string
  label: string
}

interface Props {
  open: boolean
  parentOptions: ParentOption[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { loading, run } = useAsyncAction('创建菜单失败')
const { getLabel: getMenuTypeLabel, options: menuTypeOptions } = useDict('menu_type')

interface QuickCreateForm {
  parentId: string
  type: string
  name: string
}

const form = reactive<QuickCreateForm>({
  parentId: '0',
  type: '0',
  name: ''
})

const dialogOpen = ref(false)

watch(() => props.open, (val) => {
  dialogOpen.value = val
  if (val) {
    resetForm()
  }
})

watch(dialogOpen, (val) => {
  emit('update:open', val)
})

const resetForm = () => {
  form.parentId = '0'
  form.type = '0'
  form.name = ''
}

const validateForm = () => {
  if (!form.name.trim()) {
    toast.warning('请输入菜单名称')
    return false
  }
  return true
}

const handleSubmit = () => {
  if (!validateForm()) return

  run(async () => {
    await createMenu({
      parentId: form.parentId === '0' ? undefined : form.parentId,
      type: Number(form.type),
      name: form.name.trim(),
      sortOrder: 0,
      visible: 1,
      status: 1
    })
  }, {
    successMessage: '菜单创建成功',
    onSuccess: () => {
      form.name = ''
      // 保持对话框打开，继续创建
    }
  })
}
</script>

<template>
  <Dialog v-model:open="dialogOpen">
    <DialogContent class="sm:max-w-[480px]">
      <DialogHeader>
        <DialogTitle>快速创建菜单</DialogTitle>
        <DialogDescription>
          配置父级、类型和名称，快速创建菜单
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label>父级菜单</Label>
            <Select v-model="form.parentId">
              <SelectTrigger>
                <SelectValue placeholder="请选择父级" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in parentOptions"
                  :key="option.id"
                  :value="option.id"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2">
            <Label>菜单类型</Label>
            <Select v-model="form.type">
              <SelectTrigger>
                <SelectValue placeholder="请选择类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in menuTypeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <Label>菜单名称 <span class="text-destructive">*</span></Label>
          <Input
            v-model="form.name"
            placeholder="请输入菜单名称"
            @keyup.enter="handleSubmit"
          />
        </div>

        <p class="text-xs text-muted-foreground">
          提示：其他字段将使用默认值（排序: 0，可见: 显示，状态: 正常），创建后可在编辑中修改
        </p>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="dialogOpen = false"
        >
          完成
        </Button>
        <Button
          :disabled="loading"
          @click="handleSubmit"
        >
          创建并继续
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
```

- [ ] **Step 2: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 3: 提交**

```bash
git add frontend/src/components/menu/QuickCreateDialog.vue
git commit -m "feat: add QuickCreateDialog component"
```

---

## Task 9: 前端 - Menu.vue集成全部展开/收起按钮

**Files:**
- Modify: `frontend/src/views/system/Menu.vue`

- [ ] **Step 1: 在 import 中添加展开/收起图标**

在 `ChevronRight` 导入后添加：

```typescript
import { ChevronDown, ChevronRight, ChevronsDown, ChevronsUp, Edit, Plus, Trash2 } from '@lucide/vue'
```

- [ ] **Step 2: 在 ListSearchBar 的 #actions 插槽中添加展开/收起按钮**

找到 `ListSearchBar` 组件的 `#actions` 插槽，在第一个按钮前添加：

```vue
      <template #actions>
        <Button
          variant="outline"
          @click="collapseAll"
        >
          <ChevronsUp class="mr-2 h-4 w-4" />
          全部收起
        </Button>
        <Button
          variant="outline"
          @click="expandAll"
        >
          <ChevronsDown class="mr-2 h-4 w-4" />
          全部展开
        </Button>
```

- [ ] **Step 3: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 4: 提交**

```bash
git add frontend/src/views/system/Menu.vue
git commit -m "feat: add expand/collapse all buttons to Menu"
```

---

## Task 10: 前端 - Menu.vue集成复制功能

**Files:**
- Modify: `frontend/src/views/system/Menu.vue`

- [ ] **Step 1: 添加 Copy 组件导入和状态**

在 `script setup` 中的 `Trash2` 导入后添加 `Copy` 图标：

```typescript
import { Copy, ChevronDown, ChevronRight, ChevronsDown, ChevronsUp, Edit, Plus, Trash2 } from '@lucide/vue'
```

在 `useTreeData` 调用后添加复制对话框状态：

```typescript
// Copy dialog state
const copyDialogOpen = ref(false)
const copyMenuId = ref('')
const copyMenuName = ref('')
```

- [ ] **Step 2: 添加复制处理函数**

在 `handleBatchStatusChange` 函数后添加：

```typescript
const handleCopy = (id: string, name: string) => {
  copyMenuId.value = id
  copyMenuName.value = name
  copyDialogOpen.value = true
}

const handleCopyConfirm = () => {
  copyDialogOpen.value = false
  fetchData()
}
```

- [ ] **Step 3: 在操作列添加复制按钮**

找到操作列的按钮组，在"新增子菜单"按钮后添加：

```vue
                  <Button
                    v-if="canAddMenu"
                    size="sm"
                    variant="ghost"
                    @click="handleCopy(row.menu.id, row.menu.name)"
                  >
                    <Copy class="h-4 w-4" />
                  </Button>
```

- [ ] **Step 4: 在模板末尾添加 CopyMenuDialog 组件**

在 `ConfirmDialog` 组件后、最外层 `</div>` 前添加：

```vue
    <CopyMenuDialog
      v-if="canAddMenu"
      v-model:open="copyDialogOpen"
      :menu-id="copyMenuId"
      :menu-name="copyMenuName"
      :parent-options="parentOptions"
      @confirm="handleCopyConfirm"
    />
```

- [ ] **Step 5: 在 script setup 顶部添加组件导入**

在 `ConfirmDialog` 导入后添加：

```typescript
import { ConfirmDialog, EmptyState, ListSearchBar, StatusBadge } from '@/components/common'
import CopyMenuDialog from '@/components/menu/CopyMenuDialog.vue'
```

- [ ] **Step 6: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 7: 提交**

```bash
git add frontend/src/views/system/Menu.vue
git commit -m "feat: integrate copy menu feature into Menu"
```

---

## Task 11: 前端 - Menu.vue集成快速创建功能

**Files:**
- Modify: `frontend/src/views/system/Menu.vue`

- [ ] **Step 1: 添加快速创建对话框状态**

在复制对话框状态后添加：

```typescript
// Quick create dialog state
const quickCreateDialogOpen = ref(false)
```

- [ ] **Step 2: 添加快速创建处理函数**

在 `handleCopyConfirm` 函数后添加：

```typescript
const handleQuickCreate = () => {
  quickCreateDialogOpen.value = true
}

const handleQuickCreateConfirm = () => {
  fetchData()
}
```

- [ ] **Step 3: 在 ListSearchBar 的 #actions 插槽添加快速创建按钮**

在"新增菜单"按钮前添加：

```vue
        <Button
          v-if="canAddMenu"
          variant="outline"
          @click="handleQuickCreate"
        >
          <Plus class="mr-2 h-4 w-4" />
          快速创建
        </Button>
```

- [ ] **Step 4: 在模板末尾添加 QuickCreateDialog 组件**

在 `CopyMenuDialog` 组件后添加：

```vue
    <QuickCreateDialog
      v-if="canAddMenu"
      v-model:open="quickCreateDialogOpen"
      :parent-options="parentOptions"
      @confirm="handleQuickCreateConfirm"
    />
```

- [ ] **Step 5: 在 script setup 顶部添加组件导入**

在 `CopyMenuDialog` 导入后添加：

```typescript
import CopyMenuDialog from '@/components/menu/CopyMenuDialog.vue'
import QuickCreateDialog from '@/components/menu/QuickCreateDialog.vue'
```

- [ ] **Step 6: 验证类型检查**

```bash
cd frontend && npm run type-check --silent 2>/dev/null || npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 7: 提交**

```bash
git add frontend/src/views/system/Menu.vue
git commit -m "feat: integrate quick create feature into Menu"
```

---

## Task 12: 前端 - 创建组件目录

**Files:**
- Create: `frontend/src/components/menu/`

- [ ] **Step 1: 创建组件目录**

```bash
mkdir -p frontend/src/components/menu
```

- [ ] **Step 2: 验证目录创建**

```bash
ls -la frontend/src/components/menu/
```

Expected: 目录存在且为空

- [ ] **Step 3: 提交（空目录添加 .gitkeep）**

```bash
touch frontend/src/components/menu/.gitkeep
git add frontend/src/components/menu/.gitkeep
git commit -m "chore: add menu components directory"
```

---

## Task 13: 端到端测试验证

**Files:**
- No file changes

- [ ] **Step 1: 启动后端服务**

```bash
cd backend && mvn spring-boot:run &
```

Expected: 服务在 8081 端口启动成功

- [ ] **Step 2: 启动前端服务**

```bash
cd frontend && npm run dev &
```

Expected: 服务在 5173 端口启动成功

- [ ] **Step 3: 手动测试全部展开/收起**

1. 登录系统
2. 进入菜单管理页面
3. 点击"全部展开"按钮 → 所有菜单应展开
4. 点击"全部收起"按钮 → 所有菜单应收起

- [ ] **Step 4: 手动测试复制功能**

1. 点击某个菜单的复制按钮
2. 选择目标父级
3. 确认复制
4. 验证新菜单出现在目标位置，名称带" (副本)"后缀

- [ ] **Step 5: 手动测试快速创建功能**

1. 点击"快速创建"按钮
2. 选择父级、类型、输入名称
3. 点击"创建并继续"
4. 验证菜单创建成功，对话框保持打开
5. 继续创建第二个菜单
6. 点击"完成"关闭对话框

---

## Task 14: 后端集成测试

**Files:**
- Modify: `backend/src/test/java/com/adminplus/service/MenuServiceTest.java`

- [ ] **Step 1: 运行所有菜单测试**

```bash
cd backend && mvn test -Dtest=MenuServiceTest -q
```

Expected: 所有测试通过

- [ ] **Step 2: 提交测试通过记录**

```bash
git commit --allow-empty -m "test: all MenuService tests passing"
```

---

## Self-Review Summary

**Spec Coverage:**
- 全部展开/收起 → Task 9
- 菜单复制 → Tasks 1-5, 6-7, 10
- 快速创建 → Tasks 8, 11
- API设计 → Tasks 1-4, 6
- 权限控制 → Task 4 (使用 `menu:add`)

**Placeholder Check:** 无 TBD/TODO/实现占位符

**Type Consistency:**
- `CopyMenuRequest.targetParentId()` 一致使用
- `MenuResp` record 类型一致
- 前端 `Menu` 类型一致

**Edge Cases Handled:**
- 复制到自己的子菜单 → 抛出异常
- 复制时不存在的菜单 → EntityHelper 抛出异常
- 目标父级不存在 → EntityHelper 抛出异常
