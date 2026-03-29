import { ref } from 'vue'
import { toast } from 'vue-sonner'

/**
 * 异步操作 composable — 统一 try-catch + toast + loading
 *
 * 消灭 58 处手写 `catch (error) { const message = ... ; toast.error(message) }` 样板
 */
export function useAsyncAction(defaultErrorMessage = '操作失败') {
  const loading = ref(false)

  const run = async <T>(
    fn: () => Promise<T>,
    options?: {
      errorMessage?: string
      onSuccess?: (result: T) => void
      onError?: (error: Error) => void
      successMessage?: string
    }
  ): Promise<T | undefined> => {
    loading.value = true
    try {
      const result = await fn()
      if (options?.successMessage) {
        toast.success(options.successMessage)
      }
      options?.onSuccess?.(result)
      return result
    } catch (error) {
      const message = error instanceof Error ? error.message : (options?.errorMessage ?? defaultErrorMessage)
      toast.error(message)
      options?.onError?.(error instanceof Error ? error : new Error(message))
      return undefined
    } finally {
      loading.value = false
    }
  }

  return { loading, run }
}
