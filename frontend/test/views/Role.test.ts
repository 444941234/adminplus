import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// ---------------------------------------------------------------------------
// Mocks – must come BEFORE importing the component under test
// ---------------------------------------------------------------------------

vi.mock('@/api', () => ({
  getRoleList: vi.fn(),
  getRoleById: vi.fn(),
  createRole: vi.fn(),
  updateRole: vi.fn(),
  deleteRole: vi.fn(),
  assignMenus: vi.fn(),
  getRoleMenus: vi.fn(),
  getMenuTree: vi.fn()
}))

vi.mock('@/lib/page-permissions', () => ({
  getRolePagePermissionState: vi.fn()
}))

vi.mock('@/lib/validators', () => ({
  isValidRoleCode: vi.fn()
}))

vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports that resolve through the mocked modules above
// ---------------------------------------------------------------------------
import Role from '@/views/system/Role.vue'
import {
  getRoleList,
  getRoleById,
  createRole,
  updateRole,
  deleteRole,
  assignMenus,
  getRoleMenus,
  getMenuTree
} from '@/api'
import { getRolePagePermissionState } from '@/lib/page-permissions'
import { isValidRoleCode } from '@/lib/validators'
import { useUserStore } from '@/stores/user'

// ---------------------------------------------------------------------------
// Test fixtures
// ---------------------------------------------------------------------------

const mockRoles = [
  {
    id: 'role-1',
    name: '管理员',
    code: 'ADMIN',
    description: '系统管理员',
    status: 1,
    sortOrder: 1,
    dataScope: 1,
    createTime: '2026-01-01 00:00:00',
    updateTime: '2026-01-01 00:00:00'
  },
  {
    id: 'role-2',
    name: '普通用户',
    code: 'USER',
    description: '普通用户角色',
    status: 1,
    sortOrder: 2,
    dataScope: 4,
    createTime: '2026-01-02 00:00:00',
    updateTime: '2026-01-02 00:00:00'
  },
  {
    id: 'role-3',
    name: '审核员',
    code: 'AUDITOR',
    description: null,
    status: 0,
    sortOrder: 3,
    dataScope: 3,
    createTime: '2026-01-03 00:00:00',
    updateTime: '2026-01-03 00:00:00'
  }
]

const mockMenuTree = [
  {
    id: 'menu-1',
    parentId: '0',
    type: 0,
    name: '系统管理',
    path: '/system',
    component: '',
    permKey: '',
    icon: 'Settings',
    sortOrder: 1,
    visible: 1,
    status: 1,
    children: [
      {
        id: 'menu-1-1',
        parentId: 'menu-1',
        type: 1,
        name: '用户管理',
        path: '/system/user',
        component: 'system/User',
        permKey: 'system:user:list',
        icon: '',
        sortOrder: 1,
        visible: 1,
        status: 1,
        children: []
      },
      {
        id: 'menu-1-2',
        parentId: 'menu-1',
        type: 1,
        name: '角色管理',
        path: '/system/role',
        component: 'system/Role',
        permKey: 'system:role:list',
        icon: '',
        sortOrder: 2,
        visible: 1,
        status: 1,
        children: []
      }
    ]
  },
  {
    id: 'menu-2',
    parentId: '0',
    type: 0,
    name: '系统监控',
    path: '/monitor',
    component: '',
    permKey: '',
    icon: 'Monitor',
    sortOrder: 2,
    visible: 1,
    status: 1,
    children: []
  }
]

const fullPermissionState = {
  canAddRole: true,
  canEditRole: true,
  canDeleteRole: true,
  canAssignRole: true
}

const noPermissionState = {
  canAddRole: false,
  canEditRole: false,
  canDeleteRole: false,
  canAssignRole: false
}

// ---------------------------------------------------------------------------
// Helper – flush async queues (useAsyncAction adds extra microtask layers)
// ---------------------------------------------------------------------------

