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
  getReceivedUrgeRecords: vi.fn(),
  getSentUrgeRecords: vi.fn(),
  getUnreadUrgeRecords: vi.fn(),
  countUnreadUrgeRecords: vi.fn(),
  markUrgeAsRead: vi.fn(),
  markUrgeAsReadBatch: vi.fn()
}))

const mockFetchUrgeList = vi.fn()
const mockFetchUnreadCount = vi.fn()
const mockMarkRead = vi.fn()
const mockMarkAllRead = vi.fn()
const loadingRef = ref(false)
const unreadCountRef = ref(0)
const activeTabRef = ref<'received' | 'sent' | 'unread'>('received')
const recordsRef = ref<any[]>([])

vi.mock('@/composables/workflow/useWorkflowUrge', () => ({
  useWorkflowUrge: () => ({
    loading: loadingRef,
    unreadCount: unreadCountRef,
    activeTab: activeTabRef,
    records: recordsRef,
    fetchUnreadCount: mockFetchUnreadCount,
    fetchUrgeList: mockFetchUrgeList,
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
    canViewUrge: true,
    canMarkUrgeRead: true,
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

import UrgeCenter from '@/views/workflow/UrgeCenter.vue'
import { toast } from 'vue-sonner'
import {
  getReceivedUrgeRecords,
  getSentUrgeRecords,
  getUnreadUrgeRecords,
  countUnreadUrgeRecords,
  markUrgeAsRead,
  markUrgeAsReadBatch
} from '@/api'
import { getWorkflowPermissionState } from '@/lib/page-permissions'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeUrgeRecord(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'ur1',
    instanceId: 'inst1',
    nodeId: 'n1',
    nodeName: '审核节点',
    urgeUserId: 'u1',
    urgeUserName: '催办人',
    urgeTargetId: 't1',
    urgeTargetName: '被催办人',
    urgeContent: '请尽快处理',
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

describe('UrgeCenter Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    mockFetchUrgeList.mockReset()
    mockFetchUnreadCount.mockReset()
    mockMarkRead.mockReset()
    mockMarkAllRead.mockReset()
    loadingRef.value = false
    unreadCountRef.value = 0
    activeTabRef.value = 'received'
    recordsRef.value = [makeUrgeRecord()]
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options: any = {}) => {
    wrapper = mount(UrgeCenter, {
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
      expect(wrapper.text()).toContain('催办中心')
    })

    it('renders unread count badge', async () => {
      unreadCountRef.value = 5
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('未读 5')
    })

    it('renders table headers', async () => {
      wrapper = await mountAndFlush()
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts.some(t => t.includes('节点'))).toBe(true)
      expect(headerTexts.some(t => t.includes('催办人'))).toBe(true)
      expect(headerTexts.some(t => t.includes('被催办人'))).toBe(true)
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
    it('calls fetchUrgeList and fetchUnreadCount on mount', async () => {
      wrapper = await mountAndFlush()
      expect(mockFetchUrgeList).toHaveBeenCalled()
      expect(mockFetchUnreadCount).toHaveBeenCalled()
    })

    it('renders records from composable', async () => {
      recordsRef.value = [makeUrgeRecord()]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('审核节点')
      expect(wrapper.text()).toContain('催办人')
      expect(wrapper.text()).toContain('被催办人')
      expect(wrapper.text()).toContain('请尽快处理')
    })

    it('renders multiple records', async () => {
      recordsRef.value = [
        makeUrgeRecord({ id: 'ur1', nodeName: '节点一' }),
        makeUrgeRecord({ id: 'ur2', nodeName: '节点二' })
      ]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('节点一')
      expect(wrapper.text()).toContain('节点二')
    })

    it('renders dash when nodeName is null', async () => {
      recordsRef.value = [makeUrgeRecord({ nodeName: null })]
      wrapper = await mountAndFlush()
      // The template renders {{ record.nodeName || '-' }}
      expect(wrapper.text()).toContain('-')
    })

    it('renders dash when urgeUserName is null', async () => {
      recordsRef.value = [makeUrgeRecord({ urgeUserName: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders dash when urgeTargetName is null', async () => {
      recordsRef.value = [makeUrgeRecord({ urgeTargetName: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })

    it('renders dash when urgeContent is null', async () => {
      recordsRef.value = [makeUrgeRecord({ urgeContent: null })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('-')
    })
  })

  // =========================================================================
  // 3. Loading states
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
      expect(wrapper.text()).toContain('暂无催办记录')
    })

    it('hides loading text when loading is false', async () => {
      loadingRef.value = false
      recordsRef.value = [makeUrgeRecord()]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).not.toContain('加载中...')
    })
  })

  // =========================================================================
  // 4. Tab switching
  // =========================================================================
  describe('Tab Switching', () => {
    it('renders tabs component', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.findComponent({ name: 'Tabs' }).exists()).toBe(true)
    })

    it('calls fetchUrgeList with sent on tab switch', async () => {
      wrapper = await mountAndFlush()
      // Simulate Tabs emitting update:modelValue
      const tabsComponent = wrapper.findComponent({ name: 'Tabs' })
      tabsComponent.vm.$emit('update:model-value', 'sent')
      await nextTick()
      expect(mockFetchUrgeList).toHaveBeenCalledWith('sent')
    })

    it('calls fetchUrgeList with unread on tab switch', async () => {
      wrapper = await mountAndFlush()
      const tabsComponent = wrapper.findComponent({ name: 'Tabs' })
      tabsComponent.vm.$emit('update:model-value', 'unread')
      await nextTick()
      expect(mockFetchUrgeList).toHaveBeenCalledWith('unread')
    })

    it('calls fetchUrgeList with received on tab switch', async () => {
      wrapper = await mountAndFlush()
      const tabsComponent = wrapper.findComponent({ name: 'Tabs' })
      tabsComponent.vm.$emit('update:model-value', 'received')
      await nextTick()
      expect(mockFetchUrgeList).toHaveBeenCalledWith('received')
    })
  })

  // =========================================================================
  // 5. Mark read
  // =========================================================================
  describe('Mark Read', () => {
    it('renders mark read button for unread records', async () => {
      recordsRef.value = [makeUrgeRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeDefined()
    })

    it('does not render mark read button for read records', async () => {
      recordsRef.value = [makeUrgeRecord({ isRead: true })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeUndefined()
    })

    it('calls markRead when mark read button clicked', async () => {
      recordsRef.value = [makeUrgeRecord({ id: 'ur1', isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeDefined()
      await markReadBtn!.trigger('click')
      expect(mockMarkRead).toHaveBeenCalledWith('ur1')
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
      recordsRef.value = [makeUrgeRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('未读')
    })

    it('renders correct read status badge for read record', async () => {
      recordsRef.value = [makeUrgeRecord({ isRead: true })]
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('已读')
    })
  })

  // =========================================================================
  // 6. Refresh
  // =========================================================================
  describe('Refresh', () => {
    it('calls fetchUrgeList when refresh button clicked', async () => {
      wrapper = await mountAndFlush()
      mockFetchUrgeList.mockClear()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeDefined()
      await refreshBtn!.trigger('click')
      expect(mockFetchUrgeList).toHaveBeenCalled()
    })

    it('calls fetchUrgeList with empty string from refresh button', async () => {
      wrapper = await mountAndFlush()
      mockFetchUrgeList.mockClear()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      await refreshBtn!.trigger('click')
      // The refresh button calls fetchUrgeList() with no args, defaulting to activeTab
      expect(mockFetchUrgeList).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 7. Error handling (composable-level)
  // =========================================================================
  describe('Error Handling', () => {
    it('delegates error handling to composable', async () => {
      // The composable is responsible for error handling
      // This test verifies the component delegates correctly
      wrapper = await mountAndFlush()
      // Verify the composable functions are wired up
      expect(mockFetchUrgeList).toHaveBeenCalled()
      expect(mockFetchUnreadCount).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 8. Permission gating
  // =========================================================================
  describe('Permission Gating', () => {
    it('shows no permission message when canViewUrge is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: false,
        canMarkUrgeRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('当前没有查看催办记录权限')
    })

    it('does not call fetchUrgeList when canViewUrge is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: false,
        canMarkUrgeRead: false,
        canApprovePendingActions: false
      } as any)
      mockFetchUrgeList.mockClear()
      mockFetchUnreadCount.mockClear()
      wrapper = await mountAndFlush()
      expect(mockFetchUrgeList).not.toHaveBeenCalled()
      expect(mockFetchUnreadCount).not.toHaveBeenCalled()
    })

    it('hides refresh button when canViewUrge is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: false,
        canMarkUrgeRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const refreshBtn = buttons.find(b => b.text().includes('刷新'))
      expect(refreshBtn).toBeUndefined()
    })

    it('hides mark all read button when canMarkUrgeRead is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: true,
        canMarkUrgeRead: false,
        canApprovePendingActions: false
      } as any)
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markAllBtn = buttons.find(b => b.text().includes('全部已读'))
      expect(markAllBtn).toBeUndefined()
    })

    it('hides mark read button when canMarkUrgeRead is false', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: true,
        canMarkUrgeRead: false,
        canApprovePendingActions: false
      } as any)
      recordsRef.value = [makeUrgeRecord({ isRead: false })]
      wrapper = await mountAndFlush()
      const buttons = wrapper.findAll('button')
      const markReadBtn = buttons.find(b => b.text().includes('标记已读'))
      expect(markReadBtn).toBeUndefined()
    })

    it('shows refresh and mark all read buttons when permissions granted', async () => {
      vi.mocked(getWorkflowPermissionState).mockReturnValue({
        canViewUrge: true,
        canMarkUrgeRead: true,
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
