import { computed } from 'vue';

export type ThemeType = 'default' | 'gradient' | 'dark';

export interface UseThemeOptions {
  storageKey?: string;
  defaultTheme?: ThemeType;
}

export function useTheme(options: UseThemeOptions = {}) {
  const {
    storageKey = 'adminplus-theme',
    defaultTheme = 'gradient'
  } = options;

  // 从 localStorage 读取保存的主题
  const savedTheme = localStorage.getItem(storageKey) as ThemeType | null;
  const currentTheme = computed(() => savedTheme || defaultTheme);

  // 应用主题到 document
  const applyTheme = (theme: ThemeType) => {
    const html = document.documentElement;
    // 移除所有主题类
    html.classList.remove('theme-default', 'theme-gradient', 'theme-dark');
    // 添加当前主题类
    html.classList.add(`theme-${theme}`);
    // 保存到 localStorage
    localStorage.setItem(storageKey, theme);
  };

  // 设置主题
  const setTheme = (theme: ThemeType) => {
    applyTheme(theme);
  };

  // 切换到下一个主题
  const toggleTheme = () => {
    const themes: ThemeType[] = ['default', 'gradient', 'dark'];
    const currentIndex = themes.indexOf(currentTheme.value);
    const nextTheme = themes[(currentIndex + 1) % themes.length];
    setTheme(nextTheme);
  };

  // 计算属性
  const isDark = computed(() => currentTheme.value === 'dark');
  const isGradient = computed(() => currentTheme.value === 'gradient');

  // 初始化应用主题
  applyTheme(currentTheme.value);

  return {
    currentTheme,
    isDark,
    isGradient,
    setTheme,
    toggleTheme
  };
}
