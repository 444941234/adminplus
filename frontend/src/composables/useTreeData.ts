import { computed, ref, type ComputedRef, type Ref, type MaybeRefOrGetter, toValue } from 'vue'

/**
 * Configuration options for tree data handling
 */
export interface TreeOptions<T> {
  /** Key field name (default: 'id') */
  keyField?: keyof T
  /** Parent ID field name (default: 'parentId') */
  parentField?: keyof T
  /** Children field name (default: 'children') */
  childrenField?: keyof T
}

/**
 * Base type for tree items - must have id and optionally children
 */
export interface TreeItemBase {
  id: string
  children?: unknown
}

/**
 * Flattened tree row with level information
 */
export interface FlattenedTreeRow<T> {
  /** Original item data */
  item: T
  /** Key/ID of the item */
  key: string
  /** Nesting level (0 for root) */
  level: number
  /** Whether item has children */
  hasChildren: boolean
  /** Whether item is expanded (children visible) */
  isExpanded: boolean
}

/**
 * Return type of useTreeData composable
 */
export interface TreeReturn<T> {
  /** Set of expanded keys */
  expandedKeys: Ref<Set<string>>
  /** Computed flattened rows with level tracking */
  flattenedRows: ComputedRef<FlattenedTreeRow<T>[]>
  /** Toggle expansion state of a node */
  toggleExpand: (key: string) => void
  /** Expand all nodes with children */
  expandAll: () => void
  /** Collapse all nodes */
  collapseAll: () => void
  /** Check if a node is expanded */
  isExpanded: (key: string) => boolean
  /** Flatten tree to array (utility function) */
  flattenTree: <R>(items: T[], callback: (item: T, level: number) => R, level?: number) => R[]
  /** Find item in tree matching predicate */
  findInTree: (items: T[], predicate: (item: T) => boolean) => T | undefined
  /** Get direct children of an item */
  getChildren: (item: T) => T[]
  /** Get all descendants of an item (recursive) */
  getDescendants: (item: T) => T[]
  /** Get all descendant keys/IDs of an item */
  getDescendantKeys: (item: T) => string[]
  /** Find node by key and return it */
  findNodeByKey: (items: T[], key: string) => T | undefined
  /** Collect all keys from tree */
  getAllKeys: (items: T[]) => string[]
  /** Build parent options for dropdown (flattened with indentation) */
  buildParentOptions: (items: T[], excludeKey?: string, indent?: string) => Array<{ id: string; label: string }>
}

/**
 * Composable for handling tree data with expand/collapse functionality
 *
 * @param treeData - Reactive tree data source (ref, computed, or getter)
 * @param options - Configuration options for field names
 * @returns Tree handling utilities and state
 *
 * @example
 * ```ts
 * const menus = ref<Menu[]>([])
 * const { expandedKeys, flattenedRows, toggleExpand, expandAll } = useTreeData(menus)
 *
 * // In template:
 * // <tr v-for="row in flattenedRows" :key="row.key">
 * //   <div :style="{ paddingLeft: `${row.level * 24}px` }">
 * //     <button v-if="row.hasChildren" @click="toggleExpand(row.key)">
 * //       {{ row.isExpanded ? '−' : '+' }}
 * //     </button>
 * //   </div>
 * // </tr>
 * ```
 */
