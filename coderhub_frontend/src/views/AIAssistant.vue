<template>
  <div class="ai-assistant-page">
    <!-- 顶部导航栏 - 复用 Home 页面导航 -->
    <nav class="navbar">
      <div class="nav-content">
        <!-- Logo区域 -->
        <div class="nav-logo" @click="goToHome">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="8" fill="url(#gradient)" />
            <path d="M12 14L20 10L28 14V26L20 30L12 26V14Z" stroke="white" stroke-width="2" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="40" y2="40">
                <stop offset="0%" stop-color="#2c3e50" />
                <stop offset="100%" stop-color="#34495e" />
              </linearGradient>
            </defs>
          </svg>
          <span class="logo-text">CoderHub</span>
        </div>

        <!-- 导航菜单 -->
        <ul class="nav-menu">
          <li @click="goToHome">
            <a href="#">博客首页</a>
          </li>
          <li @click="goToTutorial">
            <a href="#">教程</a>
          </li>
          <li>
            <a href="#">项目</a>
          </li>
          <li class="active">
            <a href="#">智能体</a>
          </li>
        </ul>

        <!-- 模型选择器 -->
        <div class="model-selector">
          <select v-model="selectedModel" class="model-select">
            <option v-for="model in models" :key="model.id" :value="model.id">
              {{ model.name }}
            </option>
          </select>
        </div>

        <!-- 右侧操作区 -->
        <div class="nav-right">
          <button class="btn-write" @click="goToEditor">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            写文章
          </button>

          <!-- 用户头像 -->
          <div class="user-avatar-wrapper" @click="toggleUserMenu">
            <img :src="userInfo.avatar || '/default-avatar.png'" alt="avatar" class="user-avatar" />
            <div v-if="showUserMenu" class="user-menu">
              <a href="#" class="menu-item" @click.prevent="goToProfile">个人主页</a>
              <a href="#" class="menu-item">我的文章</a>
              <a href="#" class="menu-item">设置</a>
              <a href="#" class="menu-item" @click.prevent="handleLogout">退出登录</a>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容区 -->
    <div class="main-container">
      <!-- 左侧边栏 - 对话历史 -->
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <button class="new-chat-btn" @click="createNewChat">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span v-if="!sidebarCollapsed">新建对话</span>
          </button>
          <button class="toggle-sidebar-btn" @click="toggleSidebar">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M11 19L4 12L11 5M4 12H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <div v-if="!sidebarCollapsed" class="chat-history">
          <!-- 今天 -->
          <div class="history-section" v-if="todayChats.length > 0">
            <div class="section-title">今天</div>
            <div 
              v-for="chat in todayChats" 
              :key="chat.id"
              class="chat-item"
              :class="{ active: currentChatId === chat.id }"
              @click="switchChat(chat.id)"
            >
              <svg class="chat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="chat-title">{{ chat.title }}</span>
              <button class="delete-btn" @click.stop="deleteChat(chat.id)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 6H5H21M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- 昨天 -->
          <div class="history-section" v-if="yesterdayChats.length > 0">
            <div class="section-title">昨天</div>
            <div 
              v-for="chat in yesterdayChats" 
              :key="chat.id"
              class="chat-item"
              :class="{ active: currentChatId === chat.id }"
              @click="switchChat(chat.id)"
            >
              <svg class="chat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="chat-title">{{ chat.title }}</span>
              <button class="delete-btn" @click.stop="deleteChat(chat.id)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 6H5H21M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- 更早 -->
          <div class="history-section" v-if="olderChats.length > 0">
            <div class="section-title">更早</div>
            <div 
              v-for="chat in olderChats" 
              :key="chat.id"
              class="chat-item"
              :class="{ active: currentChatId === chat.id }"
              @click="switchChat(chat.id)"
            >
              <svg class="chat-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="chat-title">{{ chat.title }}</span>
              <button class="delete-btn" @click.stop="deleteChat(chat.id)">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 6H5H21M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧对话区 -->
      <main class="chat-area">
        <!-- 欢迎页面（无对话时） -->
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>CoderHub AI 助手</h2>
          <p>我可以帮助你解决编程问题、代码审查、技术咨询等</p>
          
          <div class="suggestion-cards">
            <div class="suggestion-card" @click="useSuggestion('解释一下 Vue 3 Composition API 的优势')">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 6.253V13L16 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
              <p>解释一下 Vue 3 Composition API 的优势</p>
            </div>
            <div class="suggestion-card" @click="useSuggestion('如何优化 React 组件性能？')">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <p>如何优化 React 组件性能？</p>
            </div>
            <div class="suggestion-card" @click="useSuggestion('Spring Boot 和 Spring Cloud 的区别')">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 7H4C2.89543 7 2 7.89543 2 9V19C2 20.1046 2.89543 21 4 21H20C21.1046 21 22 20.1046 22 19V9C22 7.89543 21.1046 7 20 7Z" stroke="currentColor" stroke-width="2"/>
                <path d="M16 21V5C16 4.46957 15.7893 3.96086 15.4142 3.58579C15.0391 3.21071 14.5304 3 14 3H10C9.46957 3 8.96086 3.21071 8.58579 3.58579C8.21071 3.96086 8 4.46957 8 5V21" stroke="currentColor" stroke-width="2"/>
              </svg>
              <p>Spring Boot 和 Spring Cloud 的区别</p>
            </div>
            <div class="suggestion-card" @click="useSuggestion('帮我写一个 Python 爬虫示例')">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <p>帮我写一个 Python 爬虫示例</p>
            </div>
          </div>
        </div>

        <!-- 对话消息列表 -->
        <div v-else class="messages-container">
          <div class="messages-list" ref="messagesList">
            <div 
              v-for="(msg, index) in messages" 
              :key="index"
              :class="['message-item', msg.role]"
            >
              <div class="message-avatar">
                <img v-if="msg.role === 'user'" :src="userInfo.avatar || '/default-avatar.png'" alt="用户">
                <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-sender">{{ msg.role === 'user' ? userInfo.username : 'AI 助手' }}</span>
                  <span class="message-time">{{ msg.timestamp }}</span>
                </div>
                <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>

            <!-- 加载中 -->
            <div v-if="isLoading" class="message-item assistant">
              <div class="message-avatar">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="message-sender">AI 助手</span>
                </div>
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 - 固定在底部 -->
        <div class="input-area">
          <div class="input-wrapper">
            <textarea
              v-model="userInput"
              @keydown.enter.exact.prevent="sendMessage"
              placeholder="输入您的问题...（Enter 发送，Shift+Enter 换行）"
              :disabled="isLoading"
              ref="inputTextarea"
              rows="1"
            ></textarea>
            <button 
              class="send-btn"
              @click="sendMessage"
              :disabled="isLoading || !userInput.trim()"
            >
              <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else class="loading-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 2A10 10 0 0 1 22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <div class="input-footer">
            <span class="hint">AI 可能会出错，请核实重要信息</span>
          </div>
        </div>
      </main>
    </div>

    <!-- 页脚 - 复用 Home 页面页脚 -->
    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>CoderHub</h4>
            <p>专业的技术分享社区</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <a href="#">关于我们</a>
            <a href="#">联系方式</a>
            <a href="#">使用协议</a>
          </div>
          <div class="footer-section">
            <h4>社区</h4>
            <a href="#">写文章</a>
            <a href="#">问答</a>
            <a href="#">活动</a>
          </div>
        </div>
        <div class="footer-copyright">
          © 2024 CoderHub. All rights reserved.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import { marked } from 'marked'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 自定义渲染器
