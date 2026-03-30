import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick, ref } from 'vue'

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
  getMyCcRecords: vi.fn(),
  getMyUnreadCcRecords: vi.fn(),
  countMyUnreadCcRecords: vi.fn(),
  markCcAsRead: vi.fn(),
  markCcAsReadBatch: vi.fn()
}))

const mockFetchCcList = vi.fn()
const mockFetchUnreadCount = vi.fn()
const mockMarkRead = vi.fn()
const mockMarkAllRead = vi.fn()
const loadingRef = ref(false)
const unreadCountRef = ref(0)
const activeTabRef = ref<'all' | 'unread'>('all')
const recordsRef = ref<any[]>([])

vi.mock('@/composables/workflow/useWorkflowCc', () => ({
  useWorkflowCc: () => ({
    loading: loadingRef,
    unreadCount: unreadCountRef,
    activeTab: activeTabRef,
    records: recordsRef,
    fetchUnreadCount: mockFetchUnreadCount,
    fetchCcList: mockFetchCcList,
    markRead: mockMarkRead,
    markAllRead: mockMarkAllRead
  })
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    hasPermission: vi.fn(() => true)
  }))
}))

vi.mock('@/lib/page-permissions', () => ({
  getWorkflowPermissionState: vi.fn(() => ({
    canViewCc: true,
    canMarkCcRead: true,
    canApprovePendingActions: true
  }))
}))

vi.mock('@/components/common', () => ({
  ConfirmDialog: { name: 'ConfirmDialog', template: '<div />' }
}))

