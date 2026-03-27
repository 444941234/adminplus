import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, ref } from 'vue'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UrgeCenter from './UrgeCenter.vue'

const fetchUrgeList = vi.fn()
const fetchUnreadCount = vi.fn()
const markRead = vi.fn()
const markAllRead = vi.fn()
const loadingRef = ref(false)
const unreadCountRef = ref(3)
const activeTabRef = ref<'received' | 'sent' | 'unread'>('received')
const recordsRef = ref([
  {
    id: 'urge-001',
    instanceId: 'inst-001',
    nodeId: 'node-001',
    nodeName: '财务审批',
    urgeUserId: 'user-001',
    urgeUserName: '张三',
    urgeTargetId: 'user-002',
    urgeTargetName: '李四',
    urgeContent: '请尽快审批',
    isRead: false,
    readTime: null,
    createTime: '2026-03-27T08:00:00Z'
  }
])

vi.mock('@/composables/workflow/useWorkflowUrge', () => ({
  useWorkflowUrge: () => ({
    loading: loadingRef,
    unreadCount: unreadCountRef,
    activeTab: activeTabRef,
    records: recordsRef,
    fetchUnreadCount,
    fetchUrgeList,
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
        h('button', { class: 'switch-sent', onClick: () => emit('update:modelValue', 'sent') }, '切换发出'),
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

describe('UrgeCenter.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    loadingRef.value = false
    unreadCountRef.value = 3
    activeTabRef.value = 'received'
    recordsRef.value = [
      {
        id: 'urge-001',
        instanceId: 'inst-001',
        nodeId: 'node-001',
        nodeName: '财务审批',
        urgeUserId: 'user-001',
        urgeUserName: '张三',
        urgeTargetId: 'user-002',
        urgeTargetName: '李四',
        urgeContent: '请尽快审批',
        isRead: false,
        readTime: null,
        createTime: '2026-03-27T08:00:00Z'
      }
    ]
  })

  it('fetches urge list and unread count on mount', async () => {
    mount(UrgeCenter, {
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

    expect(fetchUrgeList).toHaveBeenCalled()
    expect(fetchUnreadCount).toHaveBeenCalled()
  })

  it('switches tabs and marks urge as read', async () => {
    const wrapper = mount(UrgeCenter, {
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
    await wrapper.find('.switch-sent').trigger('click')
    await wrapper.find('.switch-unread').trigger('click')
    await wrapper.findAll('button').find((button) => button.text() === '标记已读')!.trigger('click')

    expect(fetchUrgeList).toHaveBeenCalledWith('sent')
    expect(fetchUrgeList).toHaveBeenCalledWith('unread')
    expect(markRead).toHaveBeenCalledWith('urge-001')
  })

  it('marks all unread urge records as read', async () => {
    const wrapper = mount(UrgeCenter, {
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
