import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getRoleList,
  getRoleById,
  createRole,
  updateRole,
  deleteRole,
  assignMenus,
  getRoleMenus
} from '@/api/role'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

import { get, post, put, del } from '@/utils/request'

describe('Role API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getRoleList', () => {
    it('should fetch role list', async () => {
      const mockRoles = {
        records: [
          { id: '1', roleName: 'Admin', roleKey: 'admin', sort: 1, status: 1 },
          { id: '2', roleName: 'User', roleKey: 'user', sort: 2, status: 1 }
        ],
        total: 2
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockRoles })

      const result = await getRoleList()

      expect(get).toHaveBeenCalledWith('/sys/roles')
      expect(result.data).toEqual(mockRoles)
      expect(result.data.records).toHaveLength(2)
    })
  })

  describe('getRoleById', () => {
    it('should fetch role by id', async () => {
      const mockRole = {
        id: '1',
        roleName: 'Admin',
        roleKey: 'admin',
        sort: 1,
        status: 1,
        remark: 'Administrator role'
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockRole })

      const result = await getRoleById('1')

      expect(get).toHaveBeenCalledWith('/sys/roles/1')
      expect(result.data).toEqual(mockRole)
    })
  })

  describe('createRole', () => {
    it('should create a new role', async () => {
      const newRole = {
        roleName: 'Editor',
        roleKey: 'editor',
        sort: 3,
        status: 1,
        remark: 'Editor role'
      }
      const mockCreatedRole = { id: '3', ...newRole }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedRole })

      const result = await createRole(newRole)

      expect(post).toHaveBeenCalledWith('/sys/roles', newRole)
      expect(result.data.roleName).toBe('Editor')
    })
  })

  describe('updateRole', () => {
    it('should update role information', async () => {
      const updates = { roleName: 'Super Admin', remark: 'Updated description' }
      const mockUpdatedRole = {
        id: '1',
        roleKey: 'admin',
        sort: 1,
        status: 1,
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedRole })

      const result = await updateRole('1', updates)

      expect(put).toHaveBeenCalledWith('/sys/roles/1', updates)
      expect(result.data.roleName).toBe('Super Admin')
    })
  })

  describe('deleteRole', () => {
    it('should delete a role', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteRole('1')

      expect(del).toHaveBeenCalledWith('/sys/roles/1')
      expect(result.code).toBe(200)
    })
  })

  describe('assignMenus', () => {
    it('should assign menus to role', async () => {
      const menuIds = ['menu-1', 'menu-2', 'menu-3']
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await assignMenus('1', menuIds)

      expect(put).toHaveBeenCalledWith('/sys/roles/1/menus', menuIds)
      expect(result.code).toBe(200)
    })
  })

  describe('getRoleMenus', () => {
    it('should fetch role menus', async () => {
      const mockMenus = ['menu-1', 'menu-2', 'menu-3']
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockMenus })

      const result = await getRoleMenus('1')

      expect(get).toHaveBeenCalledWith('/sys/roles/1/menus')
      expect(result.data).toEqual(mockMenus)
      expect(result.data).toHaveLength(3)
    })
  })
})
