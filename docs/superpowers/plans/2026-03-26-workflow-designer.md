# Workflow Designer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a workflow designer page that allows users to create, edit, and visualize workflow definitions with their nodes.

**Architecture:**
- Frontend: New `WorkflowDesigner.vue` page using existing `WorkflowVisualizer.vue` component for visualization
- Backend: Already has complete API for workflow definitions and nodes CRUD
- Menu: Add "流程设计" entry in the workflow menu group

**Tech Stack:** Vue 3 + TypeScript, Vue Flow, shadcn-vue, existing backend APIs

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `frontend/src/views/workflow/WorkflowDesigner.vue` | Create | Main designer page with definition list + visual editor |
| `frontend/src/api/workflow.ts` | Modify | Add definition/node CRUD APIs |
| `frontend/src/types/index.ts` | Modify | Add request types for definition/node |
| `frontend/src/layout/sidebar.ts` | Modify | Add menu entry |
| `frontend/src/views/workflow/WorkflowVisualizer.vue` | Modify | Use real API instead of mock data |
| `backend/.../runner/DataInitializationRunner.java` | Modify | Add menu data for designer |

---

### Task 1: Add TypeScript Types for Workflow Definition and Node Requests

**Files:**
- Modify: `frontend/src/types/index.ts:330-340`

- [ ] **Step 1: Add request types after WorkflowNode interface**

```typescript
// Add after WorkflowNode interface (around line 330)

export interface WorkflowDefinitionReq {
  definitionName: string
  definitionKey: string
  category?: string
  description?: string
  status: number
  formConfig?: string
}

export interface WorkflowNodeReq {
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: 'user' | 'role' | 'dept' | 'leader'
  approverId?: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description?: string
}
```

- [ ] **Step 2: Verify TypeScript compilation**

Run: `cd frontend && npm run build`
Expected: Build succeeds without type errors

- [ ] **Step 3: Commit**

```bash
git add frontend/src/types/index.ts
git commit -m "feat: add WorkflowDefinitionReq and WorkflowNodeReq types"
```

---

### Task 2: Add Workflow Definition and Node API Functions

**Files:**
- Modify: `frontend/src/api/workflow.ts`

- [ ] **Step 1: Import new types and add API functions**

Add imports at top:
```typescript
import type {
  WorkflowApproval,
  WorkflowDefinition,
  WorkflowDefinitionReq,
  WorkflowDetail,
  WorkflowInstance,
  WorkflowNode,
  WorkflowNodeReq
} from '@/types'
```

Add after `withdrawWorkflow` function:
```typescript
// ========== Workflow Definition Management ==========

export function createWorkflowDefinition(data: WorkflowDefinitionReq) {
  return post<WorkflowDefinition>('/workflow/definitions', data)
}

export function updateWorkflowDefinition(id: string, data: WorkflowDefinitionReq) {
  return post<WorkflowDefinition>(`/workflow/definitions/${id}`, data, { methodOverride: 'PUT' })
}

export function deleteWorkflowDefinition(id: string) {
  return post<void>(`/workflow/definitions/${id}`, {}, { methodOverride: 'DELETE' })
}

export function getWorkflowDefinition(id: string) {
  return get<WorkflowDefinition>(`/workflow/definitions/${id}`)
}

// ========== Workflow Node Management ==========

export function getWorkflowNodes(definitionId: string) {
  return get<WorkflowNode[]>(`/workflow/definitions/${definitionId}/nodes`)
}

export function createWorkflowNode(definitionId: string, data: WorkflowNodeReq) {
  return post<WorkflowNode>(`/workflow/definitions/${definitionId}/nodes`, data)
}

export function updateWorkflowNode(nodeId: string, data: WorkflowNodeReq) {
  return post<WorkflowNode>(`/workflow/definitions/nodes/${nodeId}`, data, { methodOverride: 'PUT' })
}

export function deleteWorkflowNode(nodeId: string) {
  return post<void>(`/workflow/definitions/nodes/${nodeId}`, {}, { methodOverride: 'DELETE' })
}
```

- [ ] **Step 2: Update request utility for method override**

