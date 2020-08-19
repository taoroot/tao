import request from '@/utils/request'

// 查找导航菜单树
export const getTree = () => {
  return request({
    url: '/authorities',
    method: 'get'
  })
}

// 保存
export const saveAuthority = (data) => {
  return request({
    url: '/authority',
    method: 'post',
    data: data
  })
}
// 删除
export const deleteAuthority = (id) => {
  return request({
    url: '/authority/' + id,
    method: 'delete'
  })
}
// 查询
export const getAuthorityById = (id) => {
  return request({
    url: `/authority/${id}`,
    method: 'get'
  })
}

// 获取路由
export const getRouters = () => {
  return request({
    url: '/authority',
    method: 'get'
  })
}

// 获取菜单列表
export const getAuthoritys = () => {
  return request({
    url: '/authority/getAuthoritys',
    method: 'get'
  })
}

// 更新菜单
export function updateAuthority(data) {
  return request({
    url: '/authority',
    method: 'put',
    data: data
  })
}

