import axios from 'axios'

/**
 * 用户模块 API 封装
 * 基于 axios 封装，请求前缀 /api
 */

const API_PREFIX = '/api'

/**
 * 创建 axios 实例
 * 可在此处统一配置请求拦截器、响应拦截器等
 */
const service = axios.create({
  baseURL: API_PREFIX,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // TODO: 在此处添加 token 等认证信息
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers['Authorization'] = `Bearer ${token}`
    // }
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
    // 根据后端统一返回格式处理
    if (res.code === 200) {
      return res
    }
    // 处理业务错误
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)
    const message = error.response?.data?.msg || error.message || '网络错误，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

/**
 * 用户相关 API
 */
const userApi = {
  /**
   * 分页查询用户列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码（默认1）
   * @param {number} params.pageSize - 每页数量（默认10）
   * @param {string} [params.username] - 用户名（模糊查询）
   * @param {string} [params.name] - 姓名（模糊查询）
   * @param {number} [params.role] - 角色（0-超级管理员(总管理)，1-管理员(物业)，2-普通用户(车主)）
   * @param {number} [params.status] - 状态（0-禁用，1-启用）
   * @returns {Promise}
   */
  getUserList(params) {
    return service.get('/user/list', { params })
  },

  /**
   * 根据ID查询用户
   * @param {number} id - 用户ID
   * @returns {Promise}
   */
  getUserById(id) {
    return service.get(`/user/${id}`)
  },

  /**
   * 新增用户
   * @param {Object} data - 用户信息
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @param {string} data.name - 姓名
   * @param {string} data.phone - 手机号
   * @param {string} [data.email] - 邮箱
   * @param {number} data.role - 角色
   * @param {number} data.status - 状态
   * @returns {Promise}
   */
  addUser(data) {
    return service.post('/user/add', data)
  },

  /**
   * 编辑用户
   * @param {Object} data - 用户信息
   * @param {number} data.id - 用户ID
   * @param {string} [data.password] - 密码（可选，不修改则为空）
   * @param {string} data.name - 姓名
   * @param {string} data.phone - 手机号
   * @param {string} [data.email] - 邮箱
   * @param {number} data.role - 角色
   * @param {number} data.status - 状态
   * @returns {Promise}
   */
  updateUser(data) {
    return service.put('/user/edit', data)
  },

  /**
   * 删除用户
   * @param {number} id - 用户ID
   * @returns {Promise}
   */
  deleteUser(id) {
    return service.delete(`/user/${id}`)
  },

  /**
   * 批量删除用户
   * @param {Array<number>} ids - 用户ID数组
   * @returns {Promise}
   */
  batchDeleteUser(ids) {
    return service.delete('/user/batch', { data: ids })
  },

  /**
   * 检查用户名是否已存在
   * @param {string} username - 用户名
   * @returns {Promise}
   */
  checkUsername(username) {
    return service.get('/user/check/username', { params: { username } })
  },

  /**
   * 检查手机号是否已存在
   * @param {string} phone - 手机号
   * @returns {Promise}
   */
  checkPhone(phone) {
    return service.get('/user/check/phone', { params: { phone } })
  }
}

export default userApi
