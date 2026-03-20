/**
 * Format timestamp to relative time string
 * @param timestamp - ISO timestamp string
 * @returns Formatted relative time string (e.g., "2 hours ago", "3 days ago")
 */
export function formatTime(timestamp: string): string {
  const now = new Date()
  const time = new Date(timestamp)
  const diffMs = now.getTime() - time.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`

  // For older dates, show the actual date
  return time.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: time.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
  })
}

/**
 * Get activity color scheme based on activity type
 * @param type - Activity type ('create' | 'update' | 'delete' | 'login')
 * @returns Object with background, text, and border colors
 */
export function getActivityColor(
  type: string
): { bg: string; text: string; border: string } {
  const colors: Record<string, { bg: string; text: string; border: string }> = {
    create: {
      bg: 'rgb(220 252 231)',
      text: 'rgb(22 101 52)',
      border: 'rgb(187 247 208)'
    },
    update: {
      bg: 'rgb(219 234 254)',
      text: 'rgb(30 64 175)',
      border: 'rgb(191 219 254)'
    },
    delete: {
      bg: 'rgb(254 226 226)',
      text: 'rgb(185 28 28)',
      border: 'rgb(254 202 202)'
    },
    login: {
      bg: 'rgb(254 249 195)',
      text: 'rgb(113 63 18)',
      border: 'rgb(254 240 138)'
    }
  }

  return colors[type] || colors.login
}
