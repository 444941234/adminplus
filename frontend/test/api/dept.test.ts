import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getDeptTree,
  getDeptById,
  createDept,
  updateDept,
  deleteDept
} from '@/api/dept'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

import { get, post, put, del } from '@/utils/request'

describe('Dept API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getDeptTree', () => {
    it('should fetch department tree', async () => {
      const mockDeptTree = [
        {
          id: '1',
          deptName: '总公司',
          parentId: '0',
          children: [
            { id: '2', deptName: '技术部', parentId: '1', children: [] },
            { id: '3', deptName: '市场部', parentId: '1', children: [] }
          ]
        }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDeptTree })

      const result = await getDeptTree()

      expect(get).toHaveBeenCalledWith('/sys/depts/tree')
      expect(result.data).toEqual(mockDeptTree)
    })
  })

  describe('getDeptById', () => {
    it('should fetch department by id', async () => {
      const mockDept = {
        id: '1',
        deptName: '技术部',
        parentId: '0',
        sort: 1,
        status: 1
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDept })

      const result = await getDeptById('1')

      expect(get).toHaveBeenCalledWith('/sys/depts/1')
      expect(result.data).toEqual(mockDept)
    })
  })

  describe('createDept', () => {
    it('should create a new department', async () => {
      const newDept = {
        deptName: '新部门',
        parentId: '1',
        sort: 1,
        status: 1
      }
      const mockCreatedDept = { id: '4', ...newDept }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedDept })

      const result = await createDept(newDept)

      expect(post).toHaveBeenCalledWith('/sys/depts', newDept)
      expect(result.data.deptName).toBe('新部门')
    })
  })

  describe('updateDept', () => {
    it('should update department information', async () => {
      const updates = { deptName: '更新后的部门', sort: 2 }
      const mockUpdatedDept = {
        id: '1',
        parentId: '0',
        status: 1,
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedDept })

      const result = await updateDept('1', updates)

      expect(put).toHaveBeenCalledWith('/sys/depts/1', updates)
      expect(result.data.deptName).toBe('更新后的部门')
    })
  })

  describe('deleteDept', () => {
    it('should delete a department', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteDept('1')

      expect(del).toHaveBeenCalledWith('/sys/depts/1')
      expect(result.code).toBe(200)
    })
  })
})
