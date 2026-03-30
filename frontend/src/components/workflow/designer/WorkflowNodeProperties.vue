<script setup lang="ts">
import {
  Checkbox,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea
} from '@/components/ui'
import WorkflowAssigneeSelector from '@/components/workflow/WorkflowAssigneeSelector.vue'

type WorkflowNodeForm = {
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: 'user' | 'role' | 'dept' | 'leader'
  approverId: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  description: string
}

const props = defineProps<{
  modelValue: WorkflowNodeForm
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: WorkflowNodeForm): void
}>()

const updateField = <K extends keyof WorkflowNodeForm>(key: K, value: WorkflowNodeForm[K]) => {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value
  })
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
            <SelectItem value="user">用户审批</SelectItem>
            <SelectItem value="role">角色审批</SelectItem>
            <SelectItem value="dept">部门审批</SelectItem>
            <SelectItem value="leader">领导审批</SelectItem>
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
        <Label for="counterSign" class="cursor-pointer text-sm font-normal">会签</Label>
      </div>
      <div class="flex items-center space-x-2">
        <Checkbox
          id="autoPass"
          :model-value="modelValue.autoPassSameUser"
          @update:model-value="(value: boolean | 'indeterminate') => updateField('autoPassSameUser', value === true)"
        />
        <Label for="autoPass" class="cursor-pointer text-sm font-normal">自动通过</Label>
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
  </div>
</template>
