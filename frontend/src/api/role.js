import request from '@/utils/request'

export function getPage() {
  return request({
    url: '/roles',
    method: 'get'
  })
}

export function delItem(ids) {
  return request({
    url: `/role`,
    method: 'delete',
    params: {
      ids: ids
    }
  })
}

export function saveItem(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateItem(data) {
  return request({
    url: `/role`,
    method: 'put',
    data
  })
}

export function updatePermission(roleId, menuIds) {
  return request({
    url: `/role/${roleId}/authorities`,
    method: 'put',
    params: {
      authorityIds: menuIds
    }
  })
}

export function getPermission(roleId) {
  return request({
    url: `/role/${roleId}/authorities`,
    method: 'get'
  })
}
