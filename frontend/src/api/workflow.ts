import { get, post } from '@/utils/request'
import type {
  WorkflowApproval,
  WorkflowDefinition,
  WorkflowDetail,
  WorkflowInstance
} from '@/types'

interface ApprovalPayload {
  comment: string
  attachments?: string
}

interface StartWorkflowPayload {
  definitionId: string
  title: string
  businessData?: string
  remark?: string
}

export function getWorkflowDefinitions() {
  return get<WorkflowDefinition[]>('/workflow/definitions')
}

export function getEnabledWorkflowDefinitions() {
  return get<WorkflowDefinition[]>('/workflow/definitions/enabled')
}

export function getWorkflowDetail(instanceId: string) {
  return get<WorkflowDetail>(`/workflow/instances/${instanceId}`)
}

export function getMyWorkflows(status?: string) {
  return get<WorkflowInstance[]>('/workflow/instances/my', status ? { status } : undefined)
}

export function getPendingWorkflows() {
  return get<WorkflowInstance[]>('/workflow/instances/pending')
}

export function getPendingWorkflowCount() {
  return get<number>('/workflow/instances/pending/count')
}

export function getWorkflowApprovals(instanceId: string) {
  return get<WorkflowApproval[]>(`/workflow/instances/${instanceId}/approvals`)
}

export function startWorkflow(data: StartWorkflowPayload) {
  return post<WorkflowInstance>('/workflow/instances/start', data)
}

export function createWorkflowDraft(data: StartWorkflowPayload) {
  return post<WorkflowInstance>('/workflow/instances/draft', data)
}

export function submitWorkflow(instanceId: string) {
  return post<WorkflowInstance>(`/workflow/instances/${instanceId}/submit`)
}

export function approveWorkflow(instanceId: string, data: ApprovalPayload) {
  return post<WorkflowInstance>(`/workflow/instances/${instanceId}/approve`, data)
}

export function rejectWorkflow(instanceId: string, data: ApprovalPayload) {
  return post<WorkflowInstance>(`/workflow/instances/${instanceId}/reject`, data)
}

export function cancelWorkflow(instanceId: string) {
  return post<void>(`/workflow/instances/${instanceId}/cancel`)
}

export function withdrawWorkflow(instanceId: string) {
  return post<void>(`/workflow/instances/${instanceId}/withdraw`)
}
