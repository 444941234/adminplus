<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Switch,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Textarea
} from '@/components/ui'
import { createConfig, updateConfig } from '@/api'
import type { Config, ConfigGroup } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

interface Props {
  open: boolean
  config?: Config
  groups: ConfigGroup[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const { loading, run } = useAsyncAction('保存配置失败')

// Value type options
const valueTypeOptions = [
  { value: 'STRING', label: '字符串' },
  { value: 'NUMBER', label: '数字' },
  { value: 'BOOLEAN', label: '布尔值' },
  { value: 'JSON', label: 'JSON对象' },
  { value: 'ARRAY', label: '数组' },
  { value: 'SECRET', label: '密文' },
  { value: 'FILE', label: '文件' }
]

// Effect type options
const effectTypeOptions = [
  { value: 'IMMEDIATE', label: '立即生效' },
  { value: 'MANUAL', label: '手动生效' },
  { value: 'RESTART', label: '重启生效' }
]

// Form state
const formData = reactive({
  groupId: '',
  name: '',
  key: '',
  value: '',
  valueType: 'STRING',
  effectType: 'IMMEDIATE',
  defaultValue: '',
  description: '',
  isRequired: false,
  validationRule: '',
  sortOrder: 0
})

// Computed
const isEdit = computed(() => !!props.config?.id)
const dialogTitle = computed(() => (isEdit.value ? '编辑配置项' : '新增配置项'))

// Reset form
const resetForm = () => {
  formData.groupId = props.groups[0]?.id || ''
  formData.name = ''
  formData.key = ''
  formData.value = ''
  formData.valueType = 'STRING'
  formData.effectType = 'IMMEDIATE'
  formData.defaultValue = ''
  formData.description = ''
  formData.isRequired = false
  formData.validationRule = ''
  formData.sortOrder = 0
}

// Watch for config changes (for edit mode)
watch(
  () => props.config,
  (config) => {
    if (config) {
      formData.groupId = config.groupId
      formData.name = config.name
      formData.key = config.key
      formData.value = config.value
      formData.valueType = config.valueType
      formData.effectType = config.effectType
      formData.defaultValue = config.defaultValue
      formData.description = config.description
      formData.isRequired = config.isRequired
      formData.validationRule = config.validationRule
      formData.sortOrder = config.sortOrder
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// Watch dialog open to reset form when opening for add
watch(
  () => props.open,
  (open) => {
    if (open && !isEdit.value) {
      resetForm()
    }
  }
)

// Form validation
const validateForm = (): boolean => {
  if (!formData.groupId) {
    throw new Error('请选择配置分组')
  }
  if (!formData.name.trim()) {
    throw new Error('请输入配置名称')
  }
  if (!formData.key.trim()) {
    throw new Error('请输入配置键')
  }
  if (!/^[a-zA-Z][a-zA-Z0-9._-]*$/.test(formData.key)) {
    throw new Error('配置键必须以字母开头，只能包含字母、数字、点、下划线和连字符')
  }
  if (formData.valueType === 'NUMBER' && formData.value && isNaN(Number(formData.value))) {
    throw new Error('配置值类型为数字时，值必须是有效的数字')
  }
  if (formData.valueType === 'BOOLEAN' && formData.value && !['true', 'false'].includes(formData.value)) {
    throw new Error('配置值类型为布尔值时，值必须是 true 或 false')
  }
  if ((formData.valueType === 'JSON' || formData.valueType === 'ARRAY') && formData.value) {
    try {
      JSON.parse(formData.value)
    } catch {
      throw new Error('配置值格式不正确，必须是有效的JSON')
    }
  }
  return true
}

// Submit handler
const handleSubmit = () => {
  validateForm()

  run(async () => {
    const data = {
      groupId: formData.groupId,
      name: formData.name,
      key: formData.key,
      value: formData.value || undefined,
      valueType: formData.valueType,
      effectType: formData.effectType,
      defaultValue: formData.defaultValue || undefined,
      description: formData.description || undefined,
      isRequired: formData.isRequired,
      validationRule: formData.validationRule || undefined,
      sortOrder: formData.sortOrder
    }

    if (isEdit.value && props.config?.id) {
      await updateConfig(props.config.id, data)
    } else {
      await createConfig(data)
    }
  }, {
    successMessage: isEdit.value ? '配置更新成功' : '配置创建成功',
    onSuccess: () => {
      emit('success')
    }
  })
}

const handleCancel = () => {
  emit('update:open', false)
}
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>{{ dialogTitle }}</DialogTitle>
        <DialogDescription>
          {{ isEdit ? '修改配置项信息' : '创建新的配置项' }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <!-- Group Selection -->
        <div class="space-y-2">
          <Label for="groupId">配置分组 <span class="text-destructive">*</span></Label>
          <Select v-model="formData.groupId" :disabled="loading || isEdit">
            <SelectTrigger id="groupId">
              <SelectValue placeholder="选择配置分组" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="group in groups" :key="group.id" :value="group.id">
                {{ group.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- Name -->
        <div class="space-y-2">
          <Label for="name">配置名称 <span class="text-destructive">*</span></Label>
          <Input
            id="name"
            v-model="formData.name"
            placeholder="请输入配置名称"
            :disabled="loading"
          />
        </div>

        <!-- Key -->
        <div class="space-y-2">
          <Label for="key">配置键 <span class="text-destructive">*</span></Label>
          <Input
            id="key"
            v-model="formData.key"
            placeholder="请输入配置键（如：system.title）"
            :disabled="loading || isEdit"
          />
          <p class="text-xs text-muted-foreground">
            配置键必须以字母开头，只能包含字母、数字、点、下划线和连字符
          </p>
        </div>

        <!-- Value Type -->
        <div class="space-y-2">
          <Label for="valueType">值类型 <span class="text-destructive">*</span></Label>
          <Select v-model="formData.valueType" :disabled="loading">
            <SelectTrigger id="valueType">
              <SelectValue placeholder="选择值类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="type in valueTypeOptions" :key="type.value" :value="type.value">
                {{ type.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- Value -->
        <div class="space-y-2">
          <Label for="value">配置值</Label>
          <Textarea
            v-if="formData.valueType === 'JSON' || formData.valueType === 'ARRAY'"
            id="value"
            v-model="formData.value"
            placeholder="请输入JSON格式的配置值"
            :disabled="loading"
            class="min-h-[100px] font-mono text-sm"
          />
          <Input
            v-else-if="formData.valueType === 'SECRET'"
            id="value"
            v-model="formData.value"
            type="password"
            placeholder="请输入配置值（密文）"
            :disabled="loading"
          />
          <Input
            v-else
            id="value"
            v-model="formData.value"
            :type="formData.valueType === 'NUMBER' ? 'number' : 'text'"
            :placeholder="formData.valueType === 'BOOLEAN' ? 'true 或 false' : '请输入配置值'"
            :disabled="loading"
          />
        </div>

        <!-- Default Value -->
        <div class="space-y-2">
          <Label for="defaultValue">默认值</Label>
          <Input
            id="defaultValue"
            v-model="formData.defaultValue"
            placeholder="请输入默认值"
            :disabled="loading"
          />
        </div>

        <!-- Effect Type -->
        <div class="space-y-2">
          <Label for="effectType">生效方式 <span class="text-destructive">*</span></Label>
          <Select v-model="formData.effectType" :disabled="loading">
            <SelectTrigger id="effectType">
              <SelectValue placeholder="选择生效方式" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="type in effectTypeOptions" :key="type.value" :value="type.value">
                {{ type.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- Required Switch -->
        <div class="flex items-center justify-between">
          <Label for="isRequired">是否必填</Label>
          <Switch id="isRequired" v-model:checked="formData.isRequired" :disabled="loading" />
        </div>

        <!-- Validation Rule -->
        <div class="space-y-2">
          <Label for="validationRule">校验规则</Label>
          <Input
            id="validationRule"
            v-model="formData.validationRule"
            placeholder="请输入正则表达式或其他校验规则"
            :disabled="loading"
          />
          <p class="text-xs text-muted-foreground">
            例如：正则表达式 /^[a-zA-Z0-9]+$/
          </p>
        </div>

        <!-- Sort Order -->
        <div class="space-y-2">
          <Label for="sortOrder">排序</Label>
          <Input
            id="sortOrder"
            v-model.number="formData.sortOrder"
            type="number"
            placeholder="0"
            :disabled="loading"
          />
        </div>

        <!-- Description -->
        <div class="space-y-2">
          <Label for="description">描述</Label>
          <Textarea
            id="description"
            v-model="formData.description"
            placeholder="请输入配置项描述"
            :disabled="loading"
            class="min-h-[80px]"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" :disabled="loading" @click="handleCancel">取消</Button>
        <Button :disabled="loading" @click="handleSubmit">
          {{ loading ? '保存中...' : '保存' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
