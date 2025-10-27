<template>
  <div class="article-detail-page">
    <!-- 顶部导航栏（复用Home页眉样式） -->
    <nav class="navbar">
      <div class="nav-content">
        <!-- Logo区域 -->
        <div class="nav-logo" @click="router.push('/home')">
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
          <li @click="router.push('/home')">
            <a href="#">博客首页</a>
          </li>
          <li>
            <a href="#">教程</a>
          </li>
          <li>
            <a href="#">项目</a>
          </li>
          <li>
            <a href="#">智能体</a>
          </li>
        </ul>

        <!-- 右侧操作区 -->
        <div class="nav-right">
          <button class="btn-write" @click="router.push('/article/editor')">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            写文章
          </button>

          <!-- 用户头像 -->
          <div class="user-avatar-wrapper" @click="toggleUserMenu">
            <img :src="currentUser?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" class="user-avatar" />
            <div v-if="showUserMenu" class="user-menu">
              <a href="#" class="menu-item" @click.prevent="router.push('/profile')">个人主页</a>
              <a href="#" class="menu-item">我的文章</a>
              <a href="#" class="menu-item">设置</a>
              <a href="#" class="menu-item" @click.prevent="handleLogout">退出登录</a>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主体内容区 -->
    <div class="article-container">
      <!-- 左侧：文章内容 + 评论 -->
      <main class="article-main">
        <!-- 文章头部信息 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          
          <div class="article-meta">
            <div class="author-info">
              <img :src="author.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + author.username" class="author-avatar" />
              <div class="author-details">
                <span class="author-name">{{ author.username }}</span>
                <div class="meta-info">
                  <span>{{ formatTime(article.publishTime) }}</span>
                  <span class="dot">·</span>
                  <span>{{ article.viewCount }} 阅读</span>
                  <span class="dot">·</span>
                  <span class="category-badge">{{ article.categoryName }}</span>
                </div>
              </div>
            </div>
            
            <span v-if="article.isOriginal === 1" class="original-badge">原创</span>
          </div>

          <!-- 文章标签 -->
          <div class="article-tags" v-if="article.tags && article.tags.length > 0">
            <span v-for="tag in article.tags" :key="tag.id" class="article-tag">
              {{ tag.tagName }}
            </span>
          </div>
        </div>

        <!-- 文章封面 -->
        <div v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" alt="封面" />
        </div>

        <!-- 文章正文 -->
        <div class="article-content markdown-body" v-html="renderedContent"></div>

        <!-- 互动栏 -->
        <div class="article-actions">
          <button 
            class="action-btn like-btn" 
            :class="{ active: isLiked }"
            @click="toggleLike"
          >
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" stroke="currentColor" stroke-width="2" fill="currentColor"/>
            </svg>
            <span>{{ article.likeCount }}</span>
          </button>
          
          <button 
            class="action-btn collect-btn" 
            :class="{ active: isCollected }"
            @click="toggleCollect"
          >
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z" stroke="currentColor" stroke-width="2" fill="currentColor"/>
            </svg>
            <span>{{ article.collectCount }}</span>
          </button>
          
          <button class="action-btn comment-btn" @click="scrollToComments">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>{{ article.commentCount }}</span>
          </button>
          
          <button class="action-btn share-btn" @click="shareArticle">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM18 22a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>分享</span>
          </button>
        </div>

        <!-- 评论区 -->
        <div ref="commentsSection" class="comments-section">
          <div class="comments-header">
            <h3>评论 ({{ article.commentCount }})</h3>
          </div>
          
          <!-- 评论输入框 -->
          <div class="comment-input-area">
            <img :src="currentUser?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" class="comment-avatar" />
            <div class="comment-input-wrapper">
              <textarea 
                v-model="commentInput"
                placeholder="写下你的评论..."
                class="comment-input"
                rows="3"
              ></textarea>
              <div class="comment-actions">
                <button class="btn-cancel" @click="commentInput = ''">取消</button>
                <button class="btn-submit" @click="submitComment" :disabled="!commentInput.trim()">发布</button>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div v-if="comments.length === 0" class="no-comments">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" stroke="currentColor" stroke-width="2"/>
              </svg>
              <p>暂无评论，快来抢沙发吧~</p>
            </div>
            
            <div v-else v-for="comment in comments" :key="comment.id" class="comment-item">
              <img :src="comment.avatar" class="comment-user-avatar" />
              <div class="comment-content-wrapper">
                <div class="comment-user-name">{{ comment.username }}</div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-footer">
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <button class="comment-reply-btn">回复</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧：作者卡片 + 推荐 -->
      <aside class="article-sidebar">
        <!-- 作者卡片 -->
        <div class="author-card">
          <div class="author-card-header">
            <img :src="author.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + author.username" class="author-card-avatar" />
            <div class="author-card-info">
              <h4 class="author-card-name">{{ author.username }}</h4>
              <p class="author-card-level">{{ author.userLevel === 1 ? '普通用户' : '管理员' }}</p>
            </div>
          </div>
          
          <div class="author-card-stats">
            <div class="stat-item">
              <span class="stat-value">{{ authorStats.articleCount }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ authorStats.followCount }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ authorStats.fansCount }}</span>
              <span class="stat-label">粉丝</span>
            </div>
          </div>
          
          <button v-if="!isAuthor" class="btn-follow" :class="{ following: isFollowing }" @click="toggleFollow">
            {{ isFollowing ? '已关注' : '+ 关注' }}
          </button>
        </div>

        <!-- 相关推荐 -->
        <div class="recommend-card">
          <h3 class="recommend-title">相关推荐</h3>
          <div class="recommend-list">
            <div v-for="item in relatedArticles" :key="item.id" class="recommend-item" @click="goToArticle(item.id)">
              <h4 class="recommend-item-title">{{ item.title }}</h4>
              <div class="recommend-item-meta">
                <span>{{ item.viewCount }} 阅读</span>
                <span class="dot">·</span>
                <span>{{ item.likeCount }} 点赞</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 页脚 -->
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 当前用户信息
const currentUser = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 用户菜单
const showUserMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

