import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { useAsyncAction } from '@/composables/useAsyncAction'

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

vi.mock('@/composables/useApiInterceptors', () => ({
  showErrorToast: vi.fn((error: Error, message: string) => {
    toastMocks.error(message)
  })
}))

describe('useAsyncAction', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // =========================================================================
  // 1. Initial State
  // =========================================================================
  describe('Initial State', () => {
    it('returns loading ref initialized to false', () => {
      const { loading } = useAsyncAction()
      expect(loading.value).toBe(false)
    })

    it('returns run function', () => {
      const { run } = useAsyncAction()
      expect(typeof run).toBe('function')
    })
  })

  // =========================================================================
  // 2. Successful Execution
  // =========================================================================
  describe('Successful Execution', () => {
    it('sets loading to true during execution', async () => {
      const { loading, run } = useAsyncAction()

      let loadingDuringExecution = false
      const promise = run(async () => {
        loadingDuringExecution = loading.value
        await Promise.resolve()
        return 'result'
      })

      expect(loading.value).toBe(true)
      await promise
      expect(loadingDuringExecution).toBe(true)
      expect(loading.value).toBe(false)
    })

    it('returns the result', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => 'test-result')

      expect(result).toBe('test-result')
    })

    it('shows success toast when provided', async () => {
      const { run } = useAsyncAction()
      await run(async () => 'result', { successMessage: '操作成功' })

      expect(toastMocks.success).toHaveBeenCalledWith('操作成功')
    })

    it('does not show toast when successMessage not provided', async () => {
      const { run } = useAsyncAction()
      await run(async () => 'result')

      expect(toastMocks.success).not.toHaveBeenCalled()
    })

    it('calls onSuccess callback with result', async () => {
      const onSuccess = vi.fn()
      const { run } = useAsyncAction()

      await run(async () => 'result', { onSuccess })

      expect(onSuccess).toHaveBeenCalledWith('result')
    })

    it('resets loading to false after success', async () => {
      const { loading, run } = useAsyncAction()

      await run(async () => 'result')

      expect(loading.value).toBe(false)
    })
  })

  // =========================================================================
  // 3. Error Handling
  // =========================================================================
  describe('Error Handling', () => {
    it('shows error toast on failure', async () => {
      const { run } = useAsyncAction()

      await run(async () => {
        throw new Error('API Error')
      })

      expect(toastMocks.error).toHaveBeenCalledWith('操作失败')
    })

    it('uses custom error message when provided', async () => {
      const { run } = useAsyncAction()

      await run(async () => {
        throw new Error('API Error')
      }, { errorMessage: '自定义错误' })

      expect(toastMocks.error).toHaveBeenCalledWith('自定义错误')
    })

    it('calls onError callback with error', async () => {
      const onError = vi.fn()
      const { run } = useAsyncAction()
      const testError = new Error('Test Error')

      await run(async () => {
        throw testError
      }, { onError })

      expect(onError).toHaveBeenCalledWith(testError)
    })

    it('returns undefined on error', async () => {
      const { run } = useAsyncAction()

      const result = await run(async () => {
        throw new Error('Error')
      })

      expect(result).toBeUndefined()
    })

    it('resets loading to false after error', async () => {
      const { loading, run } = useAsyncAction()

      await run(async () => {
        throw new Error('Error')
      })

      expect(loading.value).toBe(false)
    })
  })

  // =========================================================================
  // 4. Default Error Message
  // =========================================================================
  describe('Default Error Message', () => {
    it('uses provided default error message', async () => {
      const { run } = useAsyncAction('默认错误')

      await run(async () => {
        throw new Error('Test')
      })

      expect(toastMocks.error).toHaveBeenCalledWith('默认错误')
    })

    it('falls back to default when no custom message', async () => {
      const { run } = useAsyncAction('默认错误')

      await run(async () => {
        throw new Error('Test')
      }, { errorMessage: undefined })

      expect(toastMocks.error).toHaveBeenCalledWith('默认错误')
    })
  })

  // =========================================================================
  // 5. Multiple Sequential Calls
  // =========================================================================
  describe('Multiple Sequential Calls', () => {
    it('handles sequential calls correctly', async () => {
      const { loading, run } = useAsyncAction()

      await run(async () => 'first')
      expect(loading.value).toBe(false)

      await run(async () => 'second')
      expect(loading.value).toBe(false)
    })

    it('handles error then success', async () => {
      const { loading, run } = useAsyncAction()

      await run(async () => {
        throw new Error('Error')
      })
      expect(loading.value).toBe(false)

      const result = await run(async () => 'success')
      expect(result).toBe('success')
      expect(loading.value).toBe(false)
    })
  })

  // =========================================================================
  // 6. Different Return Types
  // =========================================================================
  describe('Different Return Types', () => {
    it('handles string return type', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => 'string result')

      expect(result).toBe('string result')
    })

    it('handles object return type', async () => {
      const { run } = useAsyncAction()
      const obj = { id: 1, name: 'test' }
      const result = await run(async () => obj)

      expect(result).toEqual(obj)
    })

    it('handles array return type', async () => {
      const { run } = useAsyncAction()
      const arr = [1, 2, 3]
      const result = await run(async () => arr)

      expect(result).toEqual(arr)
    })

    it('handles void return type', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => {
        // void function
      })

      expect(result).toBeUndefined()
    })

    it('handles null return type', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => null)

      expect(result).toBeNull()
    })

    it('handles number return type', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => 42)

      expect(result).toBe(42)
    })

    it('handles boolean return type', async () => {
      const { run } = useAsyncAction()
      const result = await run(async () => true)

      expect(result).toBe(true)
    })
  })
})