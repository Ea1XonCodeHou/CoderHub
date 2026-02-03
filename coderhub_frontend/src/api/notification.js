import axios from 'axios'

// 创建axios实例（复用 user.js 的配置模式）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.authentication = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 1) {
      return res
    } else {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/'
    }
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// ==================== 消息通知相关接口 ====================

/**
 * 获取未读消息列表（含未读计数）
 * @returns {Promise} { unreadCount: number, notifications: Array }
 */
export function getUnreadNotifications() {
  return request({
    url: '/user/notification/unread',
    method: 'get'
  })
}

/**
 * 标记消息已读（已读即删除）
 * @param {number} id 消息ID
 */
export function markAsRead(id) {
  return request({
    url: `/user/notification/${id}/read`,
    method: 'post'
  })
}

/**
 * 一键清空所有未读消息
 */
export function clearAllNotifications() {
  return request({
    url: '/user/notification/clear',
    method: 'delete'
  })
}

/**
 * 获取未读消息数量（轻量接口）
 * @returns {Promise} number
 */
export function getUnreadCount() {
  return request({
    url: '/user/notification/unread-count',
    method: 'get'
  })
}

