import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ThemeType } from '@adminplus/ui-vue';

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const currentTheme = ref<ThemeType>('gradient');
  const sidebarCollapsed = ref(false);
  const primaryColor = ref('#6366f1');

  // 计算属性
  const isDark = computed(() => currentTheme.value === 'dark');
  const isGradient = computed(() => currentTheme.value === 'gradient');
  const themeClass = computed(() => `theme-${currentTheme.value}`);

  // 方法
  function setTheme(theme: ThemeType) {
    currentTheme.value = theme;
    document.documentElement.className = themeClass.value;
    localStorage.setItem('adminplus-theme', theme);
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value;
    localStorage.setItem(
      'adminplus-sidebar-collapsed',
      String(sidebarCollapsed.value)
    );
  }

  function setPrimaryColor(color: string) {
    primaryColor.value = color;
    document.documentElement.style.setProperty('--primary-color', color);
    localStorage.setItem('adminplus-primary-color', color);
  }

  function toggleTheme() {
    const themes: ThemeType[] = ['default', 'gradient', 'dark'];
    const currentIndex = themes.indexOf(currentTheme.value);
    const nextTheme = themes[(currentIndex + 1) % themes.length];
    setTheme(nextTheme);
  }

  // 初始化
  function init() {
    const savedTheme = localStorage.getItem('adminplus-theme') as ThemeType;
    const savedCollapsed = localStorage.getItem('adminplus-sidebar-collapsed');
    const savedColor = localStorage.getItem('adminplus-primary-color');

    if (savedTheme) setTheme(savedTheme);
    if (savedCollapsed) sidebarCollapsed.value = savedCollapsed === 'true';
    if (savedColor) setPrimaryColor(savedColor);
  }

  return {
    currentTheme,
    sidebarCollapsed,
    primaryColor,
    isDark,
    isGradient,
    themeClass,
    setTheme,
    toggleTheme,
    toggleSidebar,
    setPrimaryColor,
    init
  };
});
