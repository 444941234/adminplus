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
import { useWorkflowActions } from '@/composables/workflow/useWorkflowActions'
import { useWorkflowForm } from '@/composables/workflow/useWorkflowForm'
import type { WorkflowDefinition, WorkflowFormValues, WorkflowInstance } from '@/types'
import { toast } from 'vue-sonner'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const loading = ref(false)
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
const draftLoading = ref(false)
const draftDefinitionLoading = ref(false)
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

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
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

const fetchEnabledDefinitions = async () => {
  try {
    const res = await getEnabledWorkflowDefinitions()
    enabledDefinitions.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取启用流程模板失败'
    toast.error(message)
  }
}

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

const loadDraftDefinition = async (definitionId: string) => {
  draftDefinitionLoading.value = true
  try {
    const res = await getWorkflowDefinition(definitionId)
    const nextDefinition = res.data
    const existingIndex = enabledDefinitions.value.findIndex((item) => item.id === nextDefinition.id)
    if (existingIndex >= 0) {
      enabledDefinitions.value.splice(existingIndex, 1, nextDefinition)
    } else {
      enabledDefinitions.value.push(nextDefinition)
    }
    initForm(nextDefinition.formConfig, formValues.value)
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取流程模板详情失败'
    toast.error(message)
  } finally {
    draftDefinitionLoading.value = false
  }
}

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

const openDraftDialog = async (workflow: WorkflowInstance) => {
  draftLoading.value = true
  draftDialogOpen.value = true
  selectedDraftId.value = workflow.id
  try {
    const res = await getWorkflowDraftDetail(workflow.id)
    draftForm.value = {
      definitionId: res.data.instance.definitionId,
      title: res.data.instance.title,
      remark: res.data.instance.remark || ''
    }
    initForm(res.data.formConfig, res.data.formData)
    await loadDraftDefinition(res.data.instance.definitionId)
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取草稿详情失败'
    toast.error(message)
    draftDialogOpen.value = false
    selectedDraftId.value = null
  } finally {
    draftLoading.value = false
  }
}

const handleDraftSave = async () => {
  if (!permissionState.value.canDraftWorkflow) return
  if (!selectedDraftId.value || !validateDraftForm()) return

  draftLoading.value = true
  try {
    await updateWorkflowDraft(selectedDraftId.value, {
      definitionId: draftForm.value.definitionId,
      title: draftForm.value.title.trim(),
      formData: buildSubmitPayload().formData,
      remark: draftForm.value.remark.trim() || undefined
    })
    toast.success('草稿已更新')
    draftDialogOpen.value = false
    selectedDraftId.value = null
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存草稿失败'
    toast.error(message)
  } finally {
    draftLoading.value = false
  }
}

const handleDraftSubmit = async () => {
  if (!permissionState.value.canDraftWorkflow) return
  if (!selectedDraftId.value || !validateDraftForm()) return

  draftLoading.value = true
  try {
    await submitWorkflow(selectedDraftId.value, {
      definitionId: draftForm.value.definitionId,
      title: draftForm.value.title.trim(),
      formData: buildSubmitPayload().formData,
      remark: draftForm.value.remark.trim() || undefined
    })
    toast.success('草稿提交成功')
    draftDialogOpen.value = false
    selectedDraftId.value = null
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '提交草稿失败'
    toast.error(message)
  } finally {
    draftLoading.value = false
  }
}

const handleQuickSubmitDraft = async (workflow: WorkflowInstance) => {
  await openDraftDialog(workflow)
}

const handleDeleteDraft = async (workflow: WorkflowInstance) => {
  if (!permissionState.value.canDraftWorkflow) return
  if (!window.confirm('确认删除该草稿吗？')) return

  try {
    await deleteWorkflowDraft(workflow.id)
    toast.success('草稿已删除')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除草稿失败'
    toast.error(message)
  }
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
        <WorkflowListFilters :status="statusFilter" @update:status="(value) => { statusFilter = value; fetchData() }" @refresh="fetchData" />
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
                <WorkflowStatusBadge :status="workflow.status" />
              </TableCell>
              <TableCell>{{ formatDateTime(workflow.submitTime || workflow.createTime) }}</TableCell>
              <TableCell class="text-right">
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
          <Button variant="outline" @click="urgeDialogOpen = false">取消</Button>
          <Button :disabled="actionLoading" @click="handleUrgeConfirm">发送催办</Button>
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
  </div>
</template>
