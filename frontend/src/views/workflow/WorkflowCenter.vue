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
import { getEnabledWorkflowDefinitions, getWorkflowDefinitions, startWorkflow } from '@/api'
import type { WorkflowDefinition } from '@/types'
import { toast } from 'vue-sonner'
import { Play } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const dialogLoading = ref(false)
const definitions = ref<WorkflowDefinition[]>([])
const startDialogOpen = ref(false)
const userStore = useUserStore()

const form = ref({
  definitionId: '',
  title: '',
  businessData: '',
  remark: ''
})

const canCreateWorkflow = computed(() => userStore.hasPermission('workflow:create'))

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const fetchDefinitions = async () => {
  loading.value = true
  try {
    const [allRes, enabledRes] = await Promise.all([
      getWorkflowDefinitions(),
      getEnabledWorkflowDefinitions()
    ])
    const enabledIds = new Set(enabledRes.data.map((item) => item.id))
    definitions.value = allRes.data.map((item) => ({
      ...item,
      status: enabledIds.has(item.id) ? 1 : item.status
    }))
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取流程模板失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const openStartDialog = (definition?: WorkflowDefinition) => {
  form.value = {
    definitionId: definition?.id || '',
    title: definition ? `${definition.definitionName}申请` : '',
    businessData: '',
    remark: ''
  }
  startDialogOpen.value = true
}

const handleStartWorkflow = async () => {
  if (!form.value.definitionId) {
    toast.warning('请选择流程类型')
    return
  }
  if (!form.value.title.trim()) {
    toast.warning('请输入流程标题')
    return
  }

  dialogLoading.value = true
  try {
    await startWorkflow({
      definitionId: form.value.definitionId,
      title: form.value.title.trim(),
      businessData: form.value.businessData.trim() || undefined,
      remark: form.value.remark.trim() || undefined
    })
    toast.success('流程发起成功')
    startDialogOpen.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '发起流程失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

onMounted(fetchDefinitions)
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardHeader class="flex flex-row items-center justify-between space-y-0">
        <CardTitle>流程模板</CardTitle>
        <Button v-if="canCreateWorkflow" @click="openStartDialog()">
          <Play class="mr-2 h-4 w-4" />
          新建流程
        </Button>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>流程名称</TableHead>
              <TableHead>分类</TableHead>
              <TableHead>版本</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>更新时间</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="loading">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
            </TableRow>
            <TableRow v-else-if="definitions.length === 0">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">暂无流程模板</TableCell>
            </TableRow>
            <TableRow v-for="definition in definitions" :key="definition.id">
              <TableCell class="font-medium">
                <div>{{ definition.definitionName }}</div>
                <div class="text-xs text-muted-foreground">{{ definition.description || '暂无描述' }}</div>
              </TableCell>
              <TableCell>{{ definition.category || '-' }}</TableCell>
              <TableCell>v{{ definition.version }}</TableCell>
              <TableCell>
                <Badge :variant="definition.status === 1 ? 'default' : 'secondary'">
                  {{ definition.status === 1 ? '启用' : '停用' }}
                </Badge>
              </TableCell>
              <TableCell>{{ formatDateTime(definition.updateTime) }}</TableCell>
              <TableCell class="text-right">
                <Button
                  v-if="canCreateWorkflow"
                  size="sm"
                  variant="outline"
                  :disabled="definition.status !== 1"
                  @click="openStartDialog(definition)"
                >
                  立即发起
                </Button>
                <span v-else class="text-xs text-muted-foreground">无发起权限</span>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Dialog v-model:open="startDialogOpen">
      <DialogContent class="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>新建流程</DialogTitle>
        </DialogHeader>
        <div class="space-y-4">
          <div class="space-y-2">
            <Label>流程类型 <span class="text-muted-foreground text-xs">(必填)</span></Label>
            <Select v-model="form.definitionId">
              <SelectTrigger>
                <SelectValue placeholder="请选择流程类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="definition in definitions.filter((item) => item.status === 1)"
                  :key="definition.id"
                  :value="definition.id"
                >
                  {{ definition.definitionName }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2">
            <Label>流程标题</Label>
            <Input v-model="form.title" placeholder="例如：费用报销申请" />
          </div>
          <div class="space-y-2">
            <Label>业务数据</Label>
            <Textarea v-model="form.businessData" placeholder='可填 JSON，例如：{"amount": 1200, "reason": "差旅报销"}' />
          </div>
          <div class="space-y-2">
            <Label>备注</Label>
            <Textarea v-model="form.remark" placeholder="补充说明" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="startDialogOpen = false">取消</Button>
          <Button :disabled="dialogLoading" @click="handleStartWorkflow">提交</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
