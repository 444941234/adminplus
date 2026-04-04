import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusBadge from '@/components/common/StatusBadge.vue'

describe('StatusBadge', () => {
  describe('rendering', () => {
    it('should render active text when status is 1', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1 }
      })
      expect(wrapper.text()).toBe('正常')
    })

    it('should render inactive text when status is 0', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 0 }
      })
      expect(wrapper.text()).toBe('禁用')
    })

    it('should use custom active text', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, activeText: '启用' }
      })
      expect(wrapper.text()).toBe('启用')
    })

    it('should use custom inactive text', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 0, inactiveText: '停用' }
      })
      expect(wrapper.text()).toBe('停用')
    })

    it('should have default variant when status is 1', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1 }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).props('variant')).toBe('default')
    })

    it('should have destructive variant when status is 0', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 0 }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).props('variant')).toBe('destructive')
    })
  })

  describe('non-clickable mode (default)', () => {
    it('should not have role="button" when not clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1 }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('role')).toBeUndefined()
    })

    it('should not have tabindex when not clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1 }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('tabindex')).toBeUndefined()
    })

    it('should not emit toggle event on click when not clickable', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1 }
      })
      await wrapper.findComponent({ name: 'Badge' }).trigger('click')
      expect(wrapper.emitted('toggle')).toBeFalsy()
    })
  })

  describe('clickable mode', () => {
    it('should have role="button" when clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('role')).toBe('button')
    })

    it('should have tabindex="0" when clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('tabindex')).toBe('0')
    })

    it('should have aria-label when clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('aria-label')).toBe('点击禁用')
    })

    it('should have correct aria-label for inactive status', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 0, clickable: true }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).attributes('aria-label')).toBe('点击启用')
    })

    it('should have cursor-pointer class when clickable', () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      expect(wrapper.findComponent({ name: 'Badge' }).classes()).toContain('cursor-pointer')
    })

    it('should emit toggle event on click when clickable', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      await wrapper.findComponent({ name: 'Badge' }).trigger('click')
      expect(wrapper.emitted('toggle')).toBeTruthy()
      expect(wrapper.emitted('toggle')!.length).toBe(1)
    })
  })

  describe('keyboard accessibility', () => {
    it('should emit toggle on Enter key when clickable', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      // Badge is a div, find the root element
      await wrapper.find('div').trigger('keydown', { key: 'Enter' })
      expect(wrapper.emitted('toggle')).toBeTruthy()
    })

    it('should emit toggle on Space key when clickable', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      await wrapper.find('div').trigger('keydown', { key: ' ' })
      expect(wrapper.emitted('toggle')).toBeTruthy()
    })

    it('should not emit toggle on other keys', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: true }
      })
      await wrapper.find('div').trigger('keydown', { key: 'Tab' })
      expect(wrapper.emitted('toggle')).toBeFalsy()
    })

    it('should not emit toggle on keydown when not clickable', async () => {
      const wrapper = mount(StatusBadge, {
        props: { status: 1, clickable: false }
      })
      await wrapper.find('div').trigger('keydown', { key: 'Enter' })
      expect(wrapper.emitted('toggle')).toBeFalsy()
    })
  })
})