Check if `post` function supports `methodOverride` option. If not, use `put` and `del` functions:
```typescript
// Alternative approach if methodOverride not supported:
import { get, post, put, del } from '@/utils/request'

export function updateWorkflowDefinition(id: string, data: WorkflowDefinitionReq) {
  return put<WorkflowDefinition>(`/workflow/definitions/${id}`, data)
}

export function deleteWorkflowDefinition(id: string) {
  return del<void>(`/workflow/definitions/${id}`)
}
```

- [ ] **Step 3: Verify build**

Run: `cd frontend && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/workflow.ts
git commit -m "feat: add workflow definition and node CRUD API functions"
```

---

### Task 3: Update WorkflowVisualizer to Use Real API

**Files:**
- Modify: `frontend/src/views/workflow/WorkflowVisualizer.vue`

- [ ] **Step 1: Add API import**

```typescript
import { getWorkflowDefinition, getWorkflowNodes } from '@/api'
```

- [ ] **Step 2: Replace mock data with real API call**

Replace the `loadWorkflowDefinition` function:
```typescript
async function loadWorkflowDefinition() {
  if (!props.definitionId && !props.instanceId) return

  loading.value = true
  try {
    if (props.definitionId) {
      // Load definition and nodes from API
      const [defRes, nodesRes] = await Promise.all([
        getWorkflowDefinition(props.definitionId),
        getWorkflowNodes(props.definitionId)
      ])

      definition.value = {
        id: defRes.data.id,
        name: defRes.data.definitionName,
        key: defRes.data.definitionKey,
        category: defRes.data.category || '',
        description: defRes.data.description,
        nodes: nodesRes.data.map(node => ({
          id: node.id,
          name: node.nodeName,
          code: node.nodeCode,
          order: node.nodeOrder,
          approverType: node.approverType,
          approverId: node.approverId,
          isCounterSign: node.isCounterSign,
          autoPassSameUser: node.autoPassSameUser
        }))
      }
    }
    // Note: instanceId loading can be added later if needed

    emit('loaded', definition.value!)

    setTimeout(() => {
      fitView({ padding: 0.2 })
    }, 100)
  } catch (error) {
    console.error('Failed to load workflow definition:', error)
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 3: Verify build**

Run: `cd frontend && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/workflow/WorkflowVisualizer.vue
git commit -m "feat: integrate real API in WorkflowVisualizer"
```

---

### Task 4: Create WorkflowDesigner.vue Page

**Files:**
- Create: `frontend/src/views/workflow/WorkflowDesigner.vue`

- [ ] **Step 1: Create the designer page**

```vue
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea
} from '@/components/ui'
import {
  getWorkflowDefinitions,
  getWorkflowNodes,
  createWorkflowDefinition,
  updateWorkflowDefinition,
  deleteWorkflowDefinition,
  createWorkflowNode,
  updateWorkflowNode,
  deleteWorkflowNode
} from '@/api'
import type { WorkflowDefinition, WorkflowNode, WorkflowDefinitionReq, WorkflowNodeReq } from '@/types'
import { toast } from 'vue-sonner'
import { Plus, Pencil, Trash2, Settings, ArrowLeft } from 'lucide-vue-next'
import WorkflowVisualizer from './WorkflowVisualizer.vue'

// State
const loading = ref(false)
const definitions = ref<WorkflowDefinition[]>([])
const selectedDefinition = ref<WorkflowDefinition | null>(null)
const nodes = ref<WorkflowNode[]>([])

// Definition dialog
const definitionDialogOpen = ref(false)
const definitionDialogMode = ref<'create' | 'edit'>('create')
const definitionForm = ref<WorkflowDefinitionReq>({
  definitionName: '',
  definitionKey: '',
  category: '',
  description: '',
  status: 1
})

// Node dialog
const nodeDialogOpen = ref(false)
const nodeDialogMode = ref<'create' | 'edit'>('create')
const editingNodeId = ref<string | null>(null)
const nodeForm = ref<WorkflowNodeReq>({
  nodeName: '',
  nodeCode: '',
  nodeOrder: 1,
  approverType: 'role',
  approverId: '',
  isCounterSign: false,
  autoPassSameUser: false,
  description: ''
})

const canManageWorkflow = computed(() => true) // TODO: check permission

// Format
const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

