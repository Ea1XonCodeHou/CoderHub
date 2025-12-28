<template>
  <div class="ai-assistant" :class="{ 'dark-mode': isDarkMode }">
    <!-- 公用导航栏 -->
    <NavBar :showSearch="false" :showWriteBtn="false" />

    <div class="main-layout">
      <!-- 侧边栏 - 默认展示 -->
      <aside class="sidebar" :class="{ collapsed: !sidebarOpen }">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <div class="sidebar-actions">
            <button class="new-chat-btn" @click="startNewChat" title="新对话">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
            </button>
            <button class="collapse-btn" @click="toggleSidebar" title="收起侧边栏">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
            </button>
          </div>
        </div>
        <div class="chat-list">
          <div 
            v-for="chat in chatHistory" 
            :key="chat.id"
            class="chat-item"
            :class="{ active: currentChatId === chat.id }"
            @click="loadConversation(chat)"
          >
            <div class="chat-item-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
            <div class="chat-item-content">
              <span class="chat-title">{{ chat.title }}</span>
              <span class="chat-time">{{ formatTime(chat.updatedAt) }}</span>
            </div>
            <button class="chat-delete" @click.stop="deleteConversation(chat.id)" title="删除">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </button>
          </div>
          <div v-if="chatHistory.length === 0" class="empty-history">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <p>暂无对话历史</p>
            <span>开始新的对话吧</span>
          </div>
        </div>
      </aside>

      <!-- 折叠时的展开按钮 -->
      <button v-if="!sidebarOpen" class="sidebar-expand-btn" @click="toggleSidebar" title="展开侧边栏">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>

      <!-- 主内容区 -->
      <main class="chat-main">
        <!-- 顶部工具栏 -->
        <div class="chat-toolbar">
          <div class="model-selector">
            <select v-model="selectedModel" @change="onModelChange">
              <option v-for="m in availableModels" :key="m.id" :value="m.id">
                {{ m.icon }} {{ m.name }}
              </option>
            </select>
            <svg class="select-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
          <button class="theme-btn" @click="toggleTheme" :title="isDarkMode ? '浅色模式' : '深色模式'">
            <svg v-if="isDarkMode" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"></circle>
              <line x1="12" y1="1" x2="12" y2="3"></line>
              <line x1="12" y1="21" x2="12" y2="23"></line>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
              <line x1="1" y1="12" x2="3" y2="12"></line>
              <line x1="21" y1="12" x2="23" y2="12"></line>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
          </button>
        </div>

        <!-- 欢迎界面 -->
        <div v-if="messages.length === 0" class="welcome-view">
          <div class="welcome-content">
            <div class="welcome-logo">
              <svg viewBox="0 0 80 80" fill="none">
                <defs>
                  <linearGradient id="welcome-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#2c3e50"/>
                    <stop offset="50%" stop-color="#34495e"/>
                    <stop offset="100%" stop-color="#667eea"/>
                  </linearGradient>
                </defs>
                <circle cx="40" cy="40" r="35" stroke="url(#welcome-gradient)" stroke-width="3" fill="none" opacity="0.3"/>
                <circle cx="40" cy="40" r="25" stroke="url(#welcome-gradient)" stroke-width="3" fill="none" opacity="0.6"/>
                <circle cx="40" cy="40" r="15" fill="url(#welcome-gradient)" opacity="0.9"/>
              </svg>
            </div>
            <h1 class="welcome-title">你好，我是 CoderHub AI</h1>
            <p class="welcome-subtitle">你的智能编程助手，随时为你解答技术问题</p>
            
            <div class="quick-actions">
              <button 
                v-for="prompt in quickPrompts" 
                :key="prompt.text"
                class="quick-action-card"
                @click="useQuickPrompt(prompt.text)"
              >
                <span class="quick-icon">{{ prompt.icon }}</span>
                <span class="quick-text">{{ prompt.text }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else class="messages-container" ref="messagesContainer">
          <div class="messages-list">
            <TransitionGroup name="message">
              <div 
                v-for="(msg, index) in messages" 
                :key="msg.id || index"
                class="message-wrapper"
                :class="msg.role"
              >
                <!-- 用户消息 - 居右布局 -->
                <template v-if="msg.role === 'user'">
                  <div class="message-content user-content">
                    <div class="message-body user-bubble">
                      {{ msg.content }}
                    </div>
                  </div>
                  <div class="message-avatar">
                    <img :src="userAvatar" alt="You" />
                  </div>
                </template>

                <!-- AI消息 - 居左布局 -->
                <template v-else>
                  <div class="message-avatar">
                    <div class="ai-avatar">
                      <svg viewBox="0 0 40 40" fill="none">
                        <rect width="40" height="40" rx="8" fill="url(#ai-gradient)"/>
                        <path d="M12 14L20 10L28 14V26L20 30L12 26V14Z" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
                        <defs>
                          <linearGradient id="ai-gradient" x1="0" y1="0" x2="40" y2="40">
                            <stop offset="0%" stop-color="#2c3e50"/>
                            <stop offset="100%" stop-color="#34495e"/>
                          </linearGradient>
                        </defs>
                      </svg>
                    </div>
                  </div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="sender-name">CoderHub AI</span>
                      <span v-if="msg.model" class="message-model">{{ msg.model }}</span>
                    </div>
                    
                    <!-- 已保存的工具调用状态 -->
                    <div v-if="msg.toolCall" class="tool-call-section">
                      <!-- 工具调用胶囊 -->
                      <div class="tool-call-capsule success saved">
                        <div class="capsule-content">
                          <div class="capsule-icon">
                            <span class="check-icon">✓</span>
                          </div>
                          <div class="capsule-info">
                            <span class="capsule-title">{{ msg.toolCall.displayName || '检索完成' }}</span>
                            <span class="capsule-detail">{{ msg.toolCall.parameters || '已检索相关内容' }}</span>
                          </div>
                          <div v-if="msg.toolCall.resultCount" class="capsule-badge">
                            {{ msg.toolCall.resultCount }} 条结果
                          </div>
                        </div>
                      </div>
                      
                      <!-- 使用的工具标签 -->
                      <div class="tools-used">
                        <div class="tool-tag" v-for="tool in getUsedTools(msg.toolCall)" :key="tool.id">
                          <span class="tool-tag-icon">{{ tool.icon }}</span>
                          <span class="tool-tag-name">{{ tool.name }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div 
                      class="message-body markdown-content"
                      v-html="renderMarkdown(msg.content)"
                    ></div>
                    
                    <!-- 已完成消息的推荐卡片 -->
                    <div v-if="msg.recommendations && msg.recommendations.length > 0" class="recommendation-cards saved">
                      <div class="cards-header">
                        <span class="cards-title">📚 相关推荐</span>
                        <span class="cards-count">{{ msg.recommendations.length }} 项</span>
                      </div>
                      <div class="cards-grid">
                        <a 
                          v-for="item in msg.recommendations.slice(0, 6)" 
                          :key="item.id"
                          :href="item.link"
                          class="recommend-card"
                          :class="item.type"
                          target="_blank"
                        >
                          <div class="card-badge">{{ item.type === 'tutorial' ? '教程' : '文章' }}</div>
                          <div class="card-cover" v-if="item.coverImage">
                            <img :src="item.coverImage" :alt="item.title" />
                          </div>
                          <div class="card-body">
                            <h4 class="card-title">{{ item.title }}</h4>
                            <p class="card-desc">{{ item.description?.slice(0, 60) }}{{ item.description?.length > 60 ? '...' : '' }}</p>
                            <div class="card-meta">
                              <span v-if="item.author" class="meta-author">{{ item.author }}</span>
                              <span v-if="item.rating" class="meta-rating">⭐ {{ item.rating }}</span>
                              <span v-if="item.viewCount" class="meta-views">👁 {{ formatNumber(item.viewCount) }}</span>
                            </div>
                          </div>
                        </a>
                      </div>
                    </div>
                    
                    <div class="message-actions">
                      <button class="action-btn" @click="copyMessage(msg.content)" title="复制">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                        </svg>
                      </button>
                      <button class="action-btn" @click="regenerateMessage(index)" title="重新生成">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="23 4 23 10 17 10"></polyline>
                          <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                        </svg>
                      </button>
                    </div>
                  </div>
                </template>
              </div>
            </TransitionGroup>

            <!-- 流式输出中 -->
            <div v-if="isStreaming" class="message-wrapper assistant streaming">
              <div class="message-avatar">
                <div class="ai-avatar pulsing">
                  <svg viewBox="0 0 40 40" fill="none">
                    <rect width="40" height="40" rx="8" fill="url(#ai-gradient)"/>
                    <path d="M12 14L20 10L28 14V26L20 30L12 26V14Z" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">CoderHub AI</span>
                  <span class="typing-status">{{ isToolCalling ? '工具调用中...' : (isThinking ? '思考中...' : '输入中...') }}</span>
                </div>
                
                <!-- 工具调用状态区域 -->
                <div v-if="isToolCalling || currentToolCall" class="tool-call-section streaming">
                  <!-- 工具调用状态胶囊 -->
                  <div class="tool-call-capsule" :class="{ calling: isToolCalling, success: currentToolCall?.status === 'success', failed: currentToolCall?.status === 'failed' }">
                    <div class="capsule-glow"></div>
                    <div class="capsule-content">
                      <div class="capsule-icon">
                        <span v-if="isToolCalling" class="spinner"></span>
                        <span v-else-if="currentToolCall?.status === 'success'" class="check-icon">✓</span>
                        <span v-else-if="currentToolCall?.status === 'failed'" class="error-icon">✗</span>
                        <span v-else class="tool-emoji">{{ currentToolCall?.icon || '🔧' }}</span>
                      </div>
                      <div class="capsule-info">
                        <span class="capsule-title">{{ currentToolCall?.displayName || '调用工具' }}</span>
                        <span class="capsule-detail">{{ currentToolCall?.parameters || '正在检索...' }}</span>
                      </div>
                      <div v-if="currentToolCall?.resultCount" class="capsule-badge">
                        {{ currentToolCall.resultCount }} 条结果
                      </div>
                    </div>
                  </div>
                  
                  <!-- 使用的工具标签（成功后显示） -->
                  <div v-if="currentToolCall?.status === 'success'" class="tools-used">
                    <div class="tool-tag" v-for="tool in getUsedTools(currentToolCall)" :key="tool.id">
                      <span class="tool-tag-icon">{{ tool.icon }}</span>
                      <span class="tool-tag-name">{{ tool.name }}</span>
                    </div>
                  </div>
                </div>
                
                <div v-if="isThinking && !streamingContent && !isToolCalling" class="thinking-indicator">
                  <div class="thinking-dot"></div>
                  <div class="thinking-dot"></div>
                  <div class="thinking-dot"></div>
                </div>
                <div 
                  v-else-if="streamingContent"
                  class="message-body markdown-content"
                  v-html="renderMarkdown(streamingContent)"
                ></div>
                <div class="cursor-blink"></div>
                
                <!-- 实时推荐卡片 -->
                <div v-if="currentRecommendations.length > 0" class="recommendation-cards">
                  <div class="cards-header">
                    <span class="cards-title">📚 相关推荐</span>
                    <span class="cards-count">{{ currentRecommendations.length }} 项</span>
                  </div>
                  <div class="cards-grid">
                    <a 
                      v-for="item in currentRecommendations.slice(0, 6)" 
                      :key="item.id"
                      :href="item.link"
                      class="recommend-card"
                      :class="item.type"
                      target="_blank"
                    >
                      <div class="card-badge">{{ item.type === 'tutorial' ? '教程' : '文章' }}</div>
                      <div class="card-cover" v-if="item.coverImage">
                        <img :src="item.coverImage" :alt="item.title" />
                      </div>
                      <div class="card-body">
                        <h4 class="card-title">{{ item.title }}</h4>
                        <p class="card-desc">{{ item.description?.slice(0, 60) }}{{ item.description?.length > 60 ? '...' : '' }}</p>
                        <div class="card-meta">
                          <span v-if="item.author" class="meta-author">{{ item.author }}</span>
                          <span v-if="item.rating" class="meta-rating">⭐ {{ item.rating }}</span>
                          <span v-if="item.viewCount" class="meta-views">👁 {{ formatNumber(item.viewCount) }}</span>
                        </div>
                      </div>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-wrapper">
            <div class="input-box" :class="{ focused: inputFocused }">
              <textarea
                ref="inputRef"
                v-model="inputText"
                @keydown="handleKeydown"
                @input="autoResize"
                @focus="inputFocused = true"
                @blur="inputFocused = false"
                :placeholder="isStreaming ? 'AI 正在回复...' : '输入消息，Enter 发送，Shift+Enter 换行'"
                :disabled="isStreaming"
                rows="1"
              ></textarea>
              <div class="input-actions">
                <button 
                  v-if="isStreaming"
                  class="stop-btn"
                  @click="cancelStream"
                  title="停止生成"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <rect x="6" y="6" width="12" height="12" rx="2"/>
                  </svg>
                </button>
                <button 
                  v-else
                  class="send-btn" 
                  @click="sendMessage"
                  :disabled="!canSend"
                  title="发送消息"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="22" y1="2" x2="11" y2="13"></line>
                    <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                  </svg>
                </button>
              </div>
            </div>
            <div class="input-hint">
              <span>CoderHub AI 可能会产生错误信息，请核实重要内容</span>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Toast 提示 -->
    <Transition name="toast">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chatStore'
import { useChatStream } from '@/composables/useChatStream'
import { useMarkdownRenderer } from '@/composables/useMarkdownRenderer'
import { useChatHistory } from '@/composables/useLocalStorage'
import NavBar from '@/components/NavBar.vue'

// Router
const router = useRouter()

// Store
const chatStore = useChatStore()

// Composables
const { 
  sendMessage: streamSendMessage, 
  streamingContent: streamContent,
  isStreaming: streamIsStreaming,
  isThinking,
  cancelStream: cancelStreamRequest,
  // 工具调用相关
  isToolCalling,
  toolCallStatus,
  recommendations: streamRecommendations
} = useChatStream()

const { render: renderMarkdown } = useMarkdownRenderer()

const { 
  conversations: chatHistory, 
  saveConversation, 
  deleteConversation: removeConversation,
  formatTime 
} = useChatHistory()

// ==================== 响应式状态 ====================
const inputText = ref('')
const inputRef = ref(null)
const messagesContainer = ref(null)
const inputFocused = ref(false)
const selectedModel = ref('qwen-plus')
const sidebarOpen = ref(true) // 默认展开侧边栏
const lastToolCall = ref(null) // 保存最近的工具调用信息

const toast = ref({
  visible: false,
  message: '',
  type: 'info'
})

// ==================== 计算属性 ====================
const messages = computed(() => chatStore.messages)
const currentChatId = computed(() => chatStore.currentChatId)
const isDarkMode = computed(() => chatStore.isDarkMode)
const isStreaming = computed(() => streamIsStreaming.value)
const streamingContent = computed(() => streamContent.value)

// 工具调用状态
const currentToolCall = computed(() => toolCallStatus.value)
const currentRecommendations = computed(() => streamRecommendations.value)

const canSend = computed(() => {
  return inputText.value.trim() && !isStreaming.value
})

const userAvatar = computed(() => {
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      return user.avatar || defaultAvatar
    } catch {}
  }
  return defaultAvatar
})

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=coderhub'

