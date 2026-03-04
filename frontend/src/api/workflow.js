import request from '@/utils/request'

/**
 * 工作流定义 API
 */

/**
 * 创建工作流定义
 * @param {Object} data - 工作流定义数据
 * @returns {Promise}
 */
export const createWorkflowDefinition = (data) => {
  return request({
    url: '/v1/workflow/definitions',
    method: 'post',
    data
  })
}

/**
 * 更新工作流定义
 * @param {String} id - 工作流定义ID
 * @param {Object} data - 工作流定义数据
 * @returns {Promise}
 */
export const updateWorkflowDefinition = (id, data) => {
  return request({
    url: `/v1/workflow/definitions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除工作流定义
 * @param {String} id - 工作流定义ID
 * @returns {Promise}
 */
export const deleteWorkflowDefinition = (id) => {
  return request({
    url: `/v1/workflow/definitions/${id}`,
    method: 'delete'
  })
}

/**
 * 查询工作流定义详情
 * @param {String} id - 工作流定义ID
 * @returns {Promise}
 */
export const getWorkflowDefinition = (id) => {
  return request({
    url: `/v1/workflow/definitions/${id}`,
    method: 'get'
  })
}

/**
 * 查询所有工作流定义
 * @returns {Promise}
 */
export const listWorkflowDefinitions = () => {
  return request({
    url: '/v1/workflow/definitions',
    method: 'get'
  })
}

/**
 * 查询启用的工作流定义
 * @returns {Promise}
 */
export const listEnabledWorkflowDefinitions = () => {
  return request({
    url: '/v1/workflow/definitions/enabled',
    method: 'get'
  })
}

/**
 * 添加工作流节点
 * @param {String} definitionId - 工作流定义ID
 * @param {Object} data - 节点数据
 * @returns {Promise}
 */
export const addWorkflowNode = (definitionId, data) => {
  return request({
    url: `/v1/workflow/definitions/${definitionId}/nodes`,
    method: 'post',
    data
  })
}

/**
 * 更新工作流节点
 * @param {String} nodeId - 节点ID
 * @param {Object} data - 节点数据
 * @returns {Promise}
 */
export const updateWorkflowNode = (nodeId, data) => {
  return request({
    url: `/v1/workflow/definitions/nodes/${nodeId}`,
    method: 'put',
    data
  })
}

/**
 * 删除工作流节点
 * @param {String} nodeId - 节点ID
 * @returns {Promise}
 */
export const deleteWorkflowNode = (nodeId) => {
  return request({
    url: `/v1/workflow/definitions/nodes/${nodeId}`,
    method: 'delete'
  })
}

/**
 * 查询工作流的所有节点
 * @param {String} definitionId - 工作流定义ID
 * @returns {Promise}
 */
export const listWorkflowNodes = (definitionId) => {
  return request({
    url: `/v1/workflow/definitions/${definitionId}/nodes`,
    method: 'get'
  })
}

/**
 * 工作流实例 API
 */

/**
 * 创建工作流草稿
 * @param {Object} data - 工作流数据
 * @returns {Promise}
 */
export const createWorkflowDraft = (data) => {
  return request({
    url: '/v1/workflow/instances/draft',
    method: 'post',
    data
  })
}

/**
 * 提交工作流
 * @param {String} instanceId - 工作流实例ID
 * @returns {Promise}
 */
export const submitWorkflow = (instanceId) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/submit`,
    method: 'post'
  })
}

/**
 * 发起并提交工作流
 * @param {Object} data - 工作流数据
 * @returns {Promise}
 */
export const startWorkflow = (data) => {
  return request({
    url: '/v1/workflow/instances/start',
    method: 'post',
    data
  })
}

/**
 * 查询工作流详情
 * @param {String} instanceId - 工作流实例ID
 * @returns {Promise}
 */
export const getWorkflowDetail = (instanceId) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}`,
    method: 'get'
  })
}

/**
 * 查询我发起的工作流
 * @param {String} status - 状态筛选（可选）
 * @returns {Promise}
 */
export const getMyWorkflows = (status) => {
  return request({
    url: '/v1/workflow/instances/my',
    method: 'get',
    params: { status }
  })
}

/**
 * 查询待我审批的工作流
 * @returns {Promise}
 */
export const getPendingApprovals = () => {
  return request({
    url: '/v1/workflow/instances/pending',
    method: 'get'
  })
}

/**
 * 统计待审批数量
 * @returns {Promise}
 */
export const countPendingApprovals = () => {
  return request({
    url: '/v1/workflow/instances/pending/count',
    method: 'get'
  })
}

/**
 * 同意审批
 * @param {String} instanceId - 工作流实例ID
 * @param {Object} data - 审批意见
 * @returns {Promise}
 */
export const approveWorkflow = (instanceId, data) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/approve`,
    method: 'post',
    data
  })
}

/**
 * 拒绝审批
 * @param {String} instanceId - 工作流实例ID
 * @param {Object} data - 审批意见
 * @returns {Promise}
 */
export const rejectWorkflow = (instanceId, data) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/reject`,
    method: 'post',
    data
  })
}

/**
 * 取消工作流
 * @param {String} instanceId - 工作流实例ID
 * @returns {Promise}
 */
export const cancelWorkflow = (instanceId) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/cancel`,
    method: 'post'
  })
}

/**
 * 撤回工作流
 * @param {String} instanceId - 工作流实例ID
 * @returns {Promise}
 */
export const withdrawWorkflow = (instanceId) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/withdraw`,
    method: 'post'
  })
}

/**
 * 查询审批记录
 * @param {String} instanceId - 工作流实例ID
 * @returns {Promise}
 */
export const getWorkflowApprovals = (instanceId) => {
  return request({
    url: `/v1/workflow/instances/${instanceId}/approvals`,
    method: 'get'
  })
}
