import { ref, watch } from 'vue'

export interface UseInlineEditOptions {
  onSave?: (value: string) => Promise<void> | void
  onError?: (error: Error) => void
}

export function useInlineEdit(
  initialValue: string,
  onSaveOrOptions?: ((value: string) => Promise<void> | void) | UseInlineEditOptions
) {
  // Handle both function and object signatures
  let onSave: ((value: string) => Promise<void> | void) | undefined
  let onError: ((error: Error) => void) | undefined

  if (typeof onSaveOrOptions === 'function') {
    onSave = onSaveOrOptions
  } else if (onSaveOrOptions && typeof onSaveOrOptions === 'object') {
    onSave = onSaveOrOptions.onSave
    onError = onSaveOrOptions.onError
  }

  const isEditing = ref(false)
  const value = ref(initialValue)
  const originalValue = ref(initialValue)
  const error = ref<Error | null>(null)
  const isSaving = ref(false)

  // Update original value when initial value changes from outside
  watch(() => initialValue, (newValue) => {
    if (!isEditing.value) {
      value.value = newValue
      originalValue.value = newValue
    }
  })

  function startEditing() {
    error.value = null
    isEditing.value = true
  }

  function cancelEdit() {
    value.value = originalValue.value
    isEditing.value = false
    error.value = null
  }

  async function save() {
    if (!onSave) {
      isEditing.value = false
      return
    }

    isSaving.value = true
    error.value = null

    try {
      await onSave(value.value)
      originalValue.value = value.value
      isEditing.value = false
    } catch (e) {
      const err = e instanceof Error ? e : new Error('Save failed')
      error.value = err
      value.value = originalValue.value
      isEditing.value = false
      onError?.(err)
    } finally {
      isSaving.value = false
    }
  }

  return {
    isEditing,
    value,
    error,
    isSaving,
    startEditing,
    cancelEdit,
    save
  }
}
