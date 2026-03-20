<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea
} from '@/components/ui'
import { approveWorkflow, getPendingWorkflows, rejectWorkflow } from '@/api'
import type { WorkflowInstance } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const loading = ref(false)
const dialogLoading = ref(false)
const actionDialogOpen = ref(false)
const actionType = ref<'approve' | 'reject'>('approve')
const comment = ref('')
const currentWorkflow = ref<WorkflowInstance | null>(null)
const workflows = ref<WorkflowInstance[]>([])
const userStore = useUserStore()

const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))
const canApproveWorkflow = computed(() => permissionState.value.canApprovePendingActions)

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPendingWorkflows()
    workflows.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取待审批流程失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const openActionDialog = (workflow: WorkflowInstance, type: 'approve' | 'reject') => {
  currentWorkflow.value = workflow
  actionType.value = type
  comment.value = ''
  actionDialogOpen.value = true
}

const submitAction = async () => {
  if (!currentWorkflow.value) return
  if (!comment.value.trim()) {
    toast.warning('请输入审批意见')
    return
  }

  dialogLoading.value = true
  try {
    const payload = { comment: comment.value.trim() }
    if (actionType.value === 'approve') {
      await approveWorkflow(currentWorkflow.value.id, payload)
      toast.success('审批已通过')
    } else {
      await rejectWorkflow(currentWorkflow.value.id, payload)
      toast.success('流程已驳回')
    }
    actionDialogOpen.value = false
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '审批失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardHeader>
        <CardTitle>待我审批</CardTitle>
      </CardHeader>
      <CardContent class="p-0">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>标题</TableHead>
              <TableHead>发起人</TableHead>
              <TableHead>流程定义</TableHead>
              <TableHead>当前节点</TableHead>
              <TableHead>提交时间</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="loading">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">加载中...</TableCell>
            </TableRow>
            <TableRow v-else-if="workflows.length === 0">
              <TableCell colspan="6" class="h-24 text-center text-muted-foreground">当前没有待审批流程</TableCell>
            </TableRow>
            <TableRow v-for="workflow in workflows" :key="workflow.id">
              <TableCell class="font-medium">{{ workflow.title }}</TableCell>
              <TableCell>{{ workflow.userName }}</TableCell>
              <TableCell>{{ workflow.definitionName }}</TableCell>
              <TableCell>{{ workflow.currentNodeName || '-' }}</TableCell>
              <TableCell>{{ formatDateTime(workflow.submitTime || workflow.createTime) }}</TableCell>
              <TableCell class="text-right">
                <div class="flex justify-end gap-2">
                  <RouterLink :to="`/workflow/detail/${workflow.id}`">
                    <Button size="sm" variant="outline">详情</Button>
                  </RouterLink>
                  <Button v-if="canApproveWorkflow" size="sm" @click="openActionDialog(workflow, 'approve')">通过</Button>
                  <Button v-if="canApproveWorkflow" size="sm" variant="destructive" @click="openActionDialog(workflow, 'reject')">驳回</Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Dialog v-if="canApproveWorkflow" v-model:open="actionDialogOpen">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{{ actionType === 'approve' ? '通过审批' : '驳回流程' }}</DialogTitle>
        </DialogHeader>
        <div class="space-y-2">
          <div class="text-sm text-muted-foreground">
            {{ currentWorkflow?.title || '-' }}
          </div>
          <Textarea v-model="comment" placeholder="请输入审批意见" />
        </div>
        <DialogFooter>
          <Button variant="outline" @click="actionDialogOpen = false">取消</Button>
          <Button :variant="actionType === 'approve' ? 'default' : 'destructive'" :disabled="dialogLoading" @click="submitAction">
            确认
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