// 可用模型列表
const availableModels = ref([
  { id: 'qwen-plus', name: '通义千问 Plus', icon: '🌟' },
  { id: 'qwen-turbo', name: '通义千问 Turbo', icon: '⚡' },
  { id: 'qwen-max', name: '通义千问 Max', icon: '🚀' },
  { id: 'deepseek-chat', name: 'DeepSeek Chat', icon: '🔮' }
])

// 快捷提示
const quickPrompts = ref([
  { icon: '💡', text: '解释 Vue 3 Composition API 的核心概念' },
  { icon: '🔧', text: '如何优化 MySQL 慢查询？' },
  { icon: '📝', text: '写一个 TypeScript 防抖函数' },
  { icon: '🚀', text: 'Spring Boot 3 有哪些新特性？' }
])

// ==================== 方法 ====================

/**
 * 发送消息
 */
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isStreaming.value) return

  // 添加用户消息
  chatStore.addUserMessage(text)
  inputText.value = ''
  resetTextareaHeight()
  scrollToBottom()

  try {
    // 构建历史消息
    const history = messages.value.slice(-10).map(m => ({
      role: m.role,
      content: m.content
    }))

    // 发送流式请求
    await streamSendMessage({
      message: text,
      model: selectedModel.value,
      temperature: 0.7,
      history: history.slice(0, -1), // 不包含刚添加的用户消息
      onToken: () => {
        scrollToBottom()
      },
      onToolCall: (toolCall) => {
        console.log('工具调用中:', toolCall)
        scrollToBottom()
      },
      onToolResult: (toolCall, recommendations) => {
        console.log('工具调用完成:', toolCall, '推荐数:', recommendations?.length)
        // 保存工具调用信息供后续使用
        lastToolCall.value = toolCall
        scrollToBottom()
      },
      onComplete: (fullContent, tokenUsage, recommendations) => {
        // 添加 AI 回复到消息列表，附带推荐内容和工具调用信息
        chatStore.addAssistantMessage(fullContent, {
          recommendations: recommendations || [],
          toolCall: lastToolCall.value || null
        })
        // 重置工具调用状态
        lastToolCall.value = null
        
        // 保存对话到历史
        saveConversation({
          id: currentChatId.value,
          messages: messages.value,
          model: selectedModel.value
        })
        
        scrollToBottom()
      },
      onError: (error) => {
        showToast(error, 'error')
      }
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    showToast('发送失败，请重试', 'error')
  }
}

/**
 * 取消流式响应
 */
function cancelStream() {
  cancelStreamRequest()
  
  // 如果已有部分内容，保存它
  if (streamingContent.value) {
    chatStore.addAssistantMessage(streamingContent.value + '\n\n*[回复被中断]*')
  }
}

/**
 * 使用快捷提示
 */
function useQuickPrompt(text) {
  inputText.value = text
  nextTick(() => {
    sendMessage()
  })
}

/**
 * 复制消息
 */
async function copyMessage(content) {
  try {
    await navigator.clipboard.writeText(content)
    showToast('已复制到剪贴板', 'success')
  } catch {
    showToast('复制失败', 'error')
  }
}

/**
 * 重新生成消息
 */
function regenerateMessage(index) {
  if (index > 0 && messages.value[index - 1].role === 'user') {
    const userMessage = messages.value[index - 1].content
    chatStore.deleteMessage(messages.value[index].id)
    inputText.value = userMessage
    nextTick(() => sendMessage())
  }
}

/**
 * 开始新对话
 */
function startNewChat() {
  chatStore.startNewChat()
}

/**
 * 加载对话
 */
function loadConversation(chat) {
  chatStore.loadChat(chat)
  nextTick(() => scrollToBottom())
}

/**
 * 删除对话
 */
function deleteConversation(id) {
  removeConversation(id)
  if (currentChatId.value === id) {
    startNewChat()
  }
}

/**
 * 切换侧边栏
 */
function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

/**
 * 切换主题
 */
function toggleTheme() {
  chatStore.toggleTheme()
}

/**
 * 模型变更
 */
function onModelChange() {
  localStorage.setItem('coderhub_model', selectedModel.value)
}

/**
 * 处理键盘事件
 */
function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

/**
 * 自动调整文本框高度
 */
function autoResize() {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
  }
}

