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

vi.mock('@/api', () => ({
  getAllConfigGroups: vi.fn(),
  getConfigsByGroupCode: vi.fn(),
  deleteConfigGroup: vi.fn(),
  deleteConfig: vi.fn(),
  refreshConfigCache: vi.fn(),
  updateConfigStatus: vi.fn()
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    hasPermission: vi.fn(() => true)
  }))
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Config from '@/views/system/Config.vue'
import { useUserStore } from '@/stores/user'
import {
  getAllConfigGroups,
  getConfigsByGroupCode,
  deleteConfigGroup,
  deleteConfig,
  refreshConfigCache,
  updateConfigStatus
} from '@/api'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeConfigGroup(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'group-1',
    code: 'SYSTEM',
    name: '系统配置',
    description: '系统基础配置',
    sortOrder: 1,
    status: 1,
    createTime: '2026-01-01 00:00:00',
    ...overrides
  }
}

function makeConfig(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'config-1',
    groupId: 'group-1',
    groupCode: 'SYSTEM',
    name: '站点名称',
    key: 'SITE_NAME',
    value: 'AdminPlus',
    valueType: 'string',
    description: '网站显示名称',
    sortOrder: 1,
    status: 1,
    createTime: '2026-01-01 00:00:00',
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

describe('Config Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)

    vi.clearAllMocks()

    // Reset the mock implementation for each test
    vi.mocked(useUserStore).mockReturnValue({
      hasPermission: vi.fn(() => true)
    } as any)

    vi.mocked(getAllConfigGroups).mockResolvedValue(
      mockApiResponse([makeConfigGroup(), makeConfigGroup({ id: 'group-2', code: 'EMAIL', name: '邮件配置' })]) as any
    )
    vi.mocked(getConfigsByGroupCode).mockResolvedValue(
      mockApiResponse([makeConfig(), makeConfig({ id: 'config-2', key: 'SITE_URL', name: '站点URL' })]) as any
    )
    vi.mocked(deleteConfigGroup).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(deleteConfig).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(refreshConfigCache).mockResolvedValue(mockApiResponse(null) as any)
    vi.mocked(updateConfigStatus).mockResolvedValue(mockApiResponse(null) as any)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options = {}) => {
    wrapper = mount(Config, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfigGroupTabs: true,
          ConfigItemTable: true,
          ConfigGroupFormDialog: true,
          ConfigItemFormDialog: true,
          ConfigHistoryDialog: true,
          ConfigImportExportDialog: true,
          ConfigBatchEditDialog: true,
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

    it('renders page title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('参数配置')
    })

    it('renders page description', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('管理系统配置参数和分组')
    })

    it('renders refresh cache button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新缓存'))
      expect(refreshBtn).toBeDefined()
    })

    it('renders upload/download button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const importExportBtn = buttons.find(b => b.text().includes('上传 / 下载'))
      expect(importExportBtn).toBeDefined()
    })

    it('renders search input', async () => {
      wrapper = await mountAndFlush()
      const input = wrapper.find('input[type="text"]')
      expect(input.exists()).toBe(true)
      expect(input.attributes('placeholder')).toContain('搜索配置名称或配置键')
    })

    it('renders batch edit button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const batchBtn = buttons.find(b => b.text().includes('批量编辑'))
      expect(batchBtn).toBeDefined()
    })

    it('renders add config button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const addBtn = buttons.find(b => b.text().includes('新增配置'))
      expect(addBtn).toBeDefined()
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getAllConfigGroups on mount', async () => {
      wrapper = await mountAndFlush()
      expect(getAllConfigGroups).toHaveBeenCalled()
    })

    it('calls getConfigsByGroupCode with first group code', async () => {
      wrapper = await mountAndFlush()
      expect(getConfigsByGroupCode).toHaveBeenCalledWith('SYSTEM')
    })

    it('populates groups from API response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.groups.length).toBe(2)
      expect(vm.groups[0].code).toBe('SYSTEM')
    })

    it('populates configs from API response', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.configs.length).toBe(2)
      expect(vm.configs[0].key).toBe('SITE_NAME')
    })

    it('sets activeCode to first group code', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.activeCode).toBe('SYSTEM')
    })

    it('does not set activeCode when no groups', async () => {
      vi.mocked(getAllConfigGroups).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.activeCode).toBe('')
    })
  })

  // =========================================================================
  // 3. Search/filter functionality
  // =========================================================================
  describe('Search and Filter', () => {
    it('filters configs by name', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchKeyword = '站点名称'
      await nextTick()

      expect(vm.filteredConfigs.length).toBe(1)
      expect(vm.filteredConfigs[0].name).toBe('站点名称')
    })

    it('filters configs by key', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchKeyword = 'SITE_URL'
      await nextTick()

      expect(vm.filteredConfigs.length).toBe(1)
      expect(vm.filteredConfigs[0].key).toBe('SITE_URL')
    })

    it('shows all configs when search is cleared', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchKeyword = '站点名称'
      await nextTick()
      expect(vm.filteredConfigs.length).toBe(1)

      vm.searchKeyword = ''
      await nextTick()
      expect(vm.filteredConfigs.length).toBe(2)
    })

    it('shows no configs when search matches nothing', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.searchKeyword = 'nonexistent'
      await nextTick()

      expect(vm.filteredConfigs.length).toBe(0)
    })
  })

  // =========================================================================
  // 4. Permission checks
  // =========================================================================
  describe('Permission Checks', () => {
    it('shows management buttons when user has permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn(() => true)
      } as any)
      wrapper = await mountAndFlush()

      const buttons = wrapper.findAll('button')
      const addBtn = buttons.find(b => b.text().includes('新增配置'))
      expect(addBtn).toBeDefined()
    })

    it('hides management buttons when user lacks permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn(() => false)
      } as any)
      wrapper = await mountAndFlush()

      const buttons = wrapper.findAll('button')
      const addBtn = buttons.find(b => b.text().includes('新增配置'))
      expect(addBtn).toBeUndefined()
    })

    it('computes canManageConfig correctly when has permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn(() => true)
      } as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.canManageConfig).toBe(true)
    })

    it('computes canManageConfig correctly when no permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn(() => false)
      } as any)
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.canManageConfig).toBe(false)
    })
  })

  // =========================================================================
  // 5. Handler methods
  // =========================================================================
  describe('Handler Methods', () => {
    it('handleAddGroup sets editGroup and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleAddGroup()
      expect(vm.editGroup).toBeUndefined()
      expect(vm.groupDialogOpen).toBe(true)
    })

    it('handleAddConfig sets editConfig and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any

      vm.handleAddConfig()
      expect(vm.editConfig).toBeUndefined()
      expect(vm.configDialogOpen).toBe(true)
    })

    it('handleEditConfig sets editConfig and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const config = makeConfig({ id: 'test-id', name: 'Test Config' })

      vm.handleEditConfig(config)
      expect(vm.editConfig).toEqual(config)
      expect(vm.configDialogOpen).toBe(true)
    })

    it('handleDeleteConfig sets deleteTarget and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const config = makeConfig({ id: 'test-id', name: 'Test Config' })

      vm.handleDeleteConfig(config)
      expect(vm.deleteTarget).toEqual({ type: 'config', id: 'test-id', name: 'Test Config' })
      expect(vm.deleteDialogOpen).toBe(true)
    })

    it('handleHistory sets historyConfig and opens dialog', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      const config = makeConfig({ id: 'test-id', name: 'Test Config' })

      vm.handleHistory(config)
      expect(vm.historyConfig).toEqual(config)
      expect(vm.historyDialogOpen).toBe(true)
    })
  })

  // =========================================================================
  // 6. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('renders page after loading completes', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })
  })

  // =========================================================================
  // 7. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles getAllConfigGroups error gracefully', async () => {
      vi.mocked(getAllConfigGroups).mockRejectedValue(new Error('获取配置分组失败'))
      wrapper = await mountAndFlush()

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalled()
    })

    it('handles getConfigsByGroupCode error gracefully', async () => {
      vi.mocked(getConfigsByGroupCode).mockRejectedValue(new Error('获取配置项失败'))
      wrapper = await mountAndFlush()

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalled()
    })
  })
})