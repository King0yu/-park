import axios from 'axios'

/**
 * 小区停车场管理系统 - 停车场管理模块 API 封装
 * 包含停车区域、停车位管理接口
 */

const API_PREFIX = '/api'

const service = axios.create({
  baseURL: API_PREFIX,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加权限信息
service.interceptors.request.use(
  (config) => {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        // 确保角色是数字类型
        const role = parseInt(user.role)
        config.headers['X-User-Id'] = user.id
        config.headers['X-User-Role'] = isNaN(role) ? '' : role.toString()
        console.log('请求头已添加角色信息:', role)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)
    const message = error.response?.data?.msg || error.message || '网络错误，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

/**
 * 停车区域 API
 */
const areaApi = {
  /**
   * 分页查询停车区域列表
   */
  getAreaList(params) {
    return service.get('/area/list', { params })
  },

  /**
   * 根据ID查询停车区域
   */
  getAreaById(id) {
    return service.get(`/area/${id}`)
  },

  /**
   * 新增停车区域
   */
  addArea(data) {
    return service.post('/area/add', data)
  },

  /**
   * 编辑停车区域
   */
  updateArea(data) {
    return service.put('/area/edit', data)
  },

  /**
   * 删除停车区域
   */
  deleteArea(id) {
    return service.delete(`/area/${id}`)
  }
}

/**
 * 停车位 API
 */
const spaceApi = {
  /**
   * 分页查询停车位列表
   */
  getSpaceList(params) {
    return service.get('/space/list', { params })
  },

  /**
   * 根据ID查询停车位
   */
  getSpaceById(id) {
    return service.get(`/space/${id}`)
  },

  /**
   * 根据停车区域ID查询停车位
   */
  getSpacesByAreaId(areaId) {
    return service.get(`/space/area/${areaId}`)
  },

  /**
   * 新增停车位
   */
  addSpace(data) {
    return service.post('/space/add', data)
  },

  /**
   * 编辑停车位
   */
  updateSpace(data) {
    return service.put('/space/edit', data)
  },

  /**
   * 删除停车位
   */
  deleteSpace(id) {
    return service.delete(`/space/${id}`)
  },

  /**
   * 停车位状态统计（管理端数据大屏）
   */
  getSpaceStatistics() {
    return service.get('/space/statistics')
  }
}

export default {  areaApi,  spaceApi
}
