<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui'
import { getWorkflowDetail, getRollbackableNodes, getInstanceUrgeRecords } from '@/api'
import type { WorkflowDetail, WorkflowNode, WorkflowCc, WorkflowAddSign, WorkflowUrge } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'
import WorkflowBusinessCard from '@/components/workflow/WorkflowBusinessCard.vue'
import WorkflowOverviewCard from '@/components/workflow/WorkflowOverviewCard.vue'
import WorkflowTimelineTabs from '@/components/workflow/WorkflowTimelineTabs.vue'
import WorkflowVisualizer from '@/views/workflow/WorkflowVisualizer.vue'
import { useWorkflowActions } from '@/composables/workflow/useWorkflowActions'
import { useAsyncAction } from '@/composables/useAsyncAction'

const route = useRoute()
const router = useRouter()
const { loading, run: runFetchDetail } = useAsyncAction('获取流程详情失败')
const { run: runRollbackNodes } = useAsyncAction('获取可回退节点失败')
const approvalComment = ref('')
const selectedRollbackNodeId = ref<string>('')
const rollbackableNodes = ref<WorkflowNode[]>([])
const showRollback = ref(false)
const ccRecords = ref<WorkflowCc[]>([])
const urgeRecords = ref<WorkflowUrge[]>([])
const addSignRecords = ref<WorkflowAddSign[]>([])
const detail = ref<WorkflowDetail | null>(null)
const userStore = useUserStore()

// 加签/转办对话框
const addSignDialogOpen = ref(false)
const addSignType = ref<'BEFORE' | 'AFTER' | 'TRANSFER'>('BEFORE')
const addSignUserId = ref('')
const addSignReason = ref('')
const {
  actionLoading,
  approveWorkflowAction,
  rejectWorkflowAction,
  rollbackWorkflowAction,
  addSignWorkflowAction
} = useWorkflowActions()

const workflowId = computed(() => String(route.params.id || ''))
const workflowPermissionState = computed(() =>
  getWorkflowPermissionState(userStore.hasPermission, detail.value?.canApprove ?? false)
)

const currentNodeId = computed(() => detail.value?.currentNode?.id || detail.value?.instance?.currentNodeId)

const completedNodeIds = computed(() => {
  if (!detail.value?.approvals) return new Set<string>()
  return new Set(
    detail.value.approvals
      .filter((approval) => ['approved', 'APPROVED'].includes(approval.approvalStatus))
      .map((approval) => approval.nodeId)
  )
})

const getApproverTypeLabel = (type?: string) => {
  const map: Record<string, string> = {
    user: '指定用户',
    role: '角色',
    dept: '部门',
    leader: '部门领导'
  }
  return map[type || ''] || type || '-'
}

const fetchDetail = () => {
  if (!workflowId.value) return
  runFetchDetail(async () => {
    const res = await getWorkflowDetail(workflowId.value)
    detail.value = res.data

    // 优先使用详情聚合返回的数据，仅在聚合为空时 fallback 单独请求
    ccRecords.value = res.data.ccRecords?.length ? res.data.ccRecords : []
    addSignRecords.value = res.data.addSignRecords?.length ? res.data.addSignRecords : []

    // 催办记录未聚合到详情接口，仍需单独请求
    try {
      const urgeRes = await getInstanceUrgeRecords(workflowId.value)
      urgeRecords.value = urgeRes.data
    } catch {
      // Silent failure - urge records are optional
    }
  })
}

const handleAction = async (type: 'approve' | 'reject') => {
  if (!workflowId.value) return
  if (!approvalComment.value.trim()) {
    toast.warning('请输入审批意见')
    return
  }

  const payload = { comment: approvalComment.value.trim() }
  const result = type === 'approve'
    ? await approveWorkflowAction(workflowId.value, payload)
    : await rejectWorkflowAction(workflowId.value, payload)

  if (result) {
    approvalComment.value = ''
    showRollback.value = false
    fetchDetail()
  }
}

const handleRollback = async () => {
  if (!workflowId.value) return
  if (!approvalComment.value.trim()) {
    toast.warning('请输入回退理由')
    return
  }

  const payload = {
    comment: approvalComment.value.trim(),
    targetNodeId: selectedRollbackNodeId.value || undefined
  }
  const result = await rollbackWorkflowAction(workflowId.value, payload)

  if (result) {
    approvalComment.value = ''
    showRollback.value = false
    selectedRollbackNodeId.value = ''
    fetchDetail()
  }
}

