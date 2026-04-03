<script setup lang="ts">
import { Button } from '@/components/ui'
import { canOperateWorkflow } from '@/composables/workflow/useWorkflowStatus'
import type { WorkflowInstance } from '@/types'

defineProps<{
  workflow: WorkflowInstance
  mode: 'my' | 'pending'
  canManage?: boolean
  canApprove?: boolean
  canUrge?: boolean
  canWithdraw?: boolean
  canCancel?: boolean
  canDraft?: boolean
}>()

const emit = defineEmits<{
  (_e: 'urge', _workflow: WorkflowInstance): void
  (_e: 'withdraw', _workflow: WorkflowInstance): void
  (_e: 'cancel', _workflow: WorkflowInstance): void
  (_e: 'edit-draft', _workflow: WorkflowInstance): void
  (_e: 'submit-draft', _workflow: WorkflowInstance): void
  (_e: 'delete-draft', _workflow: WorkflowInstance): void
  (_e: 'approve', _workflow: WorkflowInstance): void
  (_e: 'reject', _workflow: WorkflowInstance): void
}>()
</script>

<template>
  <div class="flex justify-end gap-2">
    <RouterLink :to="`/workflow/detail/${workflow.id}`">
      <Button size="sm" variant="outline">详情</Button>
    </RouterLink>
    <Button
      v-if="mode === 'my' && canManage && canDraft && workflow.status === 'DRAFT'"
      size="sm"
      variant="outline"
      @click="emit('edit-draft', workflow)"
    >
      编辑
    </Button>
    <Button
      v-if="mode === 'my' && canManage && canDraft && workflow.status === 'DRAFT'"
      size="sm"
      @click="emit('submit-draft', workflow)"
    >
      继续提交
    </Button>
    <Button
      v-if="mode === 'my' && canManage && canDraft && workflow.status === 'DRAFT'"
      size="sm"
      variant="ghost"
      @click="emit('delete-draft', workflow)"
    >
      删除
    </Button>
    <Button
      v-if="mode === 'my' && canManage && canUrge && workflow.status === 'PROCESSING'"
      size="sm"
      variant="outline"
      @click="emit('urge', workflow)"
    >
      催办
    </Button>
    <Button
      v-if="mode === 'my' && canManage && canWithdraw"
      size="sm"
      variant="outline"
      :disabled="!canOperateWorkflow(workflow.status)"
      @click="emit('withdraw', workflow)"
    >
      撤回
    </Button>
    <Button
      v-if="mode === 'my' && canManage && canCancel"
      size="sm"
      variant="ghost"
      :disabled="!canOperateWorkflow(workflow.status)"
      @click="emit('cancel', workflow)"
    >
      取消
    </Button>
    <Button
      v-if="mode === 'pending' && canApprove"
      size="sm"
      @click="emit('approve', workflow)"
    >
      通过
    </Button>
    <Button
      v-if="mode === 'pending' && canApprove"
      size="sm"
      variant="destructive"
      @click="emit('reject', workflow)"
    >
      驳回
    </Button>
  </div>
</template>
