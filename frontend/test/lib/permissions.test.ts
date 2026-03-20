import { describe, expect, it } from 'vitest'
import { hasPermission } from '@/lib/permissions'

describe('hasPermission', () => {
  it('returns true when wildcard permission exists', () => {
    expect(hasPermission(['*'], 'user:delete')).toBe(true)
  })

  it('returns true for exact permission match', () => {
    expect(hasPermission(['workflow:approve', 'user:edit'], 'workflow:approve')).toBe(true)
  })

  it('returns false when permission is missing', () => {
    expect(hasPermission(['user:edit'], 'user:delete')).toBe(false)
  })
})
