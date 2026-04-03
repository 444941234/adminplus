import { beforeEach, describe, expect, it } from 'vitest'
import { ref } from 'vue'
import { useSelection } from '@/composables/useSelection'

interface TestItem {
  id: string
  name: string
}

const makeItems = (overrides: Partial<Record<string, any>> = {}) => [
  { id: 'item-001', name: 'Item 1' },
  { id: 'item-002', name: 'Item 2' },
  { id: 'item-003', name: 'Item 3' }
]

describe('useSelection', () => {
  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('initializes with empty selectedIds', () => {
      const items = ref<TestItem[]>([])
      const { selectedIds } = useSelection(items)

      expect(selectedIds.value).toEqual([])
    })

    it('initializes hasSelected as false', () => {
      const items = ref<TestItem[]>([])
      const { hasSelected } = useSelection(items)

      expect(hasSelected.value).toBe(false)
    })

    it('initializes allSelected as false when no items', () => {
      const items = ref<TestItem[]>([])
      const { allSelected } = useSelection(items)

      expect(allSelected.value).toBe(false)
    })
  })

  // =========================================================================
  // 2. Computed Properties
  // =========================================================================
  describe('Computed Properties', () => {
    it('hasSelected returns true when items are selected', () => {
      const items = ref(makeItems())
      const { selectedIds, hasSelected } = useSelection(items)

      expect(hasSelected.value).toBe(false)

      selectedIds.value = ['item-001']
      expect(hasSelected.value).toBe(true)
    })

    it('hasSelected returns false when no items selected', () => {
      const items = ref(makeItems())
      const { selectedIds, hasSelected } = useSelection(items)

      selectedIds.value = []
      expect(hasSelected.value).toBe(false)
    })

    it('allSelected returns true when all items selected', () => {
      const items = ref(makeItems())
      const { selectedIds, allSelected } = useSelection(items)

      selectedIds.value = ['item-001', 'item-002', 'item-003']
      expect(allSelected.value).toBe(true)
    })

    it('allSelected returns false when not all items selected', () => {
      const items = ref(makeItems())
      const { selectedIds, allSelected } = useSelection(items)

      selectedIds.value = ['item-001', 'item-002']
      expect(allSelected.value).toBe(false)
    })

    it('allSelected returns false when no items', () => {
      const items = ref<TestItem[]>([])
      const { allSelected } = useSelection(items)

      expect(allSelected.value).toBe(false)
    })

    it('allSelected updates reactively when items change', () => {
      const items = ref<TestItem[]>([])
      const { selectedIds, allSelected } = useSelection(items)

      expect(allSelected.value).toBe(false)

      items.value = makeItems()
      selectedIds.value = items.value.map((i) => i.id)

      expect(allSelected.value).toBe(true)
    })
  })

  // =========================================================================
  // 3. toggleSelection
  // =========================================================================
  describe('toggleSelection', () => {
    it('adds item to selection when checked is true', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelection } = useSelection(items)

      toggleSelection('item-002', true)

      expect(selectedIds.value).toEqual(['item-002'])
    })

    it('removes item from selection when checked is false', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelection } = useSelection(items)

      selectedIds.value = ['item-001', 'item-002', 'item-003']
      toggleSelection('item-002', false)

      expect(selectedIds.value).toEqual(['item-001', 'item-003'])
    })

    it('handles multiple toggles correctly', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelection } = useSelection(items)

      toggleSelection('item-001', true)
      toggleSelection('item-002', true)
      toggleSelection('item-001', false)

      expect(selectedIds.value).toEqual(['item-002'])
    })

    it('does not duplicate items when selecting same item twice', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelection } = useSelection(items)

      toggleSelection('item-001', true)
      toggleSelection('item-001', true)

      expect(selectedIds.value).toEqual(['item-001'])
    })

    it('handles toggling non-existent item gracefully', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelection } = useSelection(items)

      toggleSelection('non-existent', true)

      expect(selectedIds.value).toEqual(['non-existent'])
    })
  })

  // =========================================================================
  // 4. toggleSelectAll
  // =========================================================================
  describe('toggleSelectAll', () => {
    it('selects all items when checked is true', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelectAll } = useSelection(items)

      toggleSelectAll(true)

      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])
    })

    it('deselects all items when checked is false', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelectAll } = useSelection(items)

      selectedIds.value = ['item-001', 'item-002', 'item-003']
      toggleSelectAll(false)

      expect(selectedIds.value).toEqual([])
    })

    it('replaces existing selection when selecting all', () => {
      const items = ref(makeItems())
      const { selectedIds, toggleSelectAll } = useSelection(items)

      selectedIds.value = ['item-001']
      toggleSelectAll(true)

      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])
    })

    it('handles empty item list', () => {
      const items = ref<TestItem[]>([])
      const { selectedIds, toggleSelectAll } = useSelection(items)

      toggleSelectAll(true)

      expect(selectedIds.value).toEqual([])
    })

    it('updates selection when items list changes', () => {
      const items = ref<TestItem[]>([])
      const { selectedIds, toggleSelectAll } = useSelection(items)

      toggleSelectAll(true)
      expect(selectedIds.value).toEqual([])

      items.value = makeItems()
      toggleSelectAll(true)
      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])
    })
  })

  // =========================================================================
  // 5. Custom getId function
  // =========================================================================
  describe('Custom getId function', () => {
    it('uses custom getId function', () => {
      interface CustomItem {
        code: string
        name: string
      }
      const items = ref<CustomItem[]>([
        { code: 'A', name: 'Alpha' },
        { code: 'B', name: 'Beta' }
      ])
      const customGetId = (item: CustomItem) => item.code
      const { selectedIds, toggleSelection, toggleSelectAll } = useSelection(items, customGetId)

      toggleSelection('A', true)
      expect(selectedIds.value).toEqual(['A'])

      toggleSelectAll(true)
      expect(selectedIds.value).toEqual(['A', 'B'])
    })

    it('works with numeric IDs converted to strings', () => {
      interface NumericItem {
        id: number
        name: string
      }
      const items = ref<NumericItem[]>([
        { id: 1, name: 'One' },
        { id: 2, name: 'Two' }
      ])
      const { selectedIds, toggleSelectAll } = useSelection(items, (item) => String(item.id))

      toggleSelectAll(true)

      expect(selectedIds.value).toEqual(['1', '2'])
    })
  })

  // =========================================================================
  // 6. Integration Scenarios
  // =========================================================================
  describe('Integration Scenarios', () => {
    it('handles typical checkbox selection workflow', () => {
      const items = ref(makeItems())
      const { selectedIds, hasSelected, allSelected, toggleSelection, toggleSelectAll } = useSelection(items)

      // Initial state
      expect(hasSelected.value).toBe(false)
      expect(allSelected.value).toBe(false)

      // Select individual items
      toggleSelection('item-001', true)
      toggleSelection('item-003', true)

      expect(hasSelected.value).toBe(true)
      expect(allSelected.value).toBe(false)
      expect(selectedIds.value).toEqual(['item-001', 'item-003'])

      // Select all
      toggleSelectAll(true)

      expect(allSelected.value).toBe(true)
      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])

      // Deselect all
      toggleSelectAll(false)

      expect(hasSelected.value).toBe(false)
      expect(allSelected.value).toBe(false)
      expect(selectedIds.value).toEqual([])
    })

    it('reacts to dynamic item list changes', () => {
      const items = ref<TestItem[]>([])
      const { selectedIds, allSelected, toggleSelectAll } = useSelection(items)

      // Start with no items
      toggleSelectAll(true)
      expect(selectedIds.value).toEqual([])

      // Add items
      items.value = makeItems()
      toggleSelectAll(true)
      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])
      expect(allSelected.value).toBe(true)

      // Remove items - allSelected is true because all remaining items are selected
      items.value = items.value.slice(0, 1)
      expect(allSelected.value).toBe(true) // item-001 is still in selectedIds
      expect(selectedIds.value).toEqual(['item-001', 'item-002', 'item-003'])
    })
  })
})
