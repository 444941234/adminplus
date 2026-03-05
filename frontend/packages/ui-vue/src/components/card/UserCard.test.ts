import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import UserCard from './UserCard.vue';

describe('UserCard', () => {
  const mockUser = {
    name: '张三',
    nickname: 'SuperAdmin',
    avatar: 'https://example.com/avatar.jpg',
    email: 'admin@example.com',
    motto: '让代码改变世界',
    tags: ['管理员', '开发者', '设计师']
  };

  it('should render user info', () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser }
    });
    expect(wrapper.text()).toContain('张三');
    expect(wrapper.text()).toContain('SuperAdmin');
  });

  it('should render user motto', () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser }
    });
    expect(wrapper.text()).toContain('让代码改变世界');
  });

  it('should render tags', () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser }
    });
    expect(wrapper.findAll('.tag')).toHaveLength(3);
  });

  it('should emit click event when clicked', async () => {
    const wrapper = mount(UserCard, {
      props: { user: mockUser }
    });
    await wrapper.find('.user-card').trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });
});
