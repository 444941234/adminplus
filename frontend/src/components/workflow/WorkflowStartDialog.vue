<script setup lang="ts">
import {
  Button,
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
  Textarea
} from '@/components/ui'
import { computed } from 'vue'
import WorkflowFormRenderer from '@/components/workflow/WorkflowFormRenderer.vue'
import type { WorkflowDefinition, WorkflowFormConfig, WorkflowFormValues } from '@/types'

const props = defineProps<{
  open: boolean
  mode: 'create' | 'draft'
  definitions: WorkflowDefinition[]
  definitionId: string
  title: string
  remark: string
  formConfig?: string | WorkflowFormConfig | null
  formValues: WorkflowFormValues
  fieldErrors?: Record<string, string>
  loading?: boolean
  definitionLoading?: boolean
}>()

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'update:definitionId', _value: string): void
  (_e: 'update:title', _value: string): void
  (_e: 'update:remark', _value: string): void
  (_e: 'update:formValues', _value: WorkflowFormValues): void
  (_e: 'save-draft'): void
  (_e: 'submit'): void
}>()

const titleText = computed(() => props.mode === 'draft' ? '编辑草稿' : '新建流程')

const handleDefinitionChange = (value: unknown) => {
  emit('update:definitionId', value === null || value === undefined ? '' : String(value))
}

const handleTitleChange = (value: string | number) => {
  emit('update:title', String(value))
}

const handleRemarkChange = (value: string | number) => {
  emit('update:remark', String(value))
}

const handleFormValuesChange = (value: WorkflowFormValues) => {
  emit('update:formValues', value)
}
</script>

<template>
  <Dialog
    :open="open"
    @update:open="(value) => emit('update:open', value)"
  >
    <DialogContent class="sm:max-w-2xl max-h-[90vh] flex flex-col">
      <DialogHeader>
        <DialogTitle>{{ titleText }}</DialogTitle>
      </DialogHeader>
      <div class="flex-1 overflow-y-auto space-y-4 p-1 -m-1">
        <div class="space-y-2">
          <Label>流程类型 <span class="text-muted-foreground text-xs">(必填)</span></Label>
          <Select
            :model-value="definitionId"
            @update:model-value="handleDefinitionChange"
          >
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
          <Input
            :model-value="title"
            placeholder="例如：费用报销申请"
            @update:model-value="handleTitleChange"
          />
        </div>
        <div class="space-y-2">
          <Label>
            申请表单
            <span
              v-if="definitionLoading"
              class="ml-2 text-xs text-muted-foreground"
            >加载中...</span>
          </Label>
          <WorkflowFormRenderer
            :config="formConfig"
            :model-value="formValues"
            :errors="fieldErrors"
            :empty-text="definitionId ? '当前流程尚未配置申请表单' : '请选择流程类型后加载表单'"
            @update:model-value="handleFormValuesChange"
          />
        </div>
        <div class="space-y-2">
          <Label>备注</Label>
          <Textarea
            :model-value="remark"
            placeholder="补充说明"
            @update:model-value="handleRemarkChange"
          />
        </div>
      </div>
      <DialogFooter class="mt-4 pt-4 border-t">
        <Button
          variant="outline"
          @click="emit('update:open', false)"
        >
          取消
        </Button>
        <Button
          variant="secondary"
          :disabled="loading || definitionLoading"
          @click="emit('save-draft')"
        >
          保存草稿
        </Button>
        <Button
          :disabled="loading || definitionLoading"
          @click="emit('submit')"
        >
          提交
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
