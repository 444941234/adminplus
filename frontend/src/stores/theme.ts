import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

// 主题类型
export type ThemeMode = 'light' | 'dark'

// 主题配置
interface ThemeConfig {
  primaryColor: string
  mode: ThemeMode
}

// 默认主题配置
const defaultThemeConfig: ThemeConfig = {
  primaryColor: '#3B82F6',
  mode: 'light'
}

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const mode = ref<ThemeMode>(defaultThemeConfig.mode)
  const primaryColor = ref(defaultThemeConfig.primaryColor)
  const sidebarCollapsed = ref(false)

  // 计算属性
  const isDark = computed(() => mode.value === 'dark')
  const themeClass = computed(() => `theme-${mode.value}`)

  // 设置主题模式
  function setTheme(newMode: ThemeMode) {
    mode.value = newMode
    document.documentElement.setAttribute('data-theme', newMode)
    document.documentElement.className = themeClass.value
    localStorage.setItem('adminplus-theme-mode', newMode)

    // 更新 CSS 变量
    updateCssVariables()
  }

  // 切换主题模式
  function toggleTheme() {
    setTheme(mode.value === 'light' ? 'dark' : 'light')
  }

  // 设置主色调
  function setPrimaryColor(color: string) {
    primaryColor.value = color
    document.documentElement.style.setProperty('--el-color-primary', color)
    document.documentElement.style.setProperty('--primary-color', color)
    localStorage.setItem('adminplus-primary-color', color)

    // 生成主色调的色阶
    generatePrimaryColorScale(color)
  }

  // 生成主色调色阶
  function generatePrimaryColorScale(color: string) {
    const scales = [
      { name: 'light-3', amount: 30 },
      { name: 'light-5', amount: 50 },
      { name: 'light-7', amount: 70 },
      { name: 'light-8', amount: 80 },
      { name: 'light-9', amount: 90 },
      { name: 'dark-2', amount: -20 }
    ]

    scales.forEach(scale => {
      const newColor = adjustColorBrightness(color, scale.amount)
      document.documentElement.style.setProperty(`--el-color-primary-${scale.name}`, newColor)
    })
  }

  // 调整颜色亮度
  function adjustColorBrightness(hex: string, percent: number): string {
    const num = parseInt(hex.replace('#', ''), 16)
    const amt = Math.round(2.55 * percent)
    const R = (num >> 16) + amt
    const G = (num >> 8 & 0x00FF) + amt
    const B = (num & 0x0000FF) + amt

    return '#' + (
      0x1000000 +
      (R < 255 ? (R < 1 ? 0 : R) : 255) * 0x10000 +
      (G < 255 ? (G < 1 ? 0 : G) : 255) * 0x100 +
      (B < 255 ? (B < 1 ? 0 : B) : 255)
    ).toString(16).slice(1)
  }

  // 更新 CSS 变量
  function updateCssVariables() {
    const root = document.documentElement

    if (mode.value === 'dark') {
      // 深色主题变量
      root.style.setProperty('--bg-color', '#0F172A')
      root.style.setProperty('--bg-card', '#1E293B')
      root.style.setProperty('--bg-hover', '#334155')
      root.style.setProperty('--text-primary', '#F9FAFB')
      root.style.setProperty('--text-secondary', '#9CA3AF')
      root.style.setProperty('--border-color', '#334155')
      root.style.setProperty('--sidebar-bg', '#1E293B')
      root.style.setProperty('--sidebar-text', '#CBD5E1')
      root.style.setProperty('--sidebar-active-bg', 'rgba(59, 130, 246, 0.2)')
      root.style.setProperty('--header-bg', '#1E293B')
    } else {
      // 浅色主题变量
      root.style.setProperty('--bg-color', '#F8FAFC')
      root.style.setProperty('--bg-card', '#FFFFFF')
      root.style.setProperty('--bg-hover', '#F1F5F9')
      root.style.setProperty('--text-primary', '#1F2937')
      root.style.setProperty('--text-secondary', '#6B7280')
      root.style.setProperty('--border-color', '#E5E7EB')
      root.style.setProperty('--sidebar-bg', '#FFFFFF')
      root.style.setProperty('--sidebar-text', '#374151')
      root.style.setProperty('--sidebar-active-bg', '#EFF6FF')
      root.style.setProperty('--header-bg', '#FFFFFF')
    }
  }

  // 切换侧边栏折叠状态
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('adminplus-sidebar-collapsed', String(sidebarCollapsed.value))
  }

  // 初始化主题
  function init() {
    // 读取保存的主题设置
    const savedMode = localStorage.getItem('adminplus-theme-mode') as ThemeMode | null
    const savedPrimaryColor = localStorage.getItem('adminplus-primary-color')
    const savedCollapsed = localStorage.getItem('adminplus-sidebar-collapsed')

    // 应用主题
    if (savedMode) {
      setTheme(savedMode)
    } else {
      setTheme(defaultThemeConfig.mode)
    }

    // 应用主色调
    if (savedPrimaryColor) {
      setPrimaryColor(savedPrimaryColor)
    }

    // 应用侧边栏状态
    if (savedCollapsed) {
      sidebarCollapsed.value = savedCollapsed === 'true'
    }
  }

  return {
    // 状态
    mode,
    primaryColor,
    sidebarCollapsed,
    // 计算属性
    isDark,
    themeClass,
    // 方法
    setTheme,
    toggleTheme,
    setPrimaryColor,
    toggleSidebar,
    init
  }
})