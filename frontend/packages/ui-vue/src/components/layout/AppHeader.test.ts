import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AppHeader from './AppHeader.vue';

describe('AppHeader', () => {
  const mockUser = {
    nickname: '测试用户',
    avatar: 'https://example.com/avatar.jpg'
  };

  const mockBreadcrumbs = [
    { title: '首页', path: '/' },
    { title: '用户管理', path: '/users' }
  ];

  it('should render user info', () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: mockBreadcrumbs
      }
    });
    expect(wrapper.text()).toContain('测试用户');
  });

  it('should render breadcrumbs', () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: mockBreadcrumbs
      }
    });
    expect(wrapper.find('.el-breadcrumb').exists()).toBe(true);
  });

  it('should emit toggle event when collapse button clicked', async () => {
    const wrapper = mount(AppHeader, {
      props: {
        user: mockUser,
        breadcrumbs: [],
        collapsed: false
      }
    });
    await wrapper.find('.header-left button').trigger('click');
    expect(wrapper.emitted('toggle')).toBeTruthy();
  });
});
