import { defineStore } from 'pinia'
import { 
  getUnreadNotifications, 
  markAsRead, 
  clearAllNotifications 
} from '@/api/notification'

/**
 * 消息通知状态管理
 */
export const useNotificationStore = defineStore('notification', {
  state: () => ({
    // 未读消息数量
    unreadCount: 0,
    // 消息列表
    notifications: [],
    // 加载状态
    loading: false,
    // 轮询定时器ID
    pollingTimer: null,
    // 重试计数
    retryCount: 0,
    // 最大重试次数
    maxRetry: 3
  }),

  getters: {
    /**
     * 社区消息（点赞/评论/关注）
     */
    communityNotifications: (state) => {
      return state.notifications.filter(n => n.type && n.type.startsWith('COMMUNITY_'))
    },

    /**
     * 系统消息（审核/警告）
     */
    systemNotifications: (state) => {
      return state.notifications.filter(n => n.type && n.type.startsWith('SYSTEM_'))
    },

    /**
     * 是否有未读消息
     */
    hasUnread: (state) => {
      return state.unreadCount > 0
    }
  },

  actions: {
    /**
     * 获取未读消息
     */
    async fetchUnread() {
      // 避免重复请求
      if (this.loading) return

      try {
        this.loading = true
        const res = await getUnreadNotifications()
        
        this.unreadCount = res.data?.unreadCount || 0
        this.notifications = res.data?.notifications || []
        this.retryCount = 0  // 成功后重置重试计数

      } catch (error) {
        console.error('获取未读消息失败:', error)
        this.retryCount++

        // 超过最大重试次数，停止轮询
        if (this.retryCount >= this.maxRetry) {
          console.warn('获取消息失败次数过多，停止轮询')
          this.stopPolling()
        }
      } finally {
        this.loading = false
      }
    },

    /**
     * 标记消息已读（乐观更新）
     */
    async markAsRead(id) {
      // 乐观更新：先从列表移除
      const originalNotifications = [...this.notifications]
      const originalCount = this.unreadCount

      this.notifications = this.notifications.filter(n => n.id !== id)
      this.unreadCount = Math.max(0, this.unreadCount - 1)

      try {
        await markAsRead(id)
      } catch (error) {
        console.error('标记已读失败:', error)
        // 失败回滚
        this.notifications = originalNotifications
        this.unreadCount = originalCount
        throw error
      }
    },

    /**
     * 清空所有未读消息
     */
    async clearAll() {
      const originalNotifications = [...this.notifications]
      const originalCount = this.unreadCount

      // 乐观更新
      this.notifications = []
      this.unreadCount = 0

      try {
        await clearAllNotifications()
      } catch (error) {
        console.error('清空消息失败:', error)
        // 失败回滚
        this.notifications = originalNotifications
        this.unreadCount = originalCount
        throw error
      }
    },

    /**
     * 开始轮询（每30秒）
     */
    startPolling() {
      // 避免重复启动
      if (this.pollingTimer) return

      // 立即获取一次
      this.fetchUnread()

      // 每30秒轮询
      this.pollingTimer = setInterval(() => {
        this.fetchUnread()
      }, 30000)

      console.log('消息轮询已启动')
    },

    /**
     * 停止轮询
     */
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
        console.log('消息轮询已停止')
      }
    },

    /**
     * 重置状态
     */
    reset() {
      this.stopPolling()
      this.unreadCount = 0
      this.notifications = []
      this.loading = false
      this.retryCount = 0
    }
  }
})

