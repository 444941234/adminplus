<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  Badge,
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
  Textarea
} from '@/components/ui'
import { approveWorkflow, getWorkflowDetail, rejectWorkflow } from '@/api'
import type { WorkflowDetail } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const route = useRoute()
const loading = ref(false)
const actionLoading = ref(false)
const approvalComment = ref('')
const detail = ref<WorkflowDetail | null>(null)
const userStore = useUserStore()

const workflowId = computed(() => String(route.params.id || ''))
const workflowPermissionState = computed(() =>
  getWorkflowPermissionState(userStore.hasPermission, detail.value?.canApprove ?? false)
)

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

const fetchDetail = async () => {
  if (!workflowId.value) return
  loading.value = true
  try {
    const res = await getWorkflowDetail(workflowId.value)
    detail.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取流程详情失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const handleAction = async (type: 'approve' | 'reject') => {
  if (!workflowId.value) return
  if (!approvalComment.value.trim()) {
    toast.warning('请输入审批意见')
    return
  }

  actionLoading.value = true
  try {
    const payload = { comment: approvalComment.value.trim() }
    if (type === 'approve') {
      await approveWorkflow(workflowId.value, payload)
      toast.success('审批已通过')
    } else {
      await rejectWorkflow(workflowId.value, payload)
      toast.success('流程已驳回')
    }
    approvalComment.value = ''
    fetchDetail()
  } catch (error) {
    const message = error instanceof Error ? error.message : '审批失败'
    toast.error(message)
  } finally {
    actionLoading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="space-y-6">
    <Card>
      <CardHeader>
        <CardTitle>流程概览</CardTitle>
      </CardHeader>
      <CardContent v-if="detail?.instance" class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <div>
          <div class="text-sm text-muted-foreground">标题</div>
          <div class="mt-1 font-medium">{{ detail.instance.title }}</div>
        </div>
        <div>
          <div class="text-sm text-muted-foreground">流程定义</div>
          <div class="mt-1 font-medium">{{ detail.instance.definitionName }}</div>
        </div>
        <div>
          <div class="text-sm text-muted-foreground">状态</div>
          <div class="mt-1">
            <Badge variant="secondary">{{ getStatusLabel(detail.instance.status) }}</Badge>
          </div>
        </div>
        <div>
          <div class="text-sm text-muted-foreground">当前节点</div>
          <div class="mt-1 font-medium">{{ detail.instance.currentNodeName || '-' }}</div>
        </div>
        <div>
          <div class="text-sm text-muted-foreground">发起人</div>
          <div class="mt-1">{{ detail.instance.userName || '-' }}</div>
        </div>
        <div>
          <div class="text-sm text-muted-foreground">提交时间</div>
          <div class="mt-1">{{ formatDateTime(detail.instance.submitTime || detail.instance.createTime) }}</div>
        </div>
        <div class="md:col-span-2 xl:col-span-2">
          <div class="text-sm text-muted-foreground">备注</div>
          <div class="mt-1 whitespace-pre-wrap">{{ detail.instance.remark || '无备注' }}</div>
        </div>
      </CardContent>
      <CardContent v-else class="py-12 text-center text-muted-foreground">
        {{ loading ? '加载中...' : '未获取到流程详情' }}
      </CardContent>
    </Card>

    <Card v-if="workflowPermissionState.canApproveDetail">
      <CardHeader>
        <CardTitle>审批操作</CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <Textarea v-model="approvalComment" placeholder="请输入审批意见" />
        <div class="flex gap-3">
          <Button :disabled="actionLoading" @click="handleAction('approve')">通过</Button>
          <Button variant="destructive" :disabled="actionLoading" @click="handleAction('reject')">驳回</Button>
        </div>
      </CardContent>
    </Card>

    <div class="grid gap-6 xl:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>节点流转</CardTitle>
        </CardHeader>
        <CardContent class="p-0">
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
                <TableCell>{{ node.approverType || '-' }}</TableCell>
                <TableCell>{{ node.description || '-' }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>审批记录</CardTitle>
        </CardHeader>
        <CardContent class="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>节点</TableHead>
                <TableHead>审批人</TableHead>
                <TableHead>结果</TableHead>
                <TableHead>意见</TableHead>
                <TableHead>时间</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="!detail?.approvals?.length">
                <TableCell colspan="5" class="h-24 text-center text-muted-foreground">暂无审批记录</TableCell>
              </TableRow>
              <TableRow v-for="approval in detail?.approvals || []" :key="approval.id">
                <TableCell>{{ approval.nodeName || '-' }}</TableCell>
                <TableCell>{{ approval.approverName || '-' }}</TableCell>
                <TableCell>{{ approval.approvalStatus || '-' }}</TableCell>
                <TableCell>{{ approval.comment || '-' }}</TableCell>
                <TableCell>{{ formatDateTime(approval.approvalTime || approval.createTime) }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
