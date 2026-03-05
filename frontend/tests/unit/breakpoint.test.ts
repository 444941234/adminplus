import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { useBreakpoint } from '@adminplus/ui-vue/src/composables/useBreakpoint';
import { ref } from 'vue';

describe('useBreakpoint', () => {
  let originalInnerWidth: number;

  beforeEach(() => {
    originalInnerWidth = window.innerWidth;
  });

  afterEach(() => {
    // 恢复原始窗口大小
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: originalInnerWidth
    });
  });

  const mockResizeObserver = () => {
    return {
      observe: vi.fn(),
      unobserve: vi.fn(),
      disconnect: vi.fn()
    };
  };

  it('should detect mobile breakpoint correctly', () => {
    // 模拟移动端视口宽度
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375
    });

    const { breakpoint, isMobile, isTablet, isDesktop } = useBreakpoint();

    expect(breakpoint.value).toBe('mobile');
    expect(isMobile.value).toBe(true);
    expect(isTablet.value).toBe(false);
    expect(isDesktop.value).toBe(false);
  });

  it('should detect tablet breakpoint correctly', () => {
    // 模拟平板视口宽度
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 800
    });

    const { breakpoint, isMobile, isTablet, isDesktop } = useBreakpoint();

    expect(breakpoint.value).toBe('tablet');
    expect(isMobile.value).toBe(false);
    expect(isTablet.value).toBe(true);
    expect(isDesktop.value).toBe(false);
  });

  it('should detect desktop breakpoint correctly', () => {
    // 模拟桌面视口宽度
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920
    });

    const { breakpoint, isMobile, isTablet, isDesktop } = useBreakpoint();

    expect(breakpoint.value).toBe('desktop');
    expect(isMobile.value).toBe(false);
    expect(isTablet.value).toBe(false);
    expect(isDesktop.value).toBe(true);
  });

  it('should have correct default breakpoint values', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920
    });

    const { width, breakpoint, isMobileOrTablet } = useBreakpoint();

    expect(width.value).toBe(1920);
    expect(breakpoint.value).toBe('desktop');
    expect(isMobileOrTablet.value).toBe(false);
  });

  it('should update breakpoint on window resize', async () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920
    });

    const { breakpoint, width } = useBreakpoint();

    expect(breakpoint.value).toBe('desktop');
    expect(width.value).toBe(1920);

    // 模拟窗口大小变化
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 500
    });

    // 触发 resize 事件
    window.dispatchEvent(new Event('resize'));

    // 等待响应式更新
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(width.value).toBe(500);
    expect(breakpoint.value).toBe('mobile');
  });

  it('should use custom breakpoint values', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 900
    });

    const customBreakpoints = {
      mobile: 600,
      tablet: 900,
      desktop: 900
    };

    const { breakpoint } = useBreakpoint(customBreakpoints);

    expect(breakpoint.value).toBe('tablet');
  });

  it('should correctly identify boundary conditions', () => {
    // 测试边界条件：正好在 mobile 上限
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 767
    });

    const { breakpoint: bp1 } = useBreakpoint();
    expect(bp1.value).toBe('mobile');

    // 测试边界条件：正好在 tablet 上限
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1023
    });

    const { breakpoint: bp2 } = useBreakpoint();
    expect(bp2.value).toBe('tablet');

    // 测试边界条件：正好在 desktop 下限
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1024
    });

    const { breakpoint: bp3 } = useBreakpoint();
    expect(bp3.value).toBe('desktop');
  });

  it('should handle isMobileOrTablet correctly', () => {
    // 移动端
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 500
    });

    const { isMobileOrTablet: mobileOrTablet1 } = useBreakpoint();
    expect(mobileOrTablet1.value).toBe(true);

    // 平板
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 800
    });

    const { isMobileOrTablet: mobileOrTablet2 } = useBreakpoint();
    expect(mobileOrTablet2.value).toBe(true);

    // 桌面
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920
    });

    const { isMobileOrTablet: mobileOrTablet3 } = useBreakpoint();
    expect(mobileOrTablet3.value).toBe(false);
  });

  it('should return reactive width value', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1200
    });

    const { width } = useBreakpoint();

    expect(width.value).toBe(1200);
    expect(typeof width.value).toBe('number');
  });
});
