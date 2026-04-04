<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
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
  deleteWorkflowNode,
  getEnabledFormTemplates,
  type FormTemplate
} from '@/api'
import type { WorkflowDefinition, WorkflowNode, WorkflowDefinitionReq, WorkflowNodeReq } from '@/types'
import { toast } from 'vue-sonner'

// WorkflowVisualizer 返回的定义类型（包含 nodes）
interface WorkflowDefinitionWithNodes extends WorkflowDefinition {
  nodes: WorkflowNode[]
}
import { useAsyncAction } from '@/composables/useAsyncAction'
import { Plus, Pencil, Trash2, Settings, ArrowLeft } from 'lucide-vue-next'
import WorkflowVisualizer from './WorkflowVisualizer.vue'
import WorkflowNodeProperties from '@/components/workflow/designer/WorkflowNodeProperties.vue'
import WorkflowFormConfigEditor from '@/components/workflow/designer/WorkflowFormConfigEditor.vue'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

type WorkflowNodeForm = Omit<WorkflowNodeReq, 'approverId' | 'description'> & {
  approverId: string
  description: string
}

// State
const { loading, run: runList } = useAsyncAction('获取流程定义失败')
const { loading: dialogLoading, run: runDialog } = useAsyncAction('操作失败')
const definitions = ref<WorkflowDefinition[]>([])
const nodes = ref<WorkflowNode[]>([])

// Form templates state
const formTemplates = ref<FormTemplate[]>([])
const selectedFormTemplateId = ref<string | null>(null)

// View state
const selectedDefinition = ref<WorkflowDefinition | null>(null)
const viewMode = ref<'list' | 'design'>('list')

// Dialog state
const definitionDialogOpen = ref(false)
const nodeDialogOpen = ref(false)
const isEditMode = ref(false)

// Forms
const definitionForm = ref<WorkflowDefinitionReq>({
  definitionName: '',
  definitionKey: '',
  category: '',
  description: '',
  status: 1,
  formConfig: ''
})

const nodeForm = ref<WorkflowNodeForm>({
  nodeName: '',
  nodeCode: '',
  nodeOrder: 1,
  approverType: 'user',
  approverId: '',
  isCounterSign: false,
  autoPassSameUser: false,
  description: ''
})

const editingDefinitionId = ref<string | null>(null)
const editingNodeId = ref<string | null>(null)

const userStore = useUserStore()
const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))

// Permissions
const canCreateDefinition = computed(() => permissionState.value.canCreateDefinition)
const canEditDefinition = computed(() => permissionState.value.canEditDefinition)
const canDeleteDefinition = computed(() => permissionState.value.canDeleteDefinition)

import { formatDateTime } from '@/utils/format'

// Data fetching
const fetchDefinitions = () => runList(async () => {
  const res = await getWorkflowDefinitions()
  definitions.value = res.data
})

// Fetch form templates
const fetchFormTemplates = async () => {
  try {
    const res = await getEnabledFormTemplates()
    formTemplates.value = res.data
  } catch {
    // Silent failure - form templates are optional
  }
}

// 保留 fetchNodes 用于节点 CRUD 操作后的刷新
const fetchNodes = (definitionId: string) => runList(
  async () => {
    const res = await getWorkflowNodes(definitionId)
    nodes.value = res.data
  },
  {
    errorMessage: '获取节点列表失败',
    onError: () => { nodes.value = [] }
  }
)

// Definition CRUD
const openDefinitionDialog = (definition?: WorkflowDefinition) => {
  if (definition) {
    isEditMode.value = true
    editingDefinitionId.value = definition.id
    definitionForm.value = {
      definitionName: definition.definitionName,
      definitionKey: definition.definitionKey,
      category: definition.category || '',
      description: definition.description || '',
      status: definition.status,
      formConfig: definition.formConfig || ''
    }
  } else {
    isEditMode.value = false
    editingDefinitionId.value = null
    definitionForm.value = {
      definitionName: '',
      definitionKey: '',
      category: '',
      description: '',
      status: 1,
      formConfig: ''
    }
  }
  // 重置表单模板选择
  selectedFormTemplateId.value = null
  // 加载表单模板数据
  fetchFormTemplates()
  definitionDialogOpen.value = true
}

// 关闭流程定义对话框
const closeDefinitionDialog = () => {
  definitionDialogOpen.value = false
  // 清理表单数据
  definitionForm.value = {
    definitionName: '',
    definitionKey: '',
    category: '',
    description: '',
    status: 1,
    formConfig: ''
  }
  selectedFormTemplateId.value = null
}

