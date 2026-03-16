/**
 * 私信聊天 REST API
 */
import axios from 'axios'

const BASE = '/api/chat'

const chatApi = {
  /** 获取会话列表 */
  getConversations() {
    return axios.get(`${BASE}/conversations`).then(r => r.data)
  },

  /** 获取或创建与指定用户的会话，返回 conversationId */
  getOrCreateConversation(targetUserId) {
    return axios.post(`${BASE}/conversations/with/${targetUserId}`).then(r => r.data)
  },

  /** 获取历史消息（分页） */
  getMessages(convId, page = 1, pageSize = 50) {
    return axios.get(`${BASE}/conversations/${convId}/messages`, {
      params: { page, pageSize }
    }).then(r => r.data)
  },

  /** 标记已读 */
  markAsRead(convId) {
    return axios.post(`${BASE}/conversations/${convId}/read`).then(r => r.data)
  },

  /** 获取未读总数 */
  getUnreadCount() {
    return axios.get(`${BASE}/unread-count`).then(r => r.data)
  },

  /** 查询用户是否在线 */
  isUserOnline(userId) {
    return axios.get(`${BASE}/user/${userId}/online`).then(r => r.data)
  }
}

export default chatApi
