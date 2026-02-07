import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as dictApi from '@/api/dict'

export const useDictStore = defineStore('dict', () => {
  const dictMap = ref(new Map())

  /**
   * 获取字典项（带缓存）
   * @param {string} dictType - 字典类型
   * @returns {Promise<Object[]>} 字典项列表
   */
  const getDictItems = async (dictType) => {
    // 如果缓存中有，直接返回
    if (dictMap.value.has(dictType)) {
      return dictMap.value.get(dictType)
    }

    // 否则从后端获取
    try {
      const data = await dictApi.getDictItemsByType(dictType)
      dictMap.value.set(dictType, data)
      return data
    } catch {
      // 静默失败，返回空数组
      return []
    }
  }

  /**
   * 刷新字典缓存
   * @param {string} dictType - 字典类型
   * @returns {Promise<Object[]>} 字典项列表
   */
  const refreshDict = async (dictType) => {
    try {
      const data = await dictApi.getDictItemsByType(dictType)
      dictMap.value.set(dictType, data)
      return data
    } catch {
      // 静默失败，返回空数组
      return []
    }
  }

  /**
   * 清除所有字典缓存
   */
  const clearAllDict = () => {
    dictMap.value.clear()
  }

  /**
   * 清除指定字典缓存
   * @param {string} dictType - 字典类型
   */
  const clearDict = (dictType) => {
    dictMap.value.delete(dictType)
  }

  /**
   * 根据字典值获取字典标签
   * @param {string} dictType - 字典类型
   * @param {string} value - 字典值
   * @returns {string} 字典标签
   */
  const getDictLabel = (dictType, value) => {
    const items = dictMap.value.get(dictType)
    if (!items) return value

    const item = items.find(item => item.value === value)
    return item ? item.label : value
  }

  return {
    dictMap,
    getDictItems,
    refreshDict,
    clearAllDict,
    clearDict,
    getDictLabel
  }
})