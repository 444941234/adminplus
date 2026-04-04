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
  getDictList: vi.fn(),
  getDictById: vi.fn(),
  createDict: vi.fn(),
  updateDict: vi.fn(),
  deleteDict: vi.fn(),
  updateDictStatus: vi.fn(),
  getDictItemList: vi.fn(),
  getDictItemById: vi.fn(),
  createDictItem: vi.fn(),
  updateDictItem: vi.fn(),
  deleteDictItem: vi.fn(),
  updateDictItemStatus: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports that resolve through the mocked modules above
// ---------------------------------------------------------------------------

import Dict from '@/views/system/Dict.vue'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import {
  getDictList,
  getDictById,
  createDict,
  updateDict,
  deleteDict,
  updateDictStatus,
  getDictItemList,
  getDictItemById,
  createDictItem,
  updateDictItem,
  deleteDictItem,
  updateDictItemStatus
} from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeDict(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'd1',
    dictName: '用户状态',
    dictType: 'user_status',
    description: '用户状态字典',
    status: 1,
    createTime: '2026-01-15T10:00:00',
    updateTime: '2026-03-01T10:00:00',
    ...overrides
  }
}

function makeDictItem(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'di1',
    dictId: 'd1',
    parentId: '0',
    label: '正常',
    value: '1',
    sortOrder: 1,
    status: 1,
    remark: '',
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

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Dict Page', () => {
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
    vi.mocked(getDictList).mockResolvedValue(
      mockPageResult([makeDict()]) as any
    )
    vi.mocked(getDictById).mockResolvedValue(
      mockApiResponse(makeDict()) as any
    )
    vi.mocked(createDict).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateDict).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateDictStatus).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteDict).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(getDictItemList).mockResolvedValue(
      mockApiResponse({ records: [makeDictItem()], total: 1 }) as any
    )
    vi.mocked(getDictItemById).mockResolvedValue(
      mockApiResponse(makeDictItem()) as any
    )
    vi.mocked(createDictItem).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateDictItem).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteDictItem).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateDictItemStatus).mockResolvedValue(mockApiResponse(null) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Dict, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: true,
          ListSearchBar: true,
          Pagination: true,
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

    it('renders Card component with dict table', async () => {
      wrapper = await mountAndFlush()
      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBeGreaterThanOrEqual(1)
    })

    it('renders dict table with 7 column headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('thead th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toEqual([
        'ID', '字典名称', '字典类型', '备注', '状态', '创建时间', '操作'
      ])
    })

    it('renders ListSearchBar stub', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.findComponent({ name: 'ListSearchBar' }).exists()).toBe(true)
    })

    it('renders Pagination stub', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.findComponent({ name: 'Pagination' }).exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getDictList on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getDictList).toHaveBeenCalled()
    })

    it('populates tableData with records from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.tableData.records.length).toBe(1)
      expect(vm.tableData.records[0].dictName).toBe('用户状态')
    })
  })

  // =========================================================================
  // 3. Dict CRUD — Add
  // =========================================================================
  describe('Add Dict', () => {
    it('handleAdd resets form, sets isEdit=false, opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.dictType = 'old_type'
      vm.form.dictName = 'old_name'

      vm.handleAdd()
      expect(vm.isEdit).toBe(false)
      expect(vm.editId).toBe('')
      expect(vm.dialogOpen).toBe(true)
      expect(vm.form.dictType).toBe('')
      expect(vm.form.dictName).toBe('')
    })
  })

  // =========================================================================
  // 4. Dict CRUD — Edit
  // =========================================================================
  describe('Edit Dict', () => {
    it('handleEdit sets isEdit=true, editId, opens dialog and fetches dict', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('dict-123')
      await flushAsync()

      expect(vm.isEdit).toBe(true)
      expect(vm.editId).toBe('dict-123')
      expect(vm.dialogOpen).toBe(true)
      expect(getDictById).toHaveBeenCalledWith('dict-123')
    })

    it('closes dialog on fetch error', async () => {
      vi.mocked(getDictById).mockRejectedValueOnce(new Error('Not found'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.handleEdit('bad-id')
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 5. Dict CRUD — Submit
  // =========================================================================
  describe('Submit Dict', () => {
    it('shows warning toast when dictType is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.dictType = ''
      vm.form.dictName = 'name'
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入字典类型')
    })

    it('shows warning toast when dictName is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.dictType = 'type'
      vm.form.dictName = ''
      vm.handleSubmit()

      expect(toast.warning).toHaveBeenCalledWith('请输入字典名称')
    })

    it('calls createDict when isEdit is false', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(createDict).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = false
      vm.form.dictType = 'new_type'
      vm.form.dictName = '新字典'
      vm.form.remark = '备注'

      await vm.handleSubmit()
      await flushAsync()

      expect(createDict).toHaveBeenCalledWith(
        expect.objectContaining({
          dictType: 'new_type',
          dictName: '新字典',
          description: '备注'
        })
      )
      expect(toast.success).toHaveBeenCalledWith('字典创建成功')
    })

    it('calls updateDict when isEdit is true', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(updateDict).mockResolvedValue(mockApiResponse(null) as any)

      vm.isEdit = true
      vm.editId = 'dict-1'
      vm.form.dictType = 'user_status'
      vm.form.dictName = '更新字典'
      vm.form.status = '1'

      await vm.handleSubmit()
      await flushAsync()

      expect(updateDict).toHaveBeenCalledWith('dict-1',
        expect.objectContaining({
          dictName: '更新字典',
          status: 1
        })
      )
      expect(toast.success).toHaveBeenCalledWith('字典更新成功')
    })

    it('closes dialog and refreshes data on success', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(createDict).mockResolvedValue(mockApiResponse(null) as any)

      vm.form.dictType = 'new_type'
      vm.form.dictName = '新字典'
      vm.dialogOpen = true

      await vm.handleSubmit()
      await flushAsync()

      expect(vm.dialogOpen).toBe(false)
      expect(getDictList).toHaveBeenCalled()
    })

    it('shows error toast on create failure', async () => {
      vi.mocked(createDict).mockRejectedValueOnce(new Error('创建失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.isEdit = false
      vm.form.dictType = 'type'
      vm.form.dictName = 'name'

      await vm.handleSubmit()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('创建失败')
    })
  })

  // =========================================================================
  // 6. Dict — Status toggle
  // =========================================================================
  describe('Dict Status Toggle', () => {
    it('calls updateDictStatus with toggled status (1 -> 0)', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(updateDictStatus).mockResolvedValue(mockApiResponse(null) as any)

      const dict = makeDict({ id: 'd1', status: 1 })
      await vm.handleStatusToggle(dict)
      await flushAsync()

      expect(updateDictStatus).toHaveBeenCalledWith('d1', 0)
      expect(toast.success).toHaveBeenCalledWith('状态更新成功')
    })

    it('calls updateDictStatus with toggled status (0 -> 1)', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(updateDictStatus).mockResolvedValue(mockApiResponse(null) as any)

      const dict = makeDict({ id: 'd2', status: 0 })
      await vm.handleStatusToggle(dict)
      await flushAsync()

      expect(updateDictStatus).toHaveBeenCalledWith('d2', 1)
    })

    it('shows error toast when status update fails', async () => {
      vi.mocked(updateDictStatus).mockRejectedValueOnce(new Error('状态更新异常'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const dict = makeDict({ id: 'd1', status: 1 })
      await vm.handleStatusToggle(dict)
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('状态更新异常')
    })
  })

  // =========================================================================
  // 7. Dict — Delete
  // =========================================================================
  describe('Delete Dict', () => {
    it('handleDeleteConfirm sets deleteDictId and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteConfirm('dict-del')
      await nextTick()

      expect(vm.deleteDictId).toBe('dict-del')
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('calls deleteDict API and closes dialog on handleDelete', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)
      vi.mocked(deleteDict).mockResolvedValue(mockApiResponse(null) as any)

      vm.deleteDictId = 'dict-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(deleteDict).toHaveBeenCalledWith('dict-del')
      expect(vm.deleteDialogOpen).toBe(false)
      expect(toast.success).toHaveBeenCalledWith('字典删除成功')
    })

    it('closes dialog even when delete fails', async () => {
      vi.mocked(deleteDict).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.deleteDictId = 'dict-del'
      vm.deleteDialogOpen = true

      await vm.handleDelete()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
      expect(vm.deleteDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 8. Dict Item — Open dialog and fetch
  // =========================================================================
  describe('Dict Item Dialog', () => {
    it('openItemDialog sets activeDict, opens dialog, fetches items', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const dict = makeDict()
      await vm.openItemDialog(dict)
      await flushAsync()

      expect(vm.activeDict).toEqual(dict)
      expect(vm.itemDialogOpen).toBe(true)
      expect(getDictItemList).toHaveBeenCalledWith('d1')
    })

    it('populates itemTableData from getDictItemList response', async () => {
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({
          records: [makeDictItem({ label: '正常' }), makeDictItem({ id: 'di2', label: '禁用', value: '0' })],
          total: 2
        }) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.openItemDialog(makeDict())
      await flushAsync()

      expect(vm.itemTableData.records.length).toBe(2)
      expect(vm.itemTableData.records[0].label).toBe('正常')
    })

    it('shows error toast when fetchDictItems fails', async () => {
      vi.mocked(getDictItemList).mockRejectedValueOnce(new Error('获取失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      await vm.openItemDialog(makeDict())
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('获取失败')
    })
  })

  // =========================================================================
  // 9. Dict Item — Add
  // =========================================================================
  describe('Add Dict Item', () => {
    it('openAddItemDialog resets form, sets isEditItem=false, opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.itemForm.label = 'old'
      vm.itemForm.value = 'old'

      vm.openAddItemDialog()
      expect(vm.isEditItem).toBe(false)
      expect(vm.editItemId).toBe('')
      expect(vm.itemFormDialogOpen).toBe(true)
      expect(vm.itemForm.label).toBe('')
      expect(vm.itemForm.value).toBe('')
    })

    it('openAddChildItemDialog sets parentId', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      const parentItem = makeDictItem({ id: 'parent1', label: '父级' })
      vm.openAddChildItemDialog(parentItem)

      expect(vm.itemForm.parentId).toBe('parent1')
      expect(vm.isEditItem).toBe(false)
      expect(vm.itemFormDialogOpen).toBe(true)
    })
  })

  // =========================================================================
  // 10. Dict Item — Edit
  // =========================================================================
  describe('Edit Dict Item', () => {
    it('handleEditItem fetches item and populates form', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()
      vi.mocked(getDictItemById).mockResolvedValue(
        mockApiResponse(makeDictItem({ label: '活跃', value: '1', parentId: '0', sortOrder: 5, status: 1, remark: 'test' })) as any
      )

      await vm.handleEditItem('di1')
      await flushAsync()

      expect(vm.isEditItem).toBe(true)
      expect(vm.editItemId).toBe('di1')
      expect(vm.itemFormDialogOpen).toBe(true)
      expect(getDictItemById).toHaveBeenCalledWith('d1', 'di1')
      expect(vm.itemForm.label).toBe('活跃')
    })

    it('does nothing when activeDict is null', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = null
      vm.handleEditItem('di1')

      expect(getDictItemById).not.toHaveBeenCalled()
    })

    it('closes dialog on fetch error', async () => {
      vi.mocked(getDictItemById).mockRejectedValueOnce(new Error('Not found'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()

      await vm.handleEditItem('bad-id')
      await flushAsync()

      expect(vm.itemFormDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 11. Dict Item — Submit
  // =========================================================================
  describe('Submit Dict Item', () => {
    it('does nothing when activeDict is null', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = null
      vm.handleSubmitItem()

      expect(createDictItem).not.toHaveBeenCalled()
    })

    it('shows warning when label is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()
      vm.itemForm.label = ''
      vm.itemForm.value = '1'

      vm.handleSubmitItem()
      expect(toast.warning).toHaveBeenCalledWith('请输入字典项标签')
    })

    it('shows warning when value is empty', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()
      vm.itemForm.label = '正常'
      vm.itemForm.value = ''

      vm.handleSubmitItem()
      expect(toast.warning).toHaveBeenCalledWith('请输入字典项值')
    })

    it('calls createDictItem when isEditItem is false', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({ records: [], total: 0 }) as any
      )
      vi.mocked(createDictItem).mockResolvedValue(mockApiResponse(null) as any)

      vm.activeDict = makeDict()
      vm.isEditItem = false
      vm.itemForm.label = '正常'
      vm.itemForm.value = '1'
      vm.itemForm.sortOrder = 1
      vm.itemForm.status = '1'
      vm.itemForm.parentId = '0'

      await vm.handleSubmitItem()
      await flushAsync()

      expect(createDictItem).toHaveBeenCalledWith('d1',
        expect.objectContaining({
          label: '正常',
          value: '1',
          sortOrder: 1,
          status: 1
        })
      )
      expect(toast.success).toHaveBeenCalledWith('字典项创建成功')
    })

    it('calls updateDictItem when isEditItem is true', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({ records: [], total: 0 }) as any
      )
      vi.mocked(updateDictItem).mockResolvedValue(mockApiResponse(null) as any)

      vm.activeDict = makeDict()
      vm.isEditItem = true
      vm.editItemId = 'di1'
      vm.itemForm.label = '更新'
      vm.itemForm.value = '2'
      vm.itemForm.sortOrder = 2
      vm.itemForm.status = '1'
      vm.itemForm.parentId = '0'

      await vm.handleSubmitItem()
      await flushAsync()

      expect(updateDictItem).toHaveBeenCalledWith('d1', 'di1',
        expect.objectContaining({
          label: '更新',
          value: '2'
        })
      )
      expect(toast.success).toHaveBeenCalledWith('字典项更新成功')
    })

    it('closes dialog and refreshes items on success', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({ records: [], total: 0 }) as any
      )
      vi.mocked(createDictItem).mockResolvedValue(mockApiResponse(null) as any)

      vm.activeDict = makeDict()
      vm.isEditItem = false
      vm.itemForm.label = '正常'
      vm.itemForm.value = '1'
      vm.itemFormDialogOpen = true

      await vm.handleSubmitItem()
      await flushAsync()

      expect(vm.itemFormDialogOpen).toBe(false)
      expect(getDictItemList).toHaveBeenCalledWith('d1')
    })

    it('shows error toast on create failure', async () => {
      vi.mocked(createDictItem).mockRejectedValueOnce(new Error('创建失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()
      vm.isEditItem = false
      vm.itemForm.label = '正常'
      vm.itemForm.value = '1'

      await vm.handleSubmitItem()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('创建失败')
    })
  })

  // =========================================================================
  // 12. Dict Item — Delete
  // =========================================================================
  describe('Delete Dict Item', () => {
    it('handleDeleteItemConfirm sets deleteItemId and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleDeleteItemConfirm('item-del')
      expect(vm.deleteItemId).toBe('item-del')
      expect(vm.deleteItemDialogOpen).toBe(true)
    })

    it('calls deleteDictItem and closes dialog on handleDeleteItem', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({ records: [], total: 0 }) as any
      )
      vi.mocked(deleteDictItem).mockResolvedValue(mockApiResponse(null) as any)

      vm.activeDict = makeDict()
      vm.deleteItemId = 'item-del'
      vm.deleteItemDialogOpen = true

      await vm.handleDeleteItem()
      await flushAsync()

      expect(deleteDictItem).toHaveBeenCalledWith('d1', 'item-del')
      expect(vm.deleteItemDialogOpen).toBe(false)
      expect(toast.success).toHaveBeenCalledWith('字典项删除成功')
    })

    it('does nothing when activeDict is null', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = null
      vm.deleteItemId = 'item-del'
      vm.deleteItemDialogOpen = true

      await vm.handleDeleteItem()

      expect(deleteDictItem).not.toHaveBeenCalled()
    })

    it('closes dialog even on delete error', async () => {
      vi.mocked(deleteDictItem).mockRejectedValueOnce(new Error('删除失败'))
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = makeDict()
      vm.deleteItemId = 'item-del'
      vm.deleteItemDialogOpen = true

      await vm.handleDeleteItem()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
      expect(vm.deleteItemDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 13. Dict Item — Status toggle
  // =========================================================================
  describe('Dict Item Status Toggle', () => {
    it('calls updateDictItemStatus with toggled status (1 -> 0)', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vi.clearAllMocks()
      vi.mocked(getDictItemList).mockResolvedValue(
        mockApiResponse({ records: [], total: 0 }) as any
      )
      vi.mocked(updateDictItemStatus).mockResolvedValue(mockApiResponse(null) as any)

      vm.activeDict = makeDict()
      const item = makeDictItem({ id: 'di1', status: 1 })

      await vm.handleToggleItemStatus(item)
      await flushAsync()

      expect(updateDictItemStatus).toHaveBeenCalledWith('d1', 'di1', 0)
      expect(toast.success).toHaveBeenCalledWith('字典项状态更新成功')
    })

    it('does nothing when activeDict is null', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.activeDict = null
      const item = makeDictItem({ id: 'di1', status: 1 })

      await vm.handleToggleItemStatus(item)

      expect(updateDictItemStatus).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 14. Permission-based UI
  // =========================================================================
  describe('Permission-Based UI', () => {
    it('computes canAddDict from userStore.hasPermission', async () => {
      userStore.hasPermission = vi.fn((perm: string) => perm === 'dict:add')
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddDict).toBe(true)
      expect(vm.canDeleteDict).toBe(false)
    })

    it('computes all permission flags correctly', async () => {
      userStore.hasPermission = vi.fn(() => true)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddDict).toBe(true)
      expect(vm.canEditDict).toBe(true)
      expect(vm.canDeleteDict).toBe(true)
      expect(vm.canListDictItems).toBe(true)
      expect(vm.canAddDictItem).toBe(true)
      expect(vm.canEditDictItem).toBe(true)
      expect(vm.canDeleteDictItem).toBe(true)
    })

    it('all permissions false when user has none', async () => {
      userStore.hasPermission = vi.fn(() => false)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      expect(vm.canAddDict).toBe(false)
      expect(vm.canEditDict).toBe(false)
      expect(vm.canDeleteDict).toBe(false)
      expect(vm.canListDictItems).toBe(false)
      expect(vm.canAddDictItem).toBe(false)
      expect(vm.canEditDictItem).toBe(false)
      expect(vm.canDeleteDictItem).toBe(false)
    })
  })

  // =========================================================================
  // 15. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading is true', async () => {
      vi.mocked(getDictList).mockReturnValue(new Promise(() => {}))

      wrapper = mount(Dict, {
        global: {
          plugins: [pinia],
          stubs: {
            ConfirmDialog: true,
            ListSearchBar: true,
            Pagination: true,
            StatusBadge: true
          }
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when no records', async () => {
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([], 0) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('暂无数据')
    })

    it('sets loading to false after data fetch completes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 16. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getDictList error gracefully', async () => {
      vi.mocked(getDictList).mockRejectedValue(new Error('获取字典列表失败'))
      wrapper = await mountAndFlush()

      expect(toast.error).toHaveBeenCalledWith('获取字典列表失败')
    })
  })

  // =========================================================================
  // 17. Table content rendering
  // =========================================================================
  describe('Table Content', () => {
    it('renders dict data in table cells', async () => {
      const dict = makeDict({
        id: 'd1',
        dictName: '用户状态',
        dictType: 'user_status',
        description: '用户状态枚举'
      })
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([dict]) as any)
      wrapper = await mountAndFlush()

      expect(wrapper.text()).toContain('用户状态')
      expect(wrapper.text()).toContain('user_status')
      expect(wrapper.text()).toContain('用户状态枚举')
    })

    it('shows "-" for empty description', async () => {
      const dict = makeDict({ description: '' })
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([dict]) as any)
      wrapper = await mountAndFlush()

      const cells = wrapper.findAll('tbody td')
      const remarkCell = cells.find(c => c.text().trim() === '-')
      expect(remarkCell).toBeDefined()
    })

    it('renders multiple dict rows', async () => {
      const dicts = [
        makeDict({ id: 'd1', dictName: '字典1' }),
        makeDict({ id: 'd2', dictName: '字典2' })
      ]
      vi.mocked(getDictList).mockResolvedValue(mockPageResult(dicts, 2) as any)
      wrapper = await mountAndFlush()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(2)
      expect(wrapper.text()).toContain('字典1')
      expect(wrapper.text()).toContain('字典2')
    })
  })

  // =========================================================================
  // 18. itemParentOptions computed
  // =========================================================================
  describe('Item Parent Options', () => {
    it('excludes current item from options when editing', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      // Set up item table data with 2 items
      vm.itemTableData = {
        records: [
          makeDictItem({ id: 'di1', label: '正常' }),
          makeDictItem({ id: 'di2', label: '禁用' })
        ],
        total: 2
      }
      vm.isEditItem = true
      vm.editItemId = 'di1'
      await nextTick()

      // Should exclude di1 from options
      expect(vm.itemParentOptions.length).toBe(1)
      expect(vm.itemParentOptions[0].id).toBe('di2')
    })

    it('includes all items when not editing', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.itemTableData = {
        records: [
          makeDictItem({ id: 'di1', label: '正常' }),
          makeDictItem({ id: 'di2', label: '禁用' })
        ],
        total: 2
      }
      vm.isEditItem = false
      await nextTick()

      expect(vm.itemParentOptions.length).toBe(2)
    })
  })

  // =========================================================================
  // 19. itemLabelMap computed
  // =========================================================================
  describe('Item Label Map', () => {
    it('builds id->label map from itemTableData', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.itemTableData = {
        records: [
          makeDictItem({ id: 'di1', label: '正常' }),
          makeDictItem({ id: 'di2', label: '禁用' })
        ],
        total: 2
      }
      await nextTick()

      expect(vm.itemLabelMap['di1']).toBe('正常')
      expect(vm.itemLabelMap['di2']).toBe('禁用')
    })
  })

  // =========================================================================
  // 20. Reset helpers
  // =========================================================================
  describe('Reset Helpers', () => {
    it('resetForm resets all form fields', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.form.dictType = 'old'
      vm.form.dictName = 'old'
      vm.form.remark = 'old'
      vm.form.status = '0'

      vm.resetForm()

      expect(vm.form.dictType).toBe('')
      expect(vm.form.dictName).toBe('')
      expect(vm.form.remark).toBe('')
      expect(vm.form.status).toBe('1')
    })

    it('resetItemForm resets all itemForm fields', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.itemForm.parentId = 'p1'
      vm.itemForm.label = 'old'
      vm.itemForm.value = 'old'
      vm.itemForm.sortOrder = 99
      vm.itemForm.status = '0'
      vm.itemForm.remark = 'old'

      vm.resetItemForm()

      expect(vm.itemForm.parentId).toBe('0')
      expect(vm.itemForm.label).toBe('')
      expect(vm.itemForm.value).toBe('')
      expect(vm.itemForm.sortOrder).toBe(0)
      expect(vm.itemForm.status).toBe('1')
      expect(vm.itemForm.remark).toBe('')
    })
  })

  // =========================================================================
  // 21. Pagination integration
  // =========================================================================
  describe('Pagination', () => {
    it('passes correct props to Pagination', async () => {
      vi.mocked(getDictList).mockResolvedValue(
        mockPageResult([makeDict()], 42) as any
      )
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      expect(pagination.props('current')).toBe(1)
      expect(pagination.props('total')).toBe(42)
      expect(pagination.props('pageSize')).toBe(10)
    })

    it('calls goToPage when Pagination emits change', async () => {
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([makeDict()], 25) as any)
      wrapper = await mountAndFlush()
      const pagination = wrapper.findComponent({ name: 'Pagination' })

      vi.clearAllMocks()
      vi.mocked(getDictList).mockResolvedValue(mockPageResult([]) as any)

      pagination.vm.$emit('change', 2)
      await nextTick()
      await nextTick()
      await nextTick()

      expect(getDictList).toHaveBeenCalledWith(expect.objectContaining({ page: 2 }))
    })
  })
})
