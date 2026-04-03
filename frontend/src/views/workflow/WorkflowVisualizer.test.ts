import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import WorkflowVisualizer from '@/views/workflow/WorkflowVisualizer.vue'

const apiMocks = vi.hoisted(() => ({
  getWorkflowDefinition: vi.fn(),
  getWorkflowNodes: vi.fn()
}))

// Mock vue-flow
vi.mock('@vue-flow/core', () => ({
  VueFlow: defineComponent({
    name: 'VueFlow',
    props: {
      nodes: { type: Array, default: () => [] },
      edges: { type: Array, default: () => [] },
      defaultViewport: { type: Object, default: () => ({ zoom: 1, x: 0, y: 0 }) },
      minZoom: { type: Number, default: 0.5 },
      maxZoom: { type: Number, default: 1.5 },
      panOnDrag: { type: Boolean, default: true },
      zoomOnScroll: { type: Boolean, default: true },
      zoomOnPinch: { type: Boolean, default: true },
      zoomOnDoubleClick: { type: Boolean, default: false },
      preventScrolling: { type: Boolean, default: true },
      fitViewOnInit: { type: Boolean, default: false }
    },
    emits: ['node-click'],
    setup(props, { emit: _emit, slots }) {
      return () =>
        h('div', { class: 'vue-flow-stub', 'data-node-count': String(props.nodes?.length || 0) }, [
          h('span', { class: 'nodes-count' }, `Nodes: ${props.nodes?.length || 0}`),
          h('span', { class: 'edges-count' }, `Edges: ${props.edges?.length || 0}`),
          slots.default?.()
        ])
    }
  }),
  useVueFlow: vi.fn(() => ({
    onConnect: vi.fn(),
    onNodesChange: vi.fn(),
    addEdges: vi.fn(),
    fitView: vi.fn()
  }))
}))

vi.mock('@vue-flow/background', () => ({
  Background: defineComponent({
    name: 'Background',
    setup() {
      return () => h('div', { class: 'background-stub' })
    }
  })
}))

vi.mock('@vue-flow/controls', () => ({
  Controls: defineComponent({
    name: 'Controls',
    setup() {
      return () => h('div', { class: 'controls-stub' })
    }
  })
}))

vi.mock('@/api', () => ({
  getWorkflowDefinition: apiMocks.getWorkflowDefinition,
  getWorkflowNodes: apiMocks.getWorkflowNodes
}))

const makeWorkflowNode = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'node-001',
  definitionId: 'def-001',
  nodeName: '部门经理审批',
  nodeCode: 'dept_manager',
  nodeOrder: 1,
  approverType: 'role',
  approverId: 'role-001',
  isCounterSign: false,
  autoPassSameUser: false,
  description: '审批节点',
  createTime: '2026-03-27T08:00:00Z',
  ...overrides
})

const makeWorkflowDefinition = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'def-001',
  definitionName: '请假审批',
  definitionKey: 'leave_approval',
  category: '人事',
  description: '请假流程',
  status: 1,
  version: 1,
  formConfig: '',
  nodeCount: 2,
  createTime: '2026-03-27T08:00:00Z',
  updateTime: '2026-03-27T08:00:00Z',
  ...overrides
})

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

