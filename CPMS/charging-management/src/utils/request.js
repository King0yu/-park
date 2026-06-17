import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * axios 实例配置
 * 统一管理所有 API 请求
 */

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  (config) => {
    // 添加 token 到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加用户ID和角色到请求头
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        if (user.id) {
          config.headers['X-User-Id'] = user.id
        }
        if (user.role !== undefined && user.role !== null) {
          config.headers['X-User-Role'] = user.role
        }
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

/**
 * 响应拦截器
 */
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // code 为 200 表示成功
    if (res.code === 200) {
      return res
    }
    // 其他业务错误
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)
    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = error.response.data?.msg || '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          // 清除本地存储并跳转登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          message = '没有访问权限'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.msg || '请求失败'
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
