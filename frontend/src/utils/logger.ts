/**
 * 应用程序错误日志工具
 *
 * 统一的错误处理和日志记录，用于替代空的 catch 块
 */

type LogLevel = 'error' | 'warn' | 'info' | 'debug'

interface LogEntry {
  timestamp: string
  level: LogLevel
  message: string
  context?: string
  error?: Error
}

const logBuffer: LogEntry[] = []
const MAX_LOG_BUFFER = 100

function formatLogEntry(entry: LogEntry): string {
  const prefix = `[${entry.timestamp}] [${entry.level.toUpperCase()}]`
  const context = entry.context ? ` [${entry.context}]` : ''
  return `${prefix}${context} ${entry.message}`
}

function addLogEntry(level: LogLevel, message: string, context?: string, error?: Error): void {
  const entry: LogEntry = {
    timestamp: new Date().toISOString(),
    level,
    message,
    context,
    error
  }

  logBuffer.push(entry)
  if (logBuffer.length > MAX_LOG_BUFFER) {
    logBuffer.shift()
  }

  if (import.meta.env.DEV) {
    const formatted = formatLogEntry(entry)
    switch (level) {
      case 'error':
        console.error(formatted, error || '')
        break
      case 'warn':
        console.warn(formatted)
        break
      default:
        console.log(formatted)
    }
  }
}

export function logError(message: string, error?: Error, context?: string): void {
  addLogEntry('error', message, context, error)
}

export function logWarn(message: string, context?: string): void {
  addLogEntry('warn', message, context)
}

export function logInfo(message: string, context?: string): void {
  addLogEntry('info', message, context)
}

export function logDebug(message: string, context?: string): void {
  if (import.meta.env.DEV) {
    addLogEntry('debug', message, context)
  }
}

export function getLogBuffer(): readonly LogEntry[] {
  return logBuffer
}

export function clearLogBuffer(): void {
  logBuffer.length = 0
}

export async function withErrorHandling<T>(
  fn: () => Promise<T>,
  errorMessage: string,
  context?: string
): Promise<T | null> {
  try {
    return await fn()
  } catch (error) {
    logError(errorMessage, error as Error, context)
    return null
  }
}

export async function withSilentError(
  fn: () => Promise<void>,
  errorMessage: string,
  context?: string
): Promise<void> {
  try {
    await fn()
  } catch (error) {
    logError(errorMessage, error as Error, context)
  }
}
