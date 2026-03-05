import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import FloatingPanel from './FloatingPanel.vue';

describe('FloatingPanel', () => {
  it('should render trigger button', () => {
    const wrapper = mount(FloatingPanel);
    expect(wrapper.find('.panel-trigger').exists()).toBe(true);
  });

  it('should not show panel content by default', () => {
    const wrapper = mount(FloatingPanel);
    expect(wrapper.find('.panel-content').exists()).toBe(false);
  });

  it('should show panel content when expanded', async () => {
    const wrapper = mount(FloatingPanel);
    await wrapper.find('.panel-trigger').trigger('click');
    expect(wrapper.find('.panel-content').exists()).toBe(true);
  });

  it('should emit toggle event', async () => {
    const wrapper = mount(FloatingPanel);
    await wrapper.find('.panel-trigger').trigger('click');
    expect(wrapper.emitted('toggle')).toBeTruthy();
  });
});
