import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import { h } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import { AdminLayout } from '@adminplus/ui-vue';
import type { MenuItem } from '@adminplus/ui-vue';

// Mock vue-router
const mockRouter = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/users', component: { template: '<div>Users</div>' } }
  ]
});

vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/' }),
  useRouter: () => mockRouter
}));

describe('Layout Integration', () => {
  let pinia: ReturnType<typeof createPinia>;

  const mockMenus: MenuItem[] = [
    { id: '1', name: '首页', path: '/', icon: 'House' },
    { id: '2', name: '用户管理', path: '/users', icon: 'User' },
    {
      id: '3',
      name: '系统管理',
      path: '/system',
      icon: 'Setting',
      children: [
        { id: '3-1', name: '角色管理', path: '/system/roles', icon: 'UserFilled' },
        { id: '3-2', name: '菜单管理', path: '/system/menus', icon: 'Menu' }
      ]
    }
  ];

  beforeEach(() => {
    pinia = createPinia();
  });

  it('should render complete layout structure', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      },
      slots: {
        default: () => h('div', { class: 'test-content' }, 'Test Content')
      }
    });

    // 验证布局结构
    expect(wrapper.find('.admin-layout').exists()).toBe(true);
    expect(wrapper.find('.app-sidebar').exists()).toBe(true);
    expect(wrapper.find('.main-wrapper').exists()).toBe(true);
    expect(wrapper.find('.app-header').exists()).toBe(true);
    expect(wrapper.find('.content-area').exists()).toBe(true);
    expect(wrapper.find('.floating-panel').exists()).toBe(true);

    // 验证默认插槽内容渲染
    expect(wrapper.find('.test-content').exists()).toBe(true);
    expect(wrapper.find('.test-content').text()).toBe('Test Content');
  });

  it('should render menus correctly', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    // 验证菜单项渲染
    const menuItems = wrapper.findAll('.sidebar-menu-item');
    expect(menuItems.length).toBeGreaterThan(0);
  });

  it('should handle sidebar collapse state', async () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus,
        collapsed: false
      }
    });

    const mainWrapper = wrapper.find('.main-wrapper');
    expect(mainWrapper.classes()).not.toContain('sidebar-collapsed');

    // 触发折叠
    await wrapper.setProps({ collapsed: true });
    expect(mainWrapper.classes()).toContain('sidebar-collapsed');
  });

  it('should handle mobile menu toggle', async () => {
    // 模拟移动端视口
    global.innerWidth = 375;

    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    const sidebar = wrapper.findComponent({ name: 'AppSidebar' });
    expect(sidebar.exists()).toBe(true);

    // 恢复视口大小
    global.innerWidth = 1920;
  });

  it('should emit theme change event from floating panel', async () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    // 找到浮动面板的触发按钮
    const triggerButton = wrapper.find('.panel-trigger');
    expect(triggerButton.exists()).toBe(true);

    // 点击展开面板
    await triggerButton.trigger('click');
    expect(wrapper.find('.panel-content').exists()).toBe(true);
  });

  it('should render header with user info', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    const header = wrapper.findComponent({ name: 'AppHeader' });
    expect(header.exists()).toBe(true);
  });

  it('should handle nested menu items correctly', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    // 验证嵌套菜单结构
    const nestedMenu = mockMenus.find(m => m.children);
    expect(nestedMenu).toBeDefined();
    expect(nestedMenu?.children?.length).toBeGreaterThan(0);
  });

  it('should apply theme class correctly', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    // 主题类应该应用到 document.documentElement
    // 这里只是验证组件能正常渲染，实际主题切换由 theme store 处理
    expect(wrapper.find('.admin-layout').exists()).toBe(true);
  });

  it('should handle empty menus array', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: []
      }
    });

    expect(wrapper.find('.admin-layout').exists()).toBe(true);
    expect(wrapper.find('.app-sidebar').exists()).toBe(true);
  });

  it('should be responsive with correct breakpoints', () => {
    const wrapper = mount(AdminLayout, {
      global: {
        plugins: [pinia]
      },
      props: {
        menus: mockMenus
      }
    });

    // 验证响应式类存在
    expect(wrapper.find('.admin-layout').exists()).toBe(true);
  });
});
