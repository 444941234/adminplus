import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 权限检查 composable
 * 提供权限、角色相关的检查功能
 *
 * @example
 * ```ts
 * const { hasPermission, hasRole, hasAnyPermission, hasAllPermissions } = usePermission()
 * ```
 */
export function usePermission() {
  const userStore = useUserStore()

  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: string): boolean => {
    const permissions = userStore.permissions
    if (!permissions || permissions.length === 0) {
      return false
    }
    // 超级管理员拥有所有权限
    if (permissions.includes('*')) {
      return true
    }
    return permissions.includes(permission)
  }

  /**
   * 检查是否有任意一个权限
   */
  const hasAnyPermission = (...permissions: string[]): boolean => {
    return permissions.some(p => hasPermission(p))
  }

  /**
   * 检查是否有所有指定权限
   */
  const hasAllPermissions = (...permissions: string[]): boolean => {
    return permissions.every(p => hasPermission(p))
  }

  /**
   * 检查是否有指定角色
   */
  const hasRole = (roleCode: string): boolean => {
    const userInfo = userStore.userInfo
    if (!userInfo?.roles) {
      return false
    }
    return userInfo.roles.includes(roleCode)
  }

  /**
   * 检查是否有任意一个角色
   */
  const hasAnyRole = (...roles: string[]): boolean => {
    return roles.some(r => hasRole(r))
  }

  /**
   * 检查是否有所有指定角色
   */
  const hasAllRoles = (...roles: string[]): boolean => {
    return roles.every(r => hasRole(r))
  }

  /**
   * 检查是否是超级管理员
   */
  const isSuperAdmin = computed(() => {
    return hasRole('ROLE_ADMIN') || hasPermission('*')
  })

  /**
   * 检查是否是管理员（包括超级管理员和部门经理）
   */
  const isAdmin = computed(() => {
    return hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')
  })

  /**
   * 获取当前用户的所有权限
   */
  const permissions = computed(() => userStore.permissions)

  /**
   * 获取当前用户的所有角色
   */
  const roles = computed(() => userStore.userInfo?.roles || [])

  /**
   * 权限过滤器 - 用于过滤有权限操作的项
   */
  const filterByPermission = <T extends { permission?: string }>(
    items: T[]
  ): T[] => {
    return items.filter(item => {
      if (!item.permission) return true
      return hasPermission(item.permission)
    })
  }

  /**
   * 创建权限检查函数 - 用于 v-if 指令
   */
  const createPermissionChecker = (...requiredPermissions: string[]) => {
    return (permission?: string) => {
      if (requiredPermissions.length === 0) {
        return !permission || hasPermission(permission)
      }
      return hasAllPermissions(...requiredPermissions)
    }
  }

  /**
   * 创建角色检查函数 - 用于 v-if 指令
   */
  const createRoleChecker = (...requiredRoles: string[]) => {
    return () => {
      if (requiredRoles.length === 0) return true
      return hasAllRoles(...requiredRoles)
    }
  }

  return {
    // 权限检查
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    // 角色检查
    hasRole,
    hasAnyRole,
    hasAllRoles,
    // 计算属性
    isSuperAdmin,
    isAdmin,
    permissions,
    roles,
    // 工具函数
    filterByPermission,
    createPermissionChecker,
    createRoleChecker
  }
}

/**
 * 单例模式的权限 hook
 */
let globalPermissionHook: ReturnType<typeof usePermission> | null = null

export function usePermissionSingleton() {
  if (!globalPermissionHook) {
    globalPermissionHook = usePermission()
  }
  return globalPermissionHook
}