// 文章数据
const article = ref({
  id: '',
  title: '加载中...',
  content: '',
  contentUrl: '',
  coverImage: '',
  summary: '',
  categoryName: '',
  tags: [],
  viewCount: 0,
  likeCount: 0,
  commentCount: 0,
  collectCount: 0,
  isOriginal: 0,
  publishTime: null,
  userId: ''
})

// 作者信息
const author = ref({
  id: '',
  username: '加载中...',
  avatar: '',
  userLevel: 1
})

// 作者统计
const authorStats = ref({
  articleCount: 0,
  followCount: 0,
  fansCount: 0
})

// 互动状态
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)

// 评论相关
const commentInput = ref('')
const comments = ref([])
const commentsSection = ref(null)

// 相关推荐
const relatedArticles = ref([])

// 渲染的Markdown内容
const renderedContent = ref('')

// 是否是作者本人
const isAuthor = computed(() => {
  return currentUser.value.id === author.value.id
})

// 配置marked
marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  langPrefix: 'hljs language-',
  breaks: true,
  gfm: true
})

// 获取文章详情
const fetchArticleDetail = async () => {
  try {
    const articleId = route.params.id
    const token = localStorage.getItem('token')
    
    const response = await axios.get(`/api/article/${articleId}`, {
      headers: { authentication: token }
    })
    
    if (response.data.code === 1) {
      article.value = response.data.data
      
      // 获取作者信息
      await fetchAuthorInfo(article.value.userId)
      
      // 加载Markdown内容
      if (article.value.contentUrl) {
        await loadMarkdownContent(article.value.contentUrl)
      }
      
      // 获取相关推荐
      fetchRelatedArticles()
    }
  } catch (error) {
    console.error('获取文章详情失败：', error)
  }
}

// 加载Markdown内容
const loadMarkdownContent = async (url) => {
  try {
    const response = await fetch(url)
    const markdown = await response.text()
    renderedContent.value = marked(markdown)
  } catch (error) {
    console.error('加载文章内容失败：', error)
    renderedContent.value = '<p style="color: red;">文章内容加载失败</p>'
  }
}

