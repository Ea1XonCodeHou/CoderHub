<template>
  <div class="message-center">

    <!-- 左侧：联系人列表 -->
    <aside class="contact-panel">
      <div class="contact-panel-header">
        <span class="material-symbols-outlined panel-icon">forum</span>
        <h2 class="panel-title">信息中心</h2>
        <span v-if="messagingStore.unreadCount > 0" class="total-badge">
          {{ messagingStore.unreadCount > 99 ? '99+' : messagingStore.unreadCount }}
        </span>
      </div>

      <div class="contact-list" v-if="!messagingStore.loading">
        <div
          v-for="conv in messagingStore.conversations"
          :key="conv.id"
          class="contact-item"
          :class="{ active: messagingStore.activeConvId === conv.id }"
          @click="selectConversation(conv)"
        >
          <!-- 头像 -->
          <div class="contact-avatar-wrap">
            <div v-if="isSystem(conv.peerId)" class="contact-avatar system-icon-avatar">
              <span class="material-symbols-outlined">support_agent</span>
            </div>
            <img
              v-else
              :src="conv.peerAvatar || defaultAvatar(conv.peerName)"
              class="contact-avatar"
            />
            <span v-if="conv.unreadCount > 0" class="contact-badge">
              {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
            </span>
          </div>

          <!-- 信息 -->
          <div class="contact-info">
            <div class="contact-name-row">
              <span class="contact-name">
                {{ isSystem(conv.peerId) ? 'CoderHub 助手' : conv.peerName }}
              </span>
              <span class="contact-time">{{ formatTime(conv.lastMsgAt) }}</span>
            </div>
            <p class="contact-preview">{{ conv.lastMessage || '暂无消息' }}</p>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="messagingStore.conversations.length === 0" class="empty-contact">
          <span class="material-symbols-outlined">mark_unread_chat_alt</span>
          <p>暂无私信会话</p>
        </div>
      </div>

      <div v-else class="contact-loading">
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
        <div class="loading-dot"></div>
      </div>
    </aside>

    <!-- 右侧：对话区域 -->
    <main class="chat-panel">

      <!-- 未选中时的引导界面 -->
      <div v-if="!activeConv" class="chat-placeholder">
        <span class="material-symbols-outlined placeholder-icon">chat_bubble_outline</span>
        <p class="placeholder-title">选择一个会话开始聊天</p>
        <p class="placeholder-sub">你可以在文章作者卡片上点击「私信咨询」发起对话</p>
      </div>

      <!-- 聊天界面 -->
      <template v-else>
        <!-- 顶栏 -->
        <div class="chat-header">
          <div class="chat-peer-info">
            <div v-if="isSystem(activeConv.peerId)" class="peer-avatar system-icon-avatar-sm">
              <span class="material-symbols-outlined">support_agent</span>
            </div>
            <img v-else :src="activeConv.peerAvatar || defaultAvatar(activeConv.peerName)" class="peer-avatar" />
            <div>
              <span class="peer-name">{{ isSystem(activeConv.peerId) ? 'CoderHub 助手' : activeConv.peerName }}</span>
              <span class="peer-status" :class="{ online: isPeerOnline }">
                {{ isPeerOnline ? '在线' : '离线' }}
              </span>
            </div>
          </div>
          <div class="chat-header-actions">
            <div class="ws-status" :class="{ connected: wsConnected }">
              <span class="ws-dot"></span>
              {{ wsConnected ? '已连接' : '连接中…' }}
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-wrap" ref="messagesWrap">
          <div
            v-for="msg in currentMessages"
            :key="msg.id"
            class="msg-row"
            :class="msg.isMine ? 'mine' : 'theirs'"
          >
            <!-- 对方头像 -->
            <div v-if="!msg.isMine && isSystem(msg.senderId)" class="msg-avatar system-icon-avatar-msg">
              <span class="material-symbols-outlined">support_agent</span>
            </div>
            <img
              v-else-if="!msg.isMine"
              :src="msg.senderAvatar || defaultAvatar(msg.senderName)"
              class="msg-avatar"
            />

            <div class="msg-bubble-wrap">
              <span class="msg-sender" v-if="!msg.isMine">
                {{ isSystem(msg.senderId) ? 'CoderHub 助手' : msg.senderName }}
              </span>
              <div class="msg-bubble" :class="msg.isMine ? 'bubble-mine' : 'bubble-theirs'">
                {{ msg.content }}
              </div>
              <span class="msg-time">{{ formatMsgTime(msg.createdAt) }}</span>
            </div>

            <!-- 自己头像 -->
            <img v-if="msg.isMine" :src="myAvatar" class="msg-avatar" />
          </div>

          <!-- 加载提示 -->
          <div v-if="loadingMessages" class="messages-loading">
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area" v-if="!isSystem(activeConv.peerId)">
          <textarea
            ref="inputRef"
            v-model="inputText"
            class="chat-input"
            placeholder="输入消息，Enter 发送，Shift+Enter 换行"
            rows="1"
            @keydown.enter.exact.prevent="sendMessage"
            @input="autoResize"
            :disabled="!wsConnected"
          ></textarea>
          <button
            class="send-btn"
            :disabled="!inputText.trim() || !wsConnected"
            @click="sendMessage"
          >
            <span class="material-symbols-outlined">send</span>
          </button>
        </div>
        <div class="input-area system-notice" v-else>
          <span class="material-symbols-outlined">info</span>
          CoderHub 助手消息为系统通知，不支持回复
        </div>
      </template>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessagingStore } from '@/stores/messagingStore'
