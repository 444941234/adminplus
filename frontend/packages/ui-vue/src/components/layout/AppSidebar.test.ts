import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AppSidebar from './AppSidebar.vue';

describe('AppSidebar', () => {
  const mockMenus = [
    { id: '1', name: '首页', path: '/', icon: 'House' },
    { id: '2', name: '用户管理', path: '/users', icon: 'User' },
    { id: '3', name: '角色管理', path: '/roles', icon: 'UserFilled' }
  ];

  it('should render menu items', () => {
    const wrapper = mount(AppSidebar, {
      props: {
        menus: mockMenus
      }
    });
    expect(wrapper.findAll('.menu-item')).toHaveLength(3);
  });

  it('should apply collapsed class when collapsed', () => {
    const wrapper = mount(AppSidebar, {
      props: {
        menus: mockMenus,
        collapsed: true
      }
    });
    expect(wrapper.find('.app-sidebar').classes()).toContain('collapsed');
  });

  it('should emit toggle event', async () => {
    const wrapper = mount(AppSidebar, {
      props: {
        menus: mockMenus
      }
    });
    await wrapper.find('.logo-area').trigger('click');
    expect(wrapper.emitted('toggle')).toBeTruthy();
  });
});
