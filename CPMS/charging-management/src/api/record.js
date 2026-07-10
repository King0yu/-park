import axios from 'axios'

/**
 * 小区停车场管理系统 - 停车记录模块 API 封装
 * 包含停车记录管理接口
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
        const role = parseInt(user.role)
        config.headers['X-User-Id'] = user.id
        config.headers['X-User-Role'] = isNaN(role) ? '' : role.toString()
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
 * 停车记录 API
 */
const recordApi = {
  /**
   * 分页查询停车记录列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {number} [params.userId] - 用户ID（可选）
   * @param {number} [params.spaceId] - 停车位ID（可选）
   * @param {number} [params.status] - 记录状态（可选）
   * @param {string} [params.startTime] - 开始时间（可选）
   * @param {string} [params.endTime] - 结束时间（可选）
   */
  getRecordList(params) {
    return service.get('/record/page', { params })
  },

  /**
   * 根据ID查询停车记录详情
   * @param {number} id - 记录ID
   */
  getRecordById(id) {
    return service.get(`/record/${id}`)
  },

  /**
   * 获取当前用户的停车记录列表
   */
  getMyRecords() {
    return service.get('/record/my')
  },

  /**
   * 分页获取当前用户的停车记录（关联停车位名称）
   */
  getMyRecordsPage(params) {
    return service.get('/record/my/page', { params })
  },

  /**
   * 获取当前用户的进行中停车记录
   */
  getMyActiveRecords() {
    return service.get('/record/my/active')
  },

  /**
   * 获取当前用户的停车记录统计
   */
  getMyStatistics() {
    return service.get('/record/my/statistics')
  },

  /**
   * 新增停车记录
   * @param {Object} data - 停车记录数据
   */
  addRecord(data) {
    return service.post('/record', data)
  },

  /**
   * 结束停车（完成记录）
   * @param {number} id - 记录ID
   * @param {Object} data - 结束数据
   */
  finishRecord(id, data) {
    return service.put(`/record/finish/${id}`, data)
  },

  /**
   * 取消停车记录
   * @param {number} id - 记录ID
   */
  cancelRecord(id) {
    return service.put(`/record/cancel/${id}`)
  },

  /**
   * 删除停车记录
   * @param {number} id - 记录ID
   */
  deleteRecord(id) {
    return service.delete(`/record/${id}`)
  },

  /**
   * 模拟停车（自动创建停车记录）
   * 选择停车位后自动记录停车开始时间
   *
   * 业务规则：
   * - 入参：当前登录用户ID、停车位ID
   * - 新增记录状态为「已占用」，自动记录停车开始时间
   * - 数据存入park_record停车记录表，同时更新停车位状态为已占用
   *
   * @param {Object} data - 模拟停车参数
   * @param {number} data.spaceId - 停车位ID
   */
  simulateCreateRecord(data) {
    return service.post('/record/simulateCreate', data)
  },

  /**
   * 自动计算并结束停车
   * 根据停车时长自动计算停车费用
   *
   * 计算规则：
   * - 单价：5 元/小时
   * - 不满1小时向上取整
   *
   * @param {number} id - 记录ID
   */
  autoFinishRecord(id) {
    return service.put(`/record/autoFinish/${id}`)
  },

  /**
   * 今日统计（管理端数据大屏）
   */
  getTodayStatistics() {
    return service.get('/record/statistics/today')
  }
}

export default recordApi
