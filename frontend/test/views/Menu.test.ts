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
  getMenuTree: vi.fn(),
  getMenuById: vi.fn(),
  createMenu: vi.fn(),
  updateMenu: vi.fn(),
  deleteMenu: vi.fn(),
  batchDelete: vi.fn(),
  batchUpdateStatus: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Menu from '@/views/system/Menu.vue'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import {
  getMenuTree,
  getMenuById,
  createMenu,
  updateMenu,
  deleteMenu,
  batchDelete,
  batchUpdateStatus
} from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeMenu(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'm1',
    parentId: '0',
    type: 1,
    name: '用户管理',
    path: '/system/user',
    component: 'system/User.vue',
    permKey: 'user:list',
    icon: 'Users',
    sortOrder: 1,
    visible: 1,
    status: 1,
    children: [],
    ...overrides
  }
}

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Menu Page', () => {
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
    vi.mocked(getMenuTree).mockResolvedValue(
      mockApiResponse([makeMenu()]) as any
    )
    vi.mocked(getMenuById).mockResolvedValue(
      mockApiResponse(makeMenu()) as any
    )
    vi.mocked(createMenu).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateMenu).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteMenu).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(batchDelete).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(batchUpdateStatus).mockResolvedValue(mockApiResponse(null) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Menu, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: true,
          StatusBadge: true
        }
      },
      ...options
    })
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders the root container', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders table with 9 column headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('thead th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toEqual([
        '', '菜单名称', '类型', '路由路径', '组件/权限', '可见', '状态', '排序', '操作'
      ])
    })

    it('renders search input with placeholder', async () => {
      wrapper = await mountAndFlush()
      const input = wrapper.find('input')
      expect(input.exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getMenuTree on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getMenuTree).toHaveBeenCalled()
    })

    it('populates menus from API response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.menus.length).toBe(1)
      expect(vm.menus[0].name).toBe('用户管理')
    })

    it('expands all menus with children on fetch', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.expandedKeys.has('m1')).toBe(true)
    })
  })

  // =========================================================================
  // 3. Tree structure — flattenedRows
  // =========================================================================
  describe('Tree Structure', () => {
    it('flattens tree into rows with correct level', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Both expanded: should see 2 rows
      expect(vm.flattenedRows.length).toBe(2)
      expect(vm.flattenedRows[0].level).toBe(0)
      expect(vm.flattenedRows[1].level).toBe(1)
    })

    it('hides children when parent is collapsed', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Collapse
      vm.toggleExpand('m1')
      await nextTick()

      expect(vm.flattenedRows.length).toBe(1)
      expect(vm.flattenedRows[0].id).toBe('m1')
    })

    it('toggleExpand adds and removes keys', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Default: m1 has no children, so expandedKeys is empty
      expect(vm.expandedKeys.has('m1')).toBe(false)

      // Add
      vm.toggleExpand('m1')
      expect(vm.expandedKeys.has('m1')).toBe(true)

      // Remove
      vm.toggleExpand('m1')
      expect(vm.expandedKeys.has('m1')).toBe(false)
    })
  })

  // =========================================================================
  // 4. Search / filter
  // =========================================================================
  describe('Search and Filter', () => {
    it('filters menus by name keyword', async () => {
      const tree = [
        makeMenu({ id: 'm1', name: '用户管理' }),
        makeMenu({ id: 'm2', name: '角色管理' })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = '用户'
      await nextTick()

      expect(vm.filteredMenus.length).toBe(1)
      expect(vm.filteredMenus[0].name).toBe('用户管理')
    })

    it('filters menus by path keyword', async () => {
      const tree = [
        makeMenu({ id: 'm1', name: '用户管理', path: '/system/user' }),
        makeMenu({ id: 'm2', name: '角色管理', path: '/system/role' })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = 'role'
      await nextTick()

      expect(vm.filteredMenus.length).toBe(1)
      expect(vm.filteredMenus[0].name).toBe('角色管理')
    })

    it('returns all menus when search is empty', async () => {
      const tree = [
        makeMenu({ id: 'm1', name: '用户管理' }),
        makeMenu({ id: 'm2', name: '角色管理' })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = ''
      await nextTick()

      expect(vm.filteredMenus.length).toBe(2)
    })

    it('keeps parent when child matches search', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = '用户'
      await nextTick()

      expect(vm.filteredMenus.length).toBe(1)
      expect(vm.filteredMenus[0].name).toBe('系统管理')
      expect(vm.filteredMenus[0].children.length).toBe(1)
    })

    it('handleSearch expands all filtered menus', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Collapse everything first
      vm.expandedKeys = new Set()
      vm.searchQuery = '用户'
      vm.handleSearch()

      expect(vm.expandedKeys.has('m1')).toBe(true)
    })

    it('reset search re-expands all original menus', async () => {
      const tree = [
        makeMenu({
          id: 'm1',
          name: '系统管理',
          children: [
            makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
          ]
        })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = ''
      vm.handleSearch()

      expect(vm.expandedKeys.has('m1')).toBe(true)
    })
  })

  // =========================================================================
  // 5. Add menu
  // =========================================================================
  describe('Add Menu', () => {
    it('handleAdd resets form, sets defaults, opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = 'old'
      vm.handleAdd()

      expect(vm.isEdit).toBe(false)
      expect(vm.editId).toBe('')
      expect(vm.dialogOpen).toBe(true)
      expect(vm.form.parentId).toBe('0')
      expect(vm.form.type).toBe('0')
      expect(vm.form.name).toBe('')
    })

    it('handleAdd with parentId and type sets them', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleAdd('parent-1', '1')

      expect(vm.form.parentId).toBe('parent-1')
      expect(vm.form.type).toBe('1')
    })
  })

  // =========================================================================
  // 6. Edit menu
  // =========================================================================
  describe('Edit Menu', () => {
    it('handleEdit sets isEdit, editId, opens dialog and fetches menu', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('menu-123')
      await flushAsync()

      expect(vm.isEdit).toBe(true)
      expect(vm.editId).toBe('menu-123')
      expect(vm.dialogOpen).toBe(true)
      expect(getMenuById).toHaveBeenCalledWith('menu-123')
    })

    it('populates form with fetched menu data', async () => {
      vi.mocked(getMenuById).mockResolvedValue(
        mockApiResponse(makeMenu({
          name: '角色管理',
          type: 1,
          path: '/system/role',
          permKey: 'role:list',
          sortOrder: 5,
          visible: 1,
          status: 1
        })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('m1')
      await flushAsync()

      expect(vm.form.name).toBe('角色管理')
      expect(vm.form.type).toBe('1')
      expect(vm.form.path).toBe('/system/role')
      expect(vm.form.sortOrder).toBe(5)
    })

    it('closes dialog on fetch error', async () => {
      vi.mocked(getMenuById).mockRejectedValueOnce(new Error('Not found'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('bad-id')
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 7. Submit menu
  // =========================================================================
  describe('Submit Menu', () => {
    it('shows warning when name is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = ''
      vm.form.type = '0'
      vm.form.path = '/test'
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入菜单名称')
    })

    it('shows warning when path is empty for non-button type', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '测试'
      vm.form.type = '0'
      vm.form.path = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入路由路径')
    })

    it('shows warning when component is empty for menu type', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '测试'
      vm.form.type = '1'
      vm.form.path = '/test'
      vm.form.component = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入组件路径')
    })

    it('shows warning when permKey is empty for button type', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '测试'
      vm.form.type = '2'
      vm.form.permKey = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入权限标识')
    })

    it('button type does not require path', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.form.name = '新增用户'
      vm.form.type = '2'
      vm.form.path = ''
      vm.form.permKey = 'user:add'

      await vm.handleSubmit()
      await flushAsync()

      expect(createMenu).toHaveBeenCalled()
    })

    it('calls createMenu when isEdit is false', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = false
      vm.form.name = '用户管理'
      vm.form.type = '1'
      vm.form.path = '/system/user'
      vm.form.component = 'system/User.vue'

      await vm.handleSubmit()
      await flushAsync()

      expect(createMenu).toHaveBeenCalledWith(
        expect.objectContaining({
          name: '用户管理',
          type: 1,
          path: '/system/user'
        })
      )
      expect(toast.success).toHaveBeenCalledWith('菜单创建成功')
    })

    it('calls updateMenu when isEdit is true', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(updateMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = true
      vm.editId = 'menu-1'
      vm.form.name = '更新菜单'
      vm.form.type = '1'
      vm.form.path = '/test'
      vm.form.component = 'Test.vue'

      await vm.handleSubmit()
      await flushAsync()

      expect(updateMenu).toHaveBeenCalledWith('menu-1',
        expect.objectContaining({
          name: '更新菜单',
          type: 1
        })
      )
      expect(toast.success).toHaveBeenCalledWith('菜单更新成功')
    })

    it('closes dialog and refreshes data on success', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.form.name = '测试'
      vm.form.type = '1'
      vm.form.path = '/test'
      vm.form.component = 'Test.vue'
      vm.dialogOpen = true

      await vm.handleSubmit()
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
      expect(getMenuTree).toHaveBeenCalled()
    })

    it('shows error toast on create failure', async () => {
      vi.mocked(createMenu).mockRejectedValueOnce(new Error('创建失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '测试'
      vm.form.type = '1'
      vm.form.path = '/test'
      vm.form.component = 'Test.vue'

      await vm.handleSubmit()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('创建失败')
    })
  })

  // =========================================================================
  // 8. Delete menu
  // =========================================================================
  describe('Delete Menu', () => {
    it('handleDeleteConfirm sets deleteMenuId and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteConfirm('menu-del')
      expect(vm.deleteMenuId).toBe('menu-del')
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('calls deleteMenu for single delete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(deleteMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteMenuId = 'menu-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(deleteMenu).toHaveBeenCalledWith('menu-del')
      expect(vm.deleteDialogOpen).toBe(false)
      expect(toast.success).toHaveBeenCalledWith('菜单删除成功')
    })

    it('calls batchDelete when deleteMenuId is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(batchDelete).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteMenuId = ''
      vm.selectedMenuIds = ['m1', 'm2']
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(batchDelete).toHaveBeenCalledWith(['m1', 'm2'])
      expect(toast.success).toHaveBeenCalledWith('已删除 2 个菜单')
    })

    it('clears selectedMenuIds after batch delete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(batchDelete).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteMenuId = ''
      vm.selectedMenuIds = ['m1', 'm2']
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(vm.selectedMenuIds).toEqual([])
    })

    it('closes dialog even on delete error', async () => {
      vi.mocked(deleteMenu).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteMenuId = 'menu-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
      expect(vm.deleteDialogOpen).toBe(false)
    })

    it('handleBatchDeleteConfirm shows warning when nothing selected', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = []
      vm.handleBatchDeleteConfirm()

      expect(toast.warning).toHaveBeenCalledWith('请先选择要删除的菜单')
    })

    it('handleBatchDeleteConfirm opens dialog with empty deleteMenuId', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = ['m1']
      vm.handleBatchDeleteConfirm()

      expect(vm.deleteMenuId).toBe('')
      expect(vm.deleteDialogOpen).toBe(true)
    })
  })

  // =========================================================================
  // 9. Batch status change
  // =========================================================================
  describe('Batch Status Change', () => {
    it('shows warning when no menus selected', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = []
      vm.handleBatchStatusChange(1)

      expect(toast.warning).toHaveBeenCalledWith('请先选择要更新状态的菜单')
    })

    it('calls batchUpdateStatus with enable status', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(batchUpdateStatus).mockResolvedValue(mockApiResponse(null) as any)

      vm.selectedMenuIds = ['m1', 'm2']

      await vm.handleBatchStatusChange(1)
      await flushAsync()

      expect(batchUpdateStatus).toHaveBeenCalledWith(['m1', 'm2'], 1)
      expect(toast.success).toHaveBeenCalledWith('已批量启用菜单')
    })

    it('calls batchUpdateStatus with disable status', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(batchUpdateStatus).mockResolvedValue(mockApiResponse(null) as any)

      vm.selectedMenuIds = ['m1']

      await vm.handleBatchStatusChange(0)
      await flushAsync()

      expect(batchUpdateStatus).toHaveBeenCalledWith(['m1'], 0)
      expect(toast.success).toHaveBeenCalledWith('已批量禁用菜单')
    })
  })

  // =========================================================================
  // 10. Selection
  // =========================================================================
  describe('Selection', () => {
    it('toggleMenuSelection adds menu id', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleMenuSelection('m1', true)
      expect(vm.selectedMenuIds).toContain('m1')
    })

    it('toggleMenuSelection removes menu id', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = ['m1']
      vm.toggleMenuSelection('m1', false)
      expect(vm.selectedMenuIds).not.toContain('m1')
    })

    it('toggleSelectAll selects all rows', async () => {
      const tree = [
        makeMenu({ id: 'm1' }),
        makeMenu({ id: 'm2' })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleSelectAll(true)
      expect(vm.selectedMenuIds.length).toBe(2)
    })

    it('toggleSelectAll deselects all', async () => {
      const tree = [makeMenu({ id: 'm1' })]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = ['m1']
      vm.toggleSelectAll(false)
      expect(vm.selectedMenuIds).toEqual([])
    })

    it('hasSelectedMenus is true when selections exist', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.hasSelectedMenus).toBe(false)
      vm.selectedMenuIds = ['m1']
      await nextTick()
      expect(vm.hasSelectedMenus).toBe(true)
    })

    it('allSelected is true when all rows selected', async () => {
      const tree = [makeMenu({ id: 'm1' })]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = ['m1']
      await nextTick()
      expect(vm.allSelected).toBe(true)
    })

    it('allSelected is false when not all rows selected', async () => {
      const tree = [makeMenu({ id: 'm1' }), makeMenu({ id: 'm2' })]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.selectedMenuIds = ['m1']
      await nextTick()
      expect(vm.allSelected).toBe(false)
    })
  })

  // =========================================================================
  // 11. Permission-based UI
  // =========================================================================
  describe('Permission-Based UI', () => {
    it('computes canAddMenu from userStore.hasPermission', async () => {
      userStore.hasPermission = vi.fn((perm: string) => perm === 'menu:add')
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddMenu).toBe(true)
      expect(vm.canEditMenu).toBe(false)
      expect(vm.canDeleteMenu).toBe(false)
    })

    it('all permissions false when user has none', async () => {
      userStore.hasPermission = vi.fn(() => false)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddMenu).toBe(false)
      expect(vm.canEditMenu).toBe(false)
      expect(vm.canDeleteMenu).toBe(false)
    })
  })

  // =========================================================================
  // 12. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading is true', async () => {
      vi.mocked(getMenuTree).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Menu, {
        global: {
          plugins: [pinia],
          stubs: { ConfirmDialog: true, StatusBadge: true }
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when no menus', async () => {
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('暂无菜单数据')
    })

    it('sets loading to false after data fetch completes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 13. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getMenuTree error gracefully', async () => {
      vi.mocked(getMenuTree).mockRejectedValue(new Error('获取菜单失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('获取菜单失败')
    })
  })

  // =========================================================================
  // 14. Table content rendering
  // =========================================================================
  describe('Table Content', () => {
    it('renders menu data in table cells', async () => {
      const menu = makeMenu({
        name: '用户管理',
        type: 1,
        path: '/system/user',
        component: 'system/User.vue',
        sortOrder: 3,
        visible: 1
      })
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([menu]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('用户管理')
      expect(wrapper.text()).toContain('/system/user')
      expect(wrapper.text()).toContain('system/User.vue')
      expect(wrapper.text()).toContain('3')
    })

    it('renders type labels correctly', async () => {
      const menus = [
        makeMenu({ id: 'm1', name: '目录', type: 0, children: [] }),
        makeMenu({ id: 'm2', name: '菜单', type: 1, children: [] }),
        makeMenu({ id: 'm3', name: '按钮', type: 2, children: [] })
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(menus) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('目录')
      expect(wrapper.text()).toContain('菜单')
      expect(wrapper.text()).toContain('按钮')
    })

    it('renders visible badge', async () => {
      const menu = makeMenu({ visible: 1 })
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([menu]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('显示')
    })

    it('renders hidden badge', async () => {
      const menu = makeMenu({ visible: 0 })
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([menu]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('隐藏')
    })
  })

  // =========================================================================
  // 15. parentOptions computed
  // =========================================================================
  describe('Parent Options', () => {
    it('includes top-level option and all menus', async () => {
      const tree = [
        makeMenu({ id: 'm1', name: '系统管理', children: [
          makeMenu({ id: 'm2', name: '用户管理', parentId: 'm1', children: [] })
        ]})
      ]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // "顶级菜单" + "系统管理" + "　用户管理" (indented)
      expect(vm.parentOptions.length).toBe(3)
      expect(vm.parentOptions[0]).toEqual({ id: '0', label: '顶级菜单' })
      expect(vm.parentOptions[1]).toEqual({ id: 'm1', label: '系统管理' })
    })

    it('excludes current item when editing', async () => {
      const tree = [makeMenu({ id: 'm1', name: '系统管理' })]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.isEdit = true
      vm.editId = 'm1'
      await nextTick()

      // "顶级菜单" only, m1 excluded
      expect(vm.parentOptions.length).toBe(1)
      expect(vm.parentOptions[0].id).toBe('0')
    })
  })

  // =========================================================================
  // 16. Form validation edge cases
  // =========================================================================
  describe('Form Validation Edge Cases', () => {
    it('button type (2) requires permKey but not path', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createMenu).mockResolvedValue(mockApiResponse(null) as any)

      vm.form.name = '新增按钮'
      vm.form.type = '2'
      vm.form.path = ''
      vm.form.permKey = 'user:add'

      await vm.handleSubmit()
      await flushAsync()

      expect(createMenu).toHaveBeenCalled()
    })

    it('directory type (0) requires path', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '目录'
      vm.form.type = '0'
      vm.form.path = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入路由路径')
    })

    it('menu type (1) requires component', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '菜单'
      vm.form.type = '1'
      vm.form.path = '/test'
      vm.form.component = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入组件路径')
    })
  })

  // =========================================================================
  // 17. Reset form
  // =========================================================================
  describe('Reset Form', () => {
    it('resetForm resets all fields to defaults', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.parentId = 'm1'
      vm.form.name = '旧名称'
      vm.form.type = '2'
      vm.form.path = '/old'
      vm.form.component = 'Old.vue'
      vm.form.permKey = 'old:key'
      vm.form.icon = 'OldIcon'
      vm.form.sortOrder = 99
      vm.form.visible = '0'
      vm.form.status = '0'

      vm.resetForm()

      expect(vm.form.parentId).toBe('0')
      expect(vm.form.name).toBe('')
      expect(vm.form.type).toBe('0')
      expect(vm.form.path).toBe('')
      expect(vm.form.component).toBe('')
      expect(vm.form.permKey).toBe('')
      expect(vm.form.icon).toBe('')
      expect(vm.form.sortOrder).toBe(0)
      expect(vm.form.visible).toBe('1')
      expect(vm.form.status).toBe('1')
    })
  })

  // =========================================================================
  // 18. matchesSearch helper
  // =========================================================================
  describe('Matches Search', () => {
    it('matches by name', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.matchesSearch(makeMenu({ name: '用户管理' }), '用户')).toBe(true)
      expect(vm.matchesSearch(makeMenu({ name: '角色管理' }), '用户')).toBe(false)
    })

    it('matches by path', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.matchesSearch(makeMenu({ path: '/system/user' }), 'user')).toBe(true)
    })

    it('matches by component', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.matchesSearch(makeMenu({ component: 'system/User.vue' }), 'user')).toBe(true)
    })

    it('matches by permKey', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.matchesSearch(makeMenu({ permKey: 'user:list' }), 'user')).toBe(true)
    })

    it('case insensitive', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.matchesSearch(makeMenu({ name: 'User Management' }), 'user')).toBe(true)
    })
  })

  // =========================================================================
  // 19. flattenMenuIds helper
  // =========================================================================
  describe('Flatten Menu IDs', () => {
    it('flattens all menu IDs including children', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const tree = [
        makeMenu({ id: 'm1', children: [
          makeMenu({ id: 'm2', parentId: 'm1', children: [
            makeMenu({ id: 'm3', parentId: 'm2', children: [] })
          ]})
        ]})
      ]

      const ids = vm.flattenMenuIds(tree)
      expect(ids).toEqual(['m1', 'm2', 'm3'])
    })
  })

  // =========================================================================
  // 20. Selected IDs cleanup on refresh
  // =========================================================================
  describe('Selection Cleanup', () => {
    it('removes stale selected IDs on data refresh', async () => {
      const tree = [makeMenu({ id: 'm1' })]
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Add a stale selection
      vm.selectedMenuIds = ['m1', 'm_deleted']
      await nextTick()

      // Refresh data — m_deleted no longer in tree
      vi.clearAllMocks()
      vi.mocked(getMenuTree).mockResolvedValue(mockApiResponse(tree) as any)

      await vm.fetchData()
      await flushAsync()

      expect(vm.selectedMenuIds).toContain('m1')
      expect(vm.selectedMenuIds).not.toContain('m_deleted')
    })
  })
})
