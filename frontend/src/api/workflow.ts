import { get, post, put, del } from '@/utils/request'
import type {
  WorkflowAddSign,
  WorkflowApproval,
  WorkflowCc,
  WorkflowDefinition,
  WorkflowDefinitionReq,
  WorkflowDetail,
  WorkflowDraftDetail,
  WorkflowFormValues,
  WorkflowInstance,
  WorkflowNode,
  WorkflowNodeReq,
  WorkflowUrge
} from '@/types'

interface ApprovalPayload {
  comment: string
  attachments?: string
  targetNodeId?: string
}

export interface StartWorkflowPayload {
  definitionId: string
  title: string
  formData?: WorkflowFormValues
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

export function getWorkflowDraftDetail(instanceId: string) {
  return get<WorkflowDraftDetail>(`/workflow/instances/${instanceId}/draft`)
}

export function updateWorkflowDraft(instanceId: string, data: StartWorkflowPayload) {
  return put<WorkflowInstance>(`/workflow/instances/${instanceId}/draft`, data)
}

export function submitWorkflow(instanceId: string, data: StartWorkflowPayload) {
  return post<WorkflowInstance>(`/workflow/instances/${instanceId}/submit`, data)
}

export function deleteWorkflowDraft(instanceId: string) {
  return del<void>(`/workflow/instances/${instanceId}/draft`)
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

export function rollbackWorkflow(instanceId: string, data: ApprovalPayload) {
  return post<WorkflowInstance>(`/workflow/instances/${instanceId}/rollback`, data)
}

export function getRollbackableNodes(instanceId: string) {
  return get<WorkflowNode[]>(`/workflow/instances/${instanceId}/rollbackable-nodes`)
}

// ========== Workflow Definition Management ==========

export function createWorkflowDefinition(data: WorkflowDefinitionReq) {
  return post<WorkflowDefinition>('/workflow/definitions', data)
}

export function updateWorkflowDefinition(id: string, data: WorkflowDefinitionReq) {
  return put<WorkflowDefinition>(`/workflow/definitions/${id}`, data)
}

export function deleteWorkflowDefinition(id: string) {
  return del<void>(`/workflow/definitions/${id}`)
}

export function getWorkflowDefinition(id: string) {
  return get<WorkflowDefinition>(`/workflow/definitions/${id}`)
}

// ========== Workflow Node Management ==========

export function getWorkflowNodes(definitionId: string) {
  return get<WorkflowNode[]>(`/workflow/definitions/${definitionId}/nodes`)
}

export function createWorkflowNode(definitionId: string, data: WorkflowNodeReq) {
  return post<WorkflowNode>(`/workflow/definitions/${definitionId}/nodes`, data)
}

export function updateWorkflowNode(nodeId: string, data: WorkflowNodeReq) {
  return put<WorkflowNode>(`/workflow/definitions/nodes/${nodeId}`, data)
}

export function deleteWorkflowNode(nodeId: string) {
  return del<void>(`/workflow/definitions/nodes/${nodeId}`)
}

// ========== Workflow CC (Carbon Copy) Management ==========

export function getMyCcRecords() {
  return get<WorkflowCc[]>('/workflow/cc/my')
}

export function getMyUnreadCcRecords() {
  return get<WorkflowCc[]>('/workflow/cc/my/unread')
}

export function countMyUnreadCcRecords() {
  return get<number>('/workflow/cc/my/unread/count')
}

export function getInstanceCcRecords(instanceId: string) {
  return get<WorkflowCc[]>(`/workflow/cc/instance/${instanceId}`)
}

export function markCcAsRead(ccId: string) {
  return put<void>(`/workflow/cc/${ccId}/read`)
}

export function markCcAsReadBatch(ccIds: string[]) {
  return put<void>('/workflow/cc/read-batch', ccIds)
}

// ========== Workflow Urge Management ==========

export function urgeWorkflow(instanceId: string, data: { content: string; targetApproverId?: string }) {
  return post<void>(`/workflow/urge/${instanceId}`, data)
}

export function getReceivedUrgeRecords() {
  return get<WorkflowUrge[]>('/workflow/urge/received')
}

export function getSentUrgeRecords() {
  return get<WorkflowUrge[]>('/workflow/urge/sent')
}

export function getUnreadUrgeRecords() {
  return get<WorkflowUrge[]>('/workflow/urge/unread')
}

export function countUnreadUrgeRecords() {
  return get<number>('/workflow/urge/unread/count')
}

export function getInstanceUrgeRecords(instanceId: string) {
  return get<WorkflowUrge[]>(`/workflow/urge/instance/${instanceId}`)
}

export function markUrgeAsRead(urgeId: string) {
  return put<void>(`/workflow/urge/${urgeId}/read`)
}

export function markUrgeAsReadBatch(urgeIds: string[]) {
  return put<void>('/workflow/urge/read-batch', urgeIds)
}

// ========== Workflow Add Sign Management ==========

export function addSignWorkflow(instanceId: string, data: { addUserId: string; addType: 'BEFORE' | 'AFTER' | 'TRANSFER'; reason: string }) {
  return post<WorkflowAddSign>(`/workflow/instances/${instanceId}/add-sign`, data)
}

export function getAddSignRecords(instanceId: string) {
  return get<WorkflowAddSign[]>(`/workflow/instances/${instanceId}/add-sign-records`)
}
