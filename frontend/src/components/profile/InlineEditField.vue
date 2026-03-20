<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Check, X, Loader2, Pencil } from 'lucide-vue-next'

interface Props {
  modelValue: string
  label?: string
  placeholder?: string
  readonly?: boolean
  disabled?: boolean
  loading?: boolean
  type?: 'text' | 'email' | 'tel' | 'url'
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'save', value: string): void
  (e: 'cancel'): void
  (e: 'startEdit'): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: 'Enter value...',
  readonly: false,
  disabled: false,
  loading: false
})

const emit = defineEmits<Emits>()

const isEditing = ref(false)
const inputValue = ref(props.modelValue)
const inputRef = ref<HTMLInputElement | null>(null)

// Update local value when modelValue changes externally
watch(() => props.modelValue, (newValue) => {
  inputValue.value = newValue
})

const displayValue = computed(() => {
  return props.modelValue || props.placeholder || 'Not set'
})

const startEditing = () => {
  if (props.readonly || props.disabled || props.loading) return
  inputValue.value = props.modelValue
  isEditing.value = true
  emit('startEdit')

  // Focus input on next tick
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const cancelEdit = () => {
  inputValue.value = props.modelValue
  isEditing.value = false
  emit('cancel')
}

const saveEdit = () => {
  if (inputValue.value.trim() === props.modelValue) {
    isEditing.value = false
    return
  }

  emit('save', inputValue.value.trim())
}

// Handle Enter key to save, Escape to cancel
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    saveEdit()
  } else if (e.key === 'Escape') {
    e.preventDefault()
    cancelEdit()
  }
}
</script>

<template>
  <div class="inline-edit-field" :class="{ 'is-editing': isEditing }">
    <!-- Display Mode -->
    <div v-if="!isEditing" class="inline-edit-field__display">
      <div class="inline-edit-field__content">
        <label v-if="label" class="inline-edit-field__label">{{ label }}</label>
        <p class="inline-edit-field__value" :class="{ 'is-empty': !modelValue }">
          {{ displayValue }}
        </p>
      </div>
      <Button
        v-if="!readonly && !disabled"
        variant="ghost"
        size="icon"
        class="inline-edit-field__edit-btn"
        :disabled="loading"
        @click="startEditing"
      >
        <Pencil class="h-4 w-4" />
      </Button>
    </div>

    <!-- Edit Mode -->
    <div v-else class="inline-edit-field__edit">
      <div class="inline-edit-field__input-wrapper">
        <label v-if="label" class="inline-edit-field__label">{{ label }}</label>
        <Input
          ref="inputRef"
          v-model="inputValue"
          :type="type"
          :placeholder="placeholder"
          class="inline-edit-field__input"
          @keydown="handleKeydown"
        />
      </div>
      <div class="inline-edit-field__actions">
        <Button
          variant="ghost"
          size="icon"
          class="inline-edit-field__action-btn"
          :disabled="loading"
          @click="cancelEdit"
        >
          <X class="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          class="inline-edit-field__action-btn is-save"
          :disabled="loading || !inputValue.trim()"
          @click="saveEdit"
        >
          <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
          <Check v-else class="h-4 w-4" />
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inline-edit-field {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.inline-edit-field__display,
.inline-edit-field__edit {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.inline-edit-field__content,
.inline-edit-field__input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.inline-edit-field__label {
  font-size: 13px;
  font-weight: 500;
  color: rgb(100 116 139);
  letter-spacing: 0.01em;
}

.inline-edit-field__value {
  font-size: 15px;
  line-height: 1.6;
  color: rgb(15 23 42);
  word-break: break-word;
  margin: 0;
}

.inline-edit-field__value.is-empty {
  color: rgb(148 163 184);
  font-style: italic;
}

.inline-edit-field__edit-btn {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.inline-edit-field:hover .inline-edit-field__edit-btn {
  opacity: 1;
}

.inline-edit-field__input {
  font-size: 15px;
}

.inline-edit-field__actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 24px; /* Align with input, not label */
}

.inline-edit-field__action-btn {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.inline-edit-field__action-btn.is-save {
  color: rgb(34 197 94);
}

.inline-edit-field__action-btn.is-save:hover:not(:disabled) {
  background-color: rgb(34 197 94 / 0.1);
}

/* Mobile responsive */
@media (max-width: 640px) {
  .inline-edit-field__edit-btn {
    opacity: 1;
  }

  .inline-edit-field__actions {
    margin-top: 0;
  }
}
</style>
