import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import WorkflowHookLogs from '@/components/workflow/WorkflowHookLogs.vue'

const apiMocks = vi.hoisted(() => ({
  getInstanceHookLogs: vi.fn()
}))

vi.mock('@/api/workflow', () => ({
  getInstanceHookLogs: apiMocks.getInstanceHookLogs
}))

const makeLog = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'log-001',
  instanceId: 'inst-001',
  nodeId: 'node-001',
  hookId: 'hook-001',
  hookSource: 'hook_table',
  hookPoint: 'PRE_SUBMIT',
  executorType: 'spel',
  executorConfig: '#formData.amount > 100',
  success: true,
  resultCode: 'SUCCESS',
  resultMessage: '校验通过',
  executionTime: 50,
  retryAttempts: 0,
  async: false,
  operatorId: 'user-001',
  operatorName: '张三',
  createTime: '2026-03-27T08:00:00Z',
  ...overrides
})

const mockApiResponse = (data: any) => ({ data, code: 200, message: 'success' })

const CardStub = defineComponent({
  name: 'Card',
  setup(_, { slots }) {
    return () => h('div', { class: 'card-stub' }, slots.default?.())
  }
})

const CardHeaderStub = defineComponent({
  name: 'CardHeader',
  setup(_, { slots }) {
    return () => h('div', { class: 'card-header-stub' }, slots.default?.())
  }
})

const CardTitleStub = defineComponent({
  name: 'CardTitle',
  setup(_, { slots }) {
    return () => h('div', { class: 'card-title-stub' }, slots.default?.())
  }
})

const CardContentStub = defineComponent({
  name: 'CardContent',
  setup(_, { slots }) {
    return () => h('div', { class: 'card-content-stub' }, slots.default?.())
  }
})

const BadgeStub = defineComponent({
  name: 'Badge',
  props: {
    variant: {
      type: String,
      default: 'default'
    }
  },
  setup(props, { slots }) {
    return () => h('span', { class: `badge-stub badge-${props.variant}` }, slots.default?.())
  }
})

