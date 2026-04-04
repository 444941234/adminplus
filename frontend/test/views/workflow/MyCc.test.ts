import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, ref } from 'vue'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MyCc from '@/views/workflow/MyCc.vue'

const fetchCcList = vi.fn()
const fetchUnreadCount = vi.fn()
const markRead = vi.fn()
const markAllRead = vi.fn()
const loadingRef = ref(false)
const unreadCountRef = ref(2)
const activeTabRef = ref<'all' | 'unread'>('all')
const recordsRef = ref([
  {
    id: 'cc-001',
    instanceId: 'inst-001',
    nodeId: 'node-001',
    nodeName: '部门经理审批',
    userId: 'user-002',
    userName: '李四',
    ccType: 'approve',
    ccContent: '审批通过抄送',
    isRead: false,
    readTime: null,
    createTime: '2026-03-27T08:00:00Z'
  }
])

vi.mock('@/composables/workflow/useWorkflowCc', () => ({
  useWorkflowCc: () => ({
    loading: loadingRef,
    unreadCount: unreadCountRef,
    activeTab: activeTabRef,
    records: recordsRef,
    fetchUnreadCount,
    fetchCcList,
    markRead,
    markAllRead
  })
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    hasPermission: vi.fn(() => true)
  })
}))

const TabsStub = defineComponent({
  name: 'Tabs',
  emits: ['update:modelValue'],
  setup(_props, { slots, emit }) {
    return () =>
      h('div', { class: 'tabs-stub' }, [
        slots.default?.(),
        h('button', { class: 'switch-unread', onClick: () => emit('update:modelValue', 'unread') }, '切换未读')
      ])
  }
})

const TabsListStub = defineComponent({
  name: 'TabsList',
  setup(_props, { slots }) {
    return () => h('div', { class: 'tabs-list-stub' }, slots.default?.())
  }
})

const TabsTriggerStub = defineComponent({
  name: 'TabsTrigger',
  setup(_props, { slots }) {
    return () => h('button', { class: 'tabs-trigger-stub' }, slots.default?.())
  }
})

describe('MyCc.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    loadingRef.value = false
    unreadCountRef.value = 2
    activeTabRef.value = 'all'
    recordsRef.value = [
      {
        id: 'cc-001',
        instanceId: 'inst-001',
        nodeId: 'node-001',
        nodeName: '部门经理审批',
        userId: 'user-002',
        userName: '李四',
        ccType: 'approve',
        ccContent: '审批通过抄送',
        isRead: false,
        readTime: null,
        createTime: '2026-03-27T08:00:00Z'
      }
    ]
  })

  it('fetches cc list and unread count on mount', async () => {
    mount(MyCc, {
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
          Tabs: TabsStub,
          TabsList: TabsListStub,
          TabsTrigger: TabsTriggerStub
        }
      }
    })

    await flushPromises()

    expect(fetchCcList).toHaveBeenCalled()
    expect(fetchUnreadCount).toHaveBeenCalled()
  })

  it('switches to unread tab and marks record as read', async () => {
    const wrapper = mount(MyCc, {
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
          Tabs: TabsStub,
          TabsList: TabsListStub,
          TabsTrigger: TabsTriggerStub
        }
      }
    })

    await flushPromises()
    await wrapper.find('.switch-unread').trigger('click')
    await wrapper.findAll('button').find((button) => button.text() === '标记已读')!.trigger('click')

    expect(fetchCcList).toHaveBeenCalledWith('unread')
    expect(markRead).toHaveBeenCalledWith('cc-001')
  })

  it('marks all unread records as read', async () => {
    const wrapper = mount(MyCc, {
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
          Tabs: TabsStub,
          TabsList: TabsListStub,
          TabsTrigger: TabsTriggerStub
        }
      }
    })

    await flushPromises()
    await wrapper.findAll('button').find((button) => button.text() === '全部已读')!.trigger('click')

    expect(markAllRead).toHaveBeenCalled()
  })
})