async function flushAsync(wrapper: VueWrapper) {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Role Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)

    // Default user store mock with all permissions
    vi.mocked(useUserStore).mockReturnValue({
      hasPermission: vi.fn(() => true)
    } as any)

    // Default permission state – full access
    vi.mocked(getRolePagePermissionState).mockReturnValue(fullPermissionState)

    // Default role code validation – valid
    vi.mocked(isValidRoleCode).mockReturnValue(true)

    // Default API responses
    vi.mocked(getRoleList).mockResolvedValue({
      code: 200,
      message: 'success',
      data: { records: mockRoles, total: 3, page: 1, size: 10 }
    } as any)
    vi.mocked(getMenuTree).mockResolvedValue({
      code: 200,
      message: 'success',
      data: mockMenuTree
    } as any)

    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  // =========================================================================
  // 1. Page structure & rendering
  // =========================================================================
  describe('Page Structure', () => {
    it('should render the page root container', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('should render the search input and reset button', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // The search input is an <input> inside a component
      const inputs = wrapper.findAll('input')
      const searchInput = inputs.find(i => i.attributes('placeholder')?.includes('搜索'))
      expect(searchInput).toBeTruthy()
    })

    it('should render the role table with correct column headers', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('角色名称')
      expect(headerTexts).toContain('角色编码')
      expect(headerTexts).toContain('描述')
      expect(headerTexts).toContain('数据范围')
      expect(headerTexts).toContain('状态')
      expect(headerTexts).toContain('排序')
      expect(headerTexts).toContain('操作')
    })

    it('should render role data rows after loading', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const rows = wrapper.findAll('tbody tr')
      // 3 role rows, no loading/empty row
      expect(rows.length).toBe(3)
    })

    it('should display role code inside a <code> element', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const codeEls = wrapper.findAll('code')
      expect(codeEls.length).toBe(3)
      expect(codeEls[0].text()).toBe('ADMIN')
      expect(codeEls[1].text()).toBe('USER')
    })

    it('should display dataScope label for scope 1 as "全部数据"', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const rows = wrapper.findAll('tbody tr')
      // First role has dataScope=1, text should contain "全部数据"
      const cells = rows[0].findAll('td')
      const dataScopeCell = cells.find(td => td.text().includes('全部数据'))
      expect(dataScopeCell).toBeTruthy()
    })

    it('should display dataScope label for non-1 scope as "范围 N"', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const rows = wrapper.findAll('tbody tr')
      // Second role has dataScope=4
      const cells = rows[1].findAll('td')
      const dataScopeCell = cells.find(td => td.text().includes('范围 4'))
      expect(dataScopeCell).toBeTruthy()
    })

    it('should display "-" when description is null', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const rows = wrapper.findAll('tbody tr')
      // Third role has description=null
      const cells = rows[2].findAll('td')
      const descCell = cells.find(td => td.text().trim() === '-')
      expect(descCell).toBeTruthy()
    })

    it('should display sortOrder for each role', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const rows = wrapper.findAll('tbody tr')
      const sortOrderCells = rows.map(row => {
        const cells = row.findAll('td')
        return cells[5].text()
      })
      expect(sortOrderCells).toEqual(['1', '2', '3'])
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching', () => {
    it('should call getRoleList and getMenuTree on mount', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(getRoleList).toHaveBeenCalledTimes(1)
      expect(getMenuTree).toHaveBeenCalledTimes(1)
    })

    it('should populate roles ref from API response', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.roles).toEqual(mockRoles)
    })

    it('should populate menus ref from API response', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.menus).toEqual(mockMenuTree)
    })

    it('should handle getRoleList returning empty records', async () => {
      vi.mocked(getRoleList).mockResolvedValue({
        code: 200,
        message: 'success',
        data: { records: [], total: 0, page: 1, size: 10 }
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.roles).toEqual([])
    })

    it('should handle getRoleList API error gracefully', async () => {
      vi.mocked(getRoleList).mockRejectedValue(new Error('Network error'))
      vi.mocked(getMenuTree).mockRejectedValue(new Error('Network error'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 3. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('should show loading indicator while fetching data', () => {
      // Keep the promise pending so loading stays true during mount
      vi.mocked(getRoleList).mockReturnValue(new Promise(() => {}) as any)
      vi.mocked(getMenuTree).mockReturnValue(new Promise(() => {}) as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })

      expect(wrapper.vm.listLoading).toBe(true)
    })

    it('should set listLoading to false after data fetch completes', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.listLoading).toBe(false)
    })

    it('should show loading text in table body while loading', async () => {
      vi.mocked(getRoleList).mockReturnValue(new Promise(() => {}) as any)
      vi.mocked(getMenuTree).mockReturnValue(new Promise(() => {}) as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await nextTick()

      // The loading row appears when listLoading is true
      const tbody = wrapper.find('tbody')
      expect(tbody.text()).toContain('加载中...')
    })

    it('should show empty state when no roles exist', async () => {
      vi.mocked(getRoleList).mockResolvedValue({
        code: 200,
        message: 'success',
        data: { records: [], total: 0, page: 1, size: 10 }
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.text()).toContain('暂无数据')
    })
  })

  // =========================================================================
  // 4. Search functionality
  // =========================================================================
  describe('Search Functionality', () => {
    it('should show all roles when searchQuery is empty', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.filteredRoles.length).toBe(3)
    })

    it('should filter roles by name (case-insensitive)', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = '管理'
      await nextTick()

      const filtered = wrapper.vm.filteredRoles
      expect(filtered.length).toBe(1)
      expect(filtered[0].name).toBe('管理员')
    })

    it('should filter roles by code', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = 'USER'
      await nextTick()

      const filtered = wrapper.vm.filteredRoles
      expect(filtered.length).toBe(1)
      expect(filtered[0].code).toBe('USER')
    })

    it('should filter roles by description', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = '系统'
      await nextTick()

      const filtered = wrapper.vm.filteredRoles
      expect(filtered.length).toBe(1)
      expect(filtered[0].description).toBe('系统管理员')
    })

    it('should handle null description gracefully in search', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // "审核员" has null description but name matches
      wrapper.vm.searchQuery = '审核'
      await nextTick()

      const filtered = wrapper.vm.filteredRoles
      expect(filtered.length).toBe(1)
      expect(filtered[0].name).toBe('审核员')
    })

    it('should return empty array for non-matching search', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = 'nonexistent'
      await nextTick()

      expect(wrapper.vm.filteredRoles.length).toBe(0)
    })

    it('should trim and lowercase the search keyword', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = '  ADMIN  '
      await nextTick()

      expect(wrapper.vm.filteredRoles.length).toBe(1)
      expect(wrapper.vm.filteredRoles[0].code).toBe('ADMIN')
    })

    it('should render filtered rows in the table', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.searchQuery = '管理员'
      await nextTick()
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
    })
  })

  // =========================================================================
  // 5. Add role dialog
  // =========================================================================
  describe('Add Role Dialog', () => {
    it('should have dialogOpen initially false', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.dialogOpen).toBe(false)
    })

    it('should open dialog in add mode via handleAdd', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      expect(wrapper.vm.dialogOpen).toBe(true)
      expect(wrapper.vm.isEdit).toBe(false)
      expect(wrapper.vm.editId).toBe('')
    })

    it('should reset form fields when opening add dialog', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // Pre-fill form to simulate leftover data
      Object.assign(wrapper.vm.form, {
        name: 'Old Name',
        code: 'OLD_CODE',
        description: 'Old desc',
        dataScope: '3',
        status: '0',
        sortOrder: 99
      })

      wrapper.vm.handleAdd()
      await nextTick()

      expect(wrapper.vm.form).toEqual({
        name: '',
        code: '',
        description: '',
        dataScope: '1',
        status: '1',
        sortOrder: 0
      })
    })

    it('should warn when submitting with empty name', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.form.name = '  '
      wrapper.vm.handleSubmit()

      const { toast } = await import('vue-sonner')
      expect(toast.warning).toHaveBeenCalledWith('请输入角色名称')
    })

    it('should warn when submitting new role with empty code', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.form.name = 'Valid Name'
      wrapper.vm.form.code = ''
      wrapper.vm.isEdit = false
      wrapper.vm.handleSubmit()

      const { toast } = await import('vue-sonner')
      expect(toast.warning).toHaveBeenCalledWith('请输入角色编码')
    })

    it('should warn when submitting new role with invalid code format', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.form.name = 'Valid Name'
      wrapper.vm.form.code = '123-invalid'
      wrapper.vm.isEdit = false
      vi.mocked(isValidRoleCode).mockReturnValue(false)
      wrapper.vm.handleSubmit()

      const { toast } = await import('vue-sonner')
      expect(toast.warning).toHaveBeenCalledWith(
        '角色编码需以字母开头，只能包含字母、数字、下划线、冒号或短横线'
      )
    })

    it('should call createRole API when submitting new role', async () => {
      vi.mocked(createRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'New Role'
      wrapper.vm.form.code = 'NEW_ROLE'
      wrapper.vm.form.description = 'A new role'
      wrapper.vm.form.dataScope = '1'
      wrapper.vm.form.status = '1'
      wrapper.vm.form.sortOrder = 5

      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      expect(createRole).toHaveBeenCalledWith({
        code: 'NEW_ROLE',
        name: 'New Role',
        description: 'A new role',
        dataScope: 1,
        status: 1,
        sortOrder: 5
      })
    })

    it('should show success toast after creating role', async () => {
      vi.mocked(createRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'New Role'
      wrapper.vm.form.code = 'NEW_ROLE'
      vi.mocked(isValidRoleCode).mockReturnValue(true)
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.success).toHaveBeenCalledWith('角色创建成功')
    })

    it('should close dialog and refresh list after creating role', async () => {
      vi.mocked(createRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // Clear the initial fetch calls
      vi.mocked(getRoleList).mockClear()
      vi.mocked(getRoleList).mockResolvedValue({
        code: 200,
        message: 'success',
        data: { records: mockRoles, total: 3, page: 1, size: 10 }
      } as any)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'New Role'
      wrapper.vm.form.code = 'NEW_ROLE'
      vi.mocked(isValidRoleCode).mockReturnValue(true)
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      expect(wrapper.vm.dialogOpen).toBe(false)
      expect(getRoleList).toHaveBeenCalled()
    })

    it('should show error toast when createRole fails', async () => {
      vi.mocked(createRole).mockRejectedValue(new Error('Duplicate code'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'New Role'
      wrapper.vm.form.code = 'EXISTING_CODE'
      vi.mocked(isValidRoleCode).mockReturnValue(true)
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('Duplicate code')
    })
  })

  // =========================================================================
  // 6. Edit role dialog
  // =========================================================================
  describe('Edit Role Dialog', () => {
    it('should open dialog in edit mode via handleEdit', async () => {
      vi.mocked(getRoleById).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockRoles[0]
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-1')
      await flushAsync(wrapper)

      expect(wrapper.vm.dialogOpen).toBe(true)
      expect(wrapper.vm.isEdit).toBe(true)
      expect(wrapper.vm.editId).toBe('role-1')
    })

    it('should populate form with existing role data', async () => {
      vi.mocked(getRoleById).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockRoles[0]
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-1')
      await flushAsync(wrapper)

      expect(wrapper.vm.form.name).toBe('管理员')
      expect(wrapper.vm.form.code).toBe('ADMIN')
      expect(wrapper.vm.form.description).toBe('系统管理员')
      expect(wrapper.vm.form.dataScope).toBe('1')
      expect(wrapper.vm.form.status).toBe('1')
      expect(wrapper.vm.form.sortOrder).toBe(1)
    })

    it('should call updateRole API when submitting edit', async () => {
      vi.mocked(getRoleById).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockRoles[0]
      } as any)
      vi.mocked(updateRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-1')
      await flushAsync(wrapper)

      wrapper.vm.form.name = '管理员V2'
      wrapper.vm.form.description = 'Updated description'
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      expect(updateRole).toHaveBeenCalledWith('role-1', expect.objectContaining({
        name: '管理员V2',
        description: 'Updated description'
      }))
    })

    it('should show success toast after updating role', async () => {
      vi.mocked(getRoleById).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockRoles[0]
      } as any)
      vi.mocked(updateRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-1')
      await flushAsync(wrapper)

      wrapper.vm.form.name = '管理员V2'
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.success).toHaveBeenCalledWith('角色更新成功')
    })

    it('should close dialog when getRoleById fails during edit', async () => {
      vi.mocked(getRoleById).mockRejectedValue(new Error('Not found'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-999')
      await flushAsync(wrapper)

      // Dialog should have been closed by onError callback
      expect(wrapper.vm.dialogOpen).toBe(false)
    })

    it('should not warn about empty code when editing (code is disabled)', async () => {
      vi.mocked(getRoleById).mockResolvedValue({
        code: 200,
        message: 'success',
        data: { ...mockRoles[0], code: '' }
      } as any)
      vi.mocked(updateRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleEdit('role-1')
      await flushAsync(wrapper)

      // In edit mode, code validation is skipped
      wrapper.vm.form.name = 'Valid Name'
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.warning).not.toHaveBeenCalledWith('请输入角色编码')
    })
  })

  // =========================================================================
  // 7. Delete role
  // =========================================================================
  describe('Delete Role', () => {
    it('should open delete confirmation dialog via handleDeleteConfirm', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleDeleteConfirm('role-2')
      await nextTick()

      expect(wrapper.vm.deleteDialogOpen).toBe(true)
      expect(wrapper.vm.deleteRoleId).toBe('role-2')
    })

    it('should call deleteRole API and refresh list on handleDelete', async () => {
      vi.mocked(deleteRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)
      vi.mocked(getRoleList).mockClear()
      vi.mocked(getRoleList).mockResolvedValue({
        code: 200,
        message: 'success',
        data: { records: mockRoles, total: 3, page: 1, size: 10 }
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.deleteDialogOpen = true

      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      expect(deleteRole).toHaveBeenCalledWith('role-2')
      expect(getRoleList).toHaveBeenCalled()
    })

    it('should show success toast after deleting role', async () => {
      vi.mocked(deleteRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.success).toHaveBeenCalledWith('角色删除成功')
    })

    it('should close delete dialog after deletion completes', async () => {
      vi.mocked(deleteRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.deleteDialogOpen = true

      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      expect(wrapper.vm.deleteDialogOpen).toBe(false)
    })

    it('should close delete dialog even if deletion fails', async () => {
      vi.mocked(deleteRole).mockRejectedValue(new Error('Cannot delete'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.deleteDialogOpen = true

      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      // .finally() should close the dialog regardless of success/error
      expect(wrapper.vm.deleteDialogOpen).toBe(false)
    })

    it('should show error toast when deletion fails', async () => {
      vi.mocked(deleteRole).mockRejectedValue(new Error('Role has bound users'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('Role has bound users')
    })
  })

  // =========================================================================
  // 8. Menu assignment dialog
  // =========================================================================
  describe('Menu Assignment', () => {
    it('should open assign dialog via handleOpenAssign', async () => {
      vi.mocked(getMenuTree).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockMenuTree
      } as any)
      vi.mocked(getRoleMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: ['menu-1', 'menu-1-1']
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleOpenAssign(mockRoles[0])
      await flushAsync(wrapper)

      expect(wrapper.vm.assignDialogOpen).toBe(true)
      expect(wrapper.vm.assignRole).toEqual(mockRoles[0])
    })

    it('should fetch menu tree and role menus when opening assign dialog', async () => {
      vi.mocked(getMenuTree).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockMenuTree
      } as any)
      vi.mocked(getRoleMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: ['menu-1', 'menu-1-1']
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // Clear the mount-time calls
      vi.mocked(getMenuTree).mockClear()
      vi.mocked(getMenuTree).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockMenuTree
      } as any)

      wrapper.vm.handleOpenAssign(mockRoles[0])
      await flushAsync(wrapper)

      expect(getMenuTree).toHaveBeenCalled()
      expect(getRoleMenus).toHaveBeenCalledWith('role-1')
    })

    it('should set selectedMenuIds from API response', async () => {
      vi.mocked(getMenuTree).mockResolvedValue({
        code: 200,
        message: 'success',
        data: mockMenuTree
      } as any)
      vi.mocked(getRoleMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: ['menu-1', 'menu-1-1']
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleOpenAssign(mockRoles[0])
      await flushAsync(wrapper)

      expect(wrapper.vm.selectedMenuIds).toEqual(['menu-1', 'menu-1-1'])
    })

    it('should close assign dialog when loading fails', async () => {
      vi.mocked(getMenuTree).mockRejectedValue(new Error('Load failed'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignDialogOpen = true
      wrapper.vm.handleOpenAssign(mockRoles[0])
      await flushAsync(wrapper)

      // onError callback closes the dialog
      expect(wrapper.vm.assignDialogOpen).toBe(false)
    })

    it('should call assignMenus API on handleAssignSubmit', async () => {
      vi.mocked(assignMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignRole = mockRoles[0]
      wrapper.vm.selectedMenuIds = ['menu-1', 'menu-1-1']

      wrapper.vm.handleAssignSubmit()
      await flushAsync(wrapper)

      expect(assignMenus).toHaveBeenCalledWith('role-1', ['menu-1', 'menu-1-1'])
    })

    it('should show success toast after assigning menus', async () => {
      vi.mocked(assignMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignRole = mockRoles[0]
      wrapper.vm.selectedMenuIds = ['menu-1']

      wrapper.vm.handleAssignSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.success).toHaveBeenCalledWith('菜单权限分配成功')
    })

    it('should close assign dialog after successful assignment', async () => {
      vi.mocked(assignMenus).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignRole = mockRoles[0]
      wrapper.vm.selectedMenuIds = ['menu-1']
      wrapper.vm.assignDialogOpen = true

      wrapper.vm.handleAssignSubmit()
      await flushAsync(wrapper)

      expect(wrapper.vm.assignDialogOpen).toBe(false)
    })

    it('should not call assignMenus when assignRole is null', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignRole = null
      wrapper.vm.handleAssignSubmit()

      expect(assignMenus).not.toHaveBeenCalled()
    })

    it('should show error toast when assignMenus fails', async () => {
      vi.mocked(assignMenus).mockRejectedValue(new Error('Assign failed'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.assignRole = mockRoles[0]
      wrapper.vm.selectedMenuIds = ['menu-1']

      wrapper.vm.handleAssignSubmit()
      await flushAsync(wrapper)

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('Assign failed')
    })
  })

  // =========================================================================
  // 9. Menu selection helpers
  // =========================================================================
  describe('Menu Selection', () => {
    it('should add menu ID to selectedMenuIds via toggleMenuSelection', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.selectedMenuIds = []
      wrapper.vm.toggleMenuSelection('menu-1', true)
      await nextTick()

      expect(wrapper.vm.selectedMenuIds).toContain('menu-1')
    })

    it('should remove menu ID from selectedMenuIds via toggleMenuSelection', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.selectedMenuIds = ['menu-1', 'menu-2']
      wrapper.vm.toggleMenuSelection('menu-1', false)
      await nextTick()

      expect(wrapper.vm.selectedMenuIds).not.toContain('menu-1')
      expect(wrapper.vm.selectedMenuIds).toContain('menu-2')
    })

    it('should select all menus via toggleAllMenus(true)', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // Menus need to be populated for menuOptions to have items
      wrapper.vm.menus = mockMenuTree
      wrapper.vm.toggleAllMenus(true)
      await nextTick()

      const allIds = wrapper.vm.menuOptions.map((item: any) => item.id)
      expect(wrapper.vm.selectedMenuIds).toEqual(allIds)
    })

    it('should deselect all menus via toggleAllMenus(false)', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.menus = mockMenuTree
      wrapper.vm.selectedMenuIds = ['menu-1', 'menu-2']
      wrapper.vm.toggleAllMenus(false)
      await nextTick()

      expect(wrapper.vm.selectedMenuIds).toEqual([])
    })

    it('should compute isAllMenusSelected correctly', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.menus = mockMenuTree
      const allIds = wrapper.vm.menuOptions.map((item: any) => item.id)

      wrapper.vm.selectedMenuIds = allIds
      await nextTick()
      expect(wrapper.vm.isAllMenusSelected).toBe(true)

      wrapper.vm.selectedMenuIds = []
      await nextTick()
      expect(wrapper.vm.isAllMenusSelected).toBe(false)
    })

    it('should compute isSomeMenusSelected correctly', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.menus = mockMenuTree
      const allIds = wrapper.vm.menuOptions.map((item: any) => item.id)

      // All selected => not "some"
      wrapper.vm.selectedMenuIds = allIds
      await nextTick()
      expect(wrapper.vm.isSomeMenusSelected).toBe(false)

      // Partially selected => "some"
      wrapper.vm.selectedMenuIds = [allIds[0]]
      await nextTick()
      expect(wrapper.vm.isSomeMenusSelected).toBe(true)

      // None selected => not "some"
      wrapper.vm.selectedMenuIds = []
      await nextTick()
      expect(wrapper.vm.isSomeMenusSelected).toBe(false)
    })

    it('should compute menuOptions with correct nesting levels', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.menus = mockMenuTree
      const options = wrapper.vm.menuOptions

      // Root menus have level 0
      expect(options.find((o: any) => o.id === 'menu-1')?.level).toBe(0)
      // Children have level 1
      expect(options.find((o: any) => o.id === 'menu-1-1')?.level).toBe(1)
      expect(options.find((o: any) => o.id === 'menu-1-2')?.level).toBe(1)
      expect(options.find((o: any) => o.id === 'menu-2')?.level).toBe(0)
    })
  })

  // =========================================================================
  // 10. Permission-based UI
  // =========================================================================
  describe('Permission-Based UI', () => {
    it('should show add button when canAddRole is true', async () => {
      vi.mocked(getRolePagePermissionState).mockReturnValue(fullPermissionState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // "新增角色" button should exist
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('新增角色'))
      expect(addBtn).toBeTruthy()
    })

    it('should hide add button when canAddRole is false', async () => {
      vi.mocked(getRolePagePermissionState).mockReturnValue(noPermissionState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      const addBtn = wrapper.findAll('button').find(b => b.text().includes('新增角色'))
      expect(addBtn).toBeFalsy()
    })

    it('should not render add/edit dialog when both canAddRole and canEditRole are false', async () => {
      vi.mocked(getRolePagePermissionState).mockReturnValue(noPermissionState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      // The dialog uses v-if="canAddRole || canEditRole"
      expect(wrapper.vm.canAddRole).toBe(false)
      expect(wrapper.vm.canEditRole).toBe(false)
    })

    it('should not render assign dialog when canAssignRole is false', async () => {
      vi.mocked(getRolePagePermissionState).mockReturnValue(noPermissionState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.canAssignRole).toBe(false)
    })

    it('should not render delete confirmation dialog when canDeleteRole is false', async () => {
      vi.mocked(getRolePagePermissionState).mockReturnValue(noPermissionState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.canDeleteRole).toBe(false)
    })

    it('should compute permission state from getRolePagePermissionState', async () => {
      const mockState = {
        canAddRole: true,
        canEditRole: false,
        canDeleteRole: true,
        canAssignRole: false
      }
      vi.mocked(getRolePagePermissionState).mockReturnValue(mockState)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.canAddRole).toBe(true)
      expect(wrapper.vm.canEditRole).toBe(false)
      expect(wrapper.vm.canDeleteRole).toBe(true)
      expect(wrapper.vm.canAssignRole).toBe(false)
    })

    it('should pass userStore.hasPermission to getRolePagePermissionState', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(getRolePagePermissionState).toHaveBeenCalledWith(
        wrapper.vm.userStore.hasPermission
      )
    })
  })

  // =========================================================================
  // 11. Form state and resetForm
  // =========================================================================
  describe('Form State', () => {
    it('should have correct default form values', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      expect(wrapper.vm.form).toEqual({
        name: '',
        code: '',
        description: '',
        dataScope: '1',
        status: '1',
        sortOrder: 0
      })
    })

    it('should reset form to defaults via resetForm', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      Object.assign(wrapper.vm.form, {
        name: 'Changed',
        code: 'CODE',
        description: 'Desc',
        dataScope: '5',
        status: '0',
        sortOrder: 100
      })

      wrapper.vm.resetForm()

      expect(wrapper.vm.form).toEqual({
        name: '',
        code: '',
        description: '',
        dataScope: '1',
        status: '1',
        sortOrder: 0
      })
    })
  })

  // =========================================================================
  // 12. deleteLoading guard
  // =========================================================================
  describe('Delete Loading Guard', () => {
    it('should set deleteLoading to true during deletion', () => {
      vi.mocked(deleteRole).mockReturnValue(new Promise(() => {}) as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      // Manually trigger delete without waiting
      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.handleDelete()

      expect(wrapper.vm.deleteLoading).toBe(true)
    })

    it('should set deleteLoading to false after deletion completes', async () => {
      vi.mocked(deleteRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      expect(wrapper.vm.deleteLoading).toBe(false)
    })

    it('should set deleteLoading to false even after deletion fails', async () => {
      vi.mocked(deleteRole).mockRejectedValue(new Error('Fail'))

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.deleteRoleId = 'role-2'
      wrapper.vm.handleDelete()
      await flushAsync(wrapper)

      expect(wrapper.vm.deleteLoading).toBe(false)
    })
  })

  // =========================================================================
  // 13. form validation edge cases
  // =========================================================================
  describe('Form Validation Edge Cases', () => {
    it('should not call createRole when name is whitespace-only', async () => {
      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.form.name = '   '
      wrapper.vm.handleSubmit()

      expect(createRole).not.toHaveBeenCalled()
    })

    it('should handle description as empty string trimmed to undefined for API', async () => {
      vi.mocked(createRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'Test Role'
      wrapper.vm.form.code = 'TEST_ROLE'
      wrapper.vm.form.description = '   '
      vi.mocked(isValidRoleCode).mockReturnValue(true)
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      expect(createRole).toHaveBeenCalledWith(
        expect.objectContaining({ description: undefined })
      )
    })

    it('should convert sortOrder 0 or NaN to 0 for API', async () => {
      vi.mocked(createRole).mockResolvedValue({
        code: 200,
        message: 'success',
        data: null
      } as any)

      wrapper = mount(Role, { global: { plugins: [pinia] } })
      await flushAsync(wrapper)

      wrapper.vm.handleAdd()
      await nextTick()

      wrapper.vm.form.name = 'Test Role'
      wrapper.vm.form.code = 'TEST_ROLE'
      wrapper.vm.form.sortOrder = 0
      vi.mocked(isValidRoleCode).mockReturnValue(true)
      wrapper.vm.handleSubmit()
      await flushAsync(wrapper)

      expect(createRole).toHaveBeenCalledWith(
        expect.objectContaining({ sortOrder: 0 })
      )
    })
  })
})
