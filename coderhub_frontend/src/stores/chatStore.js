/**
 * Chat Store - Pinia 状态管理
 * 
 * 管理 AI 对话的全局状态
 * 
 * @author CoderHub
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useChatStore = defineStore('chat', () => {
  // ==================== 状态 ====================
  
  /** 当前对话消息列表 */
  const messages = ref([])
  
  /** 当前对话 ID */
  const currentChatId = ref(null)
  
  /** 当前选择的模型 */
  const currentModel = ref('qwen-plus')
  
  /** 温度参数 */
  const temperature = ref(0.7)
  
  /** 是否正在流式输出 */
  const isStreaming = ref(false)
  
  /** 当前流式输出的内容 */
  const streamingContent = ref('')
  
  /** 主题模式 */
  const theme = ref('light') // 'light' | 'dark'
  
  /** 侧边栏是否展开 */
  const sidebarOpen = ref(false)

  // ==================== 计算属性 ====================
  
  /** 是否为深色模式 */
  const isDarkMode = computed(() => theme.value === 'dark')
  
  /** 消息数量 */
  const messageCount = computed(() => messages.value.length)
  
  /** 是否为空对话 */
  const isEmpty = computed(() => messages.value.length === 0)

  // ==================== 方法 ====================
  
  /**
   * 添加用户消息
   */
  function addUserMessage(content) {
    messages.value.push({
      id: generateId(),
      role: 'user',
      content,
      timestamp: new Date().toISOString()
    })
  }
  
  /**
   * 添加 AI 消息
   * @param {string} content - 消息内容
   * @param {Object} options - 可选参数
   * @param {Array} options.recommendations - 推荐内容列表
   * @param {Object} options.toolCall - 工具调用信息
   */
  function addAssistantMessage(content, options = {}) {
    const message = {
      id: generateId(),
      role: 'assistant',
      content,
      timestamp: new Date().toISOString(),
      model: currentModel.value
    }
    
    // 如果有推荐内容，添加到消息中
    if (options.recommendations && options.recommendations.length > 0) {
      message.recommendations = options.recommendations
    }
    
    // 如果有工具调用信息，添加到消息中
    if (options.toolCall) {
      message.toolCall = options.toolCall
    }
    
    messages.value.push(message)
  }
  
  /**
   * 更新最后一条 AI 消息
   */
  function updateLastAssistantMessage(content) {
    const lastIndex = messages.value.length - 1
    if (lastIndex >= 0 && messages.value[lastIndex].role === 'assistant') {
      messages.value[lastIndex].content = content
    }
  }
  
  /**
   * 删除消息
   */
  function deleteMessage(id) {
    const index = messages.value.findIndex(m => m.id === id)
    if (index >= 0) {
      messages.value.splice(index, 1)
    }
  }
  
  /**
   * 开始新对话
   */
  function startNewChat() {
    messages.value = []
    currentChatId.value = null // 新对话没有ID，发送消息时创建
    streamingContent.value = ''
    isStreaming.value = false
  }

  /**
   * 设置当前会话ID
   */
  function setCurrentChatId(id) {
    currentChatId.value = id
  }
  
  /**
   * 加载对话
   */
  function loadChat(chat) {
    messages.value = chat.messages || []
    currentChatId.value = chat.id
    currentModel.value = chat.model || 'qwen-plus'
  }
  
  /**
   * 设置流式状态
   */
  function setStreaming(streaming) {
    isStreaming.value = streaming
    if (!streaming) {
      streamingContent.value = ''
    }
  }
  
  /**
   * 追加流式内容
   */
  function appendStreamingContent(content) {
    streamingContent.value += content
  }
  
  /**
   * 切换主题
   */
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    // 持久化
    localStorage.setItem('coderhub_theme', theme.value)
    // 更新 document class
    updateDocumentTheme()
  }
  
  /**
   * 设置主题
   */
  function setTheme(newTheme) {
    theme.value = newTheme
    localStorage.setItem('coderhub_theme', newTheme)
    updateDocumentTheme()
  }
  
  /**
   * 更新文档主题类
   */
  function updateDocumentTheme() {
    if (theme.value === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
  
  /**
   * 切换侧边栏
   */
  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }
  
  /**
   * 设置模型
   */
  function setModel(model) {
    currentModel.value = model
  }
  
  /**
   * 设置温度
   */
  function setTemperature(temp) {
    temperature.value = temp
  }
  
  /**
   * 初始化 - 从 localStorage 恢复状态
   */
  function initialize() {
    const savedTheme = localStorage.getItem('coderhub_theme')
    if (savedTheme) {
      theme.value = savedTheme
    } else {
      // 检测系统主题
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      theme.value = prefersDark ? 'dark' : 'light'
    }
    updateDocumentTheme()
    
    const savedModel = localStorage.getItem('coderhub_model')
    if (savedModel) {
      currentModel.value = savedModel
    }
    
    // 初始化时不设置会话ID，让用户发送消息时从后端创建
    currentChatId.value = null
  }
  
  /**
   * 生成唯一 ID
   */
  function generateId() {
    return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  return {
    // 状态
    messages,
    currentChatId,
    currentModel,
    temperature,
    isStreaming,
    streamingContent,
    theme,
    sidebarOpen,
    
    // 计算属性
    isDarkMode,
    messageCount,
    isEmpty,
    
    // 方法
    addUserMessage,
    addAssistantMessage,
    updateLastAssistantMessage,
    deleteMessage,
    startNewChat,
    setCurrentChatId,
    loadChat,
    setStreaming,
    appendStreamingContent,
    toggleTheme,
    setTheme,
    toggleSidebar,
    setModel,
    setTemperature,
    initialize
  }
})

