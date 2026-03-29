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
import { getPendingWorkflows } from '@/api'
import WorkflowActionButtons from '@/components/workflow/WorkflowActionButtons.vue'
import { useWorkflowActions } from '@/composables/workflow/useWorkflowActions'
import type { WorkflowInstance } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const loading = ref(false)
const actionDialogOpen = ref(false)
const actionType = ref<'approve' | 'reject'>('approve')
const comment = ref('')
const currentWorkflow = ref<WorkflowInstance | null>(null)
const workflows = ref<WorkflowInstance[]>([])
const userStore = useUserStore()
const {
  actionLoading,
  approveWorkflowAction,
  rejectWorkflowAction
} = useWorkflowActions()

const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))
const canApproveWorkflow = computed(() => permissionState.value.canApprovePendingActions)

import { formatDateTime } from '@/utils/format'

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

  const payload = { comment: comment.value.trim() }
  const result = actionType.value === 'approve'
    ? await approveWorkflowAction(currentWorkflow.value.id, payload)
    : await rejectWorkflowAction(currentWorkflow.value.id, payload)

  if (result) {
    actionDialogOpen.value = false
    fetchData()
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
                <WorkflowActionButtons
                  :workflow="workflow"
                  mode="pending"
                  :can-approve="canApproveWorkflow"
                  @approve="(item) => openActionDialog(item, 'approve')"
                  @reject="(item) => openActionDialog(item, 'reject')"
                />
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
          <Button :variant="actionType === 'approve' ? 'default' : 'destructive'" :disabled="actionLoading" @click="submitAction">
            确认
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