/**
 * 重置文本框高度
 */
function resetTextareaHeight() {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTo({
        top: messagesContainer.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

/**
 * 显示 Toast
 */
function showToast(message, type = 'info') {
  toast.value = { visible: true, message, type }
  setTimeout(() => {
    toast.value.visible = false
  }, 3000)
}

/**
 * 格式化数字（如 1234 -> 1.2k）
 */
function formatNumber(num) {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

/**
 * 获取使用的工具列表（用于显示工具标签）
 */
function getUsedTools(toolCall) {
  if (!toolCall) return []
  
  // 工具名称映射
  const toolNameMap = {
    'searchTutorials': { id: 'tutorials', name: '教程检索', icon: '📚' },
    'searchArticles': { id: 'articles', name: '博客检索', icon: '📝' },
    'getHotContent': { id: 'hot', name: '热门推荐', icon: '🔥' },
    'getHotTags': { id: 'tags', name: '标签检索', icon: '🏷️' }
  }
  
  const tools = []
  
  // 根据工具名称添加对应标签
  if (toolCall.toolName) {
    const tool = toolNameMap[toolCall.toolName]
    if (tool) {
      tools.push(tool)
    }
  }
  
  // 如果有推荐结果，可能同时使用了多个工具
  // 默认添加智能体标签
  tools.push({ id: 'agent', name: 'AI 智能体', icon: '🤖' })
  
  return tools
}

// ==================== 生命周期 ====================
onMounted(() => {
  // 初始化 store
  chatStore.initialize()
  
  // 恢复选择的模型
  const savedModel = localStorage.getItem('coderhub_model')
  if (savedModel) {
    selectedModel.value = savedModel
  }
  
  // 聚焦输入框
  if (inputRef.value) {
    inputRef.value.focus()
  }
})

onUnmounted(() => {
  // 清理
  cancelStreamRequest()
})

// 监听流式内容变化，自动滚动
watch(streamingContent, () => {
  scrollToBottom()
})
</script>

<style scoped>
/* ==================== CSS 变量 - 与主界面保持一致 ==================== */
.ai-assistant {
  --color-primary: #2c3e50;
  --color-primary-light: #34495e;
  --color-accent: #667eea;
  --color-accent-hover: #5a5bd4;
  --color-accent-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  --color-bg-primary: #ffffff;
  --color-bg-secondary: #f5f7fa;
  --color-bg-tertiary: #e2e8f0;
  --color-bg-hover: #f0f2f5;
  
  --color-text-primary: #2c3e50;
  --color-text-secondary: #64748b;
  --color-text-muted: #94a3b8;
  
  --color-border: #e2e8f0;
  --color-border-light: #f1f5f9;
  
  --color-success: #10b981;
  --color-error: #ef4444;
  
  --color-user-bubble: #e8f4fd;
  --color-user-bubble-border: #bdd7ee;
  
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  
  --radius-sm: 6px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-full: 9999px;
  
  --font-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
  --font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  
  /* 关键修复：固定整体高度为视口高度，禁止页面滚动 */
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-family: var(--font-sans);
}

/* 深色模式 */
.ai-assistant.dark-mode {
  --color-primary: #e2e8f0;
  --color-primary-light: #cbd5e1;
  
  --color-bg-primary: #0f172a;
  --color-bg-secondary: #1e293b;
  --color-bg-tertiary: #334155;
  --color-bg-hover: #475569;
  
  --color-text-primary: #f1f5f9;
  --color-text-secondary: #94a3b8;
  --color-text-muted: #64748b;
  
  --color-border: #334155;
  --color-border-light: #475569;
  
  --color-user-bubble: #1e3a5f;
  --color-user-bubble-border: #2d5a87;
}

/* ==================== 主布局 ==================== */
.main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
  /* 关键：限制高度，计算去掉导航栏后的剩余空间 */
  height: calc(100vh - 64px);
  min-height: 0; /* 重要：允许flex子元素收缩 */
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  width: 280px;
  background: var(--color-bg-primary);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s ease, opacity 0.3s ease;
  /* 关键：固定高度，防止随内容增长 */
  height: 100%;
  max-height: 100%;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.sidebar-actions {
  display: flex;
  gap: 8px;
}

.new-chat-btn,
.collapse-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
}

.new-chat-btn:hover,
.collapse-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.new-chat-btn svg,
.collapse-btn svg {
  width: 16px;
  height: 16px;
}

.sidebar-expand-btn {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
  z-index: 10;
}

.sidebar-expand-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.sidebar-expand-btn svg {
  width: 18px;
  height: 18px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px;
  /* 关键：确保对话历史可滚动，而不是无限增长 */
  min-height: 0; /* 重要：允许flex子元素收缩 */
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.chat-item:hover {
  background: var(--color-bg-secondary);
}

.chat-item.active {
  background: var(--color-accent);
  color: white;
}

.chat-item.active .chat-title,
.chat-item.active .chat-time,
.chat-item.active .chat-item-icon {
  color: white;
}

.chat-item-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
}

.chat-item.active .chat-item-icon {
  background: rgba(255, 255, 255, 0.2);
}

.chat-item-icon svg {
  width: 16px;
  height: 16px;
}

.chat-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.chat-delete {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-muted);
  opacity: 0;
  transition: all 0.2s;
}

.chat-item:hover .chat-delete {
  opacity: 1;
}

.chat-delete:hover {
  background: var(--color-error);
  color: white;
}

.chat-delete svg {
  width: 14px;
  height: 14px;
}

.empty-history {
  padding: 40px 20px;
  text-align: center;
  color: var(--color-text-muted);
}

.empty-history svg {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-history p {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: var(--color-text-secondary);
}

.empty-history span {
  font-size: 13px;
}

/* ==================== 主内容区 ==================== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-primary);
  /* 关键：固定高度，防止随内容增长 */
  height: 100%;
  max-height: 100%;
  min-height: 0; /* 重要：允许flex子元素收缩 */
}

/* ==================== 工具栏 ==================== */
.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px 24px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  /* 确保工具栏固定不收缩 */
  flex-shrink: 0;
}

/* 模型选择器 */
.model-selector {
  position: relative;
  display: flex;
  align-items: center;
}

.model-selector select {
  appearance: none;
  padding: 10px 40px 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--font-sans);
}

