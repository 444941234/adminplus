import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usePermission } from '@/composables/usePermission'

const mockUserStore = vi.hoisted(() => ({
  permissions: [] as string[],
  userInfo: {
    roles: [] as string[]
  }
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => mockUserStore)
}))

describe('usePermission', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUserStore.permissions = []
    mockUserStore.userInfo = { roles: [] }
  })

  // =========================================================================
  // 1. hasPermission
  // =========================================================================
  describe('hasPermission', () => {
    it('returns false when no permissions', () => {
      mockUserStore.permissions = []
      const { hasPermission } = usePermission()

      expect(hasPermission('user:create')).toBe(false)
    })

    it('returns false when permissions is null', () => {
      mockUserStore.permissions = null as any
      const { hasPermission } = usePermission()

      expect(hasPermission('user:create')).toBe(false)
    })

    it('returns true when user has exact permission', () => {
      mockUserStore.permissions = ['user:create', 'user:edit']
      const { hasPermission } = usePermission()

      expect(hasPermission('user:create')).toBe(true)
      expect(hasPermission('user:edit')).toBe(true)
      expect(hasPermission('user:delete')).toBe(false)
    })

    it('returns true for any permission when user has wildcard', () => {
      mockUserStore.permissions = ['*']
      const { hasPermission } = usePermission()

      expect(hasPermission('user:create')).toBe(true)
      expect(hasPermission('any:permission')).toBe(true)
      expect(hasPermission('something:else')).toBe(true)
    })
  })

  // =========================================================================
  // 2. hasAnyPermission
  // =========================================================================
  describe('hasAnyPermission', () => {
    it('returns false when no permissions match', () => {
      mockUserStore.permissions = ['user:create']
      const { hasAnyPermission } = usePermission()

      expect(hasAnyPermission('user:delete', 'user:edit')).toBe(false)
    })

    it('returns true when any permission matches', () => {
      mockUserStore.permissions = ['user:create', 'user:edit']
      const { hasAnyPermission } = usePermission()

      expect(hasAnyPermission('user:delete', 'user:edit')).toBe(true)
      expect(hasAnyPermission('user:create', 'user:delete')).toBe(true)
    })

    it('returns false when called with no permissions', () => {
      mockUserStore.permissions = ['user:create']
      const { hasAnyPermission } = usePermission()

      expect(hasAnyPermission()).toBe(false)
    })
  })

  // =========================================================================
  // 3. hasAllPermissions
  // =========================================================================
  describe('hasAllPermissions', () => {
    it('returns false when not all permissions match', () => {
      mockUserStore.permissions = ['user:create', 'user:edit']
      const { hasAllPermissions } = usePermission()

      expect(hasAllPermissions('user:create', 'user:delete')).toBe(false)
    })

    it('returns true when all permissions match', () => {
      mockUserStore.permissions = ['user:create', 'user:edit', 'user:delete']
      const { hasAllPermissions } = usePermission()

      expect(hasAllPermissions('user:create', 'user:edit')).toBe(true)
      expect(hasAllPermissions('user:create', 'user:edit', 'user:delete')).toBe(true)
    })

    it('returns true when called with no permissions', () => {
      mockUserStore.permissions = ['user:create']
      const { hasAllPermissions } = usePermission()

      expect(hasAllPermissions()).toBe(true)
    })
  })

  // =========================================================================
  // 4. hasRole
  // =========================================================================
  describe('hasRole', () => {
    it('returns false when no roles', () => {
      mockUserStore.userInfo.roles = []
      const { hasRole } = usePermission()

      expect(hasRole('ROLE_ADMIN')).toBe(false)
    })

    it('returns false when userInfo is null', () => {
      mockUserStore.userInfo = null as any
      const { hasRole } = usePermission()

      expect(hasRole('ROLE_ADMIN')).toBe(false)
    })

    it('returns true when user has role', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN', 'ROLE_USER']
      const { hasRole } = usePermission()

      expect(hasRole('ROLE_ADMIN')).toBe(true)
      expect(hasRole('ROLE_USER')).toBe(true)
      expect(hasRole('ROLE_MANAGER')).toBe(false)
    })
  })

  // =========================================================================
  // 5. hasAnyRole
  // =========================================================================
  describe('hasAnyRole', () => {
    it('returns false when no roles match', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']
      const { hasAnyRole } = usePermission()

      expect(hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')).toBe(false)
    })

    it('returns true when any role matches', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN', 'ROLE_USER']
      const { hasAnyRole } = usePermission()

      expect(hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')).toBe(true)
      expect(hasAnyRole('ROLE_USER', 'ROLE_MANAGER')).toBe(true)
    })
  })

  // =========================================================================
  // 6. hasAllRoles
  // =========================================================================
  describe('hasAllRoles', () => {
    it('returns false when not all roles match', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']
      const { hasAllRoles } = usePermission()

      expect(hasAllRoles('ROLE_USER', 'ROLE_ADMIN')).toBe(false)
    })

    it('returns true when all roles match', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_MANAGER']
      const { hasAllRoles } = usePermission()

      expect(hasAllRoles('ROLE_ADMIN', 'ROLE_USER')).toBe(true)
      expect(hasAllRoles('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MANAGER')).toBe(true)
    })
  })

  // =========================================================================
  // 7. isSuperAdmin
  // =========================================================================
  describe('isSuperAdmin', () => {
    it('returns true when user has ROLE_ADMIN role', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN']
      mockUserStore.permissions = ['user:create']

      const { isSuperAdmin } = usePermission()
      expect(isSuperAdmin.value).toBe(true)
    })

    it('returns true when user has wildcard permission', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']
      mockUserStore.permissions = ['*']

      const { isSuperAdmin } = usePermission()
      expect(isSuperAdmin.value).toBe(true)
    })

    it('returns false when user is not super admin', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']
      mockUserStore.permissions = ['user:create', 'user:edit']

      const { isSuperAdmin } = usePermission()
      expect(isSuperAdmin.value).toBe(false)
    })
  })

  // =========================================================================
  // 8. isAdmin
  // =========================================================================
  describe('isAdmin', () => {
    it('returns true when user has ROLE_ADMIN role', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN']

      const { isAdmin } = usePermission()
      expect(isAdmin.value).toBe(true)
    })

    it('returns true when user has ROLE_MANAGER role', () => {
      mockUserStore.userInfo.roles = ['ROLE_MANAGER']

      const { isAdmin } = usePermission()
      expect(isAdmin.value).toBe(true)
    })

    it('returns false when user is not admin', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']

      const { isAdmin } = usePermission()
      expect(isAdmin.value).toBe(false)
    })
  })

  // =========================================================================
  // 9. permissions computed
  // =========================================================================
  describe('permissions computed', () => {
    it('returns user permissions', () => {
      mockUserStore.permissions = ['user:create', 'user:edit']

      const { permissions } = usePermission()
      expect(permissions.value).toEqual(['user:create', 'user:edit'])
    })

    it('returns empty array when no permissions', () => {
      mockUserStore.permissions = []

      const { permissions } = usePermission()
      expect(permissions.value).toEqual([])
    })
  })

  // =========================================================================
  // 10. roles computed
  // =========================================================================
  describe('roles computed', () => {
    it('returns user roles', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN', 'ROLE_USER']

      const { roles } = usePermission()
      expect(roles.value).toEqual(['ROLE_ADMIN', 'ROLE_USER'])
    })

    it('returns empty array when no roles', () => {
      mockUserStore.userInfo.roles = []

      const { roles } = usePermission()
      expect(roles.value).toEqual([])
    })
  })

  // =========================================================================
  // 11. filterByPermission
  // =========================================================================
  describe('filterByPermission', () => {
    it('filters items by permission', () => {
      mockUserStore.permissions = ['user:create', 'user:edit']

      const items = [
        { id: 1, permission: 'user:create', name: 'Create User' },
        { id: 2, permission: 'user:delete', name: 'Delete User' },
        { id: 3, permission: 'user:edit', name: 'Edit User' },
        { id: 4, name: 'No Permission Required' }
      ]

      const { filterByPermission } = usePermission()
      const filtered = filterByPermission(items)

      expect(filtered.length).toBe(3)
      expect(filtered.map(i => i.id)).toEqual([1, 3, 4])
    })

    it('includes items without permission requirement', () => {
      mockUserStore.permissions = ['user:create']

      const items = [
        { id: 1, name: 'No Permission' },
        { id: 2, permission: 'user:create', name: 'Create User' }
      ]

      const { filterByPermission } = usePermission()
      const filtered = filterByPermission(items)

      expect(filtered.length).toBe(2)
    })
  })

  // =========================================================================
  // 12. createPermissionChecker
  // =========================================================================
  describe('createPermissionChecker', () => {
    it('creates checker that validates all required permissions', () => {
      mockUserStore.permissions = ['user:create', 'user:edit', 'user:delete']

      const { createPermissionChecker } = usePermission()
      const checker = createPermissionChecker('user:create', 'user:edit')

      expect(checker()).toBe(true)
      expect(checker('user:delete')).toBe(true)
    })

    it('creates checker that returns false when missing permissions', () => {
      mockUserStore.permissions = ['user:create']

      const { createPermissionChecker } = usePermission()
      const checker = createPermissionChecker('user:create', 'user:edit')

      expect(checker()).toBe(false)
    })

    it('creates checker that checks single permission when no required permissions', () => {
      mockUserStore.permissions = ['user:create']

      const { createPermissionChecker } = usePermission()
      const checker = createPermissionChecker()

      expect(checker('user:create')).toBe(true)
      expect(checker('user:edit')).toBe(false)
      expect(checker()).toBe(true)
    })
  })

  // =========================================================================
  // 13. createRoleChecker
  // =========================================================================
  describe('createRoleChecker', () => {
    it('creates checker that validates all required roles', () => {
      mockUserStore.userInfo.roles = ['ROLE_ADMIN', 'ROLE_USER']

      const { createRoleChecker } = usePermission()
      const checker = createRoleChecker('ROLE_ADMIN', 'ROLE_USER')

      expect(checker()).toBe(true)
    })

    it('creates checker that returns false when missing roles', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']

      const { createRoleChecker } = usePermission()
      const checker = createRoleChecker('ROLE_ADMIN', 'ROLE_USER')

      expect(checker()).toBe(false)
    })

    it('creates checker that returns true when no required roles', () => {
      mockUserStore.userInfo.roles = ['ROLE_USER']

      const { createRoleChecker } = usePermission()
      const checker = createRoleChecker()

      expect(checker()).toBe(true)
    })
  })
})