const toggleRollback = () => {
  showRollback.value = !showRollback.value
  if (showRollback.value && rollbackableNodes.value.length === 0) {
    runRollbackNodes(
      async () => {
        const res = await getRollbackableNodes(workflowId.value)
        rollbackableNodes.value = res.data
        // 默认选中第一个（上一个节点）
        if (rollbackableNodes.value.length > 0) {
          selectedRollbackNodeId.value = rollbackableNodes.value[0].id
        }
      },
      {
        onError: () => {
          showRollback.value = false
        }
      }
    )
  }
}

const handleAddSign = async () => {
  if (!workflowId.value) return
  if (!addSignUserId.value.trim()) {
    toast.warning('请选择被加签人')
    return
  }
  if (!addSignReason.value.trim()) {
    toast.warning('请输入加签原因')
    return
  }

  const result = await addSignWorkflowAction(workflowId.value, {
    addUserId: addSignUserId.value,
    addType: addSignType.value,
    reason: addSignReason.value.trim()
  })

  if (result) {
    addSignDialogOpen.value = false
    addSignUserId.value = ''
    addSignReason.value = ''
    fetchDetail()
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="space-y-6">
    <div class="sticky top-0 z-10 -mx-6 px-6 py-3 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b">
      <Button variant="ghost" size="sm" @click="router.back()">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回
      </Button>
    </div>

    <WorkflowOverviewCard :instance="detail?.instance ?? null" :loading="loading" />

    <WorkflowBusinessCard :config="detail?.formConfig" :form-data="detail?.formData" />

    <Card v-if="workflowPermissionState.canApproveDetail">
      <CardHeader>
        <CardTitle>审批操作</CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <Textarea v-model="approvalComment" placeholder="请输入审批意见" />
        <div v-if="showRollback && workflowPermissionState.canRollbackDetail" class="space-y-2">
          <label class="text-sm font-medium">回退到</label>
          <Select v-model="selectedRollbackNodeId">
            <SelectTrigger>
              <SelectValue placeholder="选择回退节点" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="node in rollbackableNodes" :key="node.id" :value="node.id">
                {{ node.nodeOrder }}. {{ node.nodeName }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div class="flex gap-3 flex-wrap">
          <Button :disabled="actionLoading" @click="handleAction('approve')">通过</Button>
          <Button v-if="workflowPermissionState.canRejectDetail" variant="destructive" :disabled="actionLoading" @click="handleAction('reject')">驳回</Button>
          <Button v-if="workflowPermissionState.canRollbackDetail" variant="outline" :disabled="actionLoading" @click="toggleRollback">
            {{ showRollback ? '取消回退' : '回退' }}
          </Button>
          <Button v-if="showRollback && workflowPermissionState.canRollbackDetail" :disabled="actionLoading" @click="handleRollback">
            确认回退
          </Button>
          <Button v-if="workflowPermissionState.canAddSignDetail" variant="secondary" @click="addSignDialogOpen = true">加签/转办</Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardHeader>
        <CardTitle>节点流转</CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <WorkflowVisualizer
          :nodes="detail?.nodes"
          :current-node-id="currentNodeId"
          :completed-node-ids="completedNodeIds"
          readonly
        />
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>顺序</TableHead>
              <TableHead>节点名称</TableHead>
              <TableHead>审批类型</TableHead>
              <TableHead>说明</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="!detail?.nodes?.length">
              <TableCell colspan="4" class="h-24 text-center text-muted-foreground">暂无节点数据</TableCell>
            </TableRow>
            <TableRow v-for="node in detail?.nodes || []" :key="node.id">
              <TableCell>{{ node.nodeOrder }}</TableCell>
              <TableCell class="font-medium">{{ node.nodeName }}</TableCell>
              <TableCell>{{ getApproverTypeLabel(node.approverType) }}</TableCell>
              <TableCell>{{ node.description || '-' }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <WorkflowTimelineTabs
      :approvals="detail?.approvals || []"
      :cc-records="ccRecords"
      :urge-records="urgeRecords"
      :add-sign-records="addSignRecords"
      :instance-id="workflowId"
    />

    <!-- 加签/转办对话框 -->
    <Dialog v-model:open="addSignDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>加签/转办</DialogTitle>
        </DialogHeader>
        <div class="space-y-4">
          <div>
            <label class="text-sm font-medium">类型</label>
            <Select v-model="addSignType">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="BEFORE">前加签</SelectItem>
                <SelectItem value="AFTER">后加签</SelectItem>
                <SelectItem value="TRANSFER">转办</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <label class="text-sm font-medium">被加签人</label>
            <input v-model="addSignUserId" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" placeholder="请输入用户ID" />
          </div>
          <div>
            <label class="text-sm font-medium">原因</label>
            <Textarea v-model="addSignReason" placeholder="请输入加签原因" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="addSignDialogOpen = false">取消</Button>
          <Button :disabled="actionLoading" @click="handleAddSign">确认</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
