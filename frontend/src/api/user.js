import request from '@/utils/request'

export function login(params) {
  return request({
    url: '/login',
    method: 'post',
    params
  })
}

export function loginPhone(params) {
  return request({
    url: '/login/phone',
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

export function getSms(params) {
  return request({
    url: '/code/sms',
    method: 'get',
    params
  })
}

export function getSocial() {
  return request({
    url: '/user/socials',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'delete'
  })
}
