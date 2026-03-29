<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
  TableRow
} from '@/components/ui'
import { createWorkflowDraft, getEnabledWorkflowDefinitions, getWorkflowDefinition, getWorkflowDefinitions, startWorkflow } from '@/api'
import WorkflowStartDialog from '@/components/workflow/WorkflowStartDialog.vue'
import { useWorkflowForm } from '@/composables/workflow/useWorkflowForm'
import type { WorkflowDefinition, WorkflowFormValues } from '@/types'
import { toast } from 'vue-sonner'
import { Play } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

const loading = ref(false)
const dialogLoading = ref(false)
const definitionLoading = ref(false)
const definitions = ref<WorkflowDefinition[]>([])
const selectedDefinition = ref<WorkflowDefinition | null>(null)
const startDialogOpen = ref(false)
const userStore = useUserStore()
const {
  formConfig,
  formValues,
  fieldErrors,
  initForm,
  validateForm,
  buildSubmitPayload
} = useWorkflowForm()

const form = ref({
  definitionId: '',
  title: '',
  remark: ''
})

const permissionState = computed(() => getWorkflowPermissionState(userStore.hasPermission))
const canCreateWorkflow = computed(() => permissionState.value.canStartWorkflow)
const formModel = computed<WorkflowFormValues>({
  get: () => formValues.value,
  set: (value) => {
    formValues.value = value
  }
})

import { formatDateTime } from '@/utils/format'

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

const openStartDialog = async (definition?: WorkflowDefinition) => {
  form.value = {
    definitionId: definition?.id || '',
    title: definition ? `${definition.definitionName}申请` : '',
    remark: ''
  }
  selectedDefinition.value = definition || null
  initForm(definition?.formConfig)
  startDialogOpen.value = true

  if (definition?.id) {
    await loadDefinitionDetail(definition.id)
  }
}

const loadDefinitionDetail = async (definitionId: string) => {
  definitionLoading.value = true
  dialogLoading.value = true
  try {
    const res = await getWorkflowDefinition(definitionId)
    selectedDefinition.value = res.data
    initForm(res.data.formConfig)
    if (!form.value.title.trim()) {
      form.value.title = `${res.data.definitionName}申请`
    }
  } catch (error) {
    initForm()
    const message = error instanceof Error ? error.message : '获取流程模板详情失败'
    toast.error(message)
  } finally {
    definitionLoading.value = false
    dialogLoading.value = false
  }
}

const validateBeforeSubmit = () => {
  if (!form.value.definitionId) {
    toast.warning('请选择流程类型')
    return false
  }
  if (!form.value.title.trim()) {
    toast.warning('请输入流程标题')
    return false
  }
  if (!validateForm()) {
    toast.warning('请完善表单必填项')
    return false
  }
  return true
}

const handleStartWorkflow = async () => {
  if (!validateBeforeSubmit()) return

  dialogLoading.value = true
  try {
    await startWorkflow({
      definitionId: form.value.definitionId,
      title: form.value.title.trim(),
      formData: buildSubmitPayload().formData,
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

const handleSaveDraft = async () => {
  if (!permissionState.value.canDraftWorkflow) {
    toast.warning('当前没有保存草稿权限')
    return
  }
  if (!validateBeforeSubmit()) return

  dialogLoading.value = true
  try {
    await createWorkflowDraft({
      definitionId: form.value.definitionId,
      title: form.value.title.trim(),
      formData: buildSubmitPayload().formData,
      remark: form.value.remark.trim() || undefined
    })
    toast.success('草稿已保存')
    startDialogOpen.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存草稿失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

watch(
  () => form.value.definitionId,
  async (definitionId, previousDefinitionId) => {
    if (!startDialogOpen.value || !definitionId || definitionId === previousDefinitionId) return
    await loadDefinitionDetail(definitionId)
  }
)

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

    <WorkflowStartDialog
      :open="startDialogOpen"
      mode="create"
      :definitions="definitions"
      :definition-id="form.definitionId"
      :title="form.title"
      :remark="form.remark"
      :form-config="selectedDefinition?.formConfig || formConfig"
      :form-values="formModel"
      :field-errors="fieldErrors"
      :loading="dialogLoading"
      :definition-loading="definitionLoading"
      @update:open="(value) => { startDialogOpen = value }"
      @update:definition-id="(value) => { form.definitionId = value }"
      @update:title="(value) => { form.title = value }"
      @update:remark="(value) => { form.remark = value }"
      @update:form-values="(value) => { formValues = value }"
      @save-draft="handleSaveDraft"
      @submit="handleStartWorkflow"
    />
  </div>
</template>
