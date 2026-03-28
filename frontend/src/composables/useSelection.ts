import { computed, type Ref } from 'vue'

import { ref } from 'vue'

/**
 * Generic composable for managing batch selection state (checkbox select/deselect)
 * across a list of items
 *
 * @typeparam T The item type
 */
export function useSelection<T extends { id: string }>(
  items: Ref<T[]>,
  getId: (item: T) => string = (item) => item.id
) {
  const selectedIds = ref<string[]>([])

  const hasSelected = computed(() => selectedIds.value.length > 0)

  const allSelected = computed(
    () => items.value.length > 0 && items.value.every((item) => selectedIds.value.includes(getId(item)))
  )

  const toggleSelectAll = (checked: boolean) => {
    selectedIds.value = checked ? items.value.map((item) => getId(item)) : []
  }

  const toggleSelection = (itemId: string, checked: boolean) => {
    const next = new Set(selectedIds.value)
    if (checked) {
      next.add(itemId)
    } else {
      next.delete(itemId)
    }
    selectedIds.value = Array.from(next)
  }

  return {
    selectedIds,
    hasSelected,
    allSelected,
    toggleSelectAll,
    toggleSelection
  }
}
