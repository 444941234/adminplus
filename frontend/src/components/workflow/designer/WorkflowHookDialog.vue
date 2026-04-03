<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Input, Label, Textarea, Select, SelectContent, SelectItem, SelectTrigger, SelectValue, Checkbox } from '@/components/ui'
import { getNodeHooks, createHook, updateHook, deleteHook, type WorkflowNodeHook, type WorkflowHookReq } from '@/api/workflow'
import { showErrorToast } from '@/composables/useApiInterceptors'

const HOOK_POINTS = [
  { value: 'PRE_SUBMIT', label: '提交前校验', type: 'validate' },
  { value: 'POST_SUBMIT', label: '提交后执行', type: 'execute' },
  { value: 'PRE_APPROVE', label: '同意前校验', type: 'validate' },
  { value: 'POST_APPROVE', label: '同意后执行', type: 'execute' },
  { value: 'PRE_REJECT', label: '拒绝前校验', type: 'validate' },
  { value: 'POST_REJECT', label: '拒绝后执行', type: 'execute' },
  { value: 'PRE_ROLLBACK', label: '退回前校验', type: 'validate' },
  { value: 'POST_ROLLBACK', label: '退回后执行', type: 'execute' },
  { value: 'PRE_CANCEL', label: '取消前校验', type: 'validate' },
  { value: 'POST_CANCEL', label: '取消后执行', type: 'execute' },
  { value: 'PRE_WITHDRAW', label: '撤回前校验', type: 'validate' },
  { value: 'POST_WITHDRAW', label: '撤回后执行', type: 'execute' },
  { value: 'PRE_ADD_SIGN', label: '加签前校验', type: 'validate' },
  { value: 'POST_ADD_SIGN', label: '加签后执行', type: 'execute' }
]

const EXECUTOR_TYPES = [
  { value: 'spel', label: 'SpEL表达式' },
  { value: 'bean', label: 'Bean方法' },
  { value: 'http', label: 'HTTP接口' }
]

interface Props {
  open: boolean
  nodeId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'refresh'): void
}>()

const hooks = ref<WorkflowNodeHook[]>([])
const loading = ref(false)
const editingHook = ref<WorkflowNodeHook | null>(null)
const showForm = ref(false)

const formData = ref<Omit<WorkflowHookReq, 'nodeId'>>({
  hookPoint: 'PRE_SUBMIT',
  hookType: 'validate',
  executorType: 'spel',
  executorConfig: '',
  asyncExecution: false,
  blockOnFailure: true,
  failureMessage: '',
  priority: 0,
  conditionExpression: '',
  retryCount: 0,
  retryInterval: 1000,
  hookName: '',
  description: ''
})

const selectedHookPoint = ref<string>('')

const filteredHooks = computed(() => {
  if (!selectedHookPoint.value) return hooks.value
  return hooks.value.filter(h => h.hookPoint === selectedHookPoint.value)
})

const getHookPointLabel = (point: string) => {
  const hook = HOOK_POINTS.find((h: any) => h.value === point)
  return hook?.label || point
}

const getExecutorTypeLabel = (type: string) => {
  const executor = EXECUTOR_TYPES.find((t: any) => t.value === type)
  return executor?.label || type
}

const isEditMode = computed(() => !!editingHook.value)

const currentHookPointConfig = computed(() => {
  const hook = HOOK_POINTS.find(h => h.value === formData.value.hookPoint)
  return hook
})

const loadHooks = async () => {
  if (!props.nodeId) return
  loading.value = true
  try {
    const response = await getNodeHooks(props.nodeId)
    hooks.value = (response as any).data || response
  } finally {
    loading.value = false
  }
}

const openCreateForm = (hookPoint?: string) => {
  editingHook.value = null
  showForm.value = true
  if (hookPoint) {
    formData.value.hookPoint = hookPoint
    const hook = HOOK_POINTS.find(h => h.value === hookPoint)
    if (hook) {
      formData.value.hookType = hook.type as 'validate' | 'execute'
    }
  }
}

