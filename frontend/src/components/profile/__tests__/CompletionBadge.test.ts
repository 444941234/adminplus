import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CompletionBadge from '../CompletionBadge.vue'

describe('CompletionBadge', () => {
  it('should render with 0% completion when all fields are empty', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: '',
        email: '',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('0%')
    expect(wrapper.text()).toContain('Profile Progress')
  })

  it('should calculate 25% completion with one field filled', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: '',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('25%')
  })

  it('should calculate 50% completion with two fields filled', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('50%')
  })

  it('should calculate 75% completion with three fields filled', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '+86 138****8888',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('75%')
  })

  it('should calculate 100% completion with all fields filled', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '+86 138****8888',
        hasAvatar: true
      }
    })

    expect(wrapper.text()).toContain('100%')
    expect(wrapper.text()).toContain('Profile Complete!')
  })

  it('should display appropriate message for 0% completion', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: '',
        email: '',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('Get started by adding your profile information')
  })

  it('should display appropriate message for 25% completion', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: '',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('Good start!')
  })

  it('should display appropriate message for 50% completion', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('halfway there')
  })

  it('should display appropriate message for 75% completion', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '+86 138****8888',
        hasAvatar: false
      }
    })

    expect(wrapper.text()).toContain('Almost there')
  })

  it('should display congratulations message for 100% completion', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '+86 138****8888',
        hasAvatar: true
      }
    })

    expect(wrapper.text()).toContain('Congratulations!')
  })

  it('should render trophy icon', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: '',
        email: '',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.find('.completion-badge__icon').exists()).toBe(true)
  })

  it('should render progress bar', () => {
    const wrapper = mount(CompletionBadge, {
      props: {
        nickname: 'John',
        email: 'john@example.com',
        phone: '',
        hasAvatar: false
      }
    })

    expect(wrapper.find('.completion-badge__progress-bar').exists()).toBe(true)
  })

  it('should handle default props', () => {
    const wrapper = mount(CompletionBadge, {
      props: {}
    })

    expect(wrapper.text()).toContain('0%')
  })
})
