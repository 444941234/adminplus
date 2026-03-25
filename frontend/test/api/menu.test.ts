import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getMenuTree,
  getUserMenuTree,
  getMenuById,
  createMenu,
  updateMenu,
  deleteMenu,
  batchUpdateStatus,
  batchDelete
} from '@/api/menu'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

import { get, post, put, del } from '@/utils/request'

describe('Menu API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMenuTree', () => {
    it('should fetch menu tree', async () => {
      const mockMenuTree = [
        {
          id: '1',
          menuName: '系统管理',
          parentId: '0',
          path: '/system',
          children: [
            { id: '2', menuName: '用户管理', parentId: '1', path: '/system/user', children: [] },
            { id: '3', menuName: '角色管理', parentId: '1', path: '/system/role', children: [] }
          ]
        }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockMenuTree })

      const result = await getMenuTree()

      expect(get).toHaveBeenCalledWith('/sys/menus/tree')
      expect(result.data).toEqual(mockMenuTree)
    })
  })

  describe('getUserMenuTree', () => {
    it('should fetch current user menu tree', async () => {
      const mockUserMenuTree = [
        {
          id: '1',
          menuName: '首页',
          parentId: '0',
          path: '/dashboard',
          children: []
        }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockUserMenuTree })

      const result = await getUserMenuTree()

      expect(get).toHaveBeenCalledWith('/sys/menus/user/tree')
      expect(result.data).toEqual(mockUserMenuTree)
    })
  })

  describe('getMenuById', () => {
    it('should fetch menu by id', async () => {
      const mockMenu = {
        id: '1',
        menuName: '用户管理',
        parentId: '0',
        path: '/system/user',
        component: 'system/User',
        sort: 1,
        status: 1
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockMenu })

      const result = await getMenuById('1')

      expect(get).toHaveBeenCalledWith('/sys/menus/1')
      expect(result.data).toEqual(mockMenu)
    })
  })

  describe('createMenu', () => {
    it('should create a new menu', async () => {
      const newMenu = {
        menuName: '新菜单',
        parentId: '0',
        path: '/new',
        component: 'New',
        sort: 1,
        status: 1
      }
      const mockCreatedMenu = { id: '4', ...newMenu }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedMenu })

      const result = await createMenu(newMenu)

      expect(post).toHaveBeenCalledWith('/sys/menus', newMenu)
      expect(result.data.menuName).toBe('新菜单')
    })
  })

  describe('updateMenu', () => {
    it('should update menu information', async () => {
      const updates = { menuName: '更新后的菜单', sort: 2 }
      const mockUpdatedMenu = {
        id: '1',
        parentId: '0',
        path: '/system/user',
        component: 'system/User',
        status: 1,
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedMenu })

      const result = await updateMenu('1', updates)

      expect(put).toHaveBeenCalledWith('/sys/menus/1', updates)
      expect(result.data.menuName).toBe('更新后的菜单')
    })
  })

  describe('deleteMenu', () => {
    it('should delete a menu', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteMenu('1')

      expect(del).toHaveBeenCalledWith('/sys/menus/1')
      expect(result.code).toBe(200)
    })
  })

  describe('batchUpdateStatus', () => {
    it('should batch update menu status', async () => {
      const ids = ['1', '2', '3']
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await batchUpdateStatus(ids, 0)

      expect(put).toHaveBeenCalledWith('/sys/menus/batch/status', { ids, status: 0 })
      expect(result.code).toBe(200)
    })
  })

  describe('batchDelete', () => {
    it('should batch delete menus', async () => {
      const ids = ['1', '2', '3']
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await batchDelete(ids)

      expect(del).toHaveBeenCalledWith('/sys/menus/batch', { ids })
      expect(result.code).toBe(200)
    })
  })
})
