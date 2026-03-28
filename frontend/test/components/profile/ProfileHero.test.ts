import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProfileHero from '@/components/profile/ProfileHero.vue'

describe('ProfileHero', () => {
  const mockProfile = {
    id: '1',
    username: 'admin',
    nickname: 'Admin User',
    email: 'admin@example.com',
    phone: '+86 138****8888',
    avatar: '',
    deptName: 'IT',
    roles: ['Super Admin', 'User Manager']
  }

  it('should render profile information', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    expect(wrapper.text()).toContain('Admin User')
    expect(wrapper.text()).toContain('@admin')
    expect(wrapper.text()).toContain('Super Admin · User Manager')
  })

  it('should show online status when isOnline is true', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile, isOnline: true }
    })

    expect(wrapper.find('.profile-hero__status').exists()).toBe(true)
  })

  it('should not show online status when isOnline is false', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile, isOnline: false }
    })

    expect(wrapper.find('.profile-hero__status').exists()).toBe(false)
  })

  it('should show initials from nickname', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    expect(wrapper.find('.profile-hero__avatar-img').text()).toBe('A')
  })

  it('should show email and phone badges when provided', () => {
    const wrapper = mount(ProfileHero, {
      props: { profile: mockProfile }
    })

    expect(wrapper.text()).toContain('admin@example.com')
    expect(wrapper.text()).toContain('+86 138****8888')
  })

  it('should handle profile without nickname', () => {
    const profileWithoutNickname = { ...mockProfile, nickname: '' }
    const wrapper = mount(ProfileHero, {
      props: { profile: profileWithoutNickname }
    })

    expect(wrapper.text()).toContain('admin')
    expect(wrapper.find('.profile-hero__avatar-img').text()).toBe('A')
  })

  it('should handle profile without roles', () => {
    const profileWithoutRoles = { ...mockProfile, roles: [] }
    const wrapper = mount(ProfileHero, {
      props: { profile: profileWithoutRoles }
    })

    expect(wrapper.text()).toContain('No roles assigned')
  })
})
