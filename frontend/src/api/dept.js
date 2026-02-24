import request from '@/utils/request'

export const getDeptTree = () => {
  return request({
    url: '/v1/sys/depts/tree',
    method: 'get'
  })
}

export const getDeptById = (id) => {
  return request({
    url: `/v1/sys/depts/${id}`,
    method: 'get'
  })
}

export const createDept = (data) => {
  return request({
    url: '/v1/sys/depts',
    method: 'post',
    data
  })
}

export const updateDept = (id, data) => {
  return request({
    url: `/v1/sys/depts/${id}`,
    method: 'put',
    data
  })
}

export const deleteDept = (id) => {
  return request({
    url: `/v1/sys/depts/${id}`,
    method: 'delete'
  })
}