.model-selector select:hover {
  border-color: var(--color-primary);
}

.model-selector select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

.select-arrow {
  position: absolute;
  right: 14px;
  width: 16px;
  height: 16px;
  color: var(--color-text-secondary);
  pointer-events: none;
}

.theme-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
}

.theme-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.theme-btn svg {
  width: 20px;
  height: 20px;
}

/* ==================== 欢迎界面 ==================== */
.welcome-view {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow-y: auto;
  min-height: 0; /* 重要：允许flex子元素收缩 */
}

.welcome-content {
  max-width: 700px;
  text-align: center;
}

.welcome-logo {
  margin-bottom: 32px;
  animation: float 3s ease-in-out infinite;
}

.welcome-logo svg {
  width: 80px;
  height: 80px;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--color-primary);
}

.welcome-subtitle {
  font-size: 18px;
  color: var(--color-text-secondary);
  margin-bottom: 48px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.quick-action-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-icon {
  font-size: 24px;
}

.quick-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.5;
}

/* ==================== 消息列表 ==================== */
.messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  /* 关键：确保消息区域可滚动，而不是无限增长 */
  min-height: 0; /* 重要：允许flex子元素收缩 */
  scroll-behavior: smooth;
}

.messages-list {
  max-width: 860px;
  margin: 0 auto;
}