// Fetch definitions
const fetchDefinitions = async () => {
  loading.value = true
  try {
    const res = await getWorkflowDefinitions()
    definitions.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取流程模板失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

// Select definition and load nodes
const selectDefinition = async (definition: WorkflowDefinition) => {
  selectedDefinition.value = definition
  try {
    const res = await getWorkflowNodes(definition.id)
    nodes.value = res.data.sort((a, b) => a.nodeOrder - b.nodeOrder)
  } catch (error) {
    toast.error('加载节点失败')
    nodes.value = []
  }
}

const backToList = () => {
  selectedDefinition.value = null
  nodes.value = []
}

// Definition CRUD
const openCreateDefinitionDialog = () => {
  definitionDialogMode.value = 'create'
  definitionForm.value = {
    definitionName: '',
    definitionKey: '',
    category: '',
    description: '',
    status: 1
  }
  definitionDialogOpen.value = true
}

const openEditDefinitionDialog = (definition: WorkflowDefinition) => {
  definitionDialogMode.value = 'edit'
  definitionForm.value = {
    definitionName: definition.definitionName,
    definitionKey: definition.definitionKey,
    category: definition.category || '',
    description: definition.description || '',
    status: definition.status
  }
  selectedDefinition.value = definition
  definitionDialogOpen.value = true
}

const handleSaveDefinition = async () => {
  if (!definitionForm.value.definitionName.trim()) {
    toast.warning('请输入流程名称')
    return
  }
  if (!definitionForm.value.definitionKey.trim()) {
    toast.warning('请输入流程标识')
    return
  }

  try {
    if (definitionDialogMode.value === 'create') {
      await createWorkflowDefinition(definitionForm.value)
      toast.success('创建成功')
    } else if (selectedDefinition.value) {
      await updateWorkflowDefinition(selectedDefinition.value.id, definitionForm.value)
      toast.success('更新成功')
    }
    definitionDialogOpen.value = false
    fetchDefinitions()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    toast.error(message)
  }
}

const handleDeleteDefinition = async (definition: WorkflowDefinition) => {
  if (!confirm(`确定要删除流程"${definition.definitionName}"吗？`)) return

  try {
    await deleteWorkflowDefinition(definition.id)
    toast.success('删除成功')
    fetchDefinitions()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除失败'
    toast.error(message)
  }
}

// Node CRUD
const openCreateNodeDialog = () => {
  if (!selectedDefinition.value) return
  nodeDialogMode.value = 'create'
  editingNodeId.value = null
  nodeForm.value = {
    nodeName: '',
    nodeCode: '',
    nodeOrder: nodes.value.length + 1,
    approverType: 'role',
    approverId: '',
    isCounterSign: false,
    autoPassSameUser: false,
    description: ''
  }
  nodeDialogOpen.value = true
}

const openEditNodeDialog = (node: WorkflowNode) => {
  nodeDialogMode.value = 'edit'
  editingNodeId.value = node.id
  nodeForm.value = {
    nodeName: node.nodeName,
    nodeCode: node.nodeCode,
    nodeOrder: node.nodeOrder,
    approverType: node.approverType as 'user' | 'role' | 'dept' | 'leader',
    approverId: node.approverId || '',
    isCounterSign: node.isCounterSign,
    autoPassSameUser: node.autoPassSameUser,
    description: node.description || ''
  }
  nodeDialogOpen.value = true
}

const handleSaveNode = async () => {
  if (!selectedDefinition.value) return
  if (!nodeForm.value.nodeName.trim()) {
    toast.warning('请输入节点名称')
    return
  }
  if (!nodeForm.value.nodeCode.trim()) {
    toast.warning('请输入节点编码')
    return
  }

  try {
    if (nodeDialogMode.value === 'create') {
      await createWorkflowNode(selectedDefinition.value.id, nodeForm.value)
      toast.success('节点添加成功')
    } else if (editingNodeId.value) {
      await updateWorkflowNode(editingNodeId.value, nodeForm.value)
      toast.success('节点更新成功')
    }
    nodeDialogOpen.value = false
    selectDefinition(selectedDefinition.value)
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    toast.error(message)
  }
}

const handleDeleteNode = async (node: WorkflowNode) => {
  if (!confirm(`确定要删除节点"${node.nodeName}"吗？`)) return

  try {
    await deleteWorkflowNode(node.id)
    toast.success('节点删除成功')
    if (selectedDefinition.value) {
      selectDefinition(selectedDefinition.value)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除失败'
    toast.error(message)
  }
}

onMounted(fetchDefinitions)
</script>

<template>
  <div class="space-y-4">
    <!-- Definition List View -->
    <Card v-if="!selectedDefinition">
      <CardHeader class="flex flex-row items-center justify-between space-y-0">
        <CardTitle>流程设计</CardTitle>
        <Button v-if="canManageWorkflow" @click="openCreateDefinitionDialog">
          <Plus class="mr-2 h-4 w-4" />
          新建流程
        </Button>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>流程名称</TableHead>
              <TableHead>标识</TableHead>
              <TableHead>分类</TableHead>
              <TableHead>节点数</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>更新时间</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="loading">
              <TableCell colspan="7" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
            </TableRow>
            <TableRow v-else-if="definitions.length === 0">
              <TableCell colspan="7" class="h-24 text-center text-muted-foreground">暂无流程模板</TableCell>
            </TableRow>
            <TableRow v-for="definition in definitions" :key="definition.id">
              <TableCell class="font-medium">
                <div>{{ definition.definitionName }}</div>
                <div class="text-xs text-muted-foreground">{{ definition.description || '暂无描述' }}</div>
              </TableCell>
              <TableCell><code class="text-xs bg-muted px-1 rounded">{{ definition.definitionKey }}</code></TableCell>
              <TableCell>{{ definition.category || '-' }}</TableCell>
              <TableCell>{{ definition.nodeCount || 0 }}</TableCell>
              <TableCell>
                <Badge :variant="definition.status === 1 ? 'default' : 'secondary'">
                  {{ definition.status === 1 ? '启用' : '停用' }}
                </Badge>
              </TableCell>
              <TableCell>{{ formatDateTime(definition.updateTime) }}</TableCell>
              <TableCell class="text-right">
                <Button size="sm" variant="ghost" @click="selectDefinition(definition)">
                  <Settings class="mr-1 h-3 w-3" />
                  设计
                </Button>
                <Button size="sm" variant="ghost" @click="openEditDefinitionDialog(definition)">
                  <Pencil class="mr-1 h-3 w-3" />
                  编辑
                </Button>
                <Button size="sm" variant="ghost" @click="handleDeleteDefinition(definition)">
                  <Trash2 class="mr-1 h-3 w-3" />
                  删除
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <!-- Designer View -->
    <div v-else class="space-y-4">
      <!-- Header -->
      <Card>
        <CardContent class="flex items-center justify-between p-4">
          <div class="flex items-center gap-4">
            <Button variant="ghost" size="sm" @click="backToList">
              <ArrowLeft class="mr-1 h-4 w-4" />
              返回
            </Button>
            <div>
              <h2 class="font-semibold">{{ selectedDefinition.definitionName }}</h2>
              <p class="text-sm text-muted-foreground">{{ selectedDefinition.description || '暂无描述' }}</p>
            </div>
          </div>
          <Button @click="openCreateNodeDialog">
            <Plus class="mr-2 h-4 w-4" />
            添加节点
          </Button>
        </CardContent>
      </Card>

      <!-- Visualizer and Node List -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <!-- Flow Chart -->
        <Card class="lg:col-span-2">
          <CardHeader>
            <CardTitle>流程图</CardTitle>
          </CardHeader>
          <CardContent>
            <WorkflowVisualizer
              :definition-id="selectedDefinition.id"
              :readonly="true"
              class="h-[400px]"
            />
          </CardContent>
        </Card>

        <!-- Node List -->
        <Card>
          <CardHeader>
            <CardTitle>节点列表</CardTitle>
          </CardHeader>
          <CardContent class="p-0">
            <div class="divide-y">
              <div
                v-for="node in nodes"
                :key="node.id"
                class="flex items-center justify-between p-4 hover:bg-muted/50"
              >
                <div>
                  <div class="font-medium">{{ node.nodeName }}</div>
                  <div class="text-xs text-muted-foreground">
                    顺序: {{ node.nodeOrder }} |
                    类型: {{ node.approverType }}
                    <span v-if="node.isCounterSign"> | 会签</span>
                  </div>
                </div>
                <div class="flex gap-1">
                  <Button size="sm" variant="ghost" @click="openEditNodeDialog(node)">
                    <Pencil class="h-3 w-3" />
                  </Button>
                  <Button size="sm" variant="ghost" @click="handleDeleteNode(node)">
                    <Trash2 class="h-3 w-3" />
                  </Button>
                </div>
              </div>
              <div v-if="nodes.length === 0" class="p-8 text-center text-muted-foreground">
                暂无节点，点击"添加节点"开始设计流程
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>

    <!-- Definition Dialog -->
    <Dialog v-model:open="definitionDialogOpen">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ definitionDialogMode === 'create' ? '新建流程' : '编辑流程' }}</DialogTitle>
        </DialogHeader>
        <div class="space-y-4">
          <div class="space-y-2">
            <Label>流程名称 <span class="text-destructive">*</span></Label>
            <Input v-model="definitionForm.definitionName" placeholder="例如：费用报销流程" />
          </div>
          <div class="space-y-2">
            <Label>流程标识 <span class="text-destructive">*</span></Label>
            <Input v-model="definitionForm.definitionKey" placeholder="例如：expense-reimbursement" />
          </div>
          <div class="space-y-2">
            <Label>分类</Label>
            <Input v-model="definitionForm.category" placeholder="例如：财务" />
          </div>
          <div class="space-y-2">
            <Label>描述</Label>
            <Textarea v-model="definitionForm.description" placeholder="流程用途说明" />
          </div>
          <div class="space-y-2">
            <Label>状态</Label>
            <Select v-model="definitionForm.status">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem :value="1">启用</SelectItem>
                <SelectItem :value="0">停用</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="definitionDialogOpen = false">取消</Button>
          <Button @click="handleSaveDefinition">保存</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Node Dialog -->
    <Dialog v-model:open="nodeDialogOpen">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ nodeDialogMode === 'create' ? '添加节点' : '编辑节点' }}</DialogTitle>
        </DialogHeader>
        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>节点名称 <span class="text-destructive">*</span></Label>
              <Input v-model="nodeForm.nodeName" placeholder="例如：部门经理审批" />
            </div>
            <div class="space-y-2">
              <Label>节点编码 <span class="text-destructive">*</span></Label>
              <Input v-model="nodeForm.nodeCode" placeholder="例如：manager_approve" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>审批顺序</Label>
              <Input v-model.number="nodeForm.nodeOrder" type="number" min="1" />
            </div>
            <div class="space-y-2">
              <Label>审批类型</Label>
              <Select v-model="nodeForm.approverType">
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="role">角色</SelectItem>
                  <SelectItem value="user">指定用户</SelectItem>
                  <SelectItem value="dept">部门</SelectItem>
                  <SelectItem value="leader">部门领导</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div class="space-y-2">
            <Label>审批人ID</Label>
            <Input v-model="nodeForm.approverId" placeholder="根据审批类型填写角色ID/用户ID/部门ID" />
          </div>
          <div class="flex gap-6">
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="nodeForm.isCounterSign" class="rounded" />
              <span class="text-sm">会签（所有人都要审批）</span>
            </label>
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="nodeForm.autoPassSameUser" class="rounded" />
              <span class="text-sm">自动通过（发起人=审批人时）</span>
            </label>
          </div>
          <div class="space-y-2">
            <Label>描述</Label>
            <Textarea v-model="nodeForm.description" placeholder="节点说明" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="nodeDialogOpen = false">取消</Button>
          <Button @click="handleSaveNode">保存</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
