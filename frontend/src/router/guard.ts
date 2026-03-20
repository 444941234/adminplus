export interface GuardDecisionInput {
  hasToken: boolean
  isLoginPage: boolean
  isPublicRoute: boolean
  hasMatchedRoutes: boolean
}

export type GuardDecision = 'redirect-login' | 'redirect-dashboard' | 'ensure-routes' | 'retry-navigation' | 'allow'

export const decideGuardAction = ({
  hasToken,
  isLoginPage,
  isPublicRoute,
  hasMatchedRoutes
}: GuardDecisionInput): GuardDecision => {
  if (!hasToken && !isPublicRoute) {
    return 'redirect-login'
  }

  if (isLoginPage && hasToken) {
    return 'redirect-dashboard'
  }

  if (hasToken && !hasMatchedRoutes) {
    return 'retry-navigation'
  }

  if (hasToken) {
    return 'ensure-routes'
  }

  return 'allow'
}
