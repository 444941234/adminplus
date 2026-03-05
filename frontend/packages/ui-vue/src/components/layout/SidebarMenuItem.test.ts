import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import SidebarMenuItem from './SidebarMenuItem.vue';

describe('SidebarMenuItem', () => {
  const mockMenu = {
    id: '1',
    name: '用户列表',
    path: '/users/list',
    icon: 'User'
  };

  it('should render menu item', () => {
    const wrapper = mount(SidebarMenuItem, {
      props: {
        menu: mockMenu
      }
    });
    expect(wrapper.find('.submenu-item').exists()).toBe(true);
    expect(wrapper.text()).toContain('用户列表');
  });

  it('should render icon when provided', () => {
    const wrapper = mount(SidebarMenuItem, {
      props: {
        menu: mockMenu
      }
    });
    expect(wrapper.find('.el-icon').exists()).toBe(true);
  });

  it('should handle nested children recursively', () => {
    const menuWithChildren = {
      id: '1',
      name: '用户管理',
      path: '/users',
      children: [
        { id: '2', name: '用户列表', path: '/users/list' },
        { id: '3', name: '角色管理', path: '/users/roles' }
      ]
    };

    const wrapper = mount(SidebarMenuItem, {
      props: {
        menu: menuWithChildren
      }
    });
    expect(wrapper.findAll('.submenu-item')).toHaveLength(3);
  });
});