const openEditForm = (hook: WorkflowNodeHook) => {
  editingHook.value = hook
  showForm.value = true
  formData.value = {
    hookPoint: hook.hookPoint,
    hookType: hook.hookType as 'validate' | 'execute',
    executorType: hook.executorType as 'spel' | 'bean' | 'http',
    executorConfig: hook.executorConfig,
    asyncExecution: hook.asyncExecution,
    blockOnFailure: hook.blockOnFailure,
    failureMessage: hook.failureMessage,
    priority: hook.priority,
    conditionExpression: hook.conditionExpression,
    retryCount: hook.retryCount,
    retryInterval: hook.retryInterval,
    hookName: hook.hookName,
    description: hook.description
  }
}

const closeForm = () => {
  showForm.value = false
  editingHook.value = null
  formData.value = {
    hookPoint: 'PRE_SUBMIT',
    hookType: 'validate',
    executorType: 'spel',
    executorConfig: '',
    asyncExecution: false,
    blockOnFailure: true,
    failureMessage: '',
    priority: 0,
    conditionExpression: '',
    retryCount: 0,
    retryInterval: 1000,
    hookName: '',
    description: ''
  }
}

const saveHook = async () => {
  try {
    const data: WorkflowHookReq = {
      ...formData.value,
      nodeId: props.nodeId
    }

    if (isEditMode.value && editingHook.value) {
      await updateHook(editingHook.value.id, data)
    } else {
      await createHook(data)
    }

    await loadHooks()
    closeForm()
    emit('refresh')
  } catch (error) {
    showErrorToast(error, '保存钩子失败')
  }
}

const handleDeleteHook = async (hookId: string) => {
  if (!confirm('确定要删除这个钩子配置吗？')) return
  try {
    await deleteHook(hookId)
    await loadHooks()
    emit('refresh')
  } catch (error) {
    showErrorToast(error, '删除钩子失败')
  }
}