.message-wrapper {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  animation: messageIn 0.3s ease;
}

/* 用户消息居右 */
.message-wrapper.user {
  flex-direction: row;
  justify-content: flex-end;
}

/* AI消息居左 */
.message-wrapper.assistant {
  flex-direction: row;
  justify-content: flex-start;
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border);
}

.ai-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar svg {
  width: 40px;
  height: 40px;
}

.ai-avatar.pulsing {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.message-content {
  flex: 1;
  min-width: 0;
  max-width: 75%;
}

/* 用户消息内容 */
.user-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-bubble {
  background: var(--color-user-bubble);
  border: 1px solid var(--color-user-bubble-border);
  padding: 14px 18px;
  border-radius: 18px 18px 4px 18px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--color-text-primary);
  word-wrap: break-word;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.sender-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.message-model {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
}

.typing-status {
  font-size: 12px;
  color: var(--color-accent);
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.message-body {
  font-size: 15px;
  line-height: 1.75;
  color: var(--color-text-primary);
}

/* 思考动画 */
.thinking-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 0;
}

.thinking-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
}

.thinking-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

/* 光标闪烁 */
.cursor-blink {
  display: inline-block;
  width: 2px;
  height: 18px;
  background: var(--color-primary);
  margin-left: 2px;
  animation: cursor-blink 1s step-end infinite;
  vertical-align: text-bottom;
}

