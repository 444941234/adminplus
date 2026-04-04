import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// ---------------------------------------------------------------------------
// Mocks — must come before importing the component under test
// ---------------------------------------------------------------------------

vi.mock('vue-sonner', () => {
  const mock = {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
  return { toast: mock }
})

vi.mock('@/components/ui/sonner/Sonner.vue', () => ({
  default: { name: 'Sonner', template: '<div />' }
}))

vi.mock('@/api', () => ({
  getUserList: vi.fn(),
  getRoleList: vi.fn(),
  getDeptTree: vi.fn(),
  updateUserStatus: vi.fn(),
  deleteUser: vi.fn()
}))

vi.mock('@/lib/page-permissions', () => ({
  getUserPagePermissionState: vi.fn(() => ({
    canAddUser: true,
    canEditUser: true,
    canDeleteUser: true,
    canAssignUser: true
  }))
}))

// Do NOT mock useAsyncAction or usePageList — real implementations work
// because vue-sonner and @/api are already mocked above.

// Child component stubs
vi.mock('@/components/user/UserFormDialog.vue', () => ({
  default: {
    name: 'UserFormDialog',
    template: '<div class="stub-user-form-dialog" />'
  }
}))

vi.mock('@/components/user/PasswordResetDialog.vue', () => ({
  default: {
    name: 'PasswordResetDialog',
    template: '<div class="stub-password-reset-dialog" />'
  }
}))

vi.mock('@/components/user/AssignRoleDialog.vue', () => ({
  default: {
    name: 'AssignRoleDialog',
    template: '<div class="stub-assign-role-dialog" />'
  }
}))

// ---------------------------------------------------------------------------
// Imports that resolve through the mocked modules above
// ---------------------------------------------------------------------------

import User from '@/views/system/User.vue'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import {
  getUserList,
  getRoleList,
  getDeptTree,
  updateUserStatus,
  deleteUser
} from '@/api'
import { getUserPagePermissionState } from '@/lib/page-permissions'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeUser(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'u1',
    username: 'zhangsan',
    nickname: '张三',
    email: 'zhangsan@test.com',
    phone: '13800138000',
    avatar: '',
    status: 1,
    deptId: 'd1',
    deptName: '技术部',
    roles: ['管理员'],
    createTime: '2026-01-15T10:00:00',
    updateTime: '2026-03-01T10:00:00',
    ...overrides
  }
}

