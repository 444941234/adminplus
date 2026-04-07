<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Textarea
} from '@/components/ui'
import {
  deleteWorkflowDraft,
  getEnabledWorkflowDefinitions,
  getMyWorkflows,
  getWorkflowDefinition,
  getWorkflowDraftDetail,
  submitWorkflow,
  updateWorkflowDraft
} from '@/api'
import WorkflowActionButtons from '@/components/workflow/WorkflowActionButtons.vue'
import WorkflowListFilters from '@/components/workflow/WorkflowListFilters.vue'
import WorkflowStartDialog from '@/components/workflow/WorkflowStartDialog.vue'
import WorkflowStatusBadge from '@/components/workflow/WorkflowStatusBadge.vue'
import { ConfirmDialog } from '@/components/common'
import { useWorkflowActions } from '@/composables/workflow/useWorkflowActions'
import { useWorkflowForm } from '@/composables/workflow/useWorkflowForm'
import type { WorkflowDefinition, WorkflowFormValues, WorkflowInstance } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { formatDateTime } from '@/utils/format'

const { loading, run: runList } = useAsyncAction('获取我的流程失败')
const { loading: draftLoading, run: runDraft } = useAsyncAction('操作失败')
const { loading: draftDefinitionLoading, run: runDraftDefinition } = useAsyncAction('获取流程模板详情失败')
const { run: runDeleteDraft } = useAsyncAction('删除草稿失败')

const statusFilter = ref('ALL')
const workflows = ref<WorkflowInstance[]>([])
const enabledDefinitions = ref<WorkflowDefinition[]>([])
const userStore = useUserStore()
const {
  actionLoading,
  cancelWorkflowAction,
  urgeWorkflowAction,
  withdrawWorkflowAction
} = useWorkflowActions()
const {
  formConfig,
  formValues,
  fieldErrors,
  initForm,
  validateForm,
  buildSubmitPayload
} = useWorkflowForm()

// 催办对话框
const urgeDialogOpen = ref(false)
const urgeContent = ref('')
const selectedWorkflowId = ref<string | null>(null)
const draftDialogOpen = ref(false)
const selectedDraftId = ref<string | null>(null)
const draftForm = ref({
  definitionId: '',
  title: '',
  remark: ''
})

const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))
const canManageMyWorkflow = computed(() => permissionState.value.canStartWorkflow)
const draftFormModel = computed<WorkflowFormValues>({
  get: () => formValues.value,
  set: (value) => {
    formValues.value = value
  }
})

const fetchData = () => runList(async () => {
  const res = await getMyWorkflows(statusFilter.value === 'ALL' ? undefined : statusFilter.value)
  workflows.value = res.data
}, { errorMessage: '获取我的流程失败' })

const fetchEnabledDefinitions = () => runList(async () => {
  const res = await getEnabledWorkflowDefinitions()
  enabledDefinitions.value = res.data
}, { errorMessage: '获取启用流程模板失败' })

const handleWithdraw = async (workflow: WorkflowInstance) => {
  if (!permissionState.value.canWithdrawWorkflow) return
  const result = await withdrawWorkflowAction(workflow.id)
  if (result) {
    fetchData()
  }
}

const handleCancel = async (workflow: WorkflowInstance) => {
  if (!permissionState.value.canCancelWorkflow) return
  const result = await cancelWorkflowAction(workflow.id)
  if (result) {
    fetchData()
  }
}

const handleUrge = (workflow: WorkflowInstance) => {
  if (!permissionState.value.canUrgeWorkflow) return
  selectedWorkflowId.value = workflow.id
  urgeContent.value = ''
  urgeDialogOpen.value = true
}

const handleUrgeConfirm = async () => {
  if (!selectedWorkflowId.value) return
  if (!urgeContent.value.trim()) {
    toast.warning('请输入催办内容')
    return
  }

  const result = await urgeWorkflowAction(selectedWorkflowId.value, {
    content: urgeContent.value.trim()
  })

  if (result !== null) {
    urgeDialogOpen.value = false
    urgeContent.value = ''
    selectedWorkflowId.value = null
  }
}

