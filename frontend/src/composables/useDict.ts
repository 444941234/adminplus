import { computed, ref } from 'vue'
import { getDictItems } from '@/api'
import type { DictItem } from '@/types'

/**
 * 字典数据缓存
 */
const dictCache = new Map<string, DictItem[]>()

/**
 * 字典 composable
 * 提供字典数据获取和标签映射功能
 *
 * @param dictType 字典类型，如 'operation_type', 'log_type', 'user_status' 等
 * @returns 字典数据和工具方法
 *
 * @example
 * ```ts
 * const { items, labelMap, valueMap, getLabel, getValue, isLoading } = useDict('operation_type')
 *
 * // 获取标签
 * const label = getLabel('1') // '查询'
 *
 * // 直接使用映射
 * const label = labelMap.value['1'] // '查询'
 * ```
 */
export function useDict(dictType: string) {
  const items = ref<DictItem[]>([])
  const isLoading = ref(false)
  const error = ref<Error | null>(null)

  // 标签映射: value -> label
  const labelMap = computed(() => {
    const map = new Map<string, string>()
    for (const item of items.value) {
      if (item.value !== undefined) {
        map.set(String(item.value), item.label)
      }
    }
    return map
  })

  // 值映射: label -> value
  const valueMap = computed(() => {
    const map = new Map<string, string>()
    for (const item of items.value) {
      map.set(item.label, String(item.value))
    }
    return map
  })

  // 下拉选项格式
  const options = computed(() => {
    return items.value.map(item => ({
      label: item.label,
      value: item.value
    }))
  })

  /**
   * 获取字典标签
   * @param value 字典值
   * @param defaultValue 默认值（未找到时返回）
   */
  const getLabel = (value: string | number, defaultValue = '-'): string => {
    const key = String(value)
    return labelMap.value.get(key) || defaultValue
  }

  /**
   * 获取字典值
   * @param label 字典标签
   * @param defaultValue 默认值（未找到时返回）
   */
  const getValue = (label: string, defaultValue = ''): string => {
    return valueMap.value.get(label) || defaultValue
  }

  /**
   * 加载字典数据
   */
  const loadDict = async () => {
    // 检查缓存
    if (dictCache.has(dictType)) {
      items.value = dictCache.get(dictType)!
      return
    }

    isLoading.value = true
    error.value = null

    try {
      const res = await getDictItems(dictType)
      const dictItems = (res.data || []).map(item => ({
        id: item.id,
        label: item.label,
        value: item.value,
        sortOrder: item.sortOrder,
        status: item.status,
        parentId: item.parentId,
        remark: item.remark
      })) as DictItem[]

      // 排序
      dictItems.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))

      items.value = dictItems
      dictCache.set(dictType, dictItems)
    } catch (err) {
      error.value = err as Error
      console.error(`加载字典失败: ${dictType}`, err)
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 刷新字典数据
   */
  const refresh = async () => {
    dictCache.delete(dictType)
    await loadDict()
  }

  // 初始加载
  loadDict()

  return {
    items,
    labelMap,
    valueMap,
    options,
    getLabel,
    getValue,
    isLoading,
    error,
    refresh
  }
}

/**
 * 批量获取多个字典的数据
 * @param dictTypes 字典类型数组
 * @returns 字典数据映射
 *
 * @example
 * ```ts
 * const dicts = useDicts(['operation_type', 'log_type', 'user_status'])
 * const operationLabel = dicts.operation_type.getLabel('1')
 * const logLabel = dicts.log_type.getLabel('2')
 * ```
 */
export function useDicts(dictTypes: string[]) {
  const dictsMap = ref<Record<string, ReturnType<typeof useDict>>>({})

  for (const dictType of dictTypes) {
    dictsMap.value[dictType] = useDict(dictType)
  }

  const isLoading = computed(() =>
    Object.values(dictsMap.value).some(d => d.isLoading.value)
  )

  const refreshAll = async () => {
    await Promise.all(Object.values(dictsMap.value).map(d => d.refresh()))
  }

  return {
    dicts: dictsMap.value,
    isLoading,
    refreshAll
  }
}

/**
 * 清除字典缓存
 * @param dictType 字典类型，不传则清除所有缓存
 */
export function clearDictCache(dictType?: string) {
  if (dictType) {
    dictCache.delete(dictType)
  } else {
    dictCache.clear()
  }
}
