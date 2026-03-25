import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getUserList,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus,
  resetPassword,
  assignRoles,
  getUserRoles
} from '@/api/user'

// Mock the request module
vi.mock('@/api/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

import { get, post, put, del } from '@/api/request'

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserList', () => {
    it('should fetch user list with pagination', async () => {
      const mockUsers = {
        records: [
          { id: '1', username: 'admin', nickname: 'Admin', email: 'admin@example.com' },
          { id: '2', username: 'user1', nickname: 'User 1', email: 'user1@example.com' }
        ],
        total: 2
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockUsers })

      const result = await getUserList({ page: 1, size: 10, keyword: 'admin' })

      expect(get).toHaveBeenCalledWith('/sys/users', { page: 1, size: 10, keyword: 'admin' })
      expect(result.data).toEqual(mockUsers)
    })

    it('should fetch user list with department filter', async () => {
      const mockUsers = { records: [], total: 0 }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockUsers })

      await getUserList({ deptId: 'dept-123' })

      expect(get).toHaveBeenCalledWith('/sys/users', { deptId: 'dept-123' })
    })
  })

  describe('getUserById', () => {
    it('should fetch user by id', async () => {
      const mockUser = {
        id: '1',
        username: 'admin',
        nickname: 'Admin',
        email: 'admin@example.com',
        deptName: 'IT',
        roles: ['Admin']
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockUser })

      const result = await getUserById('1')

      expect(get).toHaveBeenCalledWith('/sys/users/1')
      expect(result.data).toEqual(mockUser)
    })
  })

  describe('createUser', () => {
    it('should create a new user', async () => {
      const newUser = {
        username: 'newuser',
        nickname: 'New User',
        email: 'newuser@example.com',
        password: 'password123'
      }
      const mockCreatedUser = { id: '3', ...newUser }
      delete (mockCreatedUser as any).password
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockCreatedUser })

      const result = await createUser(newUser)

      expect(post).toHaveBeenCalledWith('/sys/users', newUser)
      expect(result.data.username).toBe('newuser')
    })
  })

  describe('updateUser', () => {
    it('should update user information', async () => {
      const updates = { nickname: 'Updated Name', email: 'updated@example.com' }
      const mockUpdatedUser = {
        id: '1',
        username: 'admin',
        ...updates
      }
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: mockUpdatedUser })

      const result = await updateUser('1', updates)

      expect(put).toHaveBeenCalledWith('/sys/users/1', updates)
      expect(result.data.nickname).toBe('Updated Name')
    })
  })

  describe('deleteUser', () => {
    it('should delete a user', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteUser('1')

      expect(del).toHaveBeenCalledWith('/sys/users/1')
      expect(result.code).toBe(200)
    })
  })

  describe('updateUserStatus', () => {
    it('should update user status', async () => {
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await updateUserStatus('1', 0)

      expect(put).toHaveBeenCalledWith('/sys/users/1/status?status=0')
      expect(result.code).toBe(200)
    })
  })

  describe('resetPassword', () => {
    it('should reset user password', async () => {
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await resetPassword('1', 'newPassword123')

      expect(put).toHaveBeenCalledWith('/sys/users/1/password?password=newPassword123')
      expect(result.code).toBe(200)
    })
  })

  describe('assignRoles', () => {
    it('should assign roles to user', async () => {
      const roleIds = ['role-1', 'role-2']
      vi.mocked(put).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await assignRoles('1', roleIds)

      expect(put).toHaveBeenCalledWith('/sys/users/1/roles', roleIds)
      expect(result.code).toBe(200)
    })
  })

  describe('getUserRoles', () => {
    it('should fetch user roles', async () => {
      const mockRoles = ['Admin', 'User']
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockRoles })

      const result = await getUserRoles('1')

      expect(get).toHaveBeenCalledWith('/sys/users/1/roles')
      expect(result.data).toEqual(mockRoles)
    })
  })
})
