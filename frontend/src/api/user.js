import request from '@/utils/request'

export function login(params) {
  return request({
    url: '/login',
    method: 'post',
    params
  })
}

export function getInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function getSocial() {
  return request({
    url: '/user/social',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'delete'
  })
}
