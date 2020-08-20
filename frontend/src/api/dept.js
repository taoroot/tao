import request from '@/utils/request'

export function getTree() {
  return request({
    url: '/depts',
    method: 'get'
  })
}

export function delItem(ids) {
  return request({
    url: `/dept`,
    method: 'delete',
    params: {
      ids: ids
    }
  })
}

export function saveItem(data) {
  return request({
    url: '/dept',
    method: 'post',
    data
  })
}

export function updateItem(data) {
  return request({
    url: `/dept`,
    method: 'put',
    data
  })
}

export const getItemById = (id) => {
  return request({
    url: `/dept/${id}`,
    method: 'get'
  })
}
