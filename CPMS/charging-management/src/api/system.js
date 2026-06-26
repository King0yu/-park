import request from '@/utils/request.js'

/**
 * 系统设置模块 API 封装
 * 使用 src/utils/request.js 统一请求实例
 */

/**
 * 获取停车场系统配置
 * GET /api/system/parkConfig
 * @returns {Promise}
 */
export function getParkConfig() {
  return request.get('/system/parkConfig')
}

/**
 * 更新停车场系统配置
 * PUT /api/system/parkConfig
 * @param {Object} data - 系统配置
 * @param {string} data.systemName - 系统名称
 * @param {string} data.copyright - 版权标语
 * @returns {Promise}
 */
export function updateParkConfig(data) {
  return request.put('/system/parkConfig', data)
}