@keyframes cursor-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 消息操作按钮 */
.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-wrapper:hover .message-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

/* ==================== Markdown 样式 ==================== */
.markdown-content :deep(p) {
  margin: 0 0 16px 0;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4) {
  margin: 24px 0 16px 0;
  font-weight: 600;
  line-height: 1.4;
  color: var(--color-text-primary);
}

.markdown-content :deep(h1) { font-size: 1.75em; }
.markdown-content :deep(h2) { font-size: 1.5em; }
.markdown-content :deep(h3) { font-size: 1.25em; }
.markdown-content :deep(h4) { font-size: 1.1em; }

.markdown-content :deep(h1:first-child),
.markdown-content :deep(h2:first-child),
.markdown-content :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0 0 16px 0;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin-bottom: 8px;
}

.markdown-content :deep(.markdown-blockquote) {
  margin: 16px 0;
  padding: 12px 20px;
  border-left: 4px solid var(--color-primary);
  background: var(--color-bg-secondary);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  color: var(--color-text-secondary);
}

.markdown-content :deep(.markdown-hr) {
  margin: 24px 0;
  border: none;
  border-top: 1px solid var(--color-border);
}

.markdown-content :deep(.inline-code) {
  padding: 2px 8px;
  font-size: 0.9em;
  background: var(--color-bg-tertiary);
  border-radius: 4px;
  font-family: var(--font-mono);
  color: #e83e8c;
}

.dark-mode .markdown-content :deep(.inline-code) {
  color: #f472b6;
}

/* 代码块样式 */
.markdown-content :deep(.code-block-wrapper) {
  margin: 16px 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: #1e1e1e;
  box-shadow: var(--shadow-sm);
}

.markdown-content :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #2d2d2d;
  border-bottom: 1px solid #404040;
}

.markdown-content :deep(.code-language) {
  font-size: 12px;
  font-weight: 600;
  color: #8b8b8b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.markdown-content :deep(.copy-code-btn) {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 500;
  color: #8b8b8b;
  background: transparent;
  border: 1px solid #404040;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--font-sans);
}