```

- [ ] **Step 2: Verify build**

Run: `cd frontend && npm run build`
Expected: Build succeeds

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/workflow/WorkflowDesigner.vue
git commit -m "feat: create WorkflowDesigner page with definition and node management"
```

---

### Task 5: Add Menu Entry for Workflow Designer

**Files:**
- Modify: `frontend/src/layout/sidebar.ts`
- Modify: `backend/src/main/java/com/adminplus/runner/DataInitializationRunner.java`

- [ ] **Step 1: Add sidebar menu entry in frontend**

Add to `staticMenus` array (after workflow/definitions):
```typescript
{ path: '/workflow/designer', icon: 'Settings', label: '流程设计' },
```

Add to `staticSidebarTree` workflow children:
```typescript
{ kind: 'item', id: 'workflow-designer', path: '/workflow/designer', label: '流程设计', icon: 'Settings' },
```

- [ ] **Step 2: Add menu data in backend initialization**

In `DataInitializationRunner.java`, add to workflow menu data:
```java
new Object[]{"M44", "M4", 1, "流程设计", "/workflow/designer", "workflow/WorkflowDesigner", "workflow:design", "Settings", 4, 1, 1},
```

Update the `getMenuNameByTempId` method:
```java
case "M44" -> "流程设计";
```

