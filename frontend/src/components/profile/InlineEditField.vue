<script setup lang="ts">
/**
 * InlineEditField Component
 *
 * A reusable inline editing component that switches between display and edit modes.
 * Features auto-save, keyboard shortcuts (Enter/Escape), and loading states.
 *
 * @author AdminPlus
 * @since 2026-03-20
 *
 * @example
 * <InlineEditField
 *   v-model="profile.email"
 *   label="Email"
 *   type="email"
 *   :loading="updating"
 *   @save="handleUpdateField('email', $event)"
 * />
 */
import { computed, nextTick, ref } from 'vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Check, X, Loader2, Pencil } from '@lucide/vue'
import { useInlineEdit } from '@/composables/useInlineEdit'

/**
 * Component props
 */
interface Props {
  /** Current field value (v-model) */
  modelValue: string
  /** Field label displayed above the value */
  label?: string
  /** Placeholder text when value is empty */
  placeholder?: string
  /** Disables editing when true */
  readonly?: boolean
  /** Disables all interactions when true */
  disabled?: boolean
  /** Shows loading state during save operation */
  loading?: boolean
  /** HTML input type */
  type?: 'text' | 'email' | 'tel' | 'url'
}

/**
 * Component events
 */
interface Emits {
  /** Updates v-model value */
  (_e: 'update:modelValue', _value: string): void
  /** Triggered when user saves changes */
  (_e: 'save', _value: string): void
  /** Triggered when user cancels editing */
  (_e: 'cancel'): void
  /** Triggered when user enters edit mode */
  (_e: 'startEdit'): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: 'Enter value...',
  readonly: false,
  disabled: false,
  loading: false
})

const emit = defineEmits<Emits>()

const inputRef = ref<HTMLInputElement | null>(null)

/**
 * Use the composable with onSave callback
 */
const { isEditing, value, isSaving, startEditing, cancelEdit, save } = useInlineEdit(
  props.modelValue,
  {
    onSave: async (newValue: string) => {
      emit('save', newValue.trim())
    },
    onError: () => {
      // Error is handled by parent component via save event
    }
  }
)

/**
 * Returns the display value or placeholder
 */
const displayValue = computed(() => {
  return props.modelValue || props.placeholder || 'Not set'
})

/**
 * Handles entering edit mode
 */
const handleStartEditing = () => {
  if (props.readonly || props.disabled || props.loading) return
  startEditing()
  emit('startEdit')

  // Focus input on next tick
  nextTick(() => {
    inputRef.value?.focus()
  })
}

/**
 * Handles canceling edit
 */
const handleCancelEdit = () => {
  cancelEdit()
  emit('cancel')
}

/**
 * Handles keyboard shortcuts
 * - Enter: Save
 * - Escape: Cancel
 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    save()
  } else if (e.key === 'Escape') {
    e.preventDefault()
    handleCancelEdit()
  }
}
</script>

<template>
  <div
    class="inline-edit-field"
    :class="{ 'is-editing': isEditing }"
  >
    <!-- Display Mode -->
    <div
      v-if="!isEditing"
      class="inline-edit-field__display"
    >
      <div class="inline-edit-field__content">
        <label
          v-if="label"
          class="inline-edit-field__label"
        >{{ label }}</label>
        <p
          class="inline-edit-field__value"
          :class="{ 'is-empty': !modelValue }"
        >
          {{ displayValue }}
        </p>
      </div>
      <Button
        v-if="!readonly && !disabled"
        variant="ghost"
        size="icon"
        class="inline-edit-field__edit-btn"
        :disabled="loading"
        @click="handleStartEditing"
      >
        <Pencil class="h-4 w-4" />
      </Button>
    </div>

    <!-- Edit Mode -->
    <div
      v-else
      class="inline-edit-field__edit"
    >
      <div class="inline-edit-field__input-wrapper">
        <label
          v-if="label"
          class="inline-edit-field__label"
        >{{ label }}</label>
        <Input
          ref="inputRef"
          v-model="value"
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
          :disabled="loading || isSaving"
          @click="handleCancelEdit"
        >
          <X class="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          class="inline-edit-field__action-btn is-save"
          :disabled="loading || isSaving || !value.trim()"
          @click="save"
        >
          <Loader2
            v-if="loading || isSaving"
            class="h-4 w-4 animate-spin"
          />
          <Check
            v-else
            class="h-4 w-4"
          />
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