describe('WorkflowHookLogs.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([]))
  })

  const mountComponent = async (props = {}) => {
    const wrapper = mount(WorkflowHookLogs, {
      props: {
        instanceId: 'inst-001',
        ...props
      },
      global: {
        stubs: {
          Card: CardStub,
          CardHeader: CardHeaderStub,
          CardTitle: CardTitleStub,
          CardContent: CardContentStub,
          Badge: BadgeStub
        }
      }
    })
    await flushPromises()
    return wrapper
  }

  // =========================================================================
  // 1. Initial Load
  // =========================================================================
  describe('Initial Load', () => {
    it('loads logs when instanceId provided', async () => {
      await mountComponent()
      expect(apiMocks.getInstanceHookLogs).toHaveBeenCalledWith('inst-001')
    })

    it('does not load when no instanceId', async () => {
      vi.clearAllMocks()
      await mountComponent({ instanceId: '' })
      expect(apiMocks.getInstanceHookLogs).not.toHaveBeenCalled()
    })

    it('displays loading state', async () => {
      apiMocks.getInstanceHookLogs.mockImplementation(() => new Promise(resolve => {
        setTimeout(() => resolve(mockApiResponse([])), 100)
      }))

      const wrapper = mount(WorkflowHookLogs, {
        props: { instanceId: 'inst-001' },
        global: {
          stubs: {
            Card: CardStub,
            CardHeader: CardHeaderStub,
            CardTitle: CardTitleStub,
            CardContent: CardContentStub,
            Badge: BadgeStub
          }
        }
      })

      expect(wrapper.text()).toContain('加载中')
    })

    it('displays empty state when no logs', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([]))
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('暂无钩子执行日志')
    })
  })

  // =========================================================================
  // 2. Log Display
  // =========================================================================
  describe('Log Display', () => {
    it('displays logs grouped by hook point', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ hookPoint: 'PRE_SUBMIT' }),
        makeLog({ id: 'log-002', hookPoint: 'PRE_SUBMIT' }),
        makeLog({ id: 'log-003', hookPoint: 'POST_APPROVE' })
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      expect(vm.groupedLogs['PRE_SUBMIT'].length).toBe(2)
      expect(vm.groupedLogs['POST_APPROVE'].length).toBe(1)
    })

    it('displays success badge for successful logs', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ success: true })
      ]))
      const wrapper = await mountComponent()

      expect(wrapper.text()).toContain('成功')
    })

    it('displays failure badge for failed logs', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ success: false, resultCode: 'VALIDATION_FAILED' })
      ]))
      const wrapper = await mountComponent()

      expect(wrapper.text()).toContain('失败')
    })

    it('displays async badge for async executions', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ async: true })
      ]))
      const wrapper = await mountComponent()

      expect(wrapper.text()).toContain('异步')
    })
  })

  // =========================================================================
  // 3. Hook Point Labels
  // =========================================================================
  describe('Hook Point Labels', () => {
    it('displays correct labels for all hook points', async () => {
      const hookPoints = [
        { value: 'PRE_SUBMIT', label: '提交前校验' },
        { value: 'POST_SUBMIT', label: '提交后执行' },
        { value: 'PRE_APPROVE', label: '同意前校验' },
        { value: 'POST_APPROVE', label: '同意后执行' },
        { value: 'PRE_REJECT', label: '拒绝前校验' },
        { value: 'POST_REJECT', label: '拒绝后执行' },
        { value: 'PRE_ROLLBACK', label: '退回前校验' },
        { value: 'POST_ROLLBACK', label: '退回后执行' },
        { value: 'PRE_CANCEL', label: '取消前校验' },
        { value: 'POST_CANCEL', label: '取消后执行' },
        { value: 'PRE_WITHDRAW', label: '撤回前校验' },
        { value: 'POST_WITHDRAW', label: '撤回后执行' },
        { value: 'PRE_ADD_SIGN', label: '加签前校验' },
        { value: 'POST_ADD_SIGN', label: '加签后执行' }
      ]

      for (const hp of hookPoints) {
        apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
          makeLog({ hookPoint: hp.value })
        ]))
        const wrapper = await mountComponent()
        expect(wrapper.text()).toContain(hp.label)
      }
    })
  })

  // =========================================================================
  // 4. Executor Type Labels
  // =========================================================================
  describe('Executor Type Labels', () => {
    it('displays correct labels for executor types', async () => {
      const executorTypes = [
        { value: 'spel', label: 'SpEL表达式' },
        { value: 'bean', label: 'Bean方法' },
        { value: 'http', label: 'HTTP接口' }
      ]

      for (const et of executorTypes) {
        apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
          makeLog({ executorType: et.value })
        ]))
        const wrapper = await mountComponent()
        expect(wrapper.text()).toContain(et.label)
      }
    })
  })

  // =========================================================================
  // 5. Expand/Collapse
  // =========================================================================
  describe('Expand/Collapse', () => {
    it('expands log details when clicked', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog()
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.toggleExpand('log-001')
      await flushPromises()

      expect(vm.expandedLogId).toBe('log-001')
    })

    it('collapses log when clicked again', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog()
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.toggleExpand('log-001')
      expect(vm.expandedLogId).toBe('log-001')

      vm.toggleExpand('log-001')
      expect(vm.expandedLogId).toBeNull()
    })
  })

  // =========================================================================
  // 6. Execution Time Formatting
  // =========================================================================
  describe('Execution Time Formatting', () => {
    it('formats milliseconds correctly', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      expect(vm.formatExecutionTime(50)).toBe('50ms')
      expect(vm.formatExecutionTime(999)).toBe('999ms')
      expect(vm.formatExecutionTime(1000)).toBe('1.00s')
      expect(vm.formatExecutionTime(1500)).toBe('1.50s')
      expect(vm.formatExecutionTime(undefined)).toBe('-')
      expect(vm.formatExecutionTime(null)).toBe('-')
    })
  })

  // =========================================================================
  // 7. Retry Attempts Display
  // =========================================================================
  describe('Retry Attempts Display', () => {
    it('displays retry count when greater than 0', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ retryAttempts: 2 })
      ]))
      const wrapper = await mountComponent()

      expect(wrapper.text()).toContain('重试2次')
    })

    it('does not display retry count when 0', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ retryAttempts: 0 })
      ]))
      const wrapper = await mountComponent()

      expect(wrapper.text()).not.toContain('重试')
    })
  })

  // =========================================================================
  // 8. Hook Source Label
  // =========================================================================
  describe('Hook Source Label', () => {
    it('displays correct source label for node_field', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ hookSource: 'node_field' })
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.expandedLogId = 'log-001'
      await flushPromises()

      expect(wrapper.text()).toContain('节点字段')
    })

    it('displays correct source label for hook_table', async () => {
      apiMocks.getInstanceHookLogs.mockResolvedValue(mockApiResponse([
        makeLog({ hookSource: 'hook_table' })
      ]))
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.expandedLogId = 'log-001'
      await flushPromises()

      expect(wrapper.text()).toContain('钩子表')
    })
  })

  // =========================================================================
  // 9. Watch for instanceId changes
  // =========================================================================
  describe('Watch for instanceId Changes', () => {
    it('reloads logs when instanceId changes', async () => {
      const wrapper = await mountComponent()
      vi.clearAllMocks()

      await wrapper.setProps({ instanceId: 'inst-002' })
      await flushPromises()

      expect(apiMocks.getInstanceHookLogs).toHaveBeenCalledWith('inst-002')
    })
  })
})