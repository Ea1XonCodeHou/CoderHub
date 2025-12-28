/**
 * AI会话API - 对话持久化接口
 * 
 * @author CoderHub
 */
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token（header名称与后端配置一致：authentication）
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['authentication'] = token  // 后端期望的header名称
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 1) {
      return res.data
    }
    console.error('API错误:', res.msg)
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('网络错误:', error)
    return Promise.reject(error)
  }
)

// ==================== 会话管理 ====================

/**
 * 获取用户的会话列表
 */
export function getConversations() {
  return api.get('/ai/conversations')
}

/**
 * 创建新会话
 * @param {Object} data - { title, model }
 */
export function createConversation(data = {}) {
  return api.post('/ai/conversations', data)
}

/**
 * 获取会话详情（包含消息）
 * @param {string} id - 会话ID
 */
export function getConversation(id) {
  return api.get(`/ai/conversations/${id}`)
}

/**
 * 更新会话（标题、置顶等）
 * @param {string} id - 会话ID
 * @param {Object} data - { title, isPinned, isArchived }
 */
export function updateConversation(id, data) {
  return api.put(`/ai/conversations/${id}`, data)
}

/**
 * 删除会话
 * @param {string} id - 会话ID
 */
export function deleteConversation(id) {
  return api.delete(`/ai/conversations/${id}`)
}

// ==================== 消息管理 ====================

/**
 * 获取会话的消息列表
 * @param {string} conversationId - 会话ID
 */
export function getMessages(conversationId) {
  return api.get(`/ai/conversations/${conversationId}/messages`)
}

/**
 * 添加消息到会话
 * @param {string} conversationId - 会话ID
 * @param {Object} message - { role, content, toolCalls, recommendations, model, tokenCount, durationMs }
 */
export function addMessage(conversationId, message) {
  return api.post(`/ai/conversations/${conversationId}/messages`, message)
}

// ==================== 导出默认对象 ====================

export default {
  getConversations,
  createConversation,
  getConversation,
  updateConversation,
  deleteConversation,
  getMessages,
  addMessage
}

