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

// 存储最近的日志条目（用于调试）
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

  // 在开发环境输出到控制台
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

/**
 * 记录错误信息
 *
 * @param message 错误描述
 * @param error 可选的错误对象
 * @param context 可选的上下文（如组件名或功能名）
 *
 * @example
 * ```ts
 * try {
 *   await fetchData()
 * } catch (error) {
 *   logError('获取数据失败', error as Error, 'MyComponent')
 * }
 * ```
 */
export function logError(message: string, error?: Error, context?: string): void {
  addLogEntry('error', message, context, error)
}

/**
 * 记录警告信息
 *
 * @param message 警告描述
 * @param context 可选的上下文
 *
 * @example
 * ```ts
 * if (!data) {
 *   logWarn('数据为空，使用默认值', 'MyComponent')
 * }
 * ```
 */
export function logWarn(message: string, context?: string): void {
  addLogEntry('warn', message, context)
}

/**
 * 记录信息日志
 *
 * @param message 信息描述
 * @param context 可选的上下文
 */
export function logInfo(message: string, context?: string): void {
  addLogEntry('info', message, context)
}

/**
 * 记录调试信息（仅在开发环境）
 *
 * @param message 调试描述
 * @param context 可选的上下文
 */
export function logDebug(message: string, context?: string): void {
  if (import.meta.env.DEV) {
    addLogEntry('debug', message, context)
  }
}

/**
 * 获取日志缓冲区（用于调试）
 */
export function getLogBuffer(): readonly LogEntry[] {
  return [...logBuffer]
}

/**
 * 清空日志缓冲区
 */
export function clearLogBuffer(): void {
  logBuffer.length = 0
}

/**
 * 错误处理包装器
 *
 * 自动捕获错误并记录日志，适用于异步操作
 *
 * @param fn 要执行的异步函数
 * @param errorMessage 错误时的日志消息
 * @param context 上下文信息
 * @returns Promise<T | null> - 成功返回结果，失败返回 null
 *
 * @example
 * ```ts
 * const data = await withErrorHandling(
 *   () => fetchData(),
 *   '获取数据失败',
 *   'MyComponent'
 * )
 * if (data) {
 *   // 处理数据
 * }
 * ```
 */
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

/**
 * 静默错误处理包装器
 *
 * 用于那些失败不影响主要功能的场景，只记录日志不抛出错误
 *
 * @param fn 要执行的异步函数
 * @param errorMessage 错误时的日志消息
 * @param context 上下文信息
 *
 * @example
 * ```ts
 * // 获取可选数据，失败不影响主流程
 * await withSilentError(
 *   () => fetchOptionalData(),
 *   '获取可选数据失败',
 *   'MyComponent'
 * )
 * ```
 */
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