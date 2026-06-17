import axios from 'axios'

/**
 * 登录模块 API 封装
 */

const API_PREFIX = '/api'

/**
 * 创建 axios 实例
 */
const service = axios.create({
  baseURL: API_PREFIX,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 登录
 * @param {Object} data - 登录参数
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function login(data) {
  return service.post('/user/login', data)
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return service.get('/user/info')
}

/**
 * 退出登录
 * @returns {Promise}
 */
export function logout() {
  return service.post('/user/logout')
}

/**
 * 用户注册
 * @param {Object} data - 注册参数
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.phone - 手机号
 * @returns {Promise}
 */
export function register(data) {
  return service.post('/user/register', data)
}

export default {
  login,
  getUserInfo,
  logout,
  register
}