const mockPageResult = (records: any[] = [], total = 0) => ({
  code: 200,
  message: 'success',
  data: {
    records,
    total,
    page: 1,
    size: 10
  }
})

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('User Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper
  let userStore: ReturnType<typeof useUserStore>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)

    userStore = useUserStore()
    userStore.hasPermission = vi.fn(() => true)

    vi.clearAllMocks()

    // Default happy-path API mocks
    vi.mocked(getUserList).mockResolvedValue(
      mockPageResult([makeUser()]) as any
    )
    vi.mocked(getRoleList).mockResolvedValue(
      mockApiResponse({ records: [{ id: 'r1', name: '管理员', code: 'admin' }] }) as any
    )
    vi.mocked(getDeptTree).mockResolvedValue(
      mockApiResponse([{ id: 'd1', name: '技术部', children: [] }]) as any
    )
    vi.mocked(updateUserStatus).mockResolvedValue(
      mockApiResponse(null) as any
    )
    vi.mocked(deleteUser).mockResolvedValue(mockApiResponse(null) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  // Helper to mount and flush async layers
  const mountAndFlush = async (options = {}) => {
    wrapper = mount(User, {
      global: {
        plugins: [pinia],
        stubs: {
          UserFormDialog: true,
          PasswordResetDialog: true,
          AssignRoleDialog: true,
          ConfirmDialog: true,
          Pagination: true,
          StatusBadge: true
        }
      },
      ...options
    })
    await new Promise(resolve => setTimeout(resolve, 0))
    await nextTick()
    await nextTick()
    await nextTick()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders the root container with correct layout class', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders one Card component (table card)', async () => {
      wrapper = await mountAndFlush()
      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBeGreaterThanOrEqual(1)
    })

    it('renders the user table with 9 column headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('thead th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toEqual([
        '用户名', '昵称', '邮箱', '电话', '部门', '角色', '状态', '创建时间', '操作'
      ])
    })

    it('renders filter controls (keyword input)', async () => {
      wrapper = await mountAndFlush()
      const input = wrapper.find('input')
      expect(input.exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getUserList on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getUserList).toHaveBeenCalled()
    })

    it('calls getRoleList and getDeptTree on mount (fetchMeta)', async () => {
      wrapper = await mountAndFlush()
      expect(getRoleList).toHaveBeenCalled()
      expect(getDeptTree).toHaveBeenCalled()
    })

    it('populates tableData with records from the API response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.tableData.records.length).toBe(1)
      expect(vm.tableData.records[0].username).toBe('zhangsan')
    })

    it('populates roleList from getRoleList response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.roleList.length).toBe(1)
      expect(vm.roleList[0].name).toBe('管理员')
    })

    it('populates deptTree from getDeptTree response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.deptTree.length).toBe(1)
      expect(vm.deptTree[0].name).toBe('技术部')
    })
  })

  // =========================================================================
  // 3. Search / filter
  // =========================================================================
  describe('Search and Filter', () => {
    it('resets filters and re-fetches when handleResetSearch is called', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.filters.keyword = 'test'
      vm.filters.deptId = 'd1'
      await nextTick()

      vi.clearAllMocks()
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([]) as any)

      await vm.handleResetSearch()
      await nextTick()

      expect(vm.filters.keyword).toBe('')
      expect(vm.filters.deptId).toBe('all')
      expect(getUserList).toHaveBeenCalled()
    })

    it('renders search and reset buttons', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map(b => b.text())
      expect(buttonTexts.some(t => t.includes('搜索'))).toBe(true)
      expect(buttonTexts.some(t => t.includes('重置'))).toBe(true)
    })
  })

  // =========================================================================
  // 4. Delete user flow
  // =========================================================================
  describe('Delete User', () => {
    it('sets deleteUserId and opens delete dialog on handleDeleteConfirm', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteConfirm('user-abc')
      await nextTick()

      expect(vm.deleteUserId).toBe('user-abc')
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('calls deleteUser API and closes dialog on handleDelete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteUserId = 'user-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(deleteUser).toHaveBeenCalledWith('user-del')
      expect(vm.deleteDialogOpen).toBe(false)
    })

    it('shows success toast on successful delete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteUserId = 'user-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(toast.success).toHaveBeenCalledWith('用户删除成功')
    })

    it('shows error toast when deleteUser fails', async () => {
      vi.mocked(deleteUser).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteUserId = 'user-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
    })

    it('closes delete dialog even when delete fails', async () => {
      vi.mocked(deleteUser).mockRejectedValueOnce(new Error('fail'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteUserId = 'user-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(vm.deleteDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 5. Status toggle
  // =========================================================================
  describe('Status Toggle', () => {
    it('opens status confirm dialog with user info on handleStatusClick', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const user = makeUser({ status: 1 })

      vm.handleStatusClick(user)
      await nextTick()

      expect(vm.statusChangeUser).toEqual(user)
      expect(vm.statusConfirmOpen).toBe(true)
    })

    it('calls updateUserStatus with toggled status on handleStatusConfirm (1 -> 0)', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const user = makeUser({ id: 'u1', status: 1 })

      vm.statusChangeUser = user
      vm.statusConfirmOpen = true

      await vm.handleStatusConfirm()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(updateUserStatus).toHaveBeenCalledWith('u1', 0)
      expect(toast.success).toHaveBeenCalledWith('状态更新成功')
      expect(vm.statusConfirmOpen).toBe(false)
      expect(vm.statusChangeUser).toBeNull()
    })

    it('calls updateUserStatus with toggled status on handleStatusConfirm (0 -> 1)', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const user = makeUser({ id: 'u2', status: 0 })

      vm.statusChangeUser = user
      vm.statusConfirmOpen = true

      await vm.handleStatusConfirm()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(updateUserStatus).toHaveBeenCalledWith('u2', 1)
    })

    it('shows error toast when status update fails', async () => {
      vi.mocked(updateUserStatus).mockRejectedValueOnce(new Error('更新状态失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const user = makeUser({ id: 'u1', status: 1 })

      vm.statusChangeUser = user
      vm.statusConfirmOpen = true

      await vm.handleStatusConfirm()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(toast.error).toHaveBeenCalledWith('更新状态失败')
      // Dialog should still close even on error (finally block)
      expect(vm.statusConfirmOpen).toBe(false)
    })

    it('does nothing in handleStatusConfirm when statusChangeUser is null', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.statusChangeUser = null
      await vm.handleStatusConfirm()

      expect(updateUserStatus).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 6. Permission-based UI
  // =========================================================================
  describe('Permission-Based UI', () => {
    it('passes userStore.hasPermission to getUserPagePermissionState', async () => {
      wrapper = await mountAndFlush()
      expect(getUserPagePermissionState).toHaveBeenCalledWith(
        userStore.hasPermission
      )
    })

    it('shows "新增用户" button when canAddUser is true', async () => {
      vi.mocked(getUserPagePermissionState).mockReturnValue({
        canAddUser: true,
        canEditUser: false,
        canDeleteUser: false,
        canAssignUser: false
      })
      wrapper = await mountAndFlush()
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('新增用户'))
      expect(addBtn).toBeDefined()
    })

    it('hides "新增用户" button when canAddUser is false', async () => {
      vi.mocked(getUserPagePermissionState).mockReturnValue({
        canAddUser: false,
        canEditUser: false,
        canDeleteUser: false,
        canAssignUser: false
      })
      wrapper = await mountAndFlush()
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('新增用户'))
      expect(addBtn).toBeUndefined()
    })

    it('computes all permission flags from getUserPagePermissionState', async () => {
      vi.mocked(getUserPagePermissionState).mockReturnValue({
        canAddUser: false,
        canEditUser: true,
        canDeleteUser: false,
        canAssignUser: true
      })
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddUser).toBe(false)
      expect(vm.canEditUser).toBe(true)
      expect(vm.canDeleteUser).toBe(false)
      expect(vm.canAssignUser).toBe(true)
    })
  })

  // =========================================================================
  // 7. Pagination
  // =========================================================================
  describe('Pagination', () => {
    it('renders the Pagination stub component', async () => {
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })
      expect(pagination.exists()).toBe(true)
    })

    it('passes correct props to Pagination (current, total, pageSize)', async () => {
      vi.mocked(getUserList).mockResolvedValue(
        mockPageResult([makeUser()], 42) as any
      )
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      expect(pagination.props('current')).toBe(1)
      expect(pagination.props('total')).toBe(42)
      expect(pagination.props('pageSize')).toBe(10)
    })

    it('calls goToPage when Pagination emits change event', async () => {
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([makeUser()], 25) as any)
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      vi.clearAllMocks()
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([]) as any)

      pagination.vm.$emit('change', 2)
      await nextTick()
      await nextTick()
      await nextTick()

      // goToPage should have triggered getUserList with page=2
      expect(getUserList).toHaveBeenCalledWith(expect.objectContaining({ page: 2 }))
    })
  })

  // =========================================================================
  // 8. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading is true', async () => {
      vi.mocked(getUserList).mockReturnValue(new Promise(() => {}))

      wrapper = mount(User, {
        global: {
          plugins: [pinia],
          stubs: {
            UserFormDialog: true,
            PasswordResetDialog: true,
            AssignRoleDialog: true,
            ConfirmDialog: true,
            Pagination: true,
            StatusBadge: true
          }
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when loading is false and no records', async () => {
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([], 0) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('暂无数据')
    })

    it('shows data rows when loading is false and records exist', async () => {
      vi.mocked(getUserList).mockResolvedValue(
        mockPageResult([makeUser({ username: 'alice' }), makeUser({ id: 'u2', username: 'bob' })], 2) as any
      )
      wrapper = await mountAndFlush()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(2)
      expect(wrapper.text()).toContain('alice')
      expect(wrapper.text()).toContain('bob')
    })

    it('sets loading to false after data fetch completes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 9. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getUserList API error gracefully', async () => {
      vi.mocked(getUserList).mockRejectedValue(new Error('获取用户列表失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('获取用户列表失败')
    })

    it('handles getRoleList / getDeptTree error gracefully', async () => {
      vi.mocked(getRoleList).mockRejectedValue(new Error('角色服务不可用'))
      vi.mocked(getDeptTree).mockRejectedValue(new Error('部门服务不可用'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('角色服务不可用')
    })

    it('handles deleteUser error and shows toast', async () => {
      vi.mocked(deleteUser).mockRejectedValue(new Error('存在关联数据'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteUserId = 'u1'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(toast.error).toHaveBeenCalledWith('存在关联数据')
    })

    it('handles updateUserStatus error and shows toast', async () => {
      vi.mocked(updateUserStatus).mockRejectedValue(new Error('状态更新异常'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.statusChangeUser = makeUser({ id: 'u1', status: 1 })
      vm.statusConfirmOpen = true

      await vm.handleStatusConfirm()
      await new Promise(resolve => setTimeout(resolve, 0))
      await nextTick()
      await nextTick()

      expect(toast.error).toHaveBeenCalledWith('状态更新异常')
    })
  })

  // =========================================================================
  // 10. Dialog open/close helpers
  // =========================================================================
  describe('Dialog Open/Close', () => {
    it('handleAdd clears editUserId and opens formDialogOpen', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.editUserId = 'old-id'
      vm.formDialogOpen = false

      vm.handleAdd()
      expect(vm.editUserId).toBe('')
      expect(vm.formDialogOpen).toBe(true)
    })

    it('handleEdit sets editUserId and opens formDialogOpen', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleEdit('user-123')
      expect(vm.editUserId).toBe('user-123')
      expect(vm.formDialogOpen).toBe(true)
    })

    it('openPasswordDialog sets resetUserId, resetUsername, and opens passwordDialogOpen', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const user = makeUser({ id: 'u1', username: 'zhangsan' })
      vm.openPasswordDialog(user)

      expect(vm.resetUserId).toBe('u1')
      expect(vm.resetUsername).toBe('zhangsan')
      expect(vm.passwordDialogOpen).toBe(true)
    })

    it('openAssignDialog sets assignUser and opens assignDialogOpen', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const user = makeUser({ id: 'u1' })
      vm.openAssignDialog(user)

      expect(vm.assignUser).toEqual(user)
      expect(vm.assignDialogOpen).toBe(true)
    })
  })

  // =========================================================================
  // 11. Dept options computed
  // =========================================================================
  describe('Dept Options', () => {
    it('computes flat deptOptions from deptTree with indentation', async () => {
      vi.mocked(getDeptTree).mockResolvedValue(
        mockApiResponse([
          {
            id: 'd1',
            name: '总部',
            children: [{ id: 'd2', name: '技术部', children: [] }]
          }
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.deptOptions.length).toBe(3)
      expect(vm.deptOptions[0]).toEqual({ id: '0', label: '无部门' })
      expect(vm.deptOptions[1]).toEqual({ id: 'd1', label: '总部' })
      // Indented child (fullwidth space for one level)
      expect(vm.deptOptions[2]).toEqual({ id: 'd2', label: '\u3000技术部' })
    })

    it('returns only "无部门" when deptTree is empty', async () => {
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.deptOptions).toEqual([{ id: '0', label: '无部门' }])
    })
  })

  // =========================================================================
  // 12. Table rendering details
  // =========================================================================
  describe('Table Content', () => {
    it('renders user data in table cells', async () => {
      const user = makeUser({
        username: 'alice',
        nickname: '爱丽丝',
        email: 'alice@test.com',
        phone: '13900139000',
        deptName: '产品部',
        roles: ['管理员', '编辑']
      })
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([user]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('alice')
      expect(wrapper.text()).toContain('爱丽丝')
      expect(wrapper.text()).toContain('alice@test.com')
      expect(wrapper.text()).toContain('13900139000')
      expect(wrapper.text()).toContain('产品部')
      expect(wrapper.text()).toContain('管理员')
    })

    it('shows "-" for missing optional fields', async () => {
      const user = makeUser({
        nickname: '',
        email: '',
        phone: '',
        deptName: '',
        roles: []
      })
      vi.mocked(getUserList).mockResolvedValue(mockPageResult([user]) as any)
      wrapper = await mountAndFlush()

      const cells = wrapper.findAll('tbody td')
      const cellTexts = cells.map(c => c.text())
      const dashCount = cellTexts.filter(t => t.trim() === '-').length
      expect(dashCount).toBeGreaterThanOrEqual(4)
    })
  })
})
