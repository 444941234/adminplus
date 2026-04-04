import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  logError,
  logWarn,
  logInfo,
  logDebug,
  getLogBuffer,
  clearLogBuffer,
  withErrorHandling,
  withSilentError
} from '@/utils/logger'

// Mock console methods
const originalConsole = {
  error: console.error,
  warn: console.warn,
  log: console.log
}

describe('logger', () => {
  beforeEach(() => {
    clearLogBuffer()
    console.error = vi.fn()
    console.warn = vi.fn()
    console.log = vi.fn()
  })

  afterEach(() => {
    console.error = originalConsole.error
    console.warn = originalConsole.warn
    console.log = originalConsole.log
  })

  describe('logError', () => {
    it('should add error entry to buffer', () => {
      const error = new Error('Test error')
      logError('Something failed', error, 'TestComponent')

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('error')
      expect(buffer[0].message).toBe('Something failed')
      expect(buffer[0].context).toBe('TestComponent')
      expect(buffer[0].error).toBe(error)
    })

    it('should work without error object', () => {
      logError('Generic error')

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].message).toBe('Generic error')
      expect(buffer[0].error).toBeUndefined()
    })
  })

  describe('logWarn', () => {
    it('should add warn entry to buffer', () => {
      logWarn('Warning message', 'TestContext')

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('warn')
      expect(buffer[0].message).toBe('Warning message')
      expect(buffer[0].context).toBe('TestContext')
    })
  })

  describe('logInfo', () => {
    it('should add info entry to buffer', () => {
      logInfo('Info message')

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('info')
      expect(buffer[0].message).toBe('Info message')
    })
  })

  describe('logDebug', () => {
    it('should add debug entry to buffer', () => {
      logDebug('Debug message')

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('debug')
    })
  })

  describe('getLogBuffer', () => {
    it('should return copy of buffer', () => {
      logError('Error 1')
      logError('Error 2')

      const buffer1 = getLogBuffer()
      const buffer2 = getLogBuffer()

      expect(buffer1).not.toBe(buffer2) // Different array references
      expect(buffer1).toHaveLength(2)
      expect(buffer2).toHaveLength(2)
    })
  })

  describe('clearLogBuffer', () => {
    it('should clear all entries from buffer', () => {
      logError('Error 1')
      logError('Error 2')
      expect(getLogBuffer()).toHaveLength(2)

      clearLogBuffer()

      expect(getLogBuffer()).toHaveLength(0)
    })
  })

  describe('withErrorHandling', () => {
    it('should return result on success', async () => {
      const result = await withErrorHandling(
        async () => 'success',
        'Operation failed'
      )

      expect(result).toBe('success')
      expect(getLogBuffer()).toHaveLength(0)
    })

    it('should return null and log error on failure', async () => {
      const error = new Error('Network error')
      const result = await withErrorHandling(
        async () => { throw error },
        'Operation failed',
        'TestContext'
      )

      expect(result).toBeNull()
      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('error')
      expect(buffer[0].message).toBe('Operation failed')
      expect(buffer[0].context).toBe('TestContext')
    })
  })

  describe('withSilentError', () => {
    it('should complete successfully on success', async () => {
      let called = false
      await withSilentError(
        async () => { called = true },
        'Operation failed'
      )

      expect(called).toBe(true)
      expect(getLogBuffer()).toHaveLength(0)
    })

    it('should log error but not throw on failure', async () => {
      const error = new Error('Silent error')
      await withSilentError(
        async () => { throw error },
        'Silent operation failed',
        'TestContext'
      )

      const buffer = getLogBuffer()
      expect(buffer).toHaveLength(1)
      expect(buffer[0].level).toBe('error')
      expect(buffer[0].message).toBe('Silent operation failed')
    })
  })

  describe('buffer overflow', () => {
    it('should limit buffer to MAX_LOG_BUFFER entries', () => {
      // Add more than MAX_LOG_BUFFER (100) entries
      for (let i = 0; i < 150; i++) {
        logInfo(`Message ${i}`)
      }

      const buffer = getLogBuffer()
      expect(buffer.length).toBe(100)
      // Oldest entries should be removed
      expect(buffer[0].message).toBe('Message 50')
      expect(buffer[99].message).toBe('Message 149')
    })
  })
})