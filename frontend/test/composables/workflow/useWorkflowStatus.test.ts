import { describe, expect, it } from 'vitest'
import {
  getWorkflowStatusLabel,
  getWorkflowStatusVariant,
  isWorkflowTerminalStatus,
  canOperateWorkflow
} from '@/composables/workflow/useWorkflowStatus'

describe('useWorkflowStatus', () => {
  // =========================================================================
  // 1. getWorkflowStatusLabel
  // =========================================================================
  describe('getWorkflowStatusLabel', () => {
    it('returns correct labels for known statuses', () => {
      expect(getWorkflowStatusLabel('DRAFT')).toBe('草稿')
      expect(getWorkflowStatusLabel('RUNNING')).toBe('运行中')
      expect(getWorkflowStatusLabel('APPROVED')).toBe('已批准')
      expect(getWorkflowStatusLabel('REJECTED')).toBe('已拒绝')
      expect(getWorkflowStatusLabel('CANCELLED')).toBe('已取消')
      // 兼容状态码
      expect(getWorkflowStatusLabel('PENDING')).toBe('运行中')
      expect(getWorkflowStatusLabel('PROCESSING')).toBe('运行中')
      expect(getWorkflowStatusLabel('WITHDRAWN')).toBe('已取消')
      expect(getWorkflowStatusLabel('FINISHED')).toBe('已批准')
      expect(getWorkflowStatusLabel('COMPLETED')).toBe('已批准')
    })

    it('returns "-" for null or undefined status', () => {
      expect(getWorkflowStatusLabel(null)).toBe('-')
      expect(getWorkflowStatusLabel(undefined)).toBe('-')
    })

    it('returns "-" for empty string', () => {
      expect(getWorkflowStatusLabel('')).toBe('-')
    })

    it('returns the status string itself for unknown statuses', () => {
      expect(getWorkflowStatusLabel('UNKNOWN_STATUS')).toBe('UNKNOWN_STATUS')
      expect(getWorkflowStatusLabel('CUSTOM')).toBe('CUSTOM')
    })
  })

  // =========================================================================
  // 2. getWorkflowStatusVariant
  // =========================================================================
  describe('getWorkflowStatusVariant', () => {
    it('returns correct variants for known statuses', () => {
      expect(getWorkflowStatusVariant('DRAFT')).toBe('outline')
      expect(getWorkflowStatusVariant('PENDING')).toBe('secondary')
      expect(getWorkflowStatusVariant('PROCESSING')).toBe('default')
      expect(getWorkflowStatusVariant('APPROVED')).toBe('default')
      expect(getWorkflowStatusVariant('REJECTED')).toBe('destructive')
      expect(getWorkflowStatusVariant('CANCELLED')).toBe('outline')
      expect(getWorkflowStatusVariant('WITHDRAWN')).toBe('outline')
      expect(getWorkflowStatusVariant('FINISHED')).toBe('default')
      expect(getWorkflowStatusVariant('COMPLETED')).toBe('default')
    })

    it('returns "secondary" for null or undefined status', () => {
      expect(getWorkflowStatusVariant(null)).toBe('secondary')
      expect(getWorkflowStatusVariant(undefined)).toBe('secondary')
    })

    it('returns "secondary" for empty string', () => {
      expect(getWorkflowStatusVariant('')).toBe('secondary')
    })

    it('returns "secondary" for unknown statuses', () => {
      expect(getWorkflowStatusVariant('UNKNOWN')).toBe('secondary')
      expect(getWorkflowStatusVariant('CUSTOM')).toBe('secondary')
    })
  })

  // =========================================================================
  // 3. isWorkflowTerminalStatus
  // =========================================================================
  describe('isWorkflowTerminalStatus', () => {
    it('returns true for terminal statuses', () => {
      expect(isWorkflowTerminalStatus('APPROVED')).toBe(true)
      expect(isWorkflowTerminalStatus('REJECTED')).toBe(true)
      expect(isWorkflowTerminalStatus('CANCELLED')).toBe(true)
      expect(isWorkflowTerminalStatus('WITHDRAWN')).toBe(true)
      expect(isWorkflowTerminalStatus('FINISHED')).toBe(true)
      expect(isWorkflowTerminalStatus('COMPLETED')).toBe(true)
    })

    it('returns false for non-terminal statuses', () => {
      expect(isWorkflowTerminalStatus('DRAFT')).toBe(false)
      expect(isWorkflowTerminalStatus('PENDING')).toBe(false)
      expect(isWorkflowTerminalStatus('PROCESSING')).toBe(false)
    })

    it('returns false for null or undefined status', () => {
      expect(isWorkflowTerminalStatus(null)).toBe(false)
      expect(isWorkflowTerminalStatus(undefined)).toBe(false)
    })

    it('returns false for empty string', () => {
      expect(isWorkflowTerminalStatus('')).toBe(false)
    })

    it('returns false for unknown statuses', () => {
      expect(isWorkflowTerminalStatus('UNKNOWN')).toBe(false)
      expect(isWorkflowTerminalStatus('CUSTOM')).toBe(false)
    })
  })

  // =========================================================================
  // 4. canOperateWorkflow
  // =========================================================================
  describe('canOperateWorkflow', () => {
    it('returns false for terminal statuses', () => {
      expect(canOperateWorkflow('APPROVED')).toBe(false)
      expect(canOperateWorkflow('REJECTED')).toBe(false)
      expect(canOperateWorkflow('CANCELLED')).toBe(false)
      expect(canOperateWorkflow('WITHDRAWN')).toBe(false)
      expect(canOperateWorkflow('FINISHED')).toBe(false)
      expect(canOperateWorkflow('COMPLETED')).toBe(false)
    })

    it('returns true for non-terminal statuses', () => {
      expect(canOperateWorkflow('DRAFT')).toBe(true)
      expect(canOperateWorkflow('PENDING')).toBe(true)
      expect(canOperateWorkflow('PROCESSING')).toBe(true)
    })

    it('returns true for null or undefined status', () => {
      expect(canOperateWorkflow(null)).toBe(true)
      expect(canOperateWorkflow(undefined)).toBe(true)
    })

    it('returns true for empty string', () => {
      expect(canOperateWorkflow('')).toBe(true)
    })

    it('returns true for unknown statuses', () => {
      expect(canOperateWorkflow('UNKNOWN')).toBe(true)
      expect(canOperateWorkflow('CUSTOM')).toBe(true)
    })
  })

  // =========================================================================
  // 5. Integration Scenarios
  // =========================================================================
  describe('Integration Scenarios', () => {
    it('correctly identifies draft workflow as operable', () => {
      const status = 'DRAFT'
      expect(getWorkflowStatusLabel(status)).toBe('草稿')
      expect(getWorkflowStatusVariant(status)).toBe('outline')
      expect(isWorkflowTerminalStatus(status)).toBe(false)
      expect(canOperateWorkflow(status)).toBe(true)
    })

    it('correctly identifies approved workflow as terminal', () => {
      const status = 'APPROVED'
      expect(getWorkflowStatusLabel(status)).toBe('已批准')
      expect(getWorkflowStatusVariant(status)).toBe('default')
      expect(isWorkflowTerminalStatus(status)).toBe(true)
      expect(canOperateWorkflow(status)).toBe(false)
    })

    it('correctly identifies rejected workflow as terminal', () => {
      const status = 'REJECTED'
      expect(getWorkflowStatusLabel(status)).toBe('已拒绝')
      expect(getWorkflowStatusVariant(status)).toBe('destructive')
      expect(isWorkflowTerminalStatus(status)).toBe(true)
      expect(canOperateWorkflow(status)).toBe(false)
    })

    it('correctly identifies running workflow as operable', () => {
      const status = 'RUNNING'
      expect(getWorkflowStatusLabel(status)).toBe('运行中')
      expect(getWorkflowStatusVariant(status)).toBe('default')
      expect(isWorkflowTerminalStatus(status)).toBe(false)
      expect(canOperateWorkflow(status)).toBe(true)
    })
  })
})
