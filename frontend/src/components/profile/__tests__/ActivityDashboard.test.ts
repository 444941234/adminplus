import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ActivityDashboard from '../ActivityDashboard.vue'

describe('ActivityDashboard', () => {
  const mockActivityStats = {
    daysActive: 45,
    totalActions: 1234,
    lastLogin: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
    lastLoginIp: '192.168.1.100',
    recentActivity: [
      {
        id: '1',
        action: 'Updated profile information',
        timestamp: new Date(Date.now() - 1800000).toISOString(), // 30 mins ago
        type: 'update' as const
      },
      {
        id: '2',
        action: 'Created new user',
        timestamp: new Date(Date.now() - 7200000).toISOString(), // 2 hours ago
        type: 'create' as const
      },
      {
        id: '3',
        action: 'Deleted old records',
        timestamp: new Date(Date.now() - 14400000).toISOString(), // 4 hours ago
        type: 'delete' as const
      },
      {
        id: '4',
        action: 'User login',
        timestamp: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
        type: 'login' as const
      },
      {
        id: '5',
        action: 'Updated settings',
        timestamp: new Date(Date.now() - 28800000).toISOString(), // 8 hours ago
        type: 'update' as const
      }
    ]
  }

  it('should render activity statistics', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    expect(wrapper.text()).toContain('45')
    expect(wrapper.text()).toContain('Days Active')
    expect(wrapper.text()).toContain('1234')
    expect(wrapper.text()).toContain('Total Actions')
  })

  it('should render recent activity timeline', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    expect(wrapper.text()).toContain('Recent Activity')
    expect(wrapper.text()).toContain('Updated profile information')
    expect(wrapper.text()).toContain('Created new user')
    expect(wrapper.text()).toContain('Deleted old records')
  })

  it('should render last login information', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    expect(wrapper.text()).toContain('Last Login')
    expect(wrapper.text()).toContain('192.168.1.100')
  })

  it('should show loading state when loading is true', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats, loading: true }
    })

    expect(wrapper.text()).toContain('Loading activity data...')
    expect(wrapper.text()).toContain('Loading login information...')
  })

  it('should show empty state when no recent activity', () => {
    const emptyActivityStats = {
      ...mockActivityStats,
      recentActivity: []
    }

    const wrapper = mount(ActivityDashboard, {
      props: { activity: emptyActivityStats }
    })

    expect(wrapper.text()).toContain('No recent activity')
  })

  it('should display only last 5 activities', () => {
    const manyActivities = {
      ...mockActivityStats,
      recentActivity: [
        ...mockActivityStats.recentActivity,
        {
          id: '6',
          action: 'Extra activity',
          timestamp: new Date().toISOString(),
          type: 'update' as const
        }
      ]
    }

    const wrapper = mount(ActivityDashboard, {
      props: { activity: manyActivities }
    })

    // Should not show the 6th activity
    expect(wrapper.text()).not.toContain('Extra activity')
  })

  it('should show activity type badges', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    expect(wrapper.text()).toContain('update')
    expect(wrapper.text()).toContain('create')
    expect(wrapper.text()).toContain('delete')
    expect(wrapper.text()).toContain('login')
  })

  it('should format timestamps as relative time', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    // Should contain relative time indicators like "ago", "hour", "minute"
    expect(wrapper.text()).toMatch(/ago|hour|minute/)
  })

  it('should have stat cards with proper styling', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBe(2)
  })

  it('should have activity items with proper styling', () => {
    const wrapper = mount(ActivityDashboard, {
      props: { activity: mockActivityStats }
    })

    const activityItems = wrapper.findAll('.activity-item')
    expect(activityItems.length).toBe(5)
  })
})
