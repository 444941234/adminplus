import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getLogList,
  getLogById,
  deleteLog,
  deleteLogsBatch,
  deleteLogsByCondition,
  cleanupExpiredLogs,
  getLogStatistics,
  exportLogExcel,
  exportLogCsv
} from '@/api/log'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  del: vi.fn()
}))

import { get, post, del } from '@/utils/request'

describe('Log API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getLogList', () => {
    it('should fetch log list with filters', async () => {
      const mockLogs = {
        records: [
          { id: '1', username: 'admin', module: '用户管理', operation: '查询', status: 1 },
          { id: '2', username: 'user1', module: '角色管理', operation: '新增', status: 1 }
        ],
        total: 2
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockLogs })

      const params = {
        page: 1,
        size: 10,
        logType: 1,
        username: 'admin',
        module: '用户管理'
      }
      const result = await getLogList(params)

      expect(get).toHaveBeenCalledWith('/sys/logs', params)
      expect(result.data).toEqual(mockLogs)
    })

    it('should fetch logs with date range', async () => {
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: { records: [], total: 0 } })

      const params = {
        startTime: '2026-01-01',
        endTime: '2026-12-31'
      }
      await getLogList(params)

      expect(get).toHaveBeenCalledWith('/sys/logs', params)
    })
  })

  describe('getLogById', () => {
    it('should fetch log by id', async () => {
      const mockLog = {
        id: '1',
        username: 'admin',
        module: '用户管理',
        operation: '查询',
        status: 1,
        requestIp: '192.168.1.1',
        requestTime: '2026-03-20 10:00:00'
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockLog })

      const result = await getLogById('1')

      expect(get).toHaveBeenCalledWith('/sys/logs/1')
      expect(result.data).toEqual(mockLog)
    })
  })

  describe('deleteLog', () => {
    it('should delete a log', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteLog('1')

      expect(del).toHaveBeenCalledWith('/sys/logs/1')
      expect(result.code).toBe(200)
    })
  })

  describe('deleteLogsBatch', () => {
    it('should batch delete logs', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await deleteLogsBatch(['1', '2', '3'])

      expect(del).toHaveBeenCalledWith('/sys/logs/batch', ['1', '2', '3'])
      expect(result.code).toBe(200)
    })
  })

  describe('deleteLogsByCondition', () => {
    it('should delete logs by condition', async () => {
      vi.mocked(del).mockResolvedValue({ code: 200, message: 'success', data: 5 })

      const condition = {
        logType: 1,
        username: 'admin',
        startTime: '2026-01-01',
        endTime: '2026-12-31'
      }
      const result = await deleteLogsByCondition(condition)

      expect(del).toHaveBeenCalledWith('/sys/logs/condition', condition)
      expect(result.data).toBe(5)
    })
  })

  describe('cleanupExpiredLogs', () => {
    it('should cleanup expired logs', async () => {
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: 100 })

      const result = await cleanupExpiredLogs()

      expect(post).toHaveBeenCalledWith('/sys/logs/cleanup')
      expect(result.data).toBe(100)
    })
  })

  describe('getLogStatistics', () => {
    it('should fetch log statistics', async () => {
      const mockStats = {
        totalCount: 1000,
        loginCount: 300,
        operationCount: 700,
        todayCount: 50,
        successRate: 98.5
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockStats })

      const result = await getLogStatistics()

      expect(get).toHaveBeenCalledWith('/sys/logs/statistics')
      expect(result.data).toEqual(mockStats)
      expect(result.data.totalCount).toBe(1000)
      expect(result.data.successRate).toBe(98.5)
    })
  })

  describe('exportLogExcel', () => {
    it('should return excel export url', () => {
      const params = {
        logType: 1,
        username: 'admin',
        startTime: '2026-01-01',
        endTime: '2026-12-31'
      }
      const result = exportLogExcel(params)

      expect(result).toContain('/api/v1/sys/logs/export/excel?')
      expect(result).toContain('logType=1')
      expect(result).toContain('username=admin')
    })
  })

  describe('exportLogCsv', () => {
    it('should return csv export url', () => {
      const params = {
        logType: 1,
        module: '用户管理'
      }
      const result = exportLogCsv(params)

      expect(result).toContain('/api/v1/sys/logs/export/csv?')
      expect(result).toContain('logType=1')
      expect(result).toContain('module=')
    })
  })
})