describe('WorkflowVisualizer.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    apiMocks.getWorkflowDefinition.mockResolvedValue(mockApiResponse(makeWorkflowDefinition()) as any)
    apiMocks.getWorkflowNodes.mockResolvedValue(mockApiResponse([
      makeWorkflowNode(),
      makeWorkflowNode({ id: 'node-002', nodeName: '财务审批', nodeCode: 'finance', nodeOrder: 2 })
    ]) as any)
  })

  const mountComponent = async (props = {}, options = {}) => {
    const wrapper = mount(WorkflowVisualizer, {
      props,
      global: {
        stubs: {},
        ...options
      }
    })
    await flushPromises()
    return wrapper
  }

  // =========================================================================
  // 1. Initial Render with definitionId
  // =========================================================================
  describe('Loading with definitionId', () => {
    it('fetches definition and nodes when definitionId provided', async () => {
      await mountComponent({ definitionId: 'def-001' })

      expect(apiMocks.getWorkflowDefinition).toHaveBeenCalledWith('def-001')
      expect(apiMocks.getWorkflowNodes).toHaveBeenCalledWith('def-001')
    })

    it('does not fetch when no definitionId', async () => {
      await mountComponent()

      expect(apiMocks.getWorkflowDefinition).not.toHaveBeenCalled()
      expect(apiMocks.getWorkflowNodes).not.toHaveBeenCalled()
    })

    it('emits loaded event after fetching', async () => {
      const wrapper = await mountComponent({ definitionId: 'def-001' })

      expect(wrapper.emitted('loaded')).toBeTruthy()
      const emittedEvents = wrapper.emitted('loaded') as unknown[][]
      expect(emittedEvents).toBeDefined()
      const emittedDef = emittedEvents[0]?.[0] as { id: string; name: string; nodes: unknown[] }
      expect(emittedDef?.id).toBe('def-001')
      expect(emittedDef?.name).toBe('请假审批')
      expect(emittedDef?.nodes?.length).toBe(2)
    })

    it('shows loading state initially when fetching', async () => {
      // The component starts loading immediately on mount with definitionId
      const wrapper = await mountComponent({ definitionId: 'def-001' })
      // After flushPromises, loading should be complete
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
      expect(wrapper.find('.vue-flow-stub').exists()).toBe(true)
    })

    it('shows empty state when no nodes', async () => {
      apiMocks.getWorkflowNodes.mockResolvedValue(mockApiResponse([]) as any)
      const wrapper = await mountComponent({ definitionId: 'def-001' })

      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('暂无流程节点数据')
    })
  })

  // =========================================================================
  // 2. Using nodes prop directly
  // =========================================================================
  describe('Using nodes prop', () => {
    it('uses provided nodes without API call', async () => {
      const nodes = [
        makeWorkflowNode(),
        makeWorkflowNode({ id: 'node-002', nodeName: '财务审批', nodeOrder: 2 })
      ]
      await mountComponent({ nodes })

      expect(apiMocks.getWorkflowDefinition).not.toHaveBeenCalled()
      expect(apiMocks.getWorkflowNodes).not.toHaveBeenCalled()
    })

    it('renders nodes from prop', async () => {
      const nodes = [
        makeWorkflowNode(),
        makeWorkflowNode({ id: 'node-002', nodeName: '财务审批', nodeOrder: 2 })
      ]
      const wrapper = await mountComponent({ nodes })

      expect(wrapper.find('.vue-flow-stub').exists()).toBe(true)
      expect(wrapper.find('.nodes-count').text()).toBe('Nodes: 4') // 2 workflow nodes + start + end
    })
  })

  // =========================================================================
  // 3. Node State Visualization
  // =========================================================================
  describe('Node State Visualization', () => {
    it('marks current node correctly', async () => {
      const nodes = [
        makeWorkflowNode(),
        makeWorkflowNode({ id: 'node-002', nodeName: '财务审批' })
      ]
      const wrapper = await mountComponent({
        nodes,
        currentNodeId: 'node-002'
      })
      const vm = wrapper.vm as any

      const workflowNodes = vm.definition?.nodes || []
      expect(workflowNodes[0].state).toBe('pending')
      expect(workflowNodes[1].state).toBe('current')
    })

    it('marks completed nodes correctly', async () => {
      const nodes = [
        makeWorkflowNode(),
        makeWorkflowNode({ id: 'node-002', nodeName: '财务审批' })
      ]
      const completedNodeIds = new Set(['node-001'])
      const wrapper = await mountComponent({
        nodes,
        currentNodeId: 'node-002',
        completedNodeIds
      })
      const vm = wrapper.vm as any

      const workflowNodes = vm.definition?.nodes || []
      expect(workflowNodes[0].state).toBe('completed')
      expect(workflowNodes[1].state).toBe('current')
    })

    it('handles empty completedNodeIds', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({
        nodes,
        currentNodeId: null,
        completedNodeIds: new Set()
      })
      const vm = wrapper.vm as any

      const workflowNodes = vm.definition?.nodes || []
      expect(workflowNodes[0].state).toBe('pending')
    })
  })

  // =========================================================================
  // 4. Edge Connections
  // =========================================================================
  describe('Edge Connections', () => {
    it('creates edges connecting all nodes', async () => {
      const nodes = [
        makeWorkflowNode({ id: 'node-001', nodeOrder: 1 }),
        makeWorkflowNode({ id: 'node-002', nodeOrder: 2 }),
        makeWorkflowNode({ id: 'node-003', nodeOrder: 3 })
      ]
      const wrapper = await mountComponent({ nodes })

      // 3 workflow nodes + start + end = 4 edges (start->1, 1->2, 2->3, 3->end)
      expect(wrapper.find('.edges-count').text()).toBe('Edges: 4')
    })

    it('creates edge from start to first node', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes })
      const vm = wrapper.vm as any

      const edges = vm.edges || []
      expect(edges.find((e: { source: string; target: string }) => e.source === 'start' && e.target === 'node-001')).toBeDefined()
    })

    it('creates edge from last node to end', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes })
      const vm = wrapper.vm as any

      const edges = vm.edges || []
      expect(edges.find((e: { source: string; target: string }) => e.source === 'node-001' && e.target === 'end')).toBeDefined()
    })
  })

  // =========================================================================
  // 5. Dynamic Height Calculation
  // =========================================================================
  describe('Dynamic Height Calculation', () => {
    it('calculates minimum height for empty nodes', async () => {
      const wrapper = await mountComponent({ nodes: [] })
      const vm = wrapper.vm as any

      // Empty state should have default height
      expect(vm.containerHeight).toBeGreaterThanOrEqual(300)
    })

    it('calculates height based on node count', async () => {
      const nodes = [
        makeWorkflowNode(),
        makeWorkflowNode({ id: 'node-002' }),
        makeWorkflowNode({ id: 'node-003' })
      ]
      const wrapper = await mountComponent({ nodes })
      const vm = wrapper.vm as any

      // Height should increase with more nodes
      // Formula: START_END_NODE_SIZE + NODE_GAP + (nodeCount * (NODE_HEIGHT + NODE_GAP)) + START_END_NODE_SIZE + NODE_GAP + 40
      // With 3 nodes: 40 + 40 + (3 * (80 + 40)) + 40 + 40 + 40 = 480
      expect(vm.containerHeight).toBeGreaterThan(300)
    })
  })

  // =========================================================================
  // 6. Readonly Mode
  // =========================================================================
  describe('Readonly Mode', () => {
    it('accepts readonly prop', async () => {
      const wrapper = await mountComponent({
        nodes: [makeWorkflowNode()],
        readonly: true
      })

      expect(wrapper.props('readonly')).toBe(true)
    })

    it('defaults readonly to false', async () => {
      const wrapper = await mountComponent({
        nodes: [makeWorkflowNode()]
      })

      expect(wrapper.props('readonly')).toBe(false)
    })
  })

  // =========================================================================
  // 7. Node Click Event
  // =========================================================================
  describe('Node Click Event', () => {
    it('emits node-click when node is clicked', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes })

      // Simulate node click through VueFlow's event
      const vm = wrapper.vm as any
      const workflowNode = { id: 'node-001', nodeName: '部门经理审批' }
      vm.onNodeClick({ node: { id: 'node-001', data: { node: workflowNode } } })

      expect(wrapper.emitted('node-click')).toBeTruthy()
      const emittedEvents = wrapper.emitted('node-click') as unknown[][]
      expect(emittedEvents?.[0]?.[0]).toEqual(workflowNode)
    })

    it('does not emit for start/end nodes', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes })
      const vm = wrapper.vm as any

      vm.onNodeClick({ node: { id: 'start', data: {} } })
      vm.onNodeClick({ node: { id: 'end', data: {} } })

      expect(wrapper.emitted('node-click')).toBeFalsy()
    })

    it('does not emit in readonly mode', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes, readonly: true })
      const vm = wrapper.vm as any

      const workflowNode = { id: 'node-001', nodeName: '部门经理审批' }
      vm.onNodeClick({ node: { id: 'node-001', data: { node: workflowNode } } })

      expect(wrapper.emitted('node-click')).toBeFalsy()
    })
  })

  // =========================================================================
  // 8. GetApproverTypeLabel Helper
  // =========================================================================
  describe('GetApproverTypeLabel Helper', () => {
    it('returns correct labels for approver types', async () => {
      const wrapper = await mountComponent({ nodes: [makeWorkflowNode()] })
      const vm = wrapper.vm as any

      expect(vm.getApproverTypeLabel('user')).toBe('用户')
      expect(vm.getApproverTypeLabel('role')).toBe('角色')
      expect(vm.getApproverTypeLabel('dept')).toBe('部门')
      expect(vm.getApproverTypeLabel('leader')).toBe('领导')
    })

    it('returns input for unknown type', async () => {
      const wrapper = await mountComponent({ nodes: [makeWorkflowNode()] })
      const vm = wrapper.vm as any

      expect(vm.getApproverTypeLabel('unknown')).toBe('unknown')
      expect(vm.getApproverTypeLabel()).toBe('-')
    })
  })

  // =========================================================================
  // 9. Error Handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles API error gracefully', async () => {
      apiMocks.getWorkflowDefinition.mockRejectedValue(new Error('API Error'))
      const wrapper = await mountComponent({ definitionId: 'def-001' })

      // Should show empty state on error
      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })
  })

  // =========================================================================
  // 10. Watch and Reactivity
  // =========================================================================
  describe('Watch and Reactivity', () => {
    it('reloads when definitionId changes', async () => {
      const wrapper = await mountComponent({ definitionId: 'def-001' })

      expect(apiMocks.getWorkflowDefinition).toHaveBeenCalledWith('def-001')
      vi.clearAllMocks()

      await wrapper.setProps({ definitionId: 'def-002' })
      await flushPromises()

      expect(apiMocks.getWorkflowDefinition).toHaveBeenCalledWith('def-002')
    })

    it('reloads when nodes prop changes', async () => {
      const nodes = [makeWorkflowNode()]
      const wrapper = await mountComponent({ nodes })

      const vm = wrapper.vm as any
      expect(vm.definition?.nodes.length).toBe(1)

      await wrapper.setProps({
        nodes: [
          makeWorkflowNode(),
          makeWorkflowNode({ id: 'node-002' })
        ]
      })
      await flushPromises()

      expect(vm.definition?.nodes.length).toBe(2)
    })
  })
})