.markdown-content :deep(.copy-code-btn:hover) {
  background: #404040;
  color: #fff;
}

.markdown-content :deep(.copy-code-btn.copied) {
  background: var(--color-success);
  border-color: var(--color-success);
  color: #fff;
}

.markdown-content :deep(.code-block-content) {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
}

.markdown-content :deep(.code-block-content code) {
  font-family: var(--font-mono);
  font-size: 14px;
  line-height: 1.6;
}

/* 表格样式 */
.markdown-content :deep(.table-wrapper) {
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-content :deep(.markdown-table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.markdown-content :deep(.markdown-table th),
.markdown-content :deep(.markdown-table td) {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  text-align: left;
}

.markdown-content :deep(.markdown-table th) {
  background: var(--color-bg-secondary);
  font-weight: 600;
}

.markdown-content :deep(.markdown-table tr:nth-child(even)) {
  background: var(--color-bg-secondary);
}

/* 链接 */
.markdown-content :deep(.markdown-link) {
  color: var(--color-accent);
  text-decoration: none;
}

.markdown-content :deep(.markdown-link:hover) {
  text-decoration: underline;
}

/* ==================== 输入区域 ==================== */
.input-area {
  flex-shrink: 0;
  flex-grow: 0; /* 确保不会增长 */
  padding: 16px 24px 24px;
  background: var(--color-bg-primary);
  border-top: 1px solid var(--color-border);
  /* 关键：固定在底部，不受消息内容影响 */
  position: relative;
  z-index: 10;
}

.input-wrapper {
  max-width: 860px;
  margin: 0 auto;
}

.input-box {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-bg-secondary);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: all 0.2s ease;
}

.input-box.focused {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

.input-box textarea {
  flex: 1;
  min-height: 24px;
  max-height: 200px;
  padding: 0;
  font-size: 15px;
  line-height: 24px;
  color: var(--color-text-primary);
  background: transparent;
  border: none;
  resize: none;
  outline: none;
  font-family: var(--font-sans);
}

.input-box textarea::placeholder {
  color: var(--color-text-muted);
}

.input-box textarea:disabled {
  cursor: not-allowed;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.send-btn, .stop-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-btn {
  background: var(--color-primary);
  color: white;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-light);
  transform: scale(1.05);
}

.send-btn:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  cursor: not-allowed;
  transform: none;
}

.send-btn svg {
  width: 20px;
  height: 20px;
}

.stop-btn {
  background: var(--color-error);
  color: white;
}

.stop-btn:hover {
  background: #dc2626;
}

.stop-btn svg {
  width: 18px;
  height: 18px;
}

.input-hint {
  text-align: center;
  margin-top: 12px;
}

.input-hint span {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ==================== Toast ==================== */
.toast {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  font-size: 14px;
  font-weight: 500;
  z-index: 1000;
}

.toast.success {
  background: var(--color-success);
  color: white;
}

.toast.error {
  background: var(--color-error);
  color: white;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

/* ==================== 消息动画 ==================== */
.message-enter-active,
.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.message-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1024px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 64px;
    bottom: 0;
    z-index: 90;
    box-shadow: var(--shadow-lg);
  }
  
  .sidebar.collapsed {
    transform: translateX(-100%);
  }
}

@media (max-width: 768px) {
  .welcome-content {
    padding: 0 16px;
  }
  
  .welcome-title {
    font-size: 28px;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .input-area {
    padding: 12px 16px 16px;
  }
  
  .message-content {
    max-width: 85%;
  }
}

/* ==================== 滚动条 ==================== */
.messages-container::-webkit-scrollbar,
.chat-list::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track,
.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb,
.chat-list::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover,
.chat-list::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted);
}

/* ==================== 工具调用区域 ==================== */
.tool-call-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.tool-call-section.streaming {
  margin: 16px 0;
}

/* ==================== 工具调用状态胶囊 ==================== */
.tool-call-capsule {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin: 0;
  padding: 3px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 50px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(102, 126, 234, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  overflow: hidden;
  animation: capsuleAppear 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.tool-call-capsule.saved {
  animation: none;
}

@keyframes capsuleAppear {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.capsule-glow {
  position: absolute;
  inset: 0;
  border-radius: 50px;
  opacity: 0;
  transition: opacity 0.3s;
}

.tool-call-capsule.calling .capsule-glow {
  opacity: 1;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.4), transparent);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.8; }
}

.tool-call-capsule.success {
  background: linear-gradient(135deg, #0d3320 0%, #134e2a 50%, #1a6934 100%);
}

.tool-call-capsule.success .capsule-glow {
  opacity: 0.5;
  background: linear-gradient(90deg, transparent, rgba(16, 185, 129, 0.4), transparent);
}

.tool-call-capsule.failed {
  background: linear-gradient(135deg, #3d1515 0%, #5c1d1d 50%, #7a2424 100%);
}

.capsule-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.02) 100%);
  border-radius: 47px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.capsule-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  border-radius: 50%;
  border: 2px solid rgba(102, 126, 234, 0.5);
  font-size: 16px;
}

