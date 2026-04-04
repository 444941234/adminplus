import { describe, it, expect } from 'vitest'
import { ref, nextTick } from 'vue'
import { useTreeData } from '@/composables/useTreeData'

interface TestItem {
  id: string
  name: string
  children?: TestItem[]
}

describe('useTreeData', () => {
  const createTestTree = (): TestItem[] => [
    {
      id: '1',
      name: 'Root 1',
      children: [
        { id: '1-1', name: 'Child 1-1' },
        { id: '1-2', name: 'Child 1-2', children: [{ id: '1-2-1', name: 'Grandchild 1-2-1' }] }
      ]
    },
    { id: '2', name: 'Root 2' }
  ]

  describe('expandedKeys', () => {
    it('should initialize with empty set', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { expandedKeys } = useTreeData(treeData)

      expect(expandedKeys.value.size).toBe(0)
    })
  })

  describe('toggleExpand', () => {
    it('should expand a collapsed node', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { expandedKeys, toggleExpand, isExpanded } = useTreeData(treeData)

      toggleExpand('1')
      expect(isExpanded('1')).toBe(true)
      expect(expandedKeys.value.has('1')).toBe(true)
    })

    it('should collapse an expanded node', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { expandedKeys, toggleExpand, isExpanded } = useTreeData(treeData)

      toggleExpand('1')
      expect(isExpanded('1')).toBe(true)

      toggleExpand('1')
      expect(isExpanded('1')).toBe(false)
      expect(expandedKeys.value.has('1')).toBe(false)
    })
  })

  describe('expandAll', () => {
    it('should expand all nodes with children', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { expandedKeys, expandAll, isExpanded } = useTreeData(treeData)

      expandAll()

      expect(isExpanded('1')).toBe(true)
      expect(isExpanded('1-2')).toBe(true)
      expect(isExpanded('2')).toBe(false) // No children
    })
  })

  describe('collapseAll', () => {
    it('should collapse all nodes', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { expandedKeys, expandAll, collapseAll, isExpanded } = useTreeData(treeData)

      expandAll()
      expect(expandedKeys.value.size).toBe(2)

      collapseAll()
      expect(expandedKeys.value.size).toBe(0)
      expect(isExpanded('1')).toBe(false)
    })
  })

  describe('flattenedRows', () => {
    it('should return all items at root level when none expanded', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { flattenedRows } = useTreeData(treeData)

      expect(flattenedRows.value).toHaveLength(2)
      expect(flattenedRows.value[0].key).toBe('1')
      expect(flattenedRows.value[1].key).toBe('2')
    })

    it('should include children when parent is expanded', async () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { flattenedRows, toggleExpand } = useTreeData(treeData)

      toggleExpand('1')
      await nextTick()

      expect(flattenedRows.value).toHaveLength(4) // Root 1, Child 1-1, Child 1-2, Root 2
      expect(flattenedRows.value[0].level).toBe(0)
      expect(flattenedRows.value[1].level).toBe(1)
    })

    it('should include nested children when all expanded', async () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { flattenedRows, expandAll } = useTreeData(treeData)

      expandAll()
      await nextTick()

      expect(flattenedRows.value).toHaveLength(5) // All items
      expect(flattenedRows.value.find(r => r.key === '1-2-1')?.level).toBe(2)
    })
  })

  describe('getChildren', () => {
    it('should return direct children', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { getChildren, findNodeByKey } = useTreeData(treeData)

      const root = findNodeByKey(treeData.value, '1')
      expect(root).toBeDefined()

      const children = getChildren(root!)
      expect(children).toHaveLength(2)
      expect(children[0].id).toBe('1-1')
    })

    it('should return empty array for leaf nodes', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { getChildren, findNodeByKey } = useTreeData(treeData)

      const leaf = findNodeByKey(treeData.value, '2')
      expect(leaf).toBeDefined()

      const children = getChildren(leaf!)
      expect(children).toHaveLength(0)
    })
  })

  describe('getDescendants', () => {
    it('should return all descendants recursively', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { getDescendants, findNodeByKey } = useTreeData(treeData)

      const root = findNodeByKey(treeData.value, '1')
      expect(root).toBeDefined()

      const descendants = getDescendants(root!)
      expect(descendants).toHaveLength(3)
      expect(descendants.map(d => d.id)).toEqual(['1-1', '1-2', '1-2-1'])
    })
  })

  describe('getDescendantKeys', () => {
    it('should return all descendant keys including self', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { getDescendantKeys, findNodeByKey } = useTreeData(treeData)

      const node = findNodeByKey(treeData.value, '1-2')
      expect(node).toBeDefined()

      const keys = getDescendantKeys(node!)
      expect(keys).toEqual(['1-2', '1-2-1'])
    })
  })

  describe('findNodeByKey', () => {
    it('should find node by key', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { findNodeByKey } = useTreeData(treeData)

      const node = findNodeByKey(treeData.value, '1-2-1')
      expect(node).toBeDefined()
      expect(node?.name).toBe('Grandchild 1-2-1')
    })

    it('should return undefined for non-existent key', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { findNodeByKey } = useTreeData(treeData)

      const node = findNodeByKey(treeData.value, 'non-existent')
      expect(node).toBeUndefined()
    })
  })

  describe('getAllKeys', () => {
    it('should return all keys from tree', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { getAllKeys } = useTreeData(treeData)

      const keys = getAllKeys(treeData.value)
      expect(keys).toEqual(['1', '1-1', '1-2', '1-2-1', '2'])
    })
  })

  describe('buildParentOptions', () => {
    it('should build options with correct indentation', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { buildParentOptions } = useTreeData(treeData)

      const options = buildParentOptions(treeData.value)
      expect(options).toHaveLength(5)
      expect(options[0].label).toBe('Root 1')
      expect(options[1].label).toContain('Child 1-1')
    })

    it('should exclude specified key', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { buildParentOptions } = useTreeData(treeData)

      const options = buildParentOptions(treeData.value, '1')
      expect(options.find(o => o.id === '1')).toBeUndefined()
      expect(options).toHaveLength(4)
    })
  })

  describe('findInTree', () => {
    it('should find item matching predicate', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { findInTree } = useTreeData(treeData)

      const found = findInTree(treeData.value, item => item.name === 'Child 1-2')
      expect(found).toBeDefined()
      expect(found?.id).toBe('1-2')
    })

    it('should return undefined when not found', () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { findInTree } = useTreeData(treeData)

      const found = findInTree(treeData.value, item => item.name === 'Non-existent')
      expect(found).toBeUndefined()
    })
  })

  describe('reactivity', () => {
    it('should update flattenedRows when tree data changes', async () => {
      const treeData = ref<TestItem[]>(createTestTree())
      const { flattenedRows } = useTreeData(treeData)

      expect(flattenedRows.value).toHaveLength(2)

      treeData.value = [{ id: 'new', name: 'New Root' }]
      await nextTick()

      expect(flattenedRows.value).toHaveLength(1)
      expect(flattenedRows.value[0].key).toBe('new')
    })
  })
})