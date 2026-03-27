<script setup lang="ts">
import {
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea
} from '@/components/ui'
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
  return String(value)
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}
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

    <template v-else-if="field.component === 'number'">
      <Input
        :id="field.field"
        type="number"
        :model-value="typeof modelValue === 'string' || typeof modelValue === 'number' ? modelValue : ''"
        :placeholder="field.placeholder"
        @update:model-value="(value) => emit('update:modelValue', value)"
      />
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
