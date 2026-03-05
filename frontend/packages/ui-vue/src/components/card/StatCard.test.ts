import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import StatCard from './StatCard.vue';

describe('StatCard', () => {
  it('should render with correct props', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'primary',
        icon: 'User',
        value: 1234,
        label: '用户总数'
      }
    });
    expect(wrapper.find('.stat-value').text()).toBe('1234');
    expect(wrapper.find('.stat-label').text()).toBe('用户总数');
  });

  it('should show trend when provided', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'primary',
        icon: 'User',
        value: 1234,
        label: '用户总数',
        trend: '+12%',
        trendUp: true
      }
    });
    expect(wrapper.find('.stat-trend').exists()).toBe(true);
    expect(wrapper.find('.stat-trend').text()).toContain('+12%');
    expect(wrapper.find('.stat-trend').classes()).toContain('trend-up');
  });

  it('should apply correct class based on type', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'success',
        icon: 'Check',
        value: 100,
        label: '完成率'
      }
    });
    expect(wrapper.find('.stat-card').classes()).toContain('stat-card-success');
  });

  it('should show loading state', () => {
    const wrapper = mount(StatCard, {
      props: {
        type: 'primary',
        icon: 'User',
        value: 1234,
        label: '用户总数',
        loading: true
      }
    });
    expect(wrapper.find('.stat-value').text()).toBe('--');
  });
});
