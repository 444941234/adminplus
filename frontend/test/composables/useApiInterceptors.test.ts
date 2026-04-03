import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError } from 'axios'
import {
  isCanceledError,
  showErrorToast,
  clearPendingRequests
} from '@/composables/useApiInterceptors'

// Mock vue-sonner
const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

// Mock window.location - use jsdom's default
describe('useApiInterceptors', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Clear localStorage
    localStorage.clear()
    // Clear pending requests by calling clear function
    clearPendingRequests()
  })

  // =========================================================================
  // 1. isCanceledError
  // =========================================================================
  describe('isCanceledError', () => {
    it('returns true for Error with canceled message', () => {
      const error = new Error('canceled')
      expect(isCanceledError(error)).toBe(true)
    })

    it('returns true for Error with CanceledError name', () => {
      const error = new Error('test')
      error.name = 'CanceledError'
      expect(isCanceledError(error)).toBe(true)
    })

    it('returns true for object with ERR_CANCELED code', () => {
      const error = { code: 'ERR_CANCELED' }
      expect(isCanceledError(error)).toBe(true)
    })

    it('returns false for other errors', () => {
      const error = new Error('network error')
      expect(isCanceledError(error)).toBe(false)
    })

    it('returns false for null', () => {
      expect(isCanceledError(null)).toBe(false)
    })

    it('returns false for undefined', () => {
      expect(isCanceledError(undefined)).toBe(false)
    })
  })

  // =========================================================================
  // 2. showErrorToast
  // =========================================================================
  describe('showErrorToast', () => {
    it('does not show toast for canceled errors', () => {
      const error = new Error('canceled')
      showErrorToast(error)

      expect(toastMocks.error).not.toHaveBeenCalled()
    })

    it('shows toast for Axios errors with message', () => {
      const axiosError: any = {
        response: {
          data: { message: 'API Error' },
          status: 400
        },
        message: 'fallback message'
      }
      showErrorToast(axiosError)

      expect(toastMocks.error).toHaveBeenCalledWith('API Error')
    })

    it('shows toast for Axios errors without data message', () => {
      const axiosError: any = {
        response: { status: 500 },
        message: 'Network Error'
      }
      showErrorToast(axiosError)

      expect(toastMocks.error).toHaveBeenCalledWith('Network Error')
    })

    it('does not show toast for 401 errors (handled by interceptor)', () => {
      const axiosError: any = {
        response: { status: 401 },
        message: 'Unauthorized'
      }
      showErrorToast(axiosError)

      expect(toastMocks.error).not.toHaveBeenCalled()
    })

    it('shows toast for generic errors', () => {
      const error = new Error('Something went wrong')
      showErrorToast(error)

      expect(toastMocks.error).toHaveBeenCalledWith('Something went wrong')
    })

    it('uses fallback message for non-Error objects', () => {
      showErrorToast('string error', 'Custom fallback')

      expect(toastMocks.error).toHaveBeenCalledWith('Custom fallback')
    })

    it('uses default fallback when not provided', () => {
      showErrorToast({})

      expect(toastMocks.error).toHaveBeenCalledWith('网络错误，请稍后重试')
    })
  })

  // =========================================================================
  // 3. clearPendingRequests
  // =========================================================================
  describe('clearPendingRequests', () => {
    it('clears all pending requests without throwing', () => {
      expect(() => clearPendingRequests()).not.toThrow()
    })
  })
})

