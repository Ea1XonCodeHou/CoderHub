<template>
  <div class="notification-popup">
    <!-- 头部 -->
    <div class="popup-header">
      <h3 class="popup-title">消息通知</h3>
      <button class="close-btn" @click="$emit('close')">
        <span class="material-symbols-outlined">close</span>
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="popup-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'community' }]"
        @click="activeTab = 'community'"
      >
        <span class="material-symbols-outlined tab-icon">forum</span>
        社区消息
        <span v-if="communityCount > 0" class="tab-badge">{{ communityCount }}</span>
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'system' }]"
        @click="activeTab = 'system'"
      >
        <span class="material-symbols-outlined tab-icon">info</span>
        系统消息
        <span v-if="systemCount > 0" class="tab-badge">{{ systemCount }}</span>
      </button>
    </div>

    <!-- 消息列表 -->
    <div class="popup-content">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredNotifications.length === 0" class="empty-state">
        <span class="material-symbols-outlined empty-icon">notifications_off</span>
        <p>暂无{{ activeTab === 'community' ? '社区' : '系统' }}消息</p>
      </div>

      <!-- 消息列表 -->
      <div v-else class="notification-list">
        <div 
          v-for="item in filteredNotifications" 
          :key="item.id"
          class="notification-item"
          @click="$emit('view', item)"
        >
          <!-- 头像/图标 -->
          <div class="item-avatar">
            <img 
              v-if="item.triggerAvatar" 
              :src="item.triggerAvatar" 
              alt="avatar"
            />
            <span v-else class="material-symbols-outlined avatar-icon">
              {{ getTypeIcon(item.type) }}
            </span>
          </div>

          <!-- 内容 -->
          <div class="item-content">
            <p class="item-text">{{ item.content }}</p>
            <span class="item-time">{{ formatTime(item.createdAt) }}</span>
          </div>

          <!-- 删除按钮 -->
          <button 
            class="item-delete" 
            @click.stop="$emit('read', item.id)"
            title="删除"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="popup-footer" v-if="notifications.length > 0">
      <button class="clear-btn" @click="$emit('clear')">
        <span class="material-symbols-outlined">delete_sweep</span>
        一键清空
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  notifications: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close', 'read', 'clear', 'view'])

const activeTab = ref('community')

// 社区消息
const communityNotifications = computed(() => {
  return props.notifications.filter(n => n.type && n.type.startsWith('COMMUNITY_'))
})

// 系统消息
const systemNotifications = computed(() => {
  return props.notifications.filter(n => n.type && n.type.startsWith('SYSTEM_'))
})

// 当前 Tab 的消息
const filteredNotifications = computed(() => {
  return activeTab.value === 'community' 
    ? communityNotifications.value 
    : systemNotifications.value
})

// 计数
const communityCount = computed(() => communityNotifications.value.length)
const systemCount = computed(() => systemNotifications.value.length)

// 获取消息类型图标
const getTypeIcon = (type) => {
  const icons = {
    'COMMUNITY_LIKE': 'favorite',
    'COMMUNITY_COMMENT': 'chat_bubble',
    'COMMUNITY_FOLLOW': 'person_add',
    'SYSTEM_AUDIT': 'check_circle',
    'SYSTEM_WARNING': 'warning'
  }
  return icons[type] || 'notifications'
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  
  const date = new Date(dateStr)
  const now = new Date()
  const diff = (now - date) / 1000  // 秒数

  if (diff < 60) {
    return '刚刚'
  } else if (diff < 3600) {
    return Math.floor(diff / 60) + '分钟前'
  } else if (diff < 86400) {
    return Math.floor(diff / 3600) + '小时前'
  } else if (diff < 604800) {
    return Math.floor(diff / 86400) + '天前'
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'numeric',
      day: 'numeric'
    })
  }
}
</script>

<style scoped>
.notification-popup {
  position: absolute;
  top: 52px;
  right: 0;
  width: 380px;
  max-height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  z-index: 200;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-warm, #e8e2d9);
}

.popup-title {
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.15s;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #64748b;
}

.close-btn .material-symbols-outlined {
  font-size: 18px;
}

/* Tab 切换 */
.popup-tabs {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
  background: #fafaf9;
  border-bottom: 1px solid var(--border-warm, #e8e2d9);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 12px;
  background: white;
  border: 1px solid var(--border-warm, #e8e2d9);
  border-radius: 10px;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: var(--primary, #c2410c);
  color: var(--primary, #c2410c);
}

.tab-btn.active {
  background: var(--primary, #c2410c);
  border-color: var(--primary, #c2410c);
  color: white;
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.25);
}

.tab-icon {
  font-size: 16px;
}

.tab-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
}

.tab-btn.active .tab-badge {
  background: white;
  color: var(--primary, #c2410c);
}

/* 内容区域 */
.popup-content {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
  max-height: 340px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 12px;
  color: #94a3b8;
  font-size: 14px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f1f5f9;
  border-top-color: var(--primary, #c2410c);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 12px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 消息列表 */
.notification-list {
  padding: 8px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.notification-item:hover {
  background: var(--surface-warm, #f3eee5);
}

/* 头像 */
.item-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #fed7aa 0%, #fdba74 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-icon {
  font-size: 20px;
  color: var(--primary, #c2410c);
}

/* 内容 */
.item-content {
  flex: 1;
  min-width: 0;
}

.item-text {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  margin: 0 0 4px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-time {
  font-size: 12px;
  color: #94a3b8;
}

/* 删除按钮 */
.item-delete {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: #cbd5e1;
  transition: all 0.15s;
  opacity: 0;
}

.notification-item:hover .item-delete {
  opacity: 1;
}

.item-delete:hover {
  background: #fee2e2;
  color: #ef4444;
}

.item-delete .material-symbols-outlined {
  font-size: 16px;
}

/* 底部 */
.popup-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-warm, #e8e2d9);
  background: #fafaf9;
}

.clear-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  background: white;
  border: 1px solid var(--border-warm, #e8e2d9);
  border-radius: 10px;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: #fef2f2;
}

.clear-btn .material-symbols-outlined {
  font-size: 18px;
}

/* 响应式 */
@media (max-width: 480px) {
  .notification-popup {
    width: calc(100vw - 32px);
    right: -16px;
  }
}
</style>

