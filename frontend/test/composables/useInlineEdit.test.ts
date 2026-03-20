import { describe, it, expect, vi } from 'vitest'
import { useInlineEdit } from '@/composables/useInlineEdit'

describe('useInlineEdit', () => {
  it('should start in non-editing mode', () => {
    const { isEditing, startEditing, value } = useInlineEdit('initial', vi.fn())

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
  })

  it('should enter edit mode when startEditing is called', () => {
    const { isEditing, startEditing } = useInlineEdit('initial', vi.fn())

    startEditing()

    expect(isEditing.value).toBe(true)
  })

  it('should cancel edit and restore original value', () => {
    const onSave = vi.fn()
    const { isEditing, value, startEditing, cancelEdit } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    cancelEdit()

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
    expect(onSave).not.toHaveBeenCalled()
  })

  it('should save and call onSave with new value', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    const { isEditing, value, startEditing, save } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    await save()

    expect(isEditing.value).toBe(false)
    expect(onSave).toHaveBeenCalledWith('modified')
  })

  it('should handle save error and restore value', async () => {
    const onSave = vi.fn().mockRejectedValue(new Error('Save failed'))
    const { isEditing, value, startEditing, save, error } = useInlineEdit('initial', onSave)

    startEditing()
    value.value = 'modified'
    await save()

    expect(isEditing.value).toBe(false)
    expect(value.value).toBe('initial')
    expect(error.value).toBeInstanceOf(Error)
  })
})
