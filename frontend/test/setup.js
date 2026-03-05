import { config } from '@vue/test-utils'
import { vi } from 'vitest'
import ElementPlus from 'element-plus'

// 配置 Vue Test Utils
config.global.plugins = [ElementPlus]

// 模拟 localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock

// 模拟 window.location
Object.defineProperty(window, 'location', {
  value: {
    href: '',
    hash: '',
  },
  writable: true,
})