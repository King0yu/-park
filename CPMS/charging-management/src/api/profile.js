import request from '@/utils/request.js'

/**
 * 个人中心模块 API 封装
 * 使用 src/utils/request.js 统一请求实例
 */

/**
 * 获取当前登录用户信息
 * GET /api/user/current
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request.get('/user/current')
}

/**
 * 更新当前用户基本信息
 * PUT /api/user/updateInfo
 * @param {Object} data - 用户信息
 * @param {string} data.username - 用户名
 * @param {string} data.phone - 手机号
 * @returns {Promise}
 */
export function updateInfo(data) {
  return request.put('/user/updateInfo', data)
}

/**
 * 修改当前用户密码
 * PUT /api/user/updatePwd
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function updatePwd(data) {
  return request.put('/user/updatePwd', data)
}