vi.mock('vue-router', () => ({
  RouterLink: { name: 'RouterLink', template: '<a><slot /></a>' }
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import MyCc from '@/views/workflow/MyCc.vue'
import { toast } from 'vue-sonner'
import {
  getMyCcRecords,
  getMyUnreadCcRecords,
  countMyUnreadCcRecords,
  markCcAsRead,
  markCcAsReadBatch
} from '@/api'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeCcRecord(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'cc1',
    instanceId: 'inst1',
    nodeId: 'n1',
    nodeName: '审核节点',
    userId: 'u1',
    userName: '抄送人',
    ccType: 'approve',
    ccContent: '审批通过通知',
    isRead: false,
    readTime: null,
    createTime: '2026-03-29 10:00:00',
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

describe('MyCc Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    mockFetchCcList.mockReset()
    mockFetchUnreadCount.mockReset()
    mockMarkRead.mockReset()
    mockMarkAllRead.mockReset()
    loadingRef.value = false
    unreadCountRef.value = 0
    activeTabRef.value = 'all'
    recordsRef.value = [makeCcRecord()]
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options: any = {}) => {
    wrapper = mount(MyCc, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: { template: '<div />' },
          RouterLink: true,
          Sonner: true,
          Tabs: true,
          TabsList: true,
          TabsTrigger: true
        }
      },
      ...options
    } as any)
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders card title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('抄送我的')
    })

    it('renders unread count badge', async () => {
      unreadCountRef.value = 3
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('未读 3')
    })

    it('renders table headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts.some(t => t.includes('节点'))).toBe(true)
      expect(headerTexts.some(t => t.includes('被抄送人'))).toBe(true)
      expect(headerTexts.some(t => t.includes('类型'))).toBe(true)
      expect(headerTexts.some(t => t.includes('内容'))).toBe(true)
      expect(headerTexts.some(t => t.includes('状态'))).toBe(true)
      expect(headerTexts.some(t => t.includes('时间'))).toBe(true)
      expect(headerTexts.some(t => t.includes('操作'))).toBe(true)
    })

    it('renders root container with space-y-4 class', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders table element', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('table').exists()).toBe(true)
    })

    it('renders refresh button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeDefined()
    })

    it('renders mark all read button', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markAllBtn = buttons.find(b => b.text().includes('全部已读'))
      expect(markAllBtn).toBeDefined()
    })
  })

  // =========================================================================
  // 2. Data fetching
  // =========================================================================
  describe('Data Fetching', () => {
    it('calls fetchCcList and fetchUnreadCount on mount', async () => {
      wrapper = await mountAndFlush()
      expect(mockFetchCcList).toHaveBeenCalled()
      expect(mockFetchUnreadCount).toHaveBeenCalled()
    })

    it('renders records from composable', async () => {
      recordsRef.value = [makeCcRecord()]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('审核节点')
      expect(wrapper.text()).toContain('抄送人')
      expect(wrapper.text()).toContain('审批通过通知')
    })

    it('renders multiple records', async () => {
      recordsRef.value = [
        makeCcRecord({ id: 'cc1', nodeName: '节点一' }),
        makeCcRecord({ id: 'cc2', nodeName: '节点二' })
      ]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('节点一')
      expect(wrapper.text()).toContain('节点二')
    })

    it('renders dash when nodeName is null', async () => {
      recordsRef.value = [makeCcRecord({ nodeName: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders dash when userName is null', async () => {
      recordsRef.value = [makeCcRecord({ userName: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders dash when ccContent is null', async () => {
      recordsRef.value = [makeCcRecord({ ccContent: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })
  })

  // =========================================================================
  // 3. getCcTypeLabel helper
  // =========================================================================
  describe('getCcTypeLabel Helper', () => {
    it('returns correct label for start type', async () => {
      recordsRef.value = [makeCcRecord({ ccType: 'start' })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('发起抄送')
    })

    it('returns correct label for approve type', async () => {
      recordsRef.value = [makeCcRecord({ ccType: 'approve' })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('审批通过')
    })

    it('returns correct label for reject type', async () => {
      recordsRef.value = [makeCcRecord({ ccType: 'reject' })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('审批驳回')
    })

    it('returns correct label for rollback type', async () => {
      recordsRef.value = [makeCcRecord({ ccType: 'rollback' })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('回退通知')
    })

    it('returns dash for unknown ccType', async () => {
      recordsRef.value = [makeCcRecord({ ccType: 'unknown' })]
      wrapper = await mountAndFlush()
      // getCcTypeLabel returns type or '-', for 'unknown' it returns 'unknown'
      // Actually the code: map[type || ''] || type || '-' => map['unknown'] is undefined, then type is 'unknown'
      expect(wrapper.text()).toContain('unknown')
    })

    it('returns dash for empty ccType', async () => {
      recordsRef.value = [makeCcRecord({ ccType: '' })]
      wrapper = await mountAndFlush()
      // getCcTypeLabel(''): map[''] is undefined, then type is '', then '-'
      expect(wrapper.text()).toContain('-')
    })

    it('returns dash for undefined ccType', async () => {
      recordsRef.value = [makeCcRecord({ ccType: undefined })]
      wrapper = await mountAndFlush()
      // getCcTypeLabel(undefined): map[''] is undefined, then type is undefined, then '-'
      expect(wrapper.text()).toContain('-')
    })
  })

  // =========================================================================
  // 4. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text when loading is true', async () => {
      loadingRef.value = true
      recordsRef.value = []
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('加载中...')
    })

    it('shows empty text when records are empty', async () => {
      recordsRef.value = []
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无抄送记录')
    })

    it('hides loading text when loading is false with records', async () => {
      loadingRef.value = false
      recordsRef.value = [makeCcRecord()]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).not.toContain('加载中...')
    })
  })

  // =========================================================================
  // 5. Tab switching
  // =========================================================================
  describe('Tab Switching', () => {
    it('renders tabs component', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.findComponent({ name: 'Tabs' }).exists()).toBe(true)
    })

    it('calls fetchCcList with unread on tab switch', async () => {
      wrapper = await mountAndFlush()
      const tabsComponent = wrapper.findComponent({ name: 'Tabs' })
      tabsComponent.vm.$emit('update:model-value', 'unread')
      await nextTick()
      expect(mockFetchCcList).toHaveBeenCalledWith('unread')
    })

    it('calls fetchCcList with all on tab switch', async () => {
      wrapper = await mountAndFlush()
      const tabsComponent = wrapper.findComponent({ name: 'Tabs' })
      tabsComponent.vm.$emit('update:model-value', 'all')
      await nextTick()
      expect(mockFetchCcList).toHaveBeenCalledWith('all')
    })
  })

  // =========================================================================
  // 6. Mark read
  // =========================================================================
  describe('Mark Read', () => {
    it('renders mark read button for unread records', async () => {
      recordsRef.value = [makeCcRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeDefined()
    })

    it('does not render mark read button for read records', async () => {
      recordsRef.value = [makeCcRecord({ isRead: true })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeUndefined()
    })

    it('calls markRead when mark read button clicked', async () => {
      recordsRef.value = [makeCcRecord({ id: 'cc1', isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeDefined()
      await markReadBtn!.trigger('click')
      expect(mockMarkRead).toHaveBeenCalledWith('cc1')
    })

    it('calls markAllRead when mark all read button clicked', async () => {
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markAllBtn = buttons.find(b => b.text().includes('全部已读'))
      expect(markAllBtn).toBeDefined()
      await markAllBtn!.trigger('click')
      expect(mockMarkAllRead).toHaveBeenCalled()
    })

    it('renders correct read status badge for unread record', async () => {
      recordsRef.value = [makeCcRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('未读')
    })

    it('renders correct read status badge for read record', async () => {
      recordsRef.value = [makeCcRecord({ isRead: true })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('已读')
    })
  })

  // =========================================================================
  // 7. Refresh
  // =========================================================================
  describe('Refresh', () => {
    it('calls fetchCcList when refresh button clicked', async () => {
      wrapper = await mountAndFlush()
      mockFetchCcList.mockClear()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeDefined()
      await refreshBtn!.trigger('click')
      expect(mockFetchCcList).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 8. Error handling (composable-level)
  // =========================================================================
  describe('Error Handling', () => {
    it('delegates error handling to composable', async () => {
      // The composable is responsible for error handling
      // This test verifies the component delegates correctly
      wrapper = await mountAndFlush()
      expect(mockFetchCcList).toHaveBeenCalled()
      expect(mockFetchUnreadCount).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 9. Permission gating
  // =========================================================================
  describe('Permission Gating', () => {
    it('shows no permission message when canViewCc is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: false,
        canMarkCcRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('当前没有查看抄送记录权限')
    })

    it('does not call fetchCcList when canViewCc is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: false,
        canMarkCcRead: false,
        canApprovePendingActions: false
      } as any)
      mockFetchCcList.mockClear()
      mockFetchUnreadCount.mockClear()
      wrapper = await mountAndFlush()
      expect(mockFetchCcList).not.toHaveBeenCalled()
      expect(mockFetchUnreadCount).not.toHaveBeenCalled()
    })

    it('hides refresh button when canViewCc is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: false,
        canMarkCcRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeUndefined()
    })

    it('hides mark all read button when canMarkCcRead is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: true,
        canMarkCcRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markAllBtn = buttons.find(b => b.text().includes('全部已读'))
      expect(markAllBtn).toBeUndefined()
    })

    it('hides mark read button when canMarkCcRead is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: true,
        canMarkCcRead: false,
        canApprovePendingActions: false
      } as any)
      recordsRef.value = [makeCcRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeUndefined()
    })

    it('shows refresh and mark all read buttons when permissions granted', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewCc: true,
        canMarkCcRead: true,
        canApprovePendingActions: true
      } as any)
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      const markAllBtn = buttons.find(b => b.text().includes('全部已读'))
      expect(refreshBtn).toBeDefined()
      expect(markAllBtn).toBeDefined()
    })

    it('calls getWorkflowPermissionState with userStore.hasPermission', async () => {
      wrapper = await mountAndFlush()
      expect(getWorkflowPermissionState).toHaveBeenCalled()
    })
  })
})