Add to role menu assignments for admin/manager/developer as needed.

- [ ] **Step 3: Verify builds**

Run: `cd frontend && npm run build && cd ../backend && mvn compile -q`
Expected: Both succeed

- [ ] **Step 4: Commit**

```bash
git add frontend/src/layout/sidebar.ts backend/src/main/java/com/adminplus/runner/DataInitializationRunner.java
git commit -m "feat: add workflow designer menu entry"
```

---

### Task 6: Update Request Utility for PUT/DELETE Methods

**Files:**
- Modify: `frontend/src/utils/request.ts` (if needed)

- [ ] **Step 1: Check if put and del functions exist**

Run: `grep -n "export function put\|export function del" frontend/src/utils/request.ts`

If they don't exist, add them:
```typescript
export function put<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request({ url, method: 'PUT', data })
}

export function del<T>(url: string): Promise<ApiResponse<T>> {
  return request({ url, method: 'DELETE' })
}
```

- [ ] **Step 2: Update API imports**

Update `frontend/src/api/workflow.ts` to import `put` and `del`:
```typescript
import { get, post, put, del } from '@/utils/request'
```

- [ ] **Step 3: Verify build**

Run: `cd frontend && npm run build`
Expected: Build succeeds

- [ ] **Step 4: Commit** (if changes were made)