const handleSaveDefinition = () => {
  if (!definitionForm.value.definitionName.trim()) {
    toast.warning('请输入流程名称')
    return
  }
  if (!definitionForm.value.definitionKey.trim()) {
    toast.warning('请输入流程标识')
    return
  }

  runDialog(async () => {
    if (isEditMode.value && editingDefinitionId.value) {
      await updateWorkflowDefinition(editingDefinitionId.value, definitionForm.value)
    } else {
      await createWorkflowDefinition(definitionForm.value)
    }
  }, {
    successMessage: isEditMode.value ? '更新流程定义成功' : '创建流程定义成功',
    errorMessage: '保存流程定义失败',
    onSuccess: () => {
      definitionDialogOpen.value = false
      fetchDefinitions()
    }
  })
}

// Import form config from template
const importFormTemplate = () => {
  if (!selectedFormTemplateId.value) {
    toast.warning('请选择表单模板')
    return
  }

  const template = formTemplates.value.find(t => t.id === selectedFormTemplateId.value)
  if (template) {
    definitionForm.value.formConfig = template.formConfig || JSON.stringify({ sections: [] })
    toast.success(`已导入表单模板: ${template.templateName}`)
    // 重置选择器
    selectedFormTemplateId.value = null
  }
}

const handleDeleteDefinition = (definition: WorkflowDefinition) => {
  if (!confirm(`确定要删除流程"${definition.definitionName}"吗？此操作将同时删除该流程的所有节点。`)) {
    return
  }
  runList(async () => {
    await deleteWorkflowDefinition(definition.id)
  }, {
    successMessage: '删除成功',
    errorMessage: '删除流程定义失败',
    onSuccess: () => fetchDefinitions()
  })
}

// Node CRUD
const openNodeDialog = (node?: WorkflowNode) => {
  if (node) {
    isEditMode.value = true
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
  } else {
    isEditMode.value = false
    editingNodeId.value = null
    nodeForm.value = {
      nodeName: '',
      nodeCode: '',
      nodeOrder: nodes.value.length + 1,
      approverType: 'user',
      approverId: '',
      isCounterSign: false,
      autoPassSameUser: false,
      description: ''
    }
  }
  nodeDialogOpen.value = true
}

const handleSaveNode = () => {
  if (!selectedDefinition.value) return

  if (!nodeForm.value.nodeName.trim()) {
    toast.warning('请输入节点名称')
    return
  }
  if (!nodeForm.value.nodeCode.trim()) {
    toast.warning('请输入节点编码')
    return
  }

  runDialog(async () => {
    if (isEditMode.value && editingNodeId.value) {
      await updateWorkflowNode(editingNodeId.value, nodeForm.value)
    } else {
      await createWorkflowNode(selectedDefinition.value!.id, nodeForm.value)
    }
  }, {
    successMessage: isEditMode.value ? '更新节点成功' : '创建节点成功',
    errorMessage: '保存节点失败',
    onSuccess: () => {
      nodeDialogOpen.value = false
      fetchNodes(selectedDefinition.value!.id)
    }
  })
}

const handleDeleteNode = (node: WorkflowNode) => {
  if (!confirm(`确定要删除节点"${node.nodeName}"吗？`)) {
    return
  }
  runList(async () => {
    await deleteWorkflowNode(node.id)
  }, {
    successMessage: '删除节点成功',
    errorMessage: '删除节点失败',
    onSuccess: () => {
      if (selectedDefinition.value) fetchNodes(selectedDefinition.value.id)
    }
  })
}

// View navigation
const enterDesignMode = (definition: WorkflowDefinition) => {
  selectedDefinition.value = definition
  viewMode.value = 'design'
  // WorkflowVisualizer 会自动加载数据并通过 @loaded 事件传递给节点列表
}

// 处理 WorkflowVisualizer 加载完成事件
const handleWorkflowLoaded = (workflowDef: WorkflowDefinitionWithNodes) => {
  // WorkflowVisualizer 内部定义的 WorkflowDefinition 包含 nodes 属性
  if (workflowDef && Array.isArray(workflowDef.nodes)) {
    nodes.value = workflowDef.nodes
  }
}

const exitDesignMode = () => {
  selectedDefinition.value = null
  nodes.value = []
  viewMode.value = 'list'
}

// Lifecycle
onMounted(() => {
  fetchDefinitions()
  fetchFormTemplates()
})
</script>