// 获取作者信息
const fetchAuthorInfo = async (userId) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/user/${userId}`, {
      headers: { authentication: token }
    })
    
    if (response.data.code === 1) {
      author.value = response.data.data
      
      // 获取作者统计（这里暂时用模拟数据）
      authorStats.value = {
        articleCount: Math.floor(Math.random() * 100),
        followCount: Math.floor(Math.random() * 50),
        fansCount: Math.floor(Math.random() * 200)
      }
    }
  } catch (error) {
    console.error('获取作者信息失败：', error)
  }
}

// 获取相关推荐
const fetchRelatedArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/article/list', {
      params: { categoryId: article.value.categoryId },
      headers: { authentication: token }
    })
    
    if (response.data.code === 1) {
      relatedArticles.value = response.data.data
        .filter(item => item.id !== article.value.id)
        .slice(0, 5)
    }
  } catch (error) {
    console.error('获取相关推荐失败：', error)
  }
}

// 点赞
const toggleLike = () => {
  isLiked.value = !isLiked.value
  article.value.likeCount += isLiked.value ? 1 : -1
  // TODO: 调用后端接口
}

// 收藏
const toggleCollect = () => {
  isCollected.value = !isCollected.value
  article.value.collectCount += isCollected.value ? 1 : -1
  // TODO: 调用后端接口
}

// 关注
const toggleFollow = () => {
  isFollowing.value = !isFollowing.value
  // TODO: 调用后端接口
}

// 分享
const shareArticle = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    alert('链接已复制到剪贴板！')
  })
}

// 滚动到评论区
const scrollToComments = () => {
  commentsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

// 提交评论
const submitComment = () => {
  if (!commentInput.value.trim()) return
  
  // TODO: 调用后端接口
  comments.value.unshift({
    id: Date.now(),
    username: currentUser.value.username,
    avatar: currentUser.value.avatar,
    content: commentInput.value,
    createTime: new Date()
  })
  
  article.value.commentCount++
  commentInput.value = ''
}

// 跳转到文章
const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
  // 重新加载文章
  nextTick(() => {
    fetchArticleDetail()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  })
}

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  
  try {
    let time
    if (Array.isArray(timeStr)) {
      time = new Date(timeStr[0], timeStr[1] - 1, timeStr[2], timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0)
    } else {
      time = new Date(timeStr)
    }
    
    if (isNaN(time.getTime())) return '未知时间'
    
    const now = new Date()
    const diff = now - time
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (days > 7) {
      return `${time.getFullYear()}-${String(time.getMonth() + 1).padStart(2, '0')}-${String(time.getDate()).padStart(2, '0')}`
    } else if (days > 0) {
      return `${days}天前`
    } else if (hours > 0) {
      return `${hours}小时前`
    } else if (minutes > 0) {
      return `${minutes}分钟前`
    } else {
      return '刚刚'
    }
  } catch (e) {
    return '未知时间'
  }
}

onMounted(() => {
  fetchArticleDetail()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* ==================== 页头（复用Home样式） ==================== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

/* Logo */
.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.nav-logo:hover {
  transform: scale(1.05);
}

.nav-logo svg {
  width: 40px;
  height: 40px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 8px;
}

.nav-menu li {
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-menu li:hover {
  background: #f0f4f8;
}

.nav-menu li.active {
  background: #2c3e50;
  color: white;
}

.nav-menu li.active a {
  color: white;
}

.nav-menu a {
  text-decoration: none;
  color: #2c3e50;
  font-weight: 500;
  font-size: 15px;
}

/* 右侧操作区 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-write {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-write:hover {
  background: #34495e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.3);
}

.btn-write svg {
  width: 18px;
  height: 18px;
}

/* 用户头像和菜单 */
.user-avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
  transition: all 0.3s;
}

.user-avatar:hover {
  border-color: #2c3e50;
  transform: scale(1.1);
}

.user-menu {
  position: absolute;
  right: 0;
  top: 50px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 160px;
  overflow: hidden;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-item {
  display: block;
  padding: 12px 20px;
  color: #2c3e50;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.3s;
}

.menu-item:hover {
  background: #f0f4f8;
}

/* ==================== 主体容器 ==================== */
.article-container {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  flex: 1;
}

/* ==================== 左侧主体 ==================== */
.article-main {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 文章头部 */
.article-header {
  margin-bottom: 32px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 24px 0;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.meta-info .dot {
  width: 3px;
  height: 3px;
  background: #ccc;
  border-radius: 50%;
}

.category-badge {
  padding: 2px 8px;
  background: #f0f4ff;
  color: #667eea;
  border-radius: 4px;
  font-size: 12px;
}

.original-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.article-tag {
  padding: 6px 14px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0fe 100%);
  color: #667eea;
  border: 1.5px solid #667eea;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
}

/* 封面 */
.article-cover {
  margin-bottom: 32px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

/* 文章正文 */
.article-content {
  margin-bottom: 48px;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

/* Markdown样式 */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin-top: 32px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body h1 {
  font-size: 28px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.markdown-body h2 {
  font-size: 24px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.markdown-body h3 {
  font-size: 20px;
}

.markdown-body p {
  margin-bottom: 16px;
}

.markdown-body code {
  padding: 2px 6px;
  background: #f6f8fa;
  border-radius: 4px;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.markdown-body pre {
  padding: 0;
  background: #282c34;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  position: relative;
}

.markdown-body pre::before {
  content: '';
  display: block;
  height: 36px;
  background: #21252b;
  border-bottom: 1px solid #181a1f;
}

.markdown-body pre::after {
  content: '●●●';
  position: absolute;
  top: 12px;
  left: 16px;
  color: #4b5363;
  font-size: 12px;
  letter-spacing: 4px;
}

.markdown-body pre code {
  background: none;
  padding: 20px;
  color: #abb2bf;
  display: block;
  font-size: 14px;
  line-height: 1.6;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-body blockquote {
  padding: 12px 20px;
  border-left: 4px solid #667eea;
  background: #f8f9fa;
  margin: 16px 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 28px;
  margin-bottom: 16px;
}

.markdown-body li {
  margin-bottom: 8px;
}

.markdown-body a {
  color: #667eea;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

/* 互动栏 */
.article-actions {
  display: flex;
  gap: 16px;
  padding: 24px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 48px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid #e8e8e8;
  border-radius: 24px;
  background: white;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.action-btn:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.action-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.like-btn.active svg path,
.collect-btn.active svg path {
  fill: white;
}

/* 评论区 */
.comments-section {
  margin-top: 48px;
}

.comments-header {
  margin-bottom: 24px;
}

.comments-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.comment-input-area {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  transition: border-color 0.3s;
}

.comment-input:focus {
  border-color: #667eea;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-submit {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #999;
}

.no-comments svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-reply-btn {
  font-size: 12px;
  color: #667eea;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.comment-reply-btn:hover {
  text-decoration: underline;
}

/* ==================== 右侧边栏 ==================== */
.article-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 作者卡片 */
.author-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 80px;
}

.author-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.author-card-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
}

.author-card-info {
  flex: 1;
}

.author-card-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.author-card-level {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.author-card-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  margin-bottom: 16px;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.btn-follow {
  width: 100%;
  padding: 10px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-follow:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-follow.following {
  background: #f0f0f0;
  color: #666;
}

/* 相关推荐 */
.recommend-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.recommend-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommend-item {
  cursor: pointer;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.recommend-item:hover {
  background: #f8f9fa;
}

.recommend-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommend-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.recommend-item-meta .dot {
  width: 3px;
  height: 3px;
  background: #ccc;
  border-radius: 50%;
}

/* ==================== 页脚 ==================== */
.page-footer {
  background: #2c3e50;
  color: white;
  padding: 40px 0 20px;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 32px;
}

.footer-section h4 {
  font-size: 16px;
  margin: 0 0 16px 0;
}

.footer-section p {
  font-size: 14px;
  color: #bdc3c7;
  margin: 0;
}

.footer-section a {
  display: block;
  font-size: 14px;
  color: #bdc3c7;
  text-decoration: none;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: white;
}

.footer-copyright {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  font-size: 14px;
  color: #bdc3c7;
}

/* 响应式 */
@media (max-width: 1200px) {
  .article-container {
    grid-template-columns: 1fr;
  }
  
  .article-sidebar {
    display: none;
  }
}
</style>

