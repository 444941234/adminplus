import { describe, expect, it } from 'vitest'
import {
  getDashboardQuickActions,
  getRolePagePermissionState,
  getUserPagePermissionState,
  getWorkflowPermissionState
} from '@/lib/page-permissions'

const createPermissionChecker = (permissions: string[]) => (permission: string) => permissions.includes(permission)

describe('page permission helpers', () => {
  it('filters dashboard quick actions by any matching permission', () => {
    const actions = getDashboardQuickActions(
      createPermissionChecker(['user:query', 'workflow:approve', 'file:list'])
    )

    expect(actions.map((action) => action.path)).toEqual([
      '/system/user',
      '/system/file',
      '/workflow/pending'
    ])
  })

  it('combines workflow detail visibility with approval permission', () => {
    const resultWithApprove = getWorkflowPermissionState(createPermissionChecker(['workflow:approve']), true)
    expect(resultWithApprove.canApprovePendingActions).toBe(true)
    expect(resultWithApprove.canApproveDetail).toBe(true)

    const resultWithoutApprove = getWorkflowPermissionState(createPermissionChecker(['workflow:approve']), false)
    expect(resultWithoutApprove.canApprovePendingActions).toBe(true)
    expect(resultWithoutApprove.canApproveDetail).toBe(false)
  })

  it('returns user page action capabilities from permission keys', () => {
    expect(
      getUserPagePermissionState(createPermissionChecker(['user:add', 'user:assign']))
    ).toEqual({
      canAddUser: true,
      canEditUser: false,
      canDeleteUser: false,
      canAssignUser: true
    })
  })

  it('returns role page action capabilities from permission keys', () => {
    expect(
      getRolePagePermissionState(createPermissionChecker(['role:edit', 'role:delete']))
    ).toEqual({
      canAddRole: false,
      canEditRole: true,
      canDeleteRole: true,
      canAssignRole: false
    })
  })
})
