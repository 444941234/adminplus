import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getWorkflowDefinitions,
  getEnabledWorkflowDefinitions,
  getWorkflowDetail,
  getMyWorkflows,
  getPendingWorkflows,
  getPendingWorkflowCount,
  getWorkflowApprovals,
  startWorkflow,
  createWorkflowDraft,
  submitWorkflow,
  approveWorkflow,
  rejectWorkflow,
  cancelWorkflow,
  withdrawWorkflow
} from '@/api/workflow'

// Mock the request module
vi.mock('@/utils/request', () => ({
  get: vi.fn(),
  post: vi.fn()
}))

import { get, post } from '@/utils/request'

describe('Workflow API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getWorkflowDefinitions', () => {
    it('should fetch workflow definitions', async () => {
      const mockDefinitions = [
        { id: '1', name: '请假审批', key: 'leave', version: 1, enabled: true },
        { id: '2', name: '报销审批', key: 'expense', version: 1, enabled: true }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDefinitions })

      const result = await getWorkflowDefinitions()

      expect(get).toHaveBeenCalledWith('/workflow/definitions')
      expect(result.data).toEqual(mockDefinitions)
    })
  })

  describe('getEnabledWorkflowDefinitions', () => {
    it('should fetch enabled workflow definitions', async () => {
      const mockDefinitions = [
        { id: '1', name: '请假审批', key: 'leave', version: 1, enabled: true }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDefinitions })

      const result = await getEnabledWorkflowDefinitions()

      expect(get).toHaveBeenCalledWith('/workflow/definitions/enabled')
      expect(result.data).toEqual(mockDefinitions)
    })
  })

  describe('getWorkflowDetail', () => {
    it('should fetch workflow detail by instance id', async () => {
      const mockDetail = {
        instance: {
          id: '1',
          title: '请假申请',
          status: 'pending',
          createTime: '2026-03-20 10:00:00'
        },
        approvals: [
          { id: '1', approver: '张三', status: 'approved', comment: '同意' }
        ]
      }
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockDetail })

      const result = await getWorkflowDetail('1')

      expect(get).toHaveBeenCalledWith('/workflow/instances/1')
      expect(result.data).toEqual(mockDetail)
    })
  })

  describe('getMyWorkflows', () => {
    it('should fetch my workflows', async () => {
      const mockWorkflows = [
        { id: '1', title: '请假申请', status: 'pending', createTime: '2026-03-20 10:00:00' },
        { id: '2', title: '报销申请', status: 'approved', createTime: '2026-03-19 15:00:00' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockWorkflows })

      const result = await getMyWorkflows()

      expect(get).toHaveBeenCalledWith('/workflow/instances/my', undefined)
      expect(result.data).toEqual(mockWorkflows)
    })

    it('should fetch my workflows with status filter', async () => {
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: [] })

      await getMyWorkflows('pending')

      expect(get).toHaveBeenCalledWith('/workflow/instances/my', { status: 'pending' })
    })
  })

  describe('getPendingWorkflows', () => {
    it('should fetch pending workflows', async () => {
      const mockWorkflows = [
        { id: '1', title: '请假申请', status: 'pending', createTime: '2026-03-20 10:00:00' }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockWorkflows })

      const result = await getPendingWorkflows()

      expect(get).toHaveBeenCalledWith('/workflow/instances/pending')
      expect(result.data).toEqual(mockWorkflows)
    })
  })

  describe('getPendingWorkflowCount', () => {
    it('should fetch pending workflow count', async () => {
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: 5 })

      const result = await getPendingWorkflowCount()

      expect(get).toHaveBeenCalledWith('/workflow/instances/pending/count')
      expect(result.data).toBe(5)
    })
  })

  describe('getWorkflowApprovals', () => {
    it('should fetch workflow approvals', async () => {
      const mockApprovals = [
        { id: '1', approver: '张三', status: 'approved', comment: '同意', approveTime: '2026-03-20 11:00:00' },
        { id: '2', approver: '李四', status: 'pending', comment: null, approveTime: null }
      ]
      vi.mocked(get).mockResolvedValue({ code: 200, message: 'success', data: mockApprovals })

      const result = await getWorkflowApprovals('1')

      expect(get).toHaveBeenCalledWith('/workflow/instances/1/approvals')
      expect(result.data).toEqual(mockApprovals)
    })
  })

  describe('startWorkflow', () => {
    it('should start a new workflow', async () => {
      const payload = {
        definitionId: '1',
        title: '请假申请',
        businessData: '{"days":3}',
        remark: '家中有事'
      }
      const mockInstance = {
        id: '1',
        ...payload,
        status: 'pending',
        createTime: '2026-03-20 10:00:00'
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockInstance })

      const result = await startWorkflow(payload)

      expect(post).toHaveBeenCalledWith('/workflow/instances/start', payload)
      expect(result.data.title).toBe('请假申请')
    })
  })

  describe('createWorkflowDraft', () => {
    it('should create workflow draft', async () => {
      const payload = {
        definitionId: '1',
        title: '请假申请草稿',
        businessData: '{"days":3}'
      }
      const mockInstance = {
        id: '1',
        ...payload,
        status: 'draft',
        createTime: '2026-03-20 10:00:00'
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockInstance })

      const result = await createWorkflowDraft(payload)

      expect(post).toHaveBeenCalledWith('/workflow/instances/draft', payload)
      expect(result.data.status).toBe('draft')
    })
  })

  describe('submitWorkflow', () => {
    it('should submit workflow', async () => {
      const mockInstance = {
        id: '1',
        title: '请假申请',
        status: 'pending',
        submitTime: '2026-03-20 10:00:00'
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockInstance })

      const result = await submitWorkflow('1')

      expect(post).toHaveBeenCalledWith('/workflow/instances/1/submit')
      expect(result.data.status).toBe('pending')
    })
  })

  describe('approveWorkflow', () => {
    it('should approve workflow', async () => {
      const payload = {
        comment: '同意申请',
        attachments: null
      }
      const mockInstance = {
        id: '1',
        title: '请假申请',
        status: 'approved'
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockInstance })

      const result = await approveWorkflow('1', payload)

      expect(post).toHaveBeenCalledWith('/workflow/instances/1/approve', payload)
      expect(result.data.status).toBe('approved')
    })
  })

  describe('rejectWorkflow', () => {
    it('should reject workflow', async () => {
      const payload = {
        comment: '申请不符合要求',
        attachments: null
      }
      const mockInstance = {
        id: '1',
        title: '请假申请',
        status: 'rejected'
      }
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: mockInstance })

      const result = await rejectWorkflow('1', payload)

      expect(post).toHaveBeenCalledWith('/workflow/instances/1/reject', payload)
      expect(result.data.status).toBe('rejected')
    })
  })

  describe('cancelWorkflow', () => {
    it('should cancel workflow', async () => {
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await cancelWorkflow('1')

      expect(post).toHaveBeenCalledWith('/workflow/instances/1/cancel')
      expect(result.code).toBe(200)
    })
  })

  describe('withdrawWorkflow', () => {
    it('should withdraw workflow', async () => {
      vi.mocked(post).mockResolvedValue({ code: 200, message: 'success', data: undefined })

      const result = await withdrawWorkflow('1')

      expect(post).toHaveBeenCalledWith('/workflow/instances/1/withdraw')
      expect(result.code).toBe(200)
    })
  })
})
