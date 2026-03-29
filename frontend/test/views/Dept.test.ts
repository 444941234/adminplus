import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// ---------------------------------------------------------------------------
// Mocks
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

vi.mock('@/lib/validators', () => ({
  isValidEmail: vi.fn((email: string) => /^[^@]+@[^@]+\.[^@]+$/.test(email)),
  isValidChinaPhone: vi.fn((phone: string) => /^1[3-9]\d{9}$/.test(phone))
}))

vi.mock('@/api', () => ({
  getDeptTree: vi.fn(),
  getDeptById: vi.fn(),
  createDept: vi.fn(),
  updateDept: vi.fn(),
  deleteDept: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Dept from '@/views/system/Dept.vue'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import { getDeptTree, getDeptById, createDept, updateDept, deleteDept } from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeDept(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'dept1',
    parentId: '0',
    name: '技术部',
    code: 'TECH',
    leader: '张三',
    phone: '13800138000',
    email: 'zhangsan@test.com',
    sortOrder: 1,
    status: 1,
    children: [],
    createTime: '2026-01-15T10:00:00',
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

describe('Dept Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper
  let userStore: ReturnType<typeof useUserStore>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)

    userStore = useUserStore()
    userStore.hasPermission = vi.fn(() => true)

    vi.clearAllMocks()

    vi.mocked(getDeptTree).mockResolvedValue(
      mockApiResponse([makeDept()]) as any
    )
    vi.mocked(getDeptById).mockResolvedValue(
      mockApiResponse(makeDept()) as any
    )
    vi.mocked(createDept).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateDept).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteDept).mockResolvedValue(mockApiResponse(null) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Dept, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: true
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
    it('renders root container', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders table with column headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('thead th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('部门名称')
      expect(headerTexts).toContain('部门编码')
      expect(headerTexts).toContain('负责人')
      expect(headerTexts).toContain('电话')
      expect(headerTexts).toContain('排序')
      expect(headerTexts).toContain('状态')
      expect(headerTexts).toContain('操作')
    })

    it('renders search input', async () => {
      wrapper = await mountAndFlush()
      const input = wrapper.find('input')
      expect(input.exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getDeptTree on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getDeptTree).toHaveBeenCalled()
    })

    it('populates treeData from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.treeData.length).toBe(1)
      expect(vm.treeData[0].name).toBe('技术部')
    })

    it('expands all parent nodes on fetch', async () => {
      const tree = [
        makeDept({
          id: 'd1',
          name: '总部',
          children: [makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })]
        })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.expandedKeys.has('d1')).toBe(true)
    })
  })

  // =========================================================================
  // 3. Tree rendering
  // =========================================================================
  describe('Tree Rendering', () => {
    it('renders flat rows for expanded tree', async () => {
      const tree = [
        makeDept({
          id: 'd1', name: '总部',
          children: [makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })]
        })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.tableRows.length).toBe(2)
      expect(vm.tableRows[0].level).toBe(0)
      expect(vm.tableRows[1].level).toBe(1)
    })

    it('hides children when collapsed', async () => {
      const tree = [
        makeDept({
          id: 'd1', name: '总部',
          children: [makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })]
        })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleExpand('d1')
      await nextTick()

      expect(vm.tableRows.length).toBe(1)
      expect(vm.tableRows[0].id).toBe('d1')
    })

    it('toggleExpand adds and removes keys', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.toggleExpand('new-key')
      expect(vm.expandedKeys.has('new-key')).toBe(true)

      vm.toggleExpand('new-key')
      expect(vm.expandedKeys.has('new-key')).toBe(false)
    })
  })

  // =========================================================================
  // 4. Search / filter
  // =========================================================================
  describe('Search and Filter', () => {
    it('filters by name keyword', async () => {
      const tree = [
        makeDept({ id: 'd1', name: '技术部' }),
        makeDept({ id: 'd2', name: '产品部' })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = '技术'
      await nextTick()

      expect(vm.tableRows.length).toBe(1)
      expect(vm.tableRows[0].name).toBe('技术部')
    })

    it('filters by code', async () => {
      const tree = [
        makeDept({ id: 'd1', name: '技术部', code: 'TECH' }),
        makeDept({ id: 'd2', name: '产品部', code: 'PROD' })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = 'PROD'
      await nextTick()

      expect(vm.tableRows.length).toBe(1)
      expect(vm.tableRows[0].name).toBe('产品部')
    })

    it('keeps parent when child matches', async () => {
      const tree = [
        makeDept({
          id: 'd1', name: '总部',
          children: [makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })]
        })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = '技术'
      await nextTick()

      expect(vm.tableRows.length).toBe(2)
      expect(vm.tableRows[0].name).toBe('总部')
      expect(vm.tableRows[1].name).toBe('技术部')
    })

    it('shows all when search cleared', async () => {
      const tree = [
        makeDept({ id: 'd1', name: '技术部' }),
        makeDept({ id: 'd2', name: '产品部' })
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchQuery = '技术'
      await nextTick()
      expect(vm.tableRows.length).toBe(1)

      vm.searchQuery = ''
      await nextTick()
      expect(vm.tableRows.length).toBe(2)
    })
  })

  // =========================================================================
  // 5. Add dept
  // =========================================================================
  describe('Add Dept', () => {
    it('handleAdd resets form, sets isEdit=false, opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '旧部门'
      vm.handleAdd()

      expect(vm.isEdit).toBe(false)
      expect(vm.editId).toBe('')
      expect(vm.dialogOpen).toBe(true)
      expect(vm.form.name).toBe('')
    })
  })

  // =========================================================================
  // 6. Edit dept
  // =========================================================================
  describe('Edit Dept', () => {
    it('handleEdit sets isEdit, editId, opens dialog, fetches dept', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('dept-123')
      await flushAsync()

      expect(vm.isEdit).toBe(true)
      expect(vm.editId).toBe('dept-123')
      expect(vm.dialogOpen).toBe(true)
      expect(getDeptById).toHaveBeenCalledWith('dept-123')
    })

    it('populates form from fetched data', async () => {
      vi.mocked(getDeptById).mockResolvedValue(
        mockApiResponse(makeDept({ name: '财务部', code: 'FIN', leader: '李四', sortOrder: 5 })) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('dept-1')
      await flushAsync()

      expect(vm.form.name).toBe('财务部')
      expect(vm.form.code).toBe('FIN')
      expect(vm.form.leader).toBe('李四')
      expect(vm.form.sortOrder).toBe(5)
    })

    it('closes dialog on fetch error', async () => {
      vi.mocked(getDeptById).mockRejectedValueOnce(new Error('Not found'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('bad-id')
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 7. Submit dept
  // =========================================================================
  describe('Submit Dept', () => {
    it('shows warning when name is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入部门名称')
    })

    it('shows warning for invalid email', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const { isValidEmail } = await import('@/lib/validators')

      vi.mocked(isValidEmail).mockReturnValueOnce(false)
      vm.form.name = '测试'
      vm.form.email = 'bad-email'
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('邮箱格式不正确')
    })

    it('shows warning for invalid phone', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const { isValidChinaPhone } = await import('@/lib/validators')

      vi.mocked(isValidChinaPhone).mockReturnValueOnce(false)
      vm.form.name = '测试'
      vm.form.phone = 'bad-phone'
      vm.form.email = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('手机号格式不正确')
    })

    it('calls createDept when isEdit is false', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createDept).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = false
      vm.form.name = '新部门'
      vm.form.code = 'NEW'

      await vm.handleSubmit()
      await flushAsync()

      expect(createDept).toHaveBeenCalledWith(
        expect.objectContaining({ name: '新部门', code: 'NEW' })
      )
      expect(toast.success).toHaveBeenCalledWith('部门创建成功')
    })

    it('calls updateDept when isEdit is true', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(updateDept).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = true
      vm.editId = 'dept-1'
      vm.form.name = '更新部门'

      await vm.handleSubmit()
      await flushAsync()

      expect(updateDept).toHaveBeenCalledWith('dept-1',
        expect.objectContaining({ name: '更新部门' })
      )
      expect(toast.success).toHaveBeenCalledWith('部门更新成功')
    })

    it('closes dialog and refreshes on success', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(createDept).mockResolvedValue(mockApiResponse(null) as any)

      vm.form.name = '新部门'
      vm.dialogOpen = true

      await vm.handleSubmit()
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
      expect(getDeptTree).toHaveBeenCalled()
    })

    it('shows error toast on create failure', async () => {
      vi.mocked(createDept).mockRejectedValueOnce(new Error('创建失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.name = '新部门'

      await vm.handleSubmit()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('创建失败')
    })
  })

  // =========================================================================
  // 8. Delete dept
  // =========================================================================
  describe('Delete Dept', () => {
    it('handleDeleteConfirm sets id and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteConfirm('dept-del')
      expect(vm.deleteDeptId).toBe('dept-del')
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('calls deleteDept and refreshes on success', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      vi.mocked(deleteDept).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteDeptId = 'dept-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(deleteDept).toHaveBeenCalledWith('dept-del')
      expect(toast.success).toHaveBeenCalledWith('部门删除成功')
      expect(vm.deleteDialogOpen).toBe(false)
    })

    it('shows error toast on delete failure', async () => {
      vi.mocked(deleteDept).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteDeptId = 'dept-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
    })
  })

  // =========================================================================
  // 9. Permissions
  // =========================================================================
  describe('Permission-Based UI', () => {
    it('computes permissions correctly', async () => {
      userStore.hasPermission = vi.fn((perm: string) => perm === 'dept:add')
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddDept).toBe(true)
      expect(vm.canEditDept).toBe(false)
      expect(vm.canDeleteDept).toBe(false)
    })

    it('all false when no permissions', async () => {
      userStore.hasPermission = vi.fn(() => false)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddDept).toBe(false)
      expect(vm.canEditDept).toBe(false)
      expect(vm.canDeleteDept).toBe(false)
    })
  })

  // =========================================================================
  // 10. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading', async () => {
      vi.mocked(getDeptTree).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Dept, {
        global: { plugins: [pinia], stubs: { ConfirmDialog: true } }
      })
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when no data', async () => {
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('暂无数据')
    })

    it('sets loading to false after fetch', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 11. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getDeptTree error gracefully', async () => {
      vi.mocked(getDeptTree).mockRejectedValue(new Error('获取部门失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('获取部门失败')
    })
  })

  // =========================================================================
  // 12. Table content
  // =========================================================================
  describe('Table Content', () => {
    it('renders dept data in table', async () => {
      const tree = [makeDept({ name: '技术部', code: 'TECH', leader: '张三', phone: '13800138000' })]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('技术部')
      expect(wrapper.text()).toContain('TECH')
      expect(wrapper.text()).toContain('张三')
      expect(wrapper.text()).toContain('13800138000')
    })

    it('renders status badges', async () => {
      const tree = [makeDept({ status: 1 })]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('正常')
    })

    it('renders disabled status', async () => {
      const tree = [makeDept({ status: 0 })]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('禁用')
    })

    it('shows "-" for empty fields', async () => {
      const tree = [makeDept({ leader: '', phone: '', email: '' })]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()

      const cells = wrapper.findAll('tbody td')
      const cellTexts = cells.map(c => c.text())
      const dashCount = cellTexts.filter(t => t.trim() === '-').length
      expect(dashCount).toBeGreaterThanOrEqual(2)
    })
  })

  // =========================================================================
  // 13. parentOptions computed
  // =========================================================================
  describe('Parent Options', () => {
    it('includes all depts from tree (顶级部门 is in template, not computed)', async () => {
      const tree = [
        makeDept({ id: 'd1', name: '总部', children: [
          makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })
        ]})
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // parentOptions computed only has tree items (flattenDeptOptions),
      // "顶级部门" {id:'0'} is hardcoded in the template <SelectItem>
      expect(vm.parentOptions.length).toBe(2)
      expect(vm.parentOptions[0]).toEqual({ id: 'd1', label: '总部' })
      expect(vm.parentOptions[1]).toEqual({ id: 'd2', label: '\u3000技术部' })
    })

    it('excludes self and all descendants when editing', async () => {
      const tree = [
        makeDept({ id: 'd1', name: '总部', children: [
          makeDept({ id: 'd2', parentId: 'd1', name: '技术部', children: [] })
        ]})
      ]
      vi.mocked(getDeptTree).mockResolvedValue(mockApiResponse(tree) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.isEdit = true
      vm.editId = 'd1'
      await nextTick()

      // descendantIds(tree, 'd1') returns {d1, d2} — ALL items blocked
      // parentOptions only contains flattenDeptOptions results, so length = 0
      expect(vm.parentOptions.length).toBe(0)
    })
  })

  // =========================================================================
  // 14. Reset form
  // =========================================================================
  describe('Reset Form', () => {
    it('resets all fields to defaults', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.parentId = 'd1'
      vm.form.name = '旧'
      vm.form.code = 'OLD'
      vm.form.leader = '旧'
      vm.form.phone = '旧'
      vm.form.email = '旧'
      vm.form.sortOrder = 99
      vm.form.status = '0'

      vm.resetForm()

      expect(vm.form.parentId).toBe('0')
      expect(vm.form.name).toBe('')
      expect(vm.form.code).toBe('')
      expect(vm.form.leader).toBe('')
      expect(vm.form.phone).toBe('')
      expect(vm.form.email).toBe('')
      expect(vm.form.sortOrder).toBe(0)
      expect(vm.form.status).toBe('1')
    })
  })

  // =========================================================================
  // 15. descendantIds helper
  // =========================================================================
  describe('Descendant IDs', () => {
    it('collects all descendant IDs', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const tree = [
        makeDept({ id: 'd1', children: [
          makeDept({ id: 'd2', parentId: 'd1', children: [
            makeDept({ id: 'd3', parentId: 'd2', children: [] })
          ]})
        ]})
      ]

      const ids = vm.descendantIds(tree, 'd1')
      expect(ids).toEqual(new Set(['d1', 'd2', 'd3']))
    })

    it('returns empty set for non-existent id', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const ids = vm.descendantIds([], 'not-exist')
      expect(ids.size).toBe(0)
    })
  })
})
