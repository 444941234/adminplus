import { describe, expect, it } from 'vitest'
import { decideGuardAction } from '@/router/guard'

describe('decideGuardAction', () => {
  it('redirects anonymous users away from protected pages', () => {
    expect(
      decideGuardAction({
        hasToken: false,
        requiresAuth: true,
        isLoginPage: false,
        hasMatchedRoutes: true
      })
    ).toBe('redirect-login')
  })

  it('redirects logged-in users away from login page', () => {
    expect(
      decideGuardAction({
        hasToken: true,
        requiresAuth: false,
        isLoginPage: true,
        hasMatchedRoutes: true
      })
    ).toBe('redirect-dashboard')
  })

  it('retries navigation after dynamic routes are loaded for unmatched targets', () => {
    expect(
      decideGuardAction({
        hasToken: true,
        requiresAuth: true,
        isLoginPage: false,
        hasMatchedRoutes: false
      })
    ).toBe('retry-navigation')
  })

  it('allows public pages for anonymous users', () => {
    expect(
      decideGuardAction({
        hasToken: false,
        requiresAuth: false,
        isLoginPage: false,
        hasMatchedRoutes: true
      })
    ).toBe('allow')
  })
})
