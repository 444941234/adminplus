import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import AdminLayout from './AdminLayout.vue';

describe('AdminLayout', () => {
  const mockMenus = [
    { id: '1', name: '首页', path: '/', icon: 'House' },
    { id: '2', name: '用户管理', path: '/users', icon: 'User' }
  ];

  it('should render complete layout', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [createPinia()]
      },
      props: {
        menus: mockMenus
      }
    });

    expect(wrapper.find('.admin-layout').exists()).toBe(true);
    expect(wrapper.find('.app-sidebar').exists()).toBe(true);
    expect(wrapper.find('.app-header').exists()).toBe(true);
    expect(wrapper.find('.content-area').exists()).toBe(true);
  });

  it('should handle sidebar collapse', async () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [createPinia()]
      },
      props: {
        menus: mockMenus
      }
    });

    const mainWrapper = wrapper.find('.main-wrapper');
    expect(mainWrapper.classes()).not.toContain('sidebar-collapsed');

    // Trigger collapse
    await wrapper.vm.handleToggle();
    expect(mainWrapper.classes()).toContain('sidebar-collapsed');
  });
});