const loadDraftDefinition = (definitionId: string) => runDraftDefinition(async () => {
  const res = await getWorkflowDefinition(definitionId)
  const nextDefinition = res.data
  const existingIndex = enabledDefinitions.value.findIndex((item) => item.id === nextDefinition.id)
  if (existingIndex >= 0) {
    enabledDefinitions.value.splice(existingIndex, 1, nextDefinition)
  } else {
    enabledDefinitions.value.push(nextDefinition)
  }
  initForm(nextDefinition.formConfig, formValues.value)
}, { errorMessage: '获取流程模板详情失败' })

const validateDraftForm = () => {
  if (!draftForm.value.definitionId) {
    toast.warning('请选择流程类型')
    return false
  }
  if (!draftForm.value.title.trim()) {
    toast.warning('请输入流程标题')
    return false
  }
  if (!validateForm()) {
    toast.warning('请完善表单必填项')
    return false
  }
  return true
}

const openDraftDialog = (workflow: WorkflowInstance) => {
  draftDialogOpen.value = true
  selectedDraftId.value = workflow.id
  runDraft(async () => {
    const res = await getWorkflowDraftDetail(workflow.id)
    draftForm.value = {
      definitionId: res.data.instance.definitionId,
      title: res.data.instance.title,
      remark: res.data.instance.remark || ''
    }
    initForm(res.data.formConfig, res.data.formData)
    await loadDraftDefinition(res.data.instance.definitionId)
  }, {
    errorMessage: '获取草稿详情失败',
    onError: () => {
      draftDialogOpen.value = false
      selectedDraftId.value = null
    }
  })
}

const handleDraftSave = () => {
  if (!permissionState.value.canDraftWorkflow) return
  if (!selectedDraftId.value || !validateDraftForm()) return

  runDraft(async () => {
    await updateWorkflowDraft(selectedDraftId.value!, {
      definitionId: draftForm.value.definitionId,
      title: draftForm.value.title.trim(),
      formData: buildSubmitPayload().formData,
      remark: draftForm.value.remark.trim() || undefined
    })
  }, {
    successMessage: '草稿已更新',
    onSuccess: () => {
      draftDialogOpen.value = false
      selectedDraftId.value = null
      fetchData()
    }
  })
}

const handleDraftSubmit = () => {
  if (!permissionState.value.canDraftWorkflow) return
  if (!selectedDraftId.value || !validateDraftForm()) return

  runDraft(async () => {
    await submitWorkflow(selectedDraftId.value!, {
      definitionId: draftForm.value.definitionId,
      title: draftForm.value.title.trim(),
      formData: buildSubmitPayload().formData,
      remark: draftForm.value.remark.trim() || undefined
    })
  }, {
    successMessage: '草稿提交成功',
    onSuccess: () => {
      draftDialogOpen.value = false
      selectedDraftId.value = null
      fetchData()
    }
  })
}

const handleQuickSubmitDraft = async (workflow: WorkflowInstance) => {
  await openDraftDialog(workflow)
}

// 删除草稿确认
const deleteDialogOpen = ref(false)
const deleteDraftTarget = ref<WorkflowInstance | null>(null)

const handleDeleteDraft = (workflow: WorkflowInstance) => {
  if (!permissionState.value.canDraftWorkflow) return
  deleteDraftTarget.value = workflow
  deleteDialogOpen.value = true
}

