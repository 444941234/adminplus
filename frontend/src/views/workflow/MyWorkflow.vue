<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
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
  TableRow
} from '@/components/ui'
import { cancelWorkflow, getMyWorkflows, withdrawWorkflow } from '@/api'
import type { WorkflowInstance } from '@/types'
import { toast } from 'vue-sonner'
import { RefreshCw } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const statusFilter = ref('ALL')
const workflows = ref<WorkflowInstance[]>([])
const userStore = useUserStore()

const terminalStatuses = new Set(['APPROVED', 'REJECTED', 'CANCELLED', 'FINISHED', 'COMPLETED'])
const canManageMyWorkflow = computed(() => userStore.hasPermission('workflow:create'))

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '审批中',
    PROCESSING: '进行中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
    WITHDRAWN: '已撤回',
    FINISHED: '已完成',
    COMPLETED: '已完成'
  }
  return map[status || ''] || status || '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyWorkflows(statusFilter.value === 'ALL' ? undefined : statusFilter.value)
    workflows.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取我的流程失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const handleWithdraw = async (workflow: WorkflowInstance) => {
  try {
    await withdrawWorkflow(workflow.id)
    toast.success('流程已撤回')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '撤回失败'
    toast.error(message)
  }
}

const handleCancel = async (workflow: WorkflowInstance) => {
  try {
    await cancelWorkflow(workflow.id)
    toast.success('流程已取消')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消失败'
    toast.error(message)
  }
}

const canOperate = (workflow: WorkflowInstance) => !terminalStatuses.has(workflow.status)

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardHeader class="flex flex-row items-center justify-between space-y-0">
        <CardTitle>我的流程</CardTitle>
        <div class="flex items-center gap-3">
          <Select v-model="statusFilter" @update:model-value="fetchData">
            <SelectTrigger class="w-[180px]">
              <SelectValue placeholder="全部状态" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">全部状态</SelectItem>
              <SelectItem value="DRAFT">草稿</SelectItem>
              <SelectItem value="PENDING">审批中</SelectItem>
              <SelectItem value="APPROVED">已通过</SelectItem>
              <SelectItem value="REJECTED">已驳回</SelectItem>
              <SelectItem value="CANCELLED">已取消</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" @click="fetchData">
            <RefreshCw class="mr-2 h-4 w-4" />
            刷新
          </Button>
        </div>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>标题</TableHead>
              <TableHead>流程定义</TableHead>
              <TableHead>当前节点</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>提交时间</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="loading">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
            </TableRow>
            <TableRow v-else-if="workflows.length === 0">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">暂无流程记录</TableCell>
            </TableRow>
            <TableRow v-for="workflow in workflows" :key="workflow.id">
              <TableCell class="font-medium">
                <div>{{ workflow.title }}</div>
                <div class="text-xs text-muted-foreground">{{ workflow.remark || '无备注' }}</div>
              </TableCell>
              <TableCell>{{ workflow.definitionName }}</TableCell>
              <TableCell>{{ workflow.currentNodeName || '-' }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ getStatusLabel(workflow.status) }}</Badge>
              </TableCell>
              <TableCell>{{ formatDateTime(workflow.submitTime || workflow.createTime) }}</TableCell>
              <TableCell class="text-right">
                <div class="flex justify-end gap-2">
                  <RouterLink :to="`/workflow/detail/${workflow.id}`">
                    <Button size="sm" variant="outline">详情</Button>
                  </RouterLink>
                  <Button
                    v-if="canManageMyWorkflow"
                    size="sm"
                    variant="outline"
                    :disabled="!canOperate(workflow)"
                    @click="handleWithdraw(workflow)"
                  >
                    撤回
                  </Button>
                  <Button
                    v-if="canManageMyWorkflow"
                    size="sm"
                    variant="ghost"
                    :disabled="!canOperate(workflow)"
                    @click="handleCancel(workflow)"
                  >
                    取消
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