watch(() => props.open, (val) => {
  if (val) {
    loadHooks()
  }
})
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="max-w-4xl max-h-[80vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>节点钩子配置</DialogTitle>
        <DialogDescription>
          配置工作流节点的钩子，支持SpEL表达式、Bean方法和HTTP接口三种执行方式
        </DialogDescription>
      </DialogHeader>

      <div v-if="loading" class="flex justify-center py-8">
        <div class="text-muted-foreground">加载中...</div>
      </div>

      <div v-else-if="!showForm" class="space-y-4">
        <!-- 钩子点筛选 -->
        <div class="flex items-center gap-4">
          <Label>筛选钩子点:</Label>
          <Select v-model="selectedHookPoint">
            <SelectTrigger class="w-48">
              <SelectValue placeholder="全部钩子点" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">全部钩子点</SelectItem>
              <SelectItem v-for="point in HOOK_POINTS" :key="point.value" :value="point.value">
                {{ point.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- 钩子列表 -->
        <div class="space-y-2">
          <div v-for="hook in filteredHooks" :key="hook.id" class="border rounded-lg p-4">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-medium">{{ getHookPointLabel(hook.hookPoint) }}</span>
                  <span class="text-xs px-2 py-1 rounded" :class="hook.hookType === 'validate' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800'">
                    {{ hook.hookType === 'validate' ? '校验' : '执行' }}
                  </span>
                  <span class="text-xs px-2 py-1 rounded bg-gray-100 text-gray-800">
                    {{ getExecutorTypeLabel(hook.executorType) }}
                  </span>
                  <span v-if="hook.asyncExecution" class="text-xs px-2 py-1 rounded bg-purple-100 text-purple-800">
                    异步
                  </span>
                </div>
                <div v-if="hook.hookName" class="text-sm text-muted-foreground mt-1">{{ hook.hookName }}</div>
                <div v-if="hook.description" class="text-sm text-muted-foreground">{{ hook.description }}</div>
              </div>
              <div class="flex gap-2">
                <Button variant="ghost" size="sm" @click="openEditForm(hook)">编辑</Button>
                <Button variant="ghost" size="sm" class="text-destructive" @click="handleDeleteHook(hook.id)">删除</Button>
              </div>
            </div>
          </div>

          <div v-if="filteredHooks.length === 0" class="text-center py-8 text-muted-foreground">
            暂无钩子配置
          </div>
        </div>

        <!-- 快速添加钩子按钮 -->
        <div class="border-t pt-4">
          <div class="text-sm font-medium mb-2">快速添加钩子:</div>
          <div class="grid grid-cols-2 gap-2">
            <Button
              v-for="point in HOOK_POINTS"
              :key="point.value"
              variant="outline"
              size="sm"
              @click="openCreateForm(point.value)"
            >
              {{ point.label }}
            </Button>
          </div>
        </div>
      </div>

      <!-- 钩子表单 -->
      <div v-else class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label>钩子点 <span class="text-destructive">*</span></Label>
            <Select v-model="formData.hookPoint" :disabled="isEditMode">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="point in HOOK_POINTS" :key="point.value" :value="point.value">
                  {{ point.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div class="space-y-2">
            <Label>执行方式 <span class="text-destructive">*</span></Label>
            <Select v-model="formData.executorType">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="type in EXECUTOR_TYPES" :key="type.value" :value="type.value">
                  {{ type.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <Label>钩子名称</Label>
          <Input v-model="formData.hookName" placeholder="为钩子配置一个名称" />
        </div>

        <div class="space-y-2">
          <Label>描述</Label>
          <Textarea v-model="formData.description" placeholder="描述这个钩子的用途" />
        </div>

        <!-- SpEL 表达式配置 -->
        <div v-if="formData.executorType === 'spel'" class="space-y-2">
          <Label>SpEL 表达式 <span class="text-destructive">*</span></Label>
          <Textarea
            v-model="formData.executorConfig"
            placeholder='#formData.amount > 1000'
            class="font-mono text-sm"
          />
          <div class="text-xs text-muted-foreground">
            可用变量: #instance, #node, #formData, #operatorId, #operatorName, #extraParams
          </div>
          <div v-if="currentHookPointConfig?.type === 'validate'" class="space-y-2">
            <Label>校验失败提示</Label>
            <Input v-model="formData.failureMessage" placeholder="金额必须大于1000" />
          </div>
        </div>

        <!-- Bean 方法配置 -->
        <div v-if="formData.executorType === 'bean'" class="space-y-2">
          <Label>Bean 配置 (JSON) <span class="text-destructive">*</span></Label>
          <Textarea
            v-model="formData.executorConfig"
            placeholder='{"beanName": "myHookService", "methodName": "validateSubmit", "args": ["#instance", "#formData"]}'
            class="font-mono text-sm"
          />
        </div>

        <!-- HTTP 接口配置 -->
        <div v-if="formData.executorType === 'http'" class="space-y-2">
          <Label>HTTP 配置 (JSON) <span class="text-destructive">*</span></Label>
          <Textarea
            v-model="formData.executorConfig"
            placeholder='{"url": "http://api.example.com/hook", "method": "POST", "headers": {}, "bodyTemplate": "{}"}'
            class="font-mono text-sm"
          />
        </div>

        <!-- 触发条件 -->
        <div class="space-y-2">
          <Label>触发条件（可选）</Label>
          <Textarea
            v-model="formData.conditionExpression"
            placeholder='#formData.amount > 10000'
            class="font-mono text-sm"
          />
          <div class="text-xs text-muted-foreground">为空时始终触发，支持SpEL表达式</div>
        </div>

        <div class="grid grid-cols-3 gap-4">
          <div class="space-y-2">
            <Label>优先级</Label>
            <Input v-model.number="formData.priority" type="number" min="0" />
          </div>
          <div class="space-y-2">
            <Label>重试次数</Label>
            <Input v-model.number="formData.retryCount" type="number" min="0" />
          </div>
          <div class="space-y-2">
            <Label>重试间隔(ms)</Label>
            <Input v-model.number="formData.retryInterval" type="number" min="0" />
          </div>
        </div>

        <div class="flex gap-6">
          <div class="flex items-center space-x-2">
            <Checkbox id="async" v-model="formData.asyncExecution" />
            <Label for="async" class="cursor-pointer text-sm font-normal">异步执行</Label>
          </div>
          <div class="flex items-center space-x-2">
            <Checkbox id="block" v-model="formData.blockOnFailure" />
            <Label for="block" class="cursor-pointer text-sm font-normal">失败时阻断流程</Label>
          </div>
        </div>
      </div>

      <DialogFooter>
        <div v-if="showForm" class="flex gap-2">
          <Button variant="outline" @click="closeForm">取消</Button>
          <Button @click="saveHook">保存</Button>
        </div>
        <Button v-else variant="outline" @click="emit('update:open', false)">关闭</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