.tool-call-capsule.success .capsule-icon {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3) 0%, rgba(5, 150, 105, 0.3) 100%);
  border-color: rgba(16, 185, 129, 0.5);
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.check-icon {
  color: #10b981;
  font-size: 18px;
  font-weight: bold;
}

.error-icon {
  color: #ef4444;
  font-size: 18px;
  font-weight: bold;
}

.tool-emoji {
  font-size: 18px;
}

.capsule-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.capsule-title {
  font-size: 14px;
  font-weight: 600;
  color: #f8fafc;
  letter-spacing: 0.3px;
}

.capsule-detail {
  font-size: 12px;
  color: #94a3b8;
}

.capsule-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  color: #a5b4fc;
  letter-spacing: 0.5px;
}

.tool-call-capsule.success .capsule-badge {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.2) 100%);
  border-color: rgba(16, 185, 129, 0.3);
  color: #6ee7b7;
}

/* ==================== 工具标签 ==================== */
.tools-used {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tool-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  border: 1px solid rgba(99, 102, 241, 0.25);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #a5b4fc;
  transition: all 0.2s ease;
  backdrop-filter: blur(4px);
  box-shadow: 
    0 2px 8px rgba(99, 102, 241, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.tool-tag:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%);
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.tool-tag-icon {
  font-size: 14px;
}

.tool-tag-name {
  letter-spacing: 0.3px;
}

/* 浅色模式下的工具标签 */
.ai-assistant:not(.dark-mode) .tool-tag {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-color: rgba(99, 102, 241, 0.2);
  color: #6366f1;
}

.ai-assistant:not(.dark-mode) .tool-tag:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
}

/* 浅色模式下的胶囊 */
.ai-assistant:not(.dark-mode) .tool-call-capsule {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 50%, #bae6fd 100%);
  box-shadow: 
    0 4px 15px rgba(0, 0, 0, 0.08),
    0 0 20px rgba(59, 130, 246, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.ai-assistant:not(.dark-mode) .tool-call-capsule.success {
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 50%, #a7f3d0 100%);
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .capsule-content {
  background: linear-gradient(135deg, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0.7) 100%);
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .capsule-title {
  color: #1e40af;
}

.ai-assistant:not(.dark-mode) .tool-call-capsule.success .capsule-title {
  color: #065f46;
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .capsule-detail {
  color: #64748b;
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .capsule-badge {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(99, 102, 241, 0.15) 100%);
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
}

.ai-assistant:not(.dark-mode) .tool-call-capsule.success .capsule-badge {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(5, 150, 105, 0.15) 100%);
  border-color: rgba(16, 185, 129, 0.3);
  color: #059669;
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .capsule-icon {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(99, 102, 241, 0.2) 100%);
  border-color: rgba(59, 130, 246, 0.3);
}

.ai-assistant:not(.dark-mode) .tool-call-capsule.success .capsule-icon {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.2) 100%);
  border-color: rgba(16, 185, 129, 0.3);
}

.ai-assistant:not(.dark-mode) .tool-call-capsule .check-icon {
  color: #059669;
}

/* ==================== 推荐卡片区域 ==================== */
.recommendation-cards {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.6) 0%, rgba(30, 41, 59, 0.4) 100%);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  animation: cardsAppear 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes cardsAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.recommendation-cards.saved {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
}

.cards-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.cards-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.cards-count {
  font-size: 12px;
  padding: 4px 10px;
  background: var(--color-accent);
  color: white;
  border-radius: 12px;
  font-weight: 500;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

/* ==================== 推荐卡片 ==================== */
.recommend-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(145deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.02) 100%);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 16px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 6px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.ai-assistant:not(.dark-mode) .recommend-card {
  background: white;
  border-color: var(--color-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.recommend-card:hover {
  transform: translateY(-4px);
  border-color: var(--color-accent);
  box-shadow: 
    0 12px 24px rgba(0, 0, 0, 0.15),
    0 0 0 1px var(--color-accent),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.recommend-card.tutorial {
  --card-accent: #667eea;
}

.recommend-card.article {
  --card-accent: #10b981;
}

.card-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  background: var(--card-accent);
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.card-cover {
  height: 120px;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommend-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin: 0 0 12px 0;
  flex: 1;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.meta-author {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-author::before {
  content: '👤';
  font-size: 11px;
}

.meta-rating {
  display: flex;
  align-items: center;
  color: #fbbf24;
}

.meta-views {
  display: flex;
  align-items: center;
}

/* 深色模式适配 */
.dark-mode .tool-call-capsule {
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.5),
    0 0 60px rgba(102, 126, 234, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.dark-mode .recommendation-cards.saved {
  background: rgba(30, 41, 59, 0.6);
}

/* 响应式 */
@media (max-width: 768px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
  
  .tool-call-capsule {
    margin: 12px 0;
  }
  
  .capsule-content {
    padding: 10px 14px;
    gap: 10px;
  }
  
  .capsule-icon {
    width: 32px;
    height: 32px;
  }
  
  .capsule-title {
    font-size: 13px;
  }
  
  .capsule-detail {
    font-size: 11px;
  }
}
</style>