export function useTreeData<T extends TreeItemBase>(
  treeData: MaybeRefOrGetter<T[]>,
  options: TreeOptions<T> = {}
): TreeReturn<T> {
  const {
    keyField = 'id' as keyof T,
    childrenField = 'children' as keyof T
  } = options

  // State for tracking expanded nodes
  const expandedKeys = ref<Set<string>>(new Set())

  // Helper to get key from item
  const getKey = (item: T): string => {
    const key = item[keyField]
    return typeof key === 'string' ? key : String(key)
  }

  // Helper to get children from item
  const getChildrenInternal = (item: T): T[] => {
    const children = item[childrenField]
    return Array.isArray(children) ? children as T[] : []
  }

  // Toggle expansion state
  const toggleExpand = (key: string) => {
    const next = new Set(expandedKeys.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    expandedKeys.value = next
  }

  // Check if node is expanded
  const isExpanded = (key: string): boolean => {
    return expandedKeys.value.has(key)
  }

  // Expand all nodes that have children
  const expandAll = () => {
    const next = new Set<string>()
    const walk = (items: T[]) => {
      items.forEach((item) => {
        const children = getChildrenInternal(item)
        if (children.length > 0) {
          next.add(getKey(item))
          walk(children)
        }
      })
    }
    walk(toValue(treeData))
    expandedKeys.value = next
  }

  // Collapse all nodes
  const collapseAll = () => {
    expandedKeys.value = new Set()
  }

  // Get direct children
  const getChildren = (item: T): T[] => {
    return getChildrenInternal(item)
  }

  // Flatten tree with callback
  const flattenTree = <R>(
    items: T[],
    callback: (item: T, level: number) => R,
    level = 0
  ): R[] => {
    const result: R[] = []
    items.forEach((item) => {
      result.push(callback(item, level))
      const children = getChildrenInternal(item)
      if (children.length > 0) {
        result.push(...flattenTree(children, callback, level + 1))
      }
    })
    return result
  }

  // Find item matching predicate
  const findInTree = (items: T[], predicate: (item: T) => boolean): T | undefined => {
    for (const item of items) {
      if (predicate(item)) return item
      const found = findInTree(getChildrenInternal(item), predicate)
      if (found) return found
    }
    return undefined
  }

  // Find node by key
  const findNodeByKey = (items: T[], key: string): T | undefined => {
    return findInTree(items, (item) => getKey(item) === key)
  }

  // Get all descendants (recursive)
  const getDescendants = (item: T): T[] => {
    const result: T[] = []
    const children = getChildrenInternal(item)
    children.forEach((child) => {
      result.push(child)
      result.push(...getDescendants(child))
    })
    return result
  }

  // Get all descendant keys
  const getDescendantKeys = (item: T): string[] => {
    return [getKey(item), ...getDescendants(item).map(getKey)]
  }

  // Get all keys from tree
  const getAllKeys = (items: T[]): string[] => {
    return flattenTree(items, (item) => getKey(item))
  }

  // Build parent options for dropdown
  const buildParentOptions = (
    items: T[],
    excludeKey?: string,
    indent = '\u3000' // Full-width space for visual indentation
  ): Array<{ id: string; label: string }> => {
    const options: Array<{ id: string; label: string }> = []
    const walk = (menuList: T[], level = 0) => {
      menuList.forEach((item) => {
        const key = getKey(item)
        if (key !== excludeKey) {
          // Get name - try common field names
          const nameField = 'name' as keyof T
          const name = item[nameField]
          options.push({
            id: key,
            label: `${indent.repeat(level)}${typeof name === 'string' ? name : key}`
          })
        }
        const children = getChildrenInternal(item)
        if (children.length > 0) {
          walk(children, level + 1)
        }
      })
    }
    walk(items)
    return options
  }

  // Computed flattened rows with expand state
  const flattenedRows = computed<FlattenedTreeRow<T>[]>(() => {
    const rows: FlattenedTreeRow<T>[] = []
    const items = toValue(treeData)

    const walk = (menuList: T[], level = 0) => {
      menuList.forEach((item) => {
        const key = getKey(item)
        const children = getChildrenInternal(item)
        const hasChildren = children.length > 0
        const expanded = expandedKeys.value.has(key)

        rows.push({
          item,
          key,
          level,
          hasChildren,
          isExpanded: expanded
        })

        // Only render children if expanded
        if (hasChildren && expanded) {
          walk(children, level + 1)
        }
      })
    }

    walk(items)
    return rows
  })

  return {
    expandedKeys,
    flattenedRows,
    toggleExpand,
    expandAll,
    collapseAll,
    isExpanded,
    flattenTree,
    findInTree,
    getChildren,
    getDescendants,
    getDescendantKeys,
    findNodeByKey,
    getAllKeys,
    buildParentOptions
  }
}