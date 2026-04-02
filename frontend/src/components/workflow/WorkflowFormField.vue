<script setup lang="ts">
import { ref } from 'vue'
import {
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
import { Upload, X, Paperclip } from 'lucide-vue-next'
import type { WorkflowFormField as WorkflowFormFieldType } from '@/types'

const props = defineProps<{
  field: WorkflowFormFieldType
  modelValue?: unknown
  error?: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: unknown): void
}>()

const isReadonly = () => props.readonly || props.field.readonly

const getDisplayValue = (value: unknown) => {
  if (value === null || value === undefined || value === '') return '-'
  if (Array.isArray(value)) return value.join(' ~ ')

  // 对于 select 类型，根据 options 查找对应的 label
  if (props.field.component === 'select' && props.field.options) {
    const option = props.field.options.find(opt => opt.value === value)
    if (option) return option.label
  }

  // 对于 file 类型，显示文件名
  if (props.field.component === 'file' && typeof value === 'string') {
    // 如果是 URL，提取文件名
    try {
      const url = new URL(value)
      return url.pathname.split('/').pop() || value
    } catch {
      return value
    }
  }

  return String(value)
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

// 文件上传相关
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFileName = ref<string>('')

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    selectedFileName.value = file.name
    // 这里暂时只存储文件名，实际项目需要上传到服务器获取 URL
    emit('update:modelValue', file.name)
  }
}

const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const clearFile = () => {
  selectedFileName.value = ''
  emit('update:modelValue', '')
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

// 日期范围处理
const dateRangeStart = ref<string>('')
const dateRangeEnd = ref<string>('')

const handleDateRangeChange = () => {
  if (dateRangeStart.value && dateRangeEnd.value) {
    emit('update:modelValue', [dateRangeStart.value, dateRangeEnd.value])
  } else if (dateRangeStart.value || dateRangeEnd.value) {
    emit('update:modelValue', dateRangeStart.value || dateRangeEnd.value)
  } else {
    emit('update:modelValue', '')
  }
}

// 初始化日期范围值
const initDateRange = () => {
  if (Array.isArray(props.modelValue)) {
    dateRangeStart.value = props.modelValue[0] || ''
    dateRangeEnd.value = props.modelValue[1] || ''
  }
}
initDateRange()
</script>

<template>
  <div class="space-y-2">
    <Label :for="field.field">
      {{ field.label }}
      <span v-if="field.required" class="text-destructive">*</span>
    </Label>

    <div v-if="isReadonly()" class="min-h-10 rounded-md border border-input bg-muted/30 px-3 py-2 text-sm">
      {{ getDisplayValue(modelValue) }}
    </div>

    <template v-else-if="field.component === 'textarea'">
      <Textarea
        :id="field.field"
        :model-value="typeof modelValue === 'string' || typeof modelValue === 'number' ? modelValue : ''"
        :placeholder="field.placeholder"
        @update:model-value="(value) => emit('update:modelValue', value)"
      />
    </template>

    <template v-else-if="field.component === 'select'">
      <Select
        :model-value="typeof modelValue === 'string' || typeof modelValue === 'number' ? modelValue : ''"
        @update:model-value="(value) => emit('update:modelValue', value)"
      >
        <SelectTrigger :id="field.field">
          <SelectValue :placeholder="field.placeholder || `请选择${field.label}`" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem
            v-for="option in field.options || []"
            :key="`${field.field}_${option.value}`"
            :value="String(option.value)"
          >
            {{ option.label }}
          </SelectItem>
        </SelectContent>
      </Select>
    </template>

    <template v-else-if="field.component === 'date'">
      <input
        :id="field.field"
        class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
        type="date"
        :value="typeof modelValue === 'string' ? modelValue : ''"
        @input="handleInput"
      />
    </template>

    <template v-else-if="field.component === 'daterange'">
      <div class="flex items-center gap-2">
        <input
          class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          type="date"
          :value="dateRangeStart"
          @input="dateRangeStart = ($event.target as HTMLInputElement).value; handleDateRangeChange()"
        />
        <span class="text-muted-foreground">至</span>
        <input
          class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          type="date"
          :value="dateRangeEnd"
          @input="dateRangeEnd = ($event.target as HTMLInputElement).value; handleDateRangeChange()"
        />
      </div>
    </template>

    <template v-else-if="field.component === 'number'">
      <Input
        :id="field.field"
        type="number"
        :model-value="typeof modelValue === 'string' || typeof modelValue === 'number' ? modelValue : ''"
        :placeholder="field.placeholder"
        @update:model-value="(value) => emit('update:modelValue', value)"
      />
    </template>

    <template v-else-if="field.component === 'file'">
      <div class="flex items-center gap-2">
        <input
          ref="fileInputRef"
          type="file"
          class="hidden"
          @change="handleFileSelect"
        />
        <Button
          variant="outline"
          class="flex-1 justify-start"
          @click="triggerFileSelect"
        >
          <Upload class="mr-2 h-4 w-4" />
          {{ selectedFileName || modelValue || field.placeholder || '选择文件' }}
        </Button>
        <Button
          v-if="selectedFileName || modelValue"
          variant="ghost"
          size="icon"
          @click="clearFile"
        >
          <X class="h-4 w-4" />
        </Button>
      </div>
    </template>

    <template v-else-if="field.component === 'user'">
      <!-- 用户选择器 - 暂时用输入框，后续可替换为用户选择组件 -->
      <div class="flex items-center gap-2">
        <Input
          :id="field.field"
          :model-value="typeof modelValue === 'string' ? modelValue : ''"
          :placeholder="field.placeholder || '请输入用户名或选择用户'"
          @update:model-value="(value) => emit('update:modelValue', value)"
        />
        <Button variant="outline" size="icon" disabled title="用户选择功能开发中">
          <Paperclip class="h-4 w-4" />
        </Button>
      </div>
    </template>

    <template v-else-if="field.component === 'dept'">
      <!-- 部门选择器 - 暂时用输入框，后续可替换为部门选择组件 -->
      <div class="flex items-center gap-2">
        <Input
          :id="field.field"
          :model-value="typeof modelValue === 'string' ? modelValue : ''"
          :placeholder="field.placeholder || '请输入部门名或选择部门'"
          @update:model-value="(value) => emit('update:modelValue', value)"
        />
        <Button variant="outline" size="icon" disabled title="部门选择功能开发中">
          <Paperclip class="h-4 w-4" />
        </Button>
      </div>
    </template>

    <template v-else>
      <Input
        :id="field.field"
        :model-value="typeof modelValue === 'string' || typeof modelValue === 'number' ? modelValue : ''"
        :placeholder="field.placeholder"
        @update:model-value="(value) => emit('update:modelValue', value)"
      />
    </template>

    <p v-if="field.description" class="text-xs text-muted-foreground">
      {{ field.description }}
    </p>
    <p v-if="error" class="text-xs text-destructive">
      {{ error }}
    </p>
  </div>
</template>
