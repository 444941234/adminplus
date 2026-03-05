import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ActionCard from './ActionCard.vue';

describe('ActionCard', () => {
  const mockActions = [
    { id: '1', label: '添加用户', icon: 'Plus', type: 'primary' },
    { id: '2', label: '导出数据', icon: 'Download', type: 'success' },
    { id: '3', label: '刷新列表', icon: 'Refresh', type: 'warning' }
  ];

  it('should render action buttons', () => {
    const wrapper = mount(ActionCard, {
      props: {
        title: '快捷操作',
        actions: mockActions
      }
    });
    expect(wrapper.findAll('.action-button')).toHaveLength(3);
  });

  it('should render title', () => {
    const wrapper = mount(ActionCard, {
      props: {
        title: '快捷操作',
        actions: mockActions
      }
    });
    expect(wrapper.text()).toContain('快捷操作');
  });

  it('should emit action event with action id', async () => {
    const wrapper = mount(ActionCard, {
      props: {
        actions: mockActions
      }
    });
    await wrapper.findAll('.action-button')[0].trigger('click');
    expect(wrapper.emitted('action')).toBeTruthy();
    expect(wrapper.emitted('action')?.[0]).toEqual(['1']);
  });

  it('should apply column layout when specified', () => {
    const wrapper = mount(ActionCard, {
      props: {
        actions: mockActions,
        column: true
      }
    });
    expect(wrapper.find('.action-card').classes()).toContain('column');
  });
});
