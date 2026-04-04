<script setup lang="ts">
import { ref } from 'vue'
import {
  Checkbox,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
  Button
} from '@/components/ui'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import WorkflowAssigneeSelector from '@/components/workflow/WorkflowAssigneeSelector.vue'
import WorkflowHookDialog from '@/components/workflow/designer/WorkflowHookDialog.vue'

type WorkflowNodeForm = {
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: 'user' | 'role' | 'dept' | 'leader'
  approverId: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description: string
  // 钩子字段
  preSubmitValidate?: string
  preApproveValidate?: string
  preRejectValidate?: string
  preRollbackValidate?: string
  preCancelValidate?: string
  preWithdrawValidate?: string
  preAddSignValidate?: string
  postSubmitAction?: string
  postApproveAction?: string
  postRejectAction?: string
  postRollbackAction?: string
  postCancelAction?: string
  postWithdrawAction?: string
  postAddSignAction?: string
}

const props = defineProps<{
  modelValue: WorkflowNodeForm
  nodeId?: string
}>()

const emit = defineEmits<{
  (_e: 'update:modelValue', _value: WorkflowNodeForm): void
  (_e: 'refresh'): void
}>()

const hookDialogOpen = ref(false)

const updateField = <K extends keyof WorkflowNodeForm>(key: K, value: WorkflowNodeForm[K]) => {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value
  })
}

const handleHooksRefresh = () => {
  emit('refresh')
}
</script>

<template>
  <div class="space-y-4">
    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>节点名称 <span class="text-destructive">*</span></Label>
        <Input
          :model-value="modelValue.nodeName"
          placeholder="如：部门经理审批"
          @update:model-value="(value) => updateField('nodeName', String(value))"
        />
      </div>
      <div class="space-y-2">
        <Label>节点编码 <span class="text-destructive">*</span></Label>
        <Input
          :model-value="modelValue.nodeCode"
          placeholder="如：dept_manager"
          @update:model-value="(value) => updateField('nodeCode', String(value))"
        />
      </div>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div class="space-y-2">
        <Label>审批顺序</Label>
        <Input
          :model-value="modelValue.nodeOrder"
          type="number"
          min="1"
          placeholder="节点顺序"
          @update:model-value="(value) => updateField('nodeOrder', Number(value) || 1)"
        />
      </div>
      <div class="space-y-2">
        <Label>审批类型</Label>
        <Select
          :model-value="modelValue.approverType"
          @update:model-value="(value) => updateField('approverType', String(value) as WorkflowNodeForm['approverType'])"
        >
          <SelectTrigger>
            <SelectValue placeholder="请选择审批类型" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="user">
              用户审批
            </SelectItem>
            <SelectItem value="role">
              角色审批
            </SelectItem>
            <SelectItem value="dept">
              部门审批
            </SelectItem>
            <SelectItem value="leader">
              领导审批
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>

    <div class="space-y-2">
      <Label>审批对象</Label>
      <WorkflowAssigneeSelector
        :model-value="modelValue.approverId"
        :approver-type="modelValue.approverType"
        @update:model-value="(value: string) => updateField('approverId', value)"
      />
    </div>

    <div class="flex gap-6">
      <div class="flex items-center space-x-2">
        <Checkbox
          id="counterSign"
          :model-value="modelValue.isCounterSign"
          @update:model-value="(value: boolean | 'indeterminate') => updateField('isCounterSign', value === true)"
        />
        <Label
          for="counterSign"
          class="cursor-pointer text-sm font-normal"
        >会签</Label>
      </div>
      <div class="flex items-center space-x-2">
        <Checkbox
          id="autoPass"
          :model-value="modelValue.autoPassSameUser"
          @update:model-value="(value: boolean | 'indeterminate') => updateField('autoPassSameUser', value === true)"
        />
        <Label
          for="autoPass"
          class="cursor-pointer text-sm font-normal"
        >自动通过</Label>
      </div>
    </div>

    <div class="space-y-2">
      <Label>描述</Label>
      <Textarea
        :model-value="modelValue.description"
        placeholder="节点描述信息"
        @update:model-value="(value: string | number) => updateField('description', String(value))"
      />
    </div>

    <!-- 钩子配置区域 -->
    <div class="border-t pt-4 space-y-3">
      <div class="flex items-center justify-between">
        <div>
          <h4 class="text-sm font-medium">
            节点钩子
          </h4>
          <p class="text-xs text-muted-foreground">
            配置节点级别的钩子逻辑
          </p>
        </div>
        <Dialog v-model:open="hookDialogOpen">
          <DialogTrigger as-child>
            <Button
              variant="outline"
              size="sm"
              :disabled="!nodeId"
            >
              高级钩子配置
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>高级钩子配置</DialogTitle>
              <DialogDescription>配置节点的高级钩子，支持SpEL、Bean和HTTP执行器</DialogDescription>
            </DialogHeader>
            <WorkflowHookDialog
              :open="hookDialogOpen"
              :node-id="nodeId || ''"
              @update:open="hookDialogOpen = $event"
              @refresh="handleHooksRefresh"
            />
          </DialogContent>
        </Dialog>
      </div>

      <!-- 简单钩子配置（SpEL表达式） -->
      <div class="space-y-2 text-sm">
        <div class="space-y-1">
          <Label class="text-xs">提交前校验（SpEL）</Label>
          <Input
            :model-value="modelValue.preSubmitValidate"
            placeholder="#formData.amount > 0"
            @update:model-value="(value) => updateField('preSubmitValidate', String(value))"
          />
        </div>
        <div class="space-y-1">
          <Label class="text-xs">同意前校验（SpEL）</Label>
          <Input
            :model-value="modelValue.preApproveValidate"
            placeholder="#extraParams.req.comment != null && !#extraParams.req.comment.isEmpty()"
            @update:model-value="(value) => updateField('preApproveValidate', String(value))"
          />
        </div>
        <div class="space-y-1">
          <Label class="text-xs">同意后执行（SpEL）</Label>
          <Input
            :model-value="modelValue.postApproveAction"
            placeholder="@myService.updateStatus(#instance.id, &quot;approved&quot;)"
            @update:model-value="(value) => updateField('postApproveAction', String(value))"
          />
        </div>
      </div>
    </div>
  </div>
</template>