const renderer = new marked.Renderer()

// 自定义代码块渲染
renderer.code = function(code, language) {
  const validLanguage = language && hljs.getLanguage(language) ? language : 'plaintext'
  const highlighted = hljs.highlight(code, { language: validLanguage }).value
  
  return `<pre><code class="hljs language-${validLanguage}">${highlighted}</code></pre>`
}

// 自定义标题渲染（添加间距）
renderer.heading = function(text, level) {
  return `<h${level} class="markdown-heading markdown-h${level}">${text}</h${level}>`
}

// 自定义段落渲染
renderer.paragraph = function(text) {
  return `<p class="markdown-paragraph">${text}</p>`
}

// 自定义列表渲染
renderer.list = function(body, ordered) {
  const type = ordered ? 'ol' : 'ul'
  return `<${type} class="markdown-list">${body}</${type}>`
}

// 自定义列表项渲染
renderer.listitem = function(text) {
  return `<li class="markdown-list-item">${text}</li>`
}

// 自定义分隔线渲染
renderer.hr = function() {
  return '<hr class="markdown-hr">'
}

// 自定义强调渲染
renderer.strong = function(text) {
  return `<strong class="markdown-strong">${text}</strong>`
}

// 配置 marked
marked.setOptions({
  renderer: renderer,
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false
})