import { useWebSocketChat } from '@/composables/useWebSocketChat'
import chatApi from '@/api/chatApi'

const SYSTEM_USER_ID = '00000000-0000-0000-0000-000000000001'

const route = useRoute()
const messagingStore = useMessagingStore()
const { connected: wsConnected, connect: wsConnect, sendMessage: wsSend, disconnect: wsDisconnect } = useWebSocketChat()

const messagesWrap = ref(null)
const inputRef = ref(null)
const inputText = ref('')
const loadingMessages = ref(false)
const isPeerOnline = ref(false) // 简化：v1.0 不显示在线状态

// ==================== 计算属性 ====================

const activeConv = computed(() =>
  messagingStore.conversations.find(c => c.id === messagingStore.activeConvId) || null
)

const currentMessages = computed(() =>
  messagingStore.activeConvId ? (messagingStore.messages[messagingStore.activeConvId] || []) : []
)

const myAvatar = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return u.avatar || defaultAvatar(u.username || 'me')
  } catch { return defaultAvatar('me') }
})

// ==================== 方法 ====================

const isSystem = (peerId) => peerId === SYSTEM_USER_ID

function defaultAvatar(name) {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(name || 'user')}`
}

async function selectConversation(conv) {
  messagingStore.setActiveConv(conv.id)
  loadingMessages.value = true
  await messagingStore.fetchMessages(conv.id)
  loadingMessages.value = false
  await messagingStore.markAsRead(conv.id)

  // 查询对方在线状态（系统联系人永远不在线）
  if (!isSystem(conv.peerId)) {
    try {
      const res = await chatApi.isUserOnline(conv.peerId)
      isPeerOnline.value = res.code === 1 && res.data === true
    } catch {
      isPeerOnline.value = false
    }
  } else {
    isPeerOnline.value = false
  }

  scrollToBottom()
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !activeConv.value) return
  if (isSystem(activeConv.value.peerId)) return

  const receiverId = activeConv.value.peerId
  const ok = wsSend(receiverId, text)
  if (ok) {
    inputText.value = ''
    if (inputRef.value) inputRef.value.style.height = 'auto'
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 140) + 'px'
}

function scrollToBottom() {
  nextTick(() => {
    const el = messagesWrap.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// WebSocket 收到消息回调
function onWsMessage(data) {
  const { type, ...msg } = data
  if (type === 'MESSAGE' || type === 'ACK') {
    const convId = msg.conversationId
    if (!convId) return

    // isMine: ACK 是自己发的，MESSAGE 是对方发的
    msg.isMine = (type === 'ACK')
    messagingStore.appendMessage(convId, msg)

    // 如果是当前打开的会话，自动滚到底部并标记已读
    if (messagingStore.activeConvId === convId) {
      scrollToBottom()
      if (!msg.isMine) messagingStore.markAsRead(convId)
    }
  }
}

// ==================== 格式化时间 ====================

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (d.toDateString() === now.toDateString()) return d.toTimeString().slice(0, 5)
  if (diff < 86400000 * 7) return ['周日','周一','周二','周三','周四','周五','周六'][d.getDay()]
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function formatMsgTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return d.toTimeString().slice(0, 5)
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await messagingStore.fetchConversations()

  // 连接 WebSocket
  const token = localStorage.getItem('token')
  if (token) wsConnect(token, onWsMessage)

  // 如果路由携带 targetUserId（从私信按钮跳转），自动打开对应会话
  const targetUserId = route.query.targetUserId
  if (targetUserId) {
    const convId = await messagingStore.getOrCreateConversation(targetUserId)
    if (convId) {
      await messagingStore.fetchConversations()
      const conv = messagingStore.conversations.find(c => c.id === convId)
      if (conv) await selectConversation(conv)
    }
  }
})

onUnmounted(() => {
  wsDisconnect()
})

// 切换会话时滚到底部
watch(() => messagingStore.activeConvId, () => scrollToBottom())
</script>

<style scoped>
/* ==================== 整体布局 ==================== */
.message-center {
  display: flex;
  height: calc(100vh - 72px);
  background: var(--surface-warm, #f3eee5);
  font-family: 'Inter', sans-serif;
  overflow: hidden;
}

/* ==================== 左侧联系人面板 ==================== */
.contact-panel {
  width: 300px;
  min-width: 260px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid var(--border-warm, #e8e2d9);
  overflow: hidden;
}

.contact-panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--border-warm, #e8e2d9);
}

.panel-icon {
  font-size: 22px;
  color: var(--primary, #c2410c);
}

.panel-title {
  flex: 1;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.total-badge {
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 9999px;
  min-width: 20px;
  text-align: center;
}

/* 联系人列表 */
.contact-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 0;
  transition: background 0.15s;
  border-left: 3px solid transparent;
}

.contact-item:hover {
  background: var(--surface-warm, #f3eee5);
}

.contact-item.active {
  background: rgba(194, 65, 12, 0.06);
  border-left-color: var(--primary, #c2410c);
}

.contact-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.contact-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-warm, #e8e2d9);
}

/* 系统助手图标头像（联系人列表） */
.system-icon-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(194, 65, 12, 0.08);
  border: 2px solid rgba(194, 65, 12, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.system-icon-avatar .material-symbols-outlined {
  font-size: 22px;
  color: var(--primary, #c2410c);
  font-variation-settings: 'FILL' 1;
}

/* 系统助手图标头像（聊天顶栏） */
.system-icon-avatar-sm {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(194, 65, 12, 0.08);
  border: 2px solid rgba(194, 65, 12, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.system-icon-avatar-sm .material-symbols-outlined {
  font-size: 20px;
  color: var(--primary, #c2410c);
  font-variation-settings: 'FILL' 1;
}

/* 系统助手图标头像（消息气泡旁） */
.system-icon-avatar-msg {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(194, 65, 12, 0.08);
  border: 1.5px solid rgba(194, 65, 12, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.system-icon-avatar-msg .material-symbols-outlined {
  font-size: 18px;
  color: var(--primary, #c2410c);
  font-variation-settings: 'FILL' 1;
}

.contact-badge {
  position: absolute;
  top: -3px;
  right: -3px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 9999px;
  border: 1.5px solid #fff;
  min-width: 16px;
  text-align: center;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 3px;
}

.contact-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.contact-time {
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
  flex-shrink: 0;
}

.contact-preview {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

/* 空状态 */
.empty-contact {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 20px;
  color: #94a3b8;
}

.empty-contact .material-symbols-outlined {
  font-size: 40px;
}

.empty-contact p {
  margin: 0;
  font-size: 14px;
}

/* 加载动画 */
.contact-loading,
.messages-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 32px;
}

.loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary, #c2410c);
  opacity: 0.4;
  animation: dot-bounce 1.2s infinite ease-in-out;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* ==================== 右侧聊天面板 ==================== */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--surface-warm, #f3eee5);
}

/* 未选中引导 */
.chat-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #94a3b8;
}

.placeholder-icon {
  font-size: 64px;
  opacity: 0.4;
}

.placeholder-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #64748b;
}

.placeholder-sub {
  font-size: 14px;
  margin: 0;
  color: #94a3b8;
}

/* 聊天顶栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-warm, #e8e2d9);
}

.chat-peer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.peer-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid var(--border-warm, #e8e2d9);
  object-fit: cover;
}

.peer-name {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.peer-status {
  font-size: 12px;
  color: #94a3b8;
}

.peer-status.online {
  color: #22c55e;
}

.ws-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #94a3b8;
}

.ws-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #94a3b8;
  transition: background 0.3s;
}

.ws-status.connected .ws-dot {
  background: #22c55e;
}

.ws-status.connected {
  color: #22c55e;
}

/* 消息列表 */
.messages-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scroll-behavior: smooth;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.msg-row.mine {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1.5px solid var(--border-warm, #e8e2d9);
}

.msg-bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.msg-row.mine .msg-bubble-wrap {
  align-items: flex-end;
}

.msg-sender {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 3px;
  padding-left: 2px;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
}

.bubble-theirs {
  background: #fff;
  color: #1e293b;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.bubble-mine {
  background: var(--primary, #c2410c);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 3px;
  padding: 0 2px;
}

/* 输入区 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid var(--border-warm, #e8e2d9);
}

.chat-input {
  flex: 1;
  resize: none;
  border: 1.5px solid var(--border-warm, #e8e2d9);
  border-radius: 12px;
  padding: 10px 14px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1e293b;
  background: var(--surface-warm, #f3eee5);
  outline: none;
  min-height: 40px;
  max-height: 140px;
  line-height: 1.5;
  transition: border-color 0.2s;
}

.chat-input:focus {
  border-color: var(--primary, #c2410c);
  background: #fff;
}

.chat-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  background: var(--primary, #c2410c);
  color: #fff;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #9a3412;
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.send-btn .material-symbols-outlined {
  font-size: 20px;
}

.system-notice {
  color: #94a3b8;
  font-size: 13px;
  gap: 8px;
  align-items: center;
  padding: 14px 24px;
  background: rgba(148, 163, 184, 0.06);
}

.system-notice .material-symbols-outlined {
  font-size: 18px;
  color: var(--primary, #c2410c);
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .contact-panel {
    width: 72px;
    min-width: 72px;
  }

  .contact-info, .contact-time, .panel-title {
    display: none;
  }

  .contact-item {
    justify-content: center;
    padding: 12px 8px;
  }
}
</style>
