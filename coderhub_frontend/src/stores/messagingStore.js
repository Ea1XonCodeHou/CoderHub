/**
 * messagingStore — 私信聊天状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import chatApi from '@/api/chatApi'

export const useMessagingStore = defineStore('messaging', () => {
  const unreadCount = ref(0)
  const conversations = ref([])
  const activeConvId = ref(null)
  const messages = ref({}) // convId → ChatMsgVO[]
  const loading = ref(false)

  // ==================== 未读计数 ====================

  async function fetchUnreadCount() {
    try {
      const res = await chatApi.getUnreadCount()
      if (res.code === 1) unreadCount.value = res.data || 0
    } catch {
      // 静默失败
    }
  }

  function decrementUnread(amount = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - amount)
  }

  // ==================== 会话列表 ====================

  async function fetchConversations() {
    loading.value = true
    try {
      const res = await chatApi.getConversations()
      if (res.code === 1) {
        conversations.value = res.data || []
        // 同步未读总数
        unreadCount.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
      }
    } finally {
      loading.value = false
    }
  }

  async function getOrCreateConversation(targetUserId) {
    const res = await chatApi.getOrCreateConversation(targetUserId)
    if (res.code === 1) return res.data
    return null
  }

  // ==================== 消息 ====================

  async function fetchMessages(convId) {
    const res = await chatApi.getMessages(convId)
    if (res.code === 1) {
      messages.value[convId] = res.data || []
    }
    return messages.value[convId] || []
  }

  /** 追加一条实时收到的消息（WebSocket 推送） */
  function appendMessage(convId, msg) {
    if (!messages.value[convId]) messages.value[convId] = []
    // 防重（ACK 和 MESSAGE 可能有相同 id）
    const exists = messages.value[convId].some(m => m.id === msg.id)
    if (!exists) messages.value[convId].push(msg)

    // 更新会话预览
    const conv = conversations.value.find(c => c.id === convId)
    if (conv) {
      conv.lastMessage = msg.content.length > 50 ? msg.content.slice(0, 50) + '…' : msg.content
      conv.lastMsgAt = msg.createdAt
      if (!msg.isMine) conv.unreadCount = (conv.unreadCount || 0) + 1
    }
  }

  async function markAsRead(convId) {
    const conv = conversations.value.find(c => c.id === convId)
    const unread = conv?.unreadCount || 0
    if (unread === 0) return
    await chatApi.markAsRead(convId)
    if (conv) conv.unreadCount = 0
    decrementUnread(unread)
  }

  function setActiveConv(convId) {
    activeConvId.value = convId
  }

  function reset() {
    unreadCount.value = 0
    conversations.value = []
    activeConvId.value = null
    messages.value = {}
  }

  return {
    unreadCount,
    conversations,
    activeConvId,
    messages,
    loading,
    fetchUnreadCount,
    fetchConversations,
    getOrCreateConversation,
    fetchMessages,
    appendMessage,
    markAsRead,
    setActiveConv,
    reset
  }
})
