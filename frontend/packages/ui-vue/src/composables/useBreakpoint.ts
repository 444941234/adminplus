import { ref, computed, onMounted, onUnmounted } from 'vue';

export type Breakpoint = 'mobile' | 'tablet' | 'desktop';

export interface BreakpointValues {
  mobile: number;
  tablet: number;
  desktop: number;
}

export interface UseBreakpointOptions {
  mobile?: number;
  tablet?: number;
  desktop?: number;
}

export function useBreakpoint(options: UseBreakpointOptions = {}) {
  const {
    mobile = 768,
    tablet = 1024,
    desktop = 1024
  } = options;

  const width = ref(window.innerWidth);

  // 计算当前断点
  const breakpoint = computed<Breakpoint>(() => {
    if (width.value < mobile) return 'mobile';
    if (width.value < tablet) return 'tablet';
    return 'desktop';
  });

  // 便捷计算属性
  const isMobile = computed(() => breakpoint.value === 'mobile');
  const isTablet = computed(() => breakpoint.value === 'tablet');
  const isDesktop = computed(() => breakpoint.value === 'desktop');
  const isMobileOrTablet = computed(() => isMobile.value || isTablet.value);

  // 处理窗口大小变化
  const handleResize = () => {
    width.value = window.innerWidth;
  };

  // 添加事件监听
  onMounted(() => {
    window.addEventListener('resize', handleResize);
  });

  // 移除事件监听
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
  });

  return {
    width,
    breakpoint,
    isMobile,
    isTablet,
    isDesktop,
    isMobileOrTablet
  };
}
