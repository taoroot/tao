import request from '@/utils/request'

export function login(params) {
  return request({
    url: '/login',
    method: 'post',
    params
  })
}

export function getInfo(token) {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'delete'
  })
}
