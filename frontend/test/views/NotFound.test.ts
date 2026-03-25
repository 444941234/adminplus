import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import NotFound from '@/views/NotFound.vue'
import { Button } from '@/components/ui'

describe('NotFound Page', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(() => {
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/dashboard', component: { template: '<div>Dashboard</div>' } }
      ]
    })
  })

  it('should render 404 page correctly', () => {
    const wrapper = mount(NotFound, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('h1').text()).toBe('404')
    expect(wrapper.text()).toContain('页面不存在')
    expect(wrapper.text()).toContain('抱歉，您访问的页面不存在或已被删除')
  })

  it('should have back button', () => {
    const testWrapper = mount(NotFound, {
      global: {
        plugins: [router]
      }
    })

    const buttons = testWrapper.findAll('button')
    expect(buttons.length).toBeGreaterThanOrEqual(2)
  })

  it('should navigate back when back button is clicked', async () => {
    const wrapper = mount(NotFound, {
      global: {
        plugins: [router]
      }
    })

    // Mock router.back
    const backSpy = vi.spyOn(router, 'back')

    await wrapper.find('button').trigger('click')

    expect(backSpy).toHaveBeenCalled()
  })

  it('should navigate to dashboard when home button is clicked', async () => {
    const wrapper = mount(NotFound, {
      global: {
        plugins: [router]
      }
    })

    const pushSpy = vi.spyOn(router, 'push')

    // Find the second button (home button) and click it
    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')

    expect(pushSpy).toHaveBeenCalledWith('/dashboard')
  })

  it('should have proper page structure', () => {
    const wrapper = mount(NotFound, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('.min-h-screen').exists()).toBe(true)
    expect(wrapper.find('.text-center').exists()).toBe(true)
    expect(wrapper.find('.text-9xl').exists()).toBe(true)
  })
})
