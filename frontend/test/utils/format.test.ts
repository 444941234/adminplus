import { describe, expect, it } from 'vitest'
import { formatDateTime } from '@/utils/format'

describe('format', () => {
  // =========================================================================
  // 1. formatDateTime
  // =========================================================================
  describe('formatDateTime', () => {
    it('returns "-" for null or undefined values', () => {
      expect(formatDateTime(null)).toBe('-')
      expect(formatDateTime(undefined)).toBe('-')
    })

    it('returns "-" for empty string', () => {
      expect(formatDateTime('')).toBe('-')
    })

    it('formats valid ISO date strings', () => {
      const result = formatDateTime('2026-04-03T12:30:45Z')
      expect(result).toMatch(/\d{4}\/\d{1,2}\/\d{1,2}/) // Matches date format
      expect(result).not.toBe('-')
    })

    it('formats dates with zh-CN locale', () => {
      const result = formatDateTime('2026-04-03T12:30:00Z')
      // zh-CN format typically uses YYYY/MM/DD or similar
      expect(result).toBeDefined()
      expect(typeof result).toBe('string')
    })

    it('uses 24-hour format (hour12: false)', () => {
      const result = formatDateTime('2026-04-03T14:30:00Z')
      // 24-hour format should not contain AM/PM
      expect(result).not.toMatch(/AM|PM/)
      // Should contain a time-like pattern
      expect(result).toMatch(/\d{1,2}:\d{2}:\d{2}/)
    })

    it('handles various ISO date formats', () => {
      const date1 = formatDateTime('2026-04-03T12:00:00.000Z')
      const date2 = formatDateTime('2026-04-03T12:00:00+08:00')
      const date3 = formatDateTime('2026-04-03')

      expect(date1).not.toBe('-')
      expect(date2).not.toBe('-')
      expect(date3).not.toBe('-')
    })

    it('handles edge case dates', () => {
      // Unix epoch
      const epoch = formatDateTime('1970-01-01T00:00:00Z')
      expect(epoch).not.toBe('-')

      // Far future date
      const future = formatDateTime('2099-12-31T23:59:59Z')
      expect(future).not.toBe('-')
    })
  })
})