// 用户信息
const userInfo = ref({
  username: '',
  avatar: ''
})

// 状态
const showUserMenu = ref(false)
const sidebarCollapsed = ref(false)
const selectedModel = ref('qwen-vl')
const currentChatId = ref(1)
const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const messagesList = ref(null)
const inputTextarea = ref(null)
let eventSource = null

// 可用模型列表
const models = ref([
  { id: 'qwen-vl', name: 'Qwen3-VL-235B (推荐)' },
  { id: 'gpt4', name: 'GPT-4' },
  { id: 'gpt35', name: 'GPT-3.5 Turbo' },
  { id: 'claude', name: 'Claude 3' },
  { id: 'deepseek', name: 'DeepSeek' }
])

// 聊天历史（Mock 数据）
const todayChats = ref([
  { id: 1, title: '新对话', date: '今天' }
])

const yesterdayChats = ref([
  { id: 2, title: 'Vue 3 性能优化', date: '昨天' },
  { id: 3, title: 'Spring Boot 最佳实践', date: '昨天' }
])

const olderChats = ref([
  { id: 4, title: 'React Hooks 详解', date: '3天前' },
  { id: 5, title: 'Python 异步编程', date: '5天前' },
  { id: 6, title: 'Docker 容器化部署', date: '1周前' }
])

// 生命周期
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})

// 方法
const goToHome = () => {
  router.push('/home')
}

const goToTutorial = () => {
  router.push('/dashboard')
}

const goToEditor = () => {
  router.push('/editor')
}

const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const createNewChat = () => {
  messages.value = []
  currentChatId.value = Date.now()
}

const switchChat = (chatId) => {
  currentChatId.value = chatId
  messages.value = []
}

const deleteChat = (chatId) => {
  if (confirm('确定要删除这个对话吗？')) {
    todayChats.value = todayChats.value.filter(chat => chat.id !== chatId)
    yesterdayChats.value = yesterdayChats.value.filter(chat => chat.id !== chatId)
    olderChats.value = olderChats.value.filter(chat => chat.id !== chatId)
  }
}

const useSuggestion = (text) => {
  userInput.value = text
  nextTick(() => {
    sendMessage()
  })
}

// Markdown 渲染
const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    // 使用 marked() 而不是 marked.parse()
    return marked(text || '')
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    return text.replace(/\n/g, '<br>')
  }
}

const getCurrentTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesList.value) {
      messagesList.value.scrollTop = messagesList.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const message = userInput.value.trim()
  if (!message || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    timestamp: getCurrentTime()
  })

  userInput.value = ''
  isLoading.value = true
  scrollToBottom()

  // 创建 AI 消息占位
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    timestamp: getCurrentTime()
  })

  try {
    const url = `http://localhost:8080/ai/stream/chat?message=${encodeURIComponent(message)}`
    
    eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
      const token = event.data
      if (token && token !== '[DONE]') {
        messages.value[aiMessageIndex].content += token
        scrollToBottom()
      }
    }

    eventSource.onerror = (error) => {
      console.error('EventSource 错误:', error)
      eventSource.close()
      isLoading.value = false
      
      if (messages.value[aiMessageIndex].content === '') {
        messages.value[aiMessageIndex].content = '抱歉，连接失败，请检查网络或后端服务是否正常运行。'
      }
    }

    eventSource.addEventListener('close', () => {
      eventSource.close()
      isLoading.value = false
    })

    setTimeout(() => {
      if (eventSource && eventSource.readyState !== EventSource.CLOSED) {
        eventSource.close()
        isLoading.value = false
        if (messages.value[aiMessageIndex].content === '') {
          messages.value[aiMessageIndex].content = '请求超时，请重试。'
        }
      }
    }, 180000)

  } catch (error) {
    console.error('发送消息失败:', error)
    isLoading.value = false
    messages.value[aiMessageIndex].content = '发送失败: ' + error.message
  }
}
</script>