const confirmDeleteDraft = () => {
  if (!deleteDraftTarget.value) return

  runDeleteDraft(async () => {
    await deleteWorkflowDraft(deleteDraftTarget.value!.id)
  }, {
    successMessage: '草稿已删除',
    onSuccess: () => {
      fetchData()
    }
  }).finally(() => {
    deleteDialogOpen.value = false
    deleteDraftTarget.value = null
  })
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchEnabledDefinitions()])
})
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardHeader class="flex flex-row items-center justify-between space-y-0">
        <CardTitle>我的流程</CardTitle>
        <WorkflowListFilters
          :status="statusFilter"
          @update:status="(value) => { statusFilter = value; fetchData() }"
          @refresh="fetchData"
        />
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
              <TableHead class="text-center">
                操作
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="loading">
              <TableCell
                colspan="6"
                class="h-24 text-center text-muted-foreground"
              >
                加载中...
              </TableCell>
            </TableRow>
            <TableRow v-else-if="workflows.length === 0">
              <TableCell
                colspan="6"
                class="h-24 text-center text-muted-foreground"
              >
                暂无流程记录
              </TableCell>
            </TableRow>
            <TableRow
              v-for="workflow in workflows"
              :key="workflow.id"
            >
              <TableCell class="font-medium">
                <div>{{ workflow.title }}</div>
                <div class="text-xs text-muted-foreground">
                  {{ workflow.remark || '无备注' }}
                </div>
              </TableCell>
              <TableCell>{{ workflow.definitionName }}</TableCell>
              <TableCell>{{ workflow.currentNodeName || '-' }}</TableCell>
              <TableCell>
                <WorkflowStatusBadge :status="workflow.status" />
              </TableCell>
              <TableCell>{{ formatDateTime(workflow.submitTime || workflow.createTime) }}</TableCell>
              <TableCell class="text-center">
                <WorkflowActionButtons
                  :workflow="workflow"
                  mode="my"
                  :can-manage="canManageMyWorkflow"
                  :can-urge="permissionState.canUrgeWorkflow"
                  :can-withdraw="permissionState.canWithdrawWorkflow"
                  :can-cancel="permissionState.canCancelWorkflow"
                  :can-draft="permissionState.canDraftWorkflow"
                  @edit-draft="openDraftDialog"
                  @submit-draft="handleQuickSubmitDraft"
                  @delete-draft="handleDeleteDraft"
                  @urge="handleUrge"
                  @withdraw="handleWithdraw"
                  @cancel="handleCancel"
                />
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <!-- 催办对话框 -->
    <Dialog v-model:open="urgeDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>催办</DialogTitle>
          <DialogDescription>发送催办提醒给审批人</DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div>
            <label class="text-sm font-medium">催办内容</label>
            <Textarea
              v-model="urgeContent"
              placeholder="请输入催办内容，如：请尽快审批"
              class="mt-2"
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            @click="urgeDialogOpen = false"
          >
            取消
          </Button>
          <Button
            :disabled="actionLoading"
            @click="handleUrgeConfirm"
          >
            发送催办
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <WorkflowStartDialog
      :open="draftDialogOpen"
      mode="draft"
      :definitions="enabledDefinitions"
      :definition-id="draftForm.definitionId"
      :title="draftForm.title"
      :remark="draftForm.remark"
      :form-config="formConfig"
      :form-values="draftFormModel"
      :field-errors="fieldErrors"
      :loading="draftLoading"
      :definition-loading="draftDefinitionLoading"
      @update:open="(value) => { draftDialogOpen = value }"
      @update:definition-id="(value) => { draftForm.definitionId = value; loadDraftDefinition(value) }"
      @update:title="(value) => { draftForm.title = value }"
      @update:remark="(value) => { draftForm.remark = value }"
      @update:form-values="(value) => { formValues = value }"
      @save-draft="handleDraftSave"
      @submit="handleDraftSubmit"
    />

    <!-- 删除草稿确认对话框 -->
    <ConfirmDialog
      v-model:open="deleteDialogOpen"
      title="确认删除"
      description="确定要删除该草稿吗？"
      confirm-text="确认删除"
      @confirm="confirmDeleteDraft"
    />
  </div>
</template>