<template>
  <div class="space-y-4">
    <!-- Definition List View -->
    <template v-if="viewMode === 'list'">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0">
          <CardTitle>流程设计</CardTitle>
          <Button
            v-if="canCreateDefinition"
            @click="openDefinitionDialog()"
          >
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
                <TableHead class="text-right">
                  操作
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="loading">
                <TableCell
                  colspan="7"
                  class="h-24 text-center text-muted-foreground"
                >
                  加载中...
                </TableCell>
              </TableRow>
              <TableRow v-else-if="definitions.length === 0">
                <TableCell
                  colspan="7"
                  class="h-24 text-center text-muted-foreground"
                >
                  暂无流程定义
                </TableCell>
              </TableRow>
              <TableRow
                v-for="definition in definitions"
                :key="definition.id"
              >
                <TableCell class="font-medium">
                  <div>{{ definition.definitionName }}</div>
                  <div class="text-xs text-muted-foreground">
                    {{ definition.description || '暂无描述' }}
                  </div>
                </TableCell>
                <TableCell>
                  <code class="text-xs bg-muted px-1.5 py-0.5 rounded">{{ definition.definitionKey }}</code>
                </TableCell>
                <TableCell>{{ definition.category || '-' }}</TableCell>
                <TableCell>
                  <Badge variant="outline">
                    {{ definition.nodeCount ?? 0 }}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge :variant="definition.status === 1 ? 'default' : 'secondary'">
                    {{ definition.status === 1 ? '启用' : '停用' }}
                  </Badge>
                </TableCell>
                <TableCell>{{ formatDateTime(definition.updateTime) }}</TableCell>
                <TableCell class="text-right">
                  <div class="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      @click="enterDesignMode(definition)"
                    >
                      <Settings class="mr-1 h-3 w-3" />
                      设计
                    </Button>
                    <Button
                      v-if="canEditDefinition"
                      size="sm"
                      variant="ghost"
                      @click="openDefinitionDialog(definition)"
                    >
                      <Pencil class="h-3 w-3" />
                    </Button>
                    <Button
                      v-if="canDeleteDefinition"
                      size="sm"
                      variant="ghost"
                      class="text-destructive hover:text-destructive"
                      @click="handleDeleteDefinition(definition)"
                    >
                      <Trash2 class="h-3 w-3" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </template>

    <!-- Designer View -->
    <template v-else-if="viewMode === 'design' && selectedDefinition">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0">
          <div class="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              @click="exitDesignMode"
            >
              <ArrowLeft class="mr-2 h-4 w-4" />
              返回
            </Button>
            <CardTitle>{{ selectedDefinition.definitionName }}</CardTitle>
            <Badge :variant="selectedDefinition.status === 1 ? 'default' : 'secondary'">
              {{ selectedDefinition.status === 1 ? '启用' : '停用' }}
            </Badge>
          </div>
          <Button
            v-if="canCreateDefinition"
            @click="openNodeDialog()"
          >
            <Plus class="mr-2 h-4 w-4" />
            添加节点
          </Button>
        </CardHeader>
        <CardContent>
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <!-- Flow Visualization -->
            <div class="lg:col-span-2">
              <h3 class="text-sm font-medium mb-2">
                流程图
              </h3>
              <WorkflowVisualizer
                :definition-id="selectedDefinition.id"
                :readonly="true"
                class="rounded-lg"
                @loaded="handleWorkflowLoaded"
              />
            </div>

            <!-- Node List -->
            <div>
              <h3 class="text-sm font-medium mb-2">
                节点列表
              </h3>
              <div class="border rounded-lg divide-y">
                <div
                  v-if="nodes.length === 0"
                  class="p-4 text-center text-muted-foreground text-sm"
                >
                  暂无节点，点击"添加节点"创建
                </div>
                <div
                  v-for="node in nodes"
                  :key="node.id"
                  class="p-3 hover:bg-muted/50"
                >
                  <div class="flex items-start justify-between">
                    <div class="flex-1">
                      <div class="font-medium text-sm">
                        {{ node.nodeName }}
                      </div>
                      <div class="text-xs text-muted-foreground mt-1">
                        <code class="bg-muted px-1 rounded">{{ node.nodeCode }}</code>
                        <span class="mx-1">|</span>
                        <span>顺序: {{ node.nodeOrder }}</span>
                        <span class="mx-1">|</span>
                        <span>
                          {{ node.approverType === 'user' ? '用户审批' :
                            node.approverType === 'role' ? '角色审批' :
                            node.approverType === 'dept' ? '部门审批' : '领导审批' }}
                        </span>
                      </div>
                      <div class="flex gap-1 mt-1">
                        <Badge
                          v-if="node.isCounterSign"
                          variant="outline"
                          class="text-xs"
                        >
                          会签
                        </Badge>
                        <Badge
                          v-if="node.autoPassSameUser"
                          variant="outline"
                          class="text-xs"
                        >
                          自动通过
                        </Badge>
                      </div>
                    </div>
                    <div class="flex gap-1">
                      <Button
                        v-if="canEditDefinition"
                        size="icon"
                        variant="ghost"
                        class="h-7 w-7"
                        @click="openNodeDialog(node)"
                      >
                        <Pencil class="h-3 w-3" />
                      </Button>
                      <Button
                        v-if="canDeleteDefinition"
                        size="icon"
                        variant="ghost"
                        class="h-7 w-7 text-destructive hover:text-destructive"
                        @click="handleDeleteNode(node)"
                      >
                        <Trash2 class="h-3 w-3" />
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </template>

    <!-- Definition Dialog -->
    <Dialog v-model:open="definitionDialogOpen">
      <DialogContent class="sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle>{{ isEditMode ? '编辑流程定义' : '新建流程定义' }}</DialogTitle>
          <DialogDescription>
            {{ isEditMode ? '修改流程定义的基本信息和配置' : '创建新的流程定义，设置流程的基本信息和配置' }}
          </DialogDescription>
        </DialogHeader>

        <div class="max-h-[75vh] overflow-y-auto space-y-4 py-4">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>流程名称 <span class="text-destructive">*</span></Label>
              <Input
                v-model="definitionForm.definitionName"
                placeholder="请输入流程名称"
              />
            </div>
            <div class="space-y-2">
              <Label>流程标识 <span class="text-destructive">*</span></Label>
              <Input
                v-model="definitionForm.definitionKey"
                placeholder="请输入唯一标识，如：leave_approval"
              />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>分类</Label>
              <Input
                v-model="definitionForm.category"
                placeholder="如：人事审批、财务审批"
              />
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model.number="definitionForm.status">
                <SelectTrigger>
                  <SelectValue placeholder="请选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem :value="1">
                    启用
                  </SelectItem>
                  <SelectItem :value="0">
                    停用
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div class="space-y-2">
            <Label>描述</Label>
            <Textarea
              v-model="definitionForm.description"
              placeholder="请输入流程描述"
              rows="2"
            />
          </div>
          <div class="space-y-2">
            <Label class="text-base font-medium">申请表单配置</Label>

            <!-- Form template selector - 独占一行 -->
            <div class="p-4 bg-muted/50 rounded-lg border">
              <div class="flex items-end gap-3">
                <div class="flex-1 space-y-2">
                  <Label class="text-sm font-medium">从表单模板导入</Label>
                  <Select v-model="selectedFormTemplateId">
                    <SelectTrigger>
                      <SelectValue placeholder="选择表单模板（可选）" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem
                        v-for="template in formTemplates"
                        :key="template.id"
                        :value="template.id"
                      >
                        <div class="flex items-center justify-between w-full gap-2">
                          <span class="flex-1">{{ template.templateName }}</span>
                          <Badge
                            variant="secondary"
                            class="text-xs"
                          >
                            {{ template.category || '未分类' }}
                          </Badge>
                        </div>
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <Button
                  size="default"
                  :disabled="!selectedFormTemplateId"
                  @click="importFormTemplate"
                >
                  导入模板
                </Button>
              </div>
            </div>

            <!-- 表单配置编辑器 -->
            <WorkflowFormConfigEditor v-model="definitionForm.formConfig" />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            @click="closeDefinitionDialog"
          >
            取消
          </Button>
          <Button
            :disabled="dialogLoading"
            @click="handleSaveDefinition"
          >
            {{ isEditMode ? '保存' : '创建' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Node Dialog -->
    <Dialog v-model:open="nodeDialogOpen">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ isEditMode ? '编辑节点' : '添加节点' }}</DialogTitle>
          <DialogDescription>
            {{ isEditMode ? '修改流程节点的配置信息' : '为流程添加新的审批节点' }}
          </DialogDescription>
        </DialogHeader>
        <WorkflowNodeProperties v-model="nodeForm" />
        <DialogFooter>
          <Button
            variant="outline"
            @click="nodeDialogOpen = false"
          >
            取消
          </Button>
          <Button
            :disabled="dialogLoading"
            @click="handleSaveNode"
          >
            {{ isEditMode ? '保存' : '添加' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