```bash
git add frontend/src/utils/request.ts frontend/src/api/workflow.ts
git commit -m "feat: add put and del request methods"
```

---

### Task 7: Add nodeCount to WorkflowDefinition Response

**Files:**
- Modify: `backend/src/main/java/com/adminplus/pojo/dto/resp/WorkflowDefinitionResp.java`
- Modify: `backend/src/main/java/com/adminplus/service/impl/WorkflowDefinitionServiceImpl.java` (if needed)

- [ ] **Step 1: Add nodeCount field to response DTO**

```java
public record WorkflowDefinitionResp(
    String id,
    String definitionName,
    String definitionKey,
    String category,
    String description,
    Integer status,
    Integer version,
    String formConfig,
    Long nodeCount,  // Add this field
    String createTime,
    String updateTime
) {}
```

- [ ] **Step 2: Update service to populate nodeCount**

If using manual mapping, add node count query. If using projection, ensure it's included.

- [ ] **Step 3: Update frontend type**

In `frontend/src/types/index.ts`, add to `WorkflowDefinition`:
```typescript
nodeCount?: number
```

- [ ] **Step 4: Verify builds**

Run: `cd backend && mvn compile -q && cd ../frontend && npm run build`
Expected: Both succeed

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/adminplus/pojo/dto/resp/WorkflowDefinitionResp.java frontend/src/types/index.ts
git commit -m "feat: add nodeCount to WorkflowDefinition response"
```

---

### Task 8: Final Integration Test and Cleanup

**Files:**
- All modified files

- [ ] **Step 1: Full build test**

Run: `cd backend && mvn clean package -DskipTests && cd ../frontend && npm run build`
Expected: Both succeed

- [ ] **Step 2: Manual testing checklist**

1. Start backend and frontend
2. Login as admin
3. Navigate to "工作流管理" > "流程设计"
4. Create a new workflow definition
5. Add nodes to the definition
6. View the flow chart
7. Edit a node
8. Delete a node
9. Delete the workflow definition

- [ ] **Step 3: Final commit with any fixes**

```bash
git add -A
git commit -m "fix: workflow designer integration fixes"
```

---

## Summary

This plan implements a complete workflow designer feature:

1. **Types** - Request/response types for API calls
2. **API Functions** - CRUD operations for definitions and nodes
3. **WorkflowVisualizer** - Updated to use real API
4. **WorkflowDesigner Page** - Full UI for designing workflows
5. **Menu Entry** - Added to sidebar and backend initialization
6. **Request Utils** - PUT/DELETE method support
7. **nodeCount** - Display node count in definition list
8. **Integration Test** - Final verification

**Estimated Time:** 2-3 hours

**Dependencies:** None (all backend APIs already exist)