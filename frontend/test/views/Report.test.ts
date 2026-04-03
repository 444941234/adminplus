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

vi.mock('lucide-vue-next', () => ({
  BarChart3: { name: 'BarChart3', template: '<svg />' },
  GitBranch: { name: 'GitBranch', template: '<svg />' },
  ListChecks: { name: 'ListChecks', template: '<svg />' }
}))

vi.mock('vue-router', () => ({
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a :href="to"><slot /></a>'
  }
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import Report from '@/views/analysis/Report.vue'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('Report Page', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async () => {
    wrapper = mount(Report, {
      global: {
        stubs: {
          Sonner: true
        }
      }
    } as any)
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders page title', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('报表中心')
    })

    it('renders description', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('分析入口页')
    })

    it('renders root container with space-y-6 class', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-6').exists()).toBe(true)
    })

    it('renders card with border-dashed class', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.border-dashed').exists()).toBe(true)
    })
  })

  // =========================================================================
  // 2. Navigation links
  // =========================================================================
  describe('Navigation Links', () => {
    it('renders text for statistics page', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('数据统计')
    })

    it('renders text for workflow definitions page', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('流程模板')
    })

    it('renders text for pending approval page', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('待我审批')
    })

    it('renders three navigation items', async () => {
      wrapper = await mountAndFlush()
      // Check for RouterLink components by their rendered content
      const text = wrapper.text()
      expect(text).toContain('数据统计')
      expect(text).toContain('流程模板')
      expect(text).toContain('待我审批')
    })

    it('renders description for statistics', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('查看用户、访问趋势、在线用户和系统信息')
    })

    it('renders description for workflow definitions', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('查看可用的工作流模板并发起新流程')
    })

    it('renders description for pending approval', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('查看待审批事项并进入流程详情处理')
    })
  })

  // =========================================================================
  // 3. Grid layout
  // =========================================================================
  describe('Grid Layout', () => {
    it('renders grid with 3 columns', async () => {
      wrapper = await mountAndFlush()
      const grid = wrapper.find('.md\\:grid-cols-3')
      expect(grid.exists()).toBe(true)
    })
  })
})
