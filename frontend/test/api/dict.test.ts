import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getDictList,
  getDictByType,
  getDictById,
  createDict,
  updateDict,
  deleteDict,
  getDictItems,
  updateDictStatus,
  getDictItemList,
  getDictItemById,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  updateDictItemStatus
} from '@/api/dict'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

import { get, post, put, del } from '@/utils/request'

describe('Dict API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getDictList', () => {
    it('should fetch dict list with pagination', async () => {
      const mockDicts = {
        records: [
          { id: '1', dictName: '用户状态', dictType: 'user_status', status: 1 },
          { id: '2', dictName: '角色类型', dictType: 'role_type', status: 1 }
        ],
        total: 2
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDicts })

      const result = await getDictList({ page: 1, size: 10, keyword: '用户' })

      expect(get).toHaveBeenCalledWith('/sys/dicts', { page: 1, size: 10, keyword: '用户' })
      expect(result.data).toEqual(mockDicts)
    })
  })

  describe('getDictByType', () => {
    it('should fetch dict by type', async () => {
      const mockDict = {
        id: '1',
        dictName: '用户状态',
        dictType: 'user_status',
        status: 1,
        items: []
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDict })

      const result = await getDictByType('user_status')

      expect(get).toHaveBeenCalledWith('/sys/dicts/type/user_status')
      expect(result.data).toEqual(mockDict)
    })
  })

  describe('getDictById', () => {
    it('should fetch dict by id', async () => {
      const mockDict = {
        id: '1',
        dictName: '用户状态',
        dictType: 'user_status',
        status: 1
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDict })

      const result = await getDictById('1')

      expect(get).toHaveBeenCalledWith('/sys/dicts/1')
      expect(result.data).toEqual(mockDict)
    })
  })

  describe('createDict', () => {
    it('should create a new dict', async () => {
      const newDict = {
        dictName: '新字典',
        dictType: 'new_dict',
        status: 1
      }
      const mockCreatedDict = { id: '3', ...newDict }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedDict })

      const result = await createDict(newDict)

      expect(post).toHaveBeenCalledWith('/sys/dicts', newDict)
      expect(result.data.dictName).toBe('新字典')
    })
  })

  describe('updateDict', () => {
    it('should update dict information', async () => {
      const updates = { dictName: '更新后的字典', status: 0 }
      const mockUpdatedDict = {
        id: '1',
        dictType: 'user_status',
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedDict })

      const result = await updateDict('1', updates)

      expect(put).toHaveBeenCalledWith('/sys/dicts/1', updates)
      expect(result.data.dictName).toBe('更新后的字典')
    })
  })

  describe('deleteDict', () => {
    it('should delete a dict', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteDict('1')

      expect(del).toHaveBeenCalledWith('/sys/dicts/1')
      expect(result.code).toBe(200)
    })
  })

  describe('getDictItems', () => {
    it('should fetch dict items by type', async () => {
      const mockItems = [
        { id: '1', dictLabel: '正常', dictValue: '1', sort: 1 },
        { id: '2', dictLabel: '禁用', dictValue: '0', sort: 2 }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockItems })

      const result = await getDictItems('user_status')

      expect(get).toHaveBeenCalledWith('/sys/dicts/type/user_status/items')
      expect(result.data).toEqual(mockItems)
    })
  })

  describe('updateDictStatus', () => {
    it('should update dict status', async () => {
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await updateDictStatus('1', 0)

      expect(put).toHaveBeenCalledWith('/sys/dicts/1/status?status=0')
      expect(result.code).toBe(200)
    })
  })

  describe('getDictItemList', () => {
    it('should fetch dict item list', async () => {
      const mockItems = {
        records: [
          { id: '1', dictLabel: '正常', dictValue: '1', sort: 1 },
          { id: '2', dictLabel: '禁用', dictValue: '0', sort: 2 }
        ],
        total: 2
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockItems })

      const result = await getDictItemList('1')

      expect(get).toHaveBeenCalledWith('/sys/dicts/1/items')
      expect(result.data).toEqual(mockItems)
    })
  })

  describe('getDictItemById', () => {
    it('should fetch dict item by id', async () => {
      const mockItem = { id: '1', dictLabel: '正常', dictValue: '1', sort: 1, status: 1 }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockItem })

      const result = await getDictItemById('1', '1')

      expect(get).toHaveBeenCalledWith('/sys/dicts/1/items/1')
      expect(result.data).toEqual(mockItem)
    })
  })

  describe('createDictItem', () => {
    it('should create a new dict item', async () => {
      const newItem = {
        dictLabel: '新建',
        dictValue: '3',
        sort: 3,
        status: 1
      }
      const mockCreatedItem = { id: '3', dictId: '1', ...newItem }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedItem })

      const result = await createDictItem('1', newItem)

      expect(post).toHaveBeenCalledWith('/sys/dicts/1/items', { ...newItem, dictId: '1' })
      expect(result.data.dictLabel).toBe('新建')
    })
  })

  describe('updateDictItem', () => {
    it('should update dict item', async () => {
      const updates = { dictLabel: '更新后', sort: 4 }
      const mockUpdatedItem = {
        id: '1',
        dictId: '1',
        dictValue: '1',
        status: 1,
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedItem })

      const result = await updateDictItem('1', '1', updates)

      expect(put).toHaveBeenCalledWith('/sys/dicts/1/items/1', updates)
      expect(result.data.dictLabel).toBe('更新后')
    })
  })

  describe('deleteDictItem', () => {
    it('should delete a dict item', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteDictItem('1', '1')

      expect(del).toHaveBeenCalledWith('/sys/dicts/1/items/1')
      expect(result.code).toBe(200)
    })
  })

  describe('updateDictItemStatus', () => {
    it('should update dict item status', async () => {
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await updateDictItemStatus('1', '1', 0)

      expect(put).toHaveBeenCalledWith('/sys/dicts/1/items/1/status?status=0')
      expect(result.code).toBe(200)
    })
  })
})
