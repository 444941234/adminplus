import { describe, it, expect } from 'vitest'
import { formatTime, getActivityColor } from '@/utils/activityUtils'

describe('formatTime', () => {
  it('should return "Just now" for very recent timestamps', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 30000).toISOString() // 30 seconds ago
    expect(formatTime(timestamp)).toBe('Just now')
  })

  it('should return minutes ago for timestamps less than an hour', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 1800000).toISOString() // 30 minutes ago
    expect(formatTime(timestamp)).toBe('30 minutes ago')
  })

  it('should return singular "minute" for 1 minute ago', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 60000).toISOString() // 1 minute ago
    expect(formatTime(timestamp)).toBe('1 minute ago')
  })

  it('should return hours ago for timestamps less than a day', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 7200000).toISOString() // 2 hours ago
    expect(formatTime(timestamp)).toBe('2 hours ago')
  })

  it('should return singular "hour" for 1 hour ago', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 3600000).toISOString() // 1 hour ago
    expect(formatTime(timestamp)).toBe('1 hour ago')
  })

  it('should return days ago for timestamps less than a week', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 259200000).toISOString() // 3 days ago
    expect(formatTime(timestamp)).toBe('3 days ago')
  })

  it('should return singular "day" for 1 day ago', () => {
    const now = new Date()
    const timestamp = new Date(now.getTime() - 86400000).toISOString() // 1 day ago
    expect(formatTime(timestamp)).toBe('1 day ago')
  })

  it('should return formatted date for older timestamps', () => {
    const timestamp = '2024-01-15T10:30:00.000Z'
    const result = formatTime(timestamp)
    expect(result).toMatch(/Jan 15/)
  })

  it('should include year for dates from different years', () => {
    const timestamp = '2023-06-15T10:30:00.000Z'
    const result = formatTime(timestamp)
    expect(result).toMatch(/2023/)
  })
})

describe('getActivityColor', () => {
  it('should return green colors for create type', () => {
    const color = getActivityColor('create')
    expect(color.bg).toBe('rgb(220 252 231)')
    expect(color.text).toBe('rgb(22 101 52)')
    expect(color.border).toBe('rgb(187 247 208)')
  })

  it('should return blue colors for update type', () => {
    const color = getActivityColor('update')
    expect(color.bg).toBe('rgb(219 234 254)')
    expect(color.text).toBe('rgb(30 64 175)')
    expect(color.border).toBe('rgb(191 219 254)')
  })

  it('should return red colors for delete type', () => {
    const color = getActivityColor('delete')
    expect(color.bg).toBe('rgb(254 226 226)')
    expect(color.text).toBe('rgb(185 28 28)')
    expect(color.border).toBe('rgb(254 202 202)')
  })

  it('should return yellow colors for login type', () => {
    const color = getActivityColor('login')
    expect(color.bg).toBe('rgb(254 249 195)')
    expect(color.text).toBe('rgb(113 63 18)')
    expect(color.border).toBe('rgb(254 240 138)')
  })

  it('should return default login colors for unknown types', () => {
    const color = getActivityColor('unknown')
    expect(color.bg).toBe('rgb(254 249 195)')
    expect(color.text).toBe('rgb(113 63 18)')
    expect(color.border).toBe('rgb(254 240 138)')
  })

  it('should return an object with bg, text, and border properties', () => {
    const color = getActivityColor('create')
    expect(color).toHaveProperty('bg')
    expect(color).toHaveProperty('text')
    expect(color).toHaveProperty('border')
  })
})
