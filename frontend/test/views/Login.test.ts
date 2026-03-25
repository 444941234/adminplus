import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import Login from '@/views/Login.vue'
import { useUserStore } from '@/stores/user'

// Mock the router module
vi.mock('@/router', () => ({
  ensureDynamicRoutes: vi.fn().mockResolvedValue(undefined)
}))

// Mock vue-sonner
vi.mock('vue-sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  }
}))

describe('Login Page', () => {
  let router: ReturnType<typeof createRouter>
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper
  let userStore: ReturnType<typeof useUserStore>

  beforeEach(() => {
    // Clear localStorage
    localStorage.clear()

    // Create router and pinia instances
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/dashboard', component: { template: '<div>Dashboard</div>' } }
      ]
    })

    pinia = createPinia()
    setActivePinia(pinia)

    // Get user store
    userStore = useUserStore()

    // Clear all mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    localStorage.clear()
  })

  describe('Page Structure', () => {
    it('should render login page with proper structure', () => {
      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      expect(wrapper.find('.min-h-screen').exists()).toBe(true)
    })

    it('should have form fields', () => {
      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      expect(wrapper.vm.form.username).toBeDefined()
      expect(wrapper.vm.form.password).toBeDefined()
      expect(wrapper.vm.form.captchaCode).toBeDefined()
    })
  })

  describe('Captcha Functionality', () => {
    it('should fetch captcha on mount', async () => {
      userStore.fetchCaptcha = vi.fn().mockResolvedValue({
        captchaId: 'test-captcha-id',
        captchaImage: 'data:image/png;base64,test'
      })

      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      await nextTick()

      expect(userStore.fetchCaptcha).toHaveBeenCalled()
    })
  })

  describe('Form Validation', () => {
    beforeEach(async () => {
      userStore.captcha = {
        captchaId: 'test-captcha-id',
        captchaImage: 'data:image/png;base64,test'
      }

      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      await nextTick()
    })

    it('should show warning when username is empty', async () => {
      const { toast } = await import('vue-sonner')

      await wrapper.vm.handleLogin()

      expect(toast.warning).toHaveBeenCalledWith('请输入用户名')
    })

    it('should show warning when password is empty', async () => {
      wrapper.vm.form.username = 'admin'

      const { toast } = await import('vue-sonner')
      await wrapper.vm.handleLogin()

      expect(toast.warning).toHaveBeenCalledWith('请输入密码')
    })

    it('should show warning when captcha code is empty', async () => {
      wrapper.vm.form.username = 'admin'
      wrapper.vm.form.password = 'password123'

      const { toast } = await import('vue-sonner')
      await wrapper.vm.handleLogin()

      expect(toast.warning).toHaveBeenCalledWith('请输入验证码')
    })
  })

  describe('Login Functionality', () => {
    beforeEach(async () => {
      userStore.captcha = {
        captchaId: 'test-captcha-id',
        captchaImage: 'data:image/png;base64,test'
      }

      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      wrapper.vm.form.username = 'admin'
      wrapper.vm.form.password = 'admin123'
      wrapper.vm.form.captchaCode = '1234'

      await nextTick()
    })

    it('should login successfully with valid credentials', async () => {
      const mockLoginResp = {
        token: 'jwt-token',
        refreshToken: 'refresh-token',
        user: { id: '1', username: 'admin' }
      }
      userStore.login = vi.fn().mockResolvedValue({ code: 200, message: 'success', data: mockLoginResp } as any)

      const { toast } = await import('vue-sonner')

      await wrapper.vm.handleLogin()
      await nextTick()

      expect(userStore.login).toHaveBeenCalledWith('admin', 'admin123', 'test-captcha-id', '1234')
      expect(toast.success).toHaveBeenCalled()
    })

    it('should handle login failure', async () => {
      userStore.login = vi.fn().mockRejectedValue(new Error('用户名或密码错误'))
      userStore.fetchCaptcha = vi.fn().mockResolvedValue({
        captchaId: 'new-captcha-id',
        captchaImage: 'data:image/png;base64,new'
      })

      const { toast } = await import('vue-sonner')

      await wrapper.vm.handleLogin()

      expect(toast.error).toHaveBeenCalledWith('用户名或密码错误')
    })

    it('should prevent duplicate login requests', async () => {
      let resolveLogin: (value: any) => void
      userStore.login = vi.fn().mockImplementation(() => new Promise((resolve) => {
        resolveLogin = resolve
      }))

      const firstLogin = wrapper.vm.handleLogin()
      const secondLogin = wrapper.vm.handleLogin()

      expect(wrapper.vm.loading).toBe(true)

      // Cleanup
      wrapper.vm.loading = false
      resolveLogin({ code: 200, message: 'success', data: {} })
    })
  })

  describe('Captcha Refresh', () => {
    it('should refresh captcha when requested', async () => {
      userStore.fetchCaptcha = vi.fn().mockResolvedValue({
        captchaId: 'new-captcha-id',
        captchaImage: 'data:image/png;base64,new'
      })

      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      await wrapper.vm.refreshCaptcha()

      expect(userStore.fetchCaptcha).toHaveBeenCalled()
      expect(wrapper.vm.form.captchaCode).toBe('')
    })

    it('should handle captcha fetch failure', async () => {
      userStore.fetchCaptcha = vi.fn().mockRejectedValue(new Error('Network error'))

      wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })

      await wrapper.vm.refreshCaptcha()

      const { toast } = await import('vue-sonner')
      expect(toast.error).toHaveBeenCalledWith('获取验证码失败')
    })
  })
})
