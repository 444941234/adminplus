import request from '@/utils/request'

/**
 * 获取字典列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 关键字（字典类型/名称）
 * @returns {Promise<Object>} 字典列表数据
 */
export const getDictList = (params) => {
  return request({
    url: '/sys/dicts',
    method: 'get',
    params
  })
}

/**
 * 根据字典类型查询
 * @param {string} dictType - 字典类型
 * @returns {Promise<Object>} 字典信息
 */
export const getDictByType = (dictType) => {
  return request({
    url: `/sys/dicts/type/${dictType}`,
    method: 'get'
  })
}

/**
 * 根据ID查询字典
 * @param {number} id - 字典ID
 * @returns {Promise<Object>} 字典信息
 */
export const getDictById = (id) => {
  return request({
    url: `/sys/dicts/${id}`,
    method: 'get'
  })
}

/**
 * 创建��典
 * @param {Object} data - 字典信息
 * @param {string} data.dictType - 字典类型
 * @param {string} data.dictName - 字典名称
 * @param {number} data.status - 状态（1-启用，0-禁用）
 * @param {string} data.remark - 备注
 * @returns {Promise<Object>} 创建的字典信息
 */
export const createDict = (data) => {
  return request({
    url: '/sys/dicts',
    method: 'post',
    data
  })
}

/**
 * 更新字典
 * @param {number} id - 字典ID
 * @param {Object} data - 字典信息
 * @returns {Promise<Object>} 更新后的字典信息
 */
export const updateDict = (id, data) => {
  return request({
    url: `/sys/dicts/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除字典
 * @param {number} id - 字典ID
 * @returns {Promise<void>}
 */
export const deleteDict = (id) => {
  return request({
    url: `/sys/dicts/${id}`,
    method: 'delete'
  })
}

/**
 * 更新字典状态
 * @param {number} id - 字典ID
 * @param {number} status - 状态（1-启用，0-禁用）
 * @returns {Promise<void>}
 */
export const updateDictStatus = (id, status) => {
  return request({
    url: `/sys/dicts/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 获取字典项列表
 * @param {number} dictId - 字典ID
 * @returns {Promise<Object>} 字典项列表数据
 */
export const getDictItems = (dictId) => {
  return request({
    url: `/sys/dicts/${dictId}/items`,
    method: 'get'
  })
}

/**
 * 获取字典项树形结构
 * @param {number} dictId - 字典ID
 * @returns {Promise<Object[]>} 字典项树形结构
 */
export const getDictItemTree = (dictId) => {
  return request({
    url: `/sys/dicts/${dictId}/items/tree`,
    method: 'get'
  })
}

/**
 * 根据字典类型查询字典项
 * @param {string} dictType - 字典类型
 * @returns {Promise<Object[]>} 字典项列表
 */
export const getDictItemsByType = (dictType) => {
  return request({
    url: `/sys/dicts/type/${dictType}/items`,
    method: 'get'
  })
}

/**
 * 创建字典项
 * @param {number} dictId - 字典ID
 * @param {Object} data - 字典项信息
 * @param {number} data.parentId - 父节点ID
 * @param {string} data.label - 字典标签
 * @param {string} data.value - 字典值
 * @param {number} data.sortOrder - 排序
 * @param {number} data.status - 状态（1-启用，0-禁用）
 * @returns {Promise<Object>} 创建的字典项信息
 */
export const createDictItem = (dictId, data) => {
  return request({
    url: `/sys/dicts/${dictId}/items`,
    method: 'post',
    data
  })
}

/**
 * 更新字典项
 * @param {number} dictId - 字典ID
 * @param {number} id - 字典项ID
 * @param {Object} data - 字典项信息
 * @returns {Promise<Object>} 更新后的字典项信息
 */
export const updateDictItem = (dictId, id, data) => {
  return request({
    url: `/sys/dicts/${dictId}/items/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除字典项
 * @param {number} dictId - 字典ID
 * @param {number} id - 字典项ID
 * @returns {Promise<void>}
 */
export const deleteDictItem = (dictId, id) => {
  return request({
    url: `/sys/dicts/${dictId}/items/${id}`,
    method: 'delete'
  })
}

/**
 * 更新字典项状态
 * @param {number} dictId - 字典ID
 * @param {number} id - 字典项ID
 * @param {number} status - 状态（1-启用，0-禁用）
 * @returns {Promise<void>}
 */
export const updateDictItemStatus = (dictId, id, status) => {
  return request({
    url: `/sys/dicts/${dictId}/items/${id}/status`,
    method: 'put',
    params: { status }
  })
}