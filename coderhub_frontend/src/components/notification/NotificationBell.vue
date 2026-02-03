<template>
  <div class="notification-bell" ref="bellRef">
    <!-- 小喇叭按钮 -->
    <button 
      class="bell-btn" 
      :class="{ 'has-unread': hasUnread, 'shake': shouldShake }"
      @click="togglePopup"
    >
      <span class="material-symbols-outlined bell-icon">notifications</span>
      <!-- 未读角标 -->
      <span v-if="unreadCount > 0" class="badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <!-- 消息弹窗 -->
    <NotificationPopup 
      v-if="showPopup"
      :notifications="notifications"
      :loading="loading"
      @close="showPopup = false"
      @read="handleRead"
      @clear="handleClear"
      @view="handleView"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import NotificationPopup from './NotificationPopup.vue'

const router = useRouter()
const notificationStore = useNotificationStore()
const bellRef = ref(null)
const showPopup = ref(false)
const shouldShake = ref(false)

// 计算属性
const unreadCount = computed(() => notificationStore.unreadCount)
const hasUnread = computed(() => notificationStore.hasUnread)
const notifications = computed(() => notificationStore.notifications)
const loading = computed(() => notificationStore.loading)

// 监听未读数量变化，触发抖动动画
let previousCount = 0
watch(unreadCount, (newCount) => {
  if (newCount > previousCount && previousCount !== 0) {
    // 有新消息，触发抖动
    shouldShake.value = true
    setTimeout(() => {
      shouldShake.value = false
    }, 500)
  }
  previousCount = newCount
})

// 切换弹窗
const togglePopup = () => {
  showPopup.value = !showPopup.value
}

// 标记已读
const handleRead = async (id) => {
  try {
    await notificationStore.markAsRead(id)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 清空所有
const handleClear = async () => {
  try {
    await notificationStore.clearAll()
    showPopup.value = false
  } catch (error) {
    console.error('清空消息失败:', error)
  }
}

// 查看消息（跳转）
const handleView = (notification) => {
  // 先标记已读
  handleRead(notification.id)
  
  // 根据类型跳转
  if (notification.sourceType === 'ARTICLE' && notification.sourceId) {
    router.push(`/article/${notification.sourceId}`)
    showPopup.value = false
  } else if (notification.type === 'COMMUNITY_FOLLOW' && notification.triggerId) {
    // 关注消息：跳转到关注者主页（暂无，跳转到个人中心）
    router.push('/profile')
    showPopup.value = false
  }
}

// 点击外部关闭弹窗
const handleClickOutside = (e) => {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    showPopup.value = false
  }
}

// 生命周期
onMounted(() => {
  // 启动轮询
  notificationStore.startPolling()
  // 监听点击外部
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  // 停止轮询
  notificationStore.stopPolling()
  // 移除监听
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.bell-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: none;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bell-btn:hover {
  background: rgba(194, 65, 12, 0.08);
}

.bell-btn.has-unread {
  background: rgba(194, 65, 12, 0.05);
}

.bell-icon {
  font-size: 22px;
  color: #64748b;
  transition: color 0.2s ease;
}

.bell-btn:hover .bell-icon,
.bell-btn.has-unread .bell-icon {
  color: var(--primary, #c2410c);
}

/* 未读角标 */
.badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  font-family: 'Inter', sans-serif;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 抖动动画 */
.bell-btn.shake {
  animation: bellShake 0.5s ease-in-out;
}

@keyframes bellShake {
  0%, 100% {
    transform: rotate(0deg);
  }
  20% {
    transform: rotate(-15deg);
  }
  40% {
    transform: rotate(15deg);
  }
  60% {
    transform: rotate(-10deg);
  }
  80% {
    transform: rotate(10deg);
  }
}
</style>

