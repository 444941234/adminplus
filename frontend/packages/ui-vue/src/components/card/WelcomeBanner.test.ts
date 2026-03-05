import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import WelcomeBanner from './WelcomeBanner.vue';

describe('WelcomeBanner', () => {
  it('should render with username', () => {
    const wrapper = mount(WelcomeBanner, {
      props: {
        username: 'Admin',
        greeting: '下午好，继续加油！'
      }
    });
    expect(wrapper.text()).toContain('Admin');
  });

  it('should render greeting', () => {
    const wrapper = mount(WelcomeBanner, {
      props: {
        username: 'Admin',
        greeting: '下午好，继续加油！'
      }
    });
    expect(wrapper.text()).toContain('下午好，继续加油！');
  });

  it('should render custom title and subtitle', () => {
    const wrapper = mount(WelcomeBanner, {
      props: {
        username: 'Admin',
        title: '欢迎使用系统',
        subtitle: '这是您的个人仪表板'
      }
    });
    expect(wrapper.text()).toContain('欢迎使用系统');
    expect(wrapper.text()).toContain('这是您的个人仪表板');
  });

  it('should emit action event when button clicked', async () => {
    const wrapper = mount(WelcomeBanner, {
      props: {
        username: 'Admin',
        actionText: '查看详情'
      }
    });
    await wrapper.find('.banner-action').trigger('click');
    expect(wrapper.emitted('action')).toBeTruthy();
  });
});