<style scoped>
/* ==================== 全局样式 ==================== */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.ai-assistant-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

/* ==================== 导航栏 ==================== */
.navbar {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.nav-logo:hover {
  opacity: 0.8;
}

.nav-logo svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
}

.nav-menu {
  display: flex;
  list-style: none;
  gap: 4px;
  margin: 0;
}

.nav-menu li {
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.nav-menu li:hover {
  background: #f3f4f6;
}

.nav-menu li.active {
  background: #e5e7eb;
}

.nav-menu li a {
  text-decoration: none;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

.model-selector {
  margin-left: auto;
}

.model-select {
  padding: 8px 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
  font-weight: 500;
  cursor: pointer;
  outline: none;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-write {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-write:hover {
  background: #34495e;
}

.btn-write svg {
  width: 16px;
  height: 16px;
}

.user-avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e7eb;
}

.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 180px;
  overflow: hidden;
  z-index: 1000;
}

.menu-item {
  display: block;
  padding: 12px 16px;
  color: #374151;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.3s;
}

.menu-item:hover {
  background: #f9fafb;
}

/* ==================== 主容器 ==================== */
.main-container {
  flex: 1;
  display: flex;
  max-width: 1920px;
  width: 100%;
  margin: 0 auto;
  overflow: hidden;
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 8px;
}

.new-chat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.new-chat-btn:hover {
  background: #34495e;
}

.new-chat-btn svg {
  width: 18px;
  height: 18px;
}

.toggle-sidebar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.toggle-sidebar-btn:hover {
  background: #f9fafb;
}

.toggle-sidebar-btn svg {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.history-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  padding: 0 8px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  margin-bottom: 4px;
}

.chat-item:hover {
  background: #f9fafb;
}

.chat-item.active {
  background: #f3f4f6;
}

.chat-item .chat-icon {
  width: 18px;
  height: 18px;
  color: #6b7280;
  flex-shrink: 0;
}

.chat-title {
  flex: 1;
  font-size: 14px;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delete-btn {
  display: none;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-item:hover .delete-btn {
  display: flex;
}

.delete-btn:hover {
  background: #fee2e2;
}

.delete-btn svg {
  width: 16px;
  height: 16px;
  color: #ef4444;
}

/* ==================== 对话区域 ==================== */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  position: relative;
  overflow: hidden;
}

/* 欢迎屏幕 */
.welcome-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  overflow-y: auto;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-icon svg {
  width: 40px;
  height: 40px;
  color: #2c3e50;
}

.welcome-screen h2 {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.welcome-screen > p {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 40px 0;
}

.suggestion-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  max-width: 800px;
  width: 100%;
}

.suggestion-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-card:hover {
  border-color: #2c3e50;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.suggestion-card svg {
  width: 24px;
  height: 24px;
  color: #2c3e50;
}

.suggestion-card p {
  font-size: 14px;
  color: #374151;
  margin: 0;
  line-height: 1.5;
}

/* 消息容器 - 固定高度，内部滚动 */
.messages-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  min-height: 0;
}

.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e5e7eb;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-avatar svg {
  width: 20px;
  height: 20px;
  color: #2c3e50;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.message-sender {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.message-time {
  font-size: 12px;
  color: #9ca3af;
}

.message-text {
  font-size: 15px;
  line-height: 1.7;
  color: #374151;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* ==================== Markdown 样式增强 ==================== */

/* 标题样式 */
.message-text :deep(.markdown-heading) {
  font-weight: 600;
  line-height: 1.3;
  margin-top: 24px;
  margin-bottom: 16px;
  color: #1f2937;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.message-text :deep(.markdown-h1) {
  font-size: 1.875rem;
  margin-top: 0;
}

.message-text :deep(.markdown-h2) {
  font-size: 1.5rem;
}

.message-text :deep(.markdown-h3) {
  font-size: 1.25rem;
  border-bottom: none;
}

.message-text :deep(.markdown-h4) {
  font-size: 1.125rem;
  border-bottom: none;
}

/* 段落样式 */
.message-text :deep(.markdown-paragraph) {
  margin-bottom: 16px;
  line-height: 1.75;
}

/* 列表样式 */
.message-text :deep(.markdown-list) {
  margin-bottom: 16px;
  padding-left: 28px;
}

.message-text :deep(.markdown-list-item) {
  margin-bottom: 8px;
  line-height: 1.6;
}

/* 分隔线样式 */
.message-text :deep(.markdown-hr) {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 24px 0;
}

/* 强调文本 */
.message-text :deep(.markdown-strong) {
  font-weight: 600;
  color: #1f2937;
}

/* 行内代码 */
.message-text :deep(code:not(.hljs)) {
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.875em;
  color: #d63384;
  border: 1px solid #e5e7eb;
}

/* 代码块容器 */
.message-text :deep(pre) {
  background: #282c34;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
  border: 1px solid #3e4451;
}

/* 代码块内的代码 */
.message-text :deep(pre code.hljs) {
  background: transparent;
  padding: 0;
  color: #abb2bf;
  font-size: 14px;
  line-height: 1.6;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  display: block;
}

/* 引用块 */
.message-text :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  padding-left: 16px;
  margin: 16px 0;
  color: #6b7280;
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 4px;
}

/* 表格 */
.message-text :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.message-text :deep(th),
.message-text :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
}

.message-text :deep(th) {
  background: #f9fafb;
  font-weight: 600;
  color: #1f2937;
}

.message-text :deep(tr:nth-child(even)) {
  background: #fafafa;
}

/* 链接 */
.message-text :deep(a) {
  color: #3b82f6;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s;
}

.message-text :deep(a:hover) {
  border-bottom-color: #3b82f6;
}

/* 图片 */
.message-text :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

/* 修复第一个元素的上边距 */
.message-text :deep(*:first-child) {
  margin-top: 0 !important;
}

/* 修复最后一个元素的下边距 */
.message-text :deep(*:last-child) {
  margin-bottom: 0 !important;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* ==================== 输入区域 ==================== */
.input-area {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 20px;
  flex-shrink: 0;
}

.input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper textarea {
  flex: 1;
  min-height: 50px;
  max-height: 200px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 15px;
  font-family: inherit;
  color: #1f2937;
  resize: none;
  outline: none;
  transition: all 0.3s;
  background: #fafafa;
}

.input-wrapper textarea:focus {
  border-color: #2c3e50;
  background: white;
}

.input-wrapper textarea:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
}

.send-btn {
  width: 50px;
  height: 50px;
  background: #2c3e50;
  border: none;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.send-btn:not(:disabled):hover {
  background: #34495e;
  transform: translateY(-2px);
}

.send-btn:disabled {
  background: #e5e7eb;
  cursor: not-allowed;
}

.send-btn svg {
  width: 22px;
  height: 22px;
  color: white;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-footer {
  max-width: 900px;
  margin: 12px auto 0;
  text-align: center;
}

.hint {
  font-size: 12px;
  color: #9ca3af;
}

/* ==================== 页脚 ==================== */
.page-footer {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 32px 0;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 48px;
  margin-bottom: 32px;
}

.footer-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.footer-section p {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.footer-section a {
  display: block;
  font-size: 14px;
  color: #6b7280;
  text-decoration: none;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #2c3e50;
}

.footer-copyright {
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
}

/* ==================== 滚动条 ==================== */
.chat-history::-webkit-scrollbar,
.messages-list::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track,
.messages-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-history::-webkit-scrollbar-thumb,
.messages-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb:hover,
.messages-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .sidebar {
    width: 240px;
  }
  
  .suggestion-cards {
    grid-template-columns: 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .sidebar {
    position: fixed;
    left: 0;
    top: 60px;
    height: calc(100vh - 60px);
    z-index: 99;
    transform: translateX(-100%);
  }
  
  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }
  
  .btn-write span {
    display: none;
  }
}
</style>
