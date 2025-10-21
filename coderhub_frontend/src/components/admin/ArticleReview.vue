<template>
  <div class="article-review">
    <div class="review-header">
      <h2>文章审核</h2>
      <div class="stats">
        <div class="stat-item">
          <span class="stat-label">待审核</span>
          <span class="stat-value">{{ pendingArticles.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已发布</span>
          <span class="stat-value">{{ publishedArticles.length }}</span>
        </div>
      </div>
    </div>

    <div class="review-content">
      <!-- 左侧：待审核文章列表 -->
      <div class="article-column pending-column">
        <div class="column-header">
          <h3>待审核文章</h3>
          <button @click="refreshPending" class="btn-refresh">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M1 4V10H7M23 20V14H17M20.49 9C19.9828 7.56678 19.1209 6.28542 17.9845 5.27542C16.8482 4.26541 15.4745 3.55976 13.9917 3.22426C12.5089 2.88876 10.9652 2.93434 9.50481 3.35677C8.04437 3.77921 6.71475 4.56471 5.64 5.64L1 10M23 14L18.36 18.36C17.2853 19.4353 15.9556 20.2208 14.4952 20.6432C13.0348 21.0657 11.4911 21.1112 10.0083 20.7757C8.52547 20.4402 7.1518 19.7346 6.01547 18.7246C4.87913 17.7146 4.01717 16.4332 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <div v-if="pendingArticles.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M9 12H15M9 16H15M17 21H7C5.89543 21 5 20.1046 5 19V5C5 3.89543 5.89543 3 7 3H12.5858C12.851 3 13.1054 3.10536 13.2929 3.29289L18.7071 8.70711C18.8946 8.89464 19 9.149 19 9.41421V19C19 20.1046 18.1046 21 17 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>暂无待审核文章</p>
        </div>

        <div 
          v-for="article in pendingArticles" 
          :key="article.id"
          :class="['article-item', { active: selectedArticle?.id === article.id }]"
          @click="selectArticle(article)"
        >
          <div v-if="article.coverImage" class="article-thumb">
            <img :src="article.coverImage" alt="cover">
          </div>
          <div class="article-info">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-summary">{{ article.summary }}</p>
            <div class="article-meta">
              <span class="author">{{ article.username }}</span>
              <span class="dot">·</span>
              <span class="time">{{ formatTime(article.createTime) }}</span>
            </div>
            <div class="article-tags">
              <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.tagName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：文章详情预览 -->
      <div class="article-detail" v-if="selectedArticle">
        <div class="detail-header">
          <div class="detail-title-area">
            <h2>{{ selectedArticle.title }}</h2>
            <div class="detail-meta">
              <span>作者：{{ selectedArticle.username }}</span>
              <span>分类：{{ selectedArticle.categoryName }}</span>
              <span>创建时间：{{ formatFullTime(selectedArticle.createTime) }}</span>
            </div>
          </div>
          
          <div v-if="selectedArticle.auditStatus === 0" class="action-buttons">
            <button @click="approveArticle" class="btn-approve">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M9 11L12 14L22 4M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              通过审核
            </button>
            <button @click="showRejectDialog" class="btn-reject">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              拒绝
            </button>
          </div>
        </div>

        <div class="detail-content">
          <div v-if="markdownContent" class="markdown-viewer" v-html="renderedMarkdown"></div>
          <div v-else class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
        </div>
      </div>

      <div v-else class="no-selection">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M14 2V8H20M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>选择一篇文章查看详情</p>
      </div>

      <!-- 右侧：已发布文章列表 -->
      <div class="article-column published-column">
        <div class="column-header">
          <h3>已发布文章</h3>
          <button @click="refreshPublished" class="btn-refresh">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M1 4V10H7M23 20V14H17M20.49 9C19.9828 7.56678 19.1209 6.28542 17.9845 5.27542C16.8482 4.26541 15.4745 3.55976 13.9917 3.22426C12.5089 2.88876 10.9652 2.93434 9.50481 3.35677C8.04437 3.77921 6.71475 4.56471 5.64 5.64L1 10M23 14L18.36 18.36C17.2853 19.4353 15.9556 20.2208 14.4952 20.6432C13.0348 21.0657 11.4911 21.1112 10.0083 20.7757C8.52547 20.4402 7.1518 19.7346 6.01547 18.7246C4.87913 17.7146 4.01717 16.4332 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <div v-if="publishedArticles.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M9 12H15M9 16H15M17 21H7C5.89543 21 5 20.1046 5 19V5C5 3.89543 5.89543 3 7 3H12.5858C12.851 3 13.1054 3.10536 13.2929 3.29289L18.7071 8.70711C18.8946 8.89464 19 9.149 19 9.41421V19C19 20.1046 18.1046 21 17 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>暂无已发布文章</p>
        </div>

        <div 
          v-for="article in publishedArticles" 
          :key="article.id"
          :class="['article-item', { active: selectedArticle?.id === article.id }]"
          @click="selectArticle(article)"
        >
          <div v-if="article.coverImage" class="article-thumb">
            <img :src="article.coverImage" alt="cover">
          </div>
          <div class="article-info">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-summary">{{ article.summary }}</p>
            <div class="article-meta">
              <span class="author">{{ article.username }}</span>
              <span class="dot">·</span>
              <span class="time">{{ formatTime(article.publishTime) }}</span>
            </div>
            <div class="article-tags">
              <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.tagName }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝原因对话框 -->
    <div v-if="showRejectDialogFlag" class="dialog-overlay" @click.self="closeRejectDialog">
      <div class="dialog">
        <h3>拒绝审核</h3>
        <textarea 
          v-model="rejectReason"
          placeholder="请输入拒绝原因..."
          class="reject-textarea"
          maxlength="200"
        ></textarea>
        <div class="dialog-actions">
          <button @click="closeRejectDialog" class="btn-cancel">取消</button>
          <button @click="confirmReject" class="btn-confirm">确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

// 数据
const pendingArticles = ref([])
const publishedArticles = ref([])
const selectedArticle = ref(null)
const markdownContent = ref('')
const showRejectDialogFlag = ref(false)
const rejectReason = ref('')

// Markdown配置
marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true,
  gfm: true
})

// 渲染Markdown
const renderedMarkdown = computed(() => {
  if (!markdownContent.value) return ''
  return marked(markdownContent.value)
})

// 获取待审核文章
const fetchPendingArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/article/pending', {
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      pendingArticles.value = response.data.data
    }
  } catch (error) {
    console.error('获取待审核文章失败：', error)
  }
}

// 获取已发布文章
const fetchPublishedArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/article/published', {
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      publishedArticles.value = response.data.data
    }
  } catch (error) {
    console.error('获取已发布文章失败：', error)
  }
}

// 选择文章
const selectArticle = async (article) => {
  selectedArticle.value = article
  markdownContent.value = ''
  
  // 从OSS下载Markdown内容
  try {
    // 直接使用axios获取，不添加任何拦截器
    const response = await fetch(article.contentUrl)
    const text = await response.text()
    markdownContent.value = text
    console.log('加载的Markdown内容：', text.substring(0, 200))
  } catch (error) {
    console.error('加载文章内容失败：', error)
    markdownContent.value = '# 加载失败\n无法加载文章内容'
  }
}

// 审核通过
const approveArticle = async () => {
  if (!selectedArticle.value) return
  
  if (!confirm('确认通过此文章的审核？')) return
  
  try {
    const token = localStorage.getItem('token')
    const response = await axios.put(
      `/api/admin/article/approve/${selectedArticle.value.id}`,
      null,
      {
        headers: {
          authentication: token
        }
      }
    )
    
    if (response.data.code === 1) {
      alert('审核通过')
      selectedArticle.value = null
      markdownContent.value = ''
      refreshPending()
      refreshPublished()
    } else {
      alert('审核失败：' + response.data.msg)
    }
  } catch (error) {
    console.error('审核失败：', error)
    alert('审核失败')
  }
}

// 显示拒绝对话框
const showRejectDialog = () => {
  rejectReason.value = ''
  showRejectDialogFlag.value = true
}

// 关闭拒绝对话框
const closeRejectDialog = () => {
  showRejectDialogFlag.value = false
  rejectReason.value = ''
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('请输入拒绝原因')
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    const response = await axios.put(
      `/api/admin/article/reject/${selectedArticle.value.id}`,
      null,
      {
        params: {
          reason: rejectReason.value
        },
        headers: {
          authentication: token
        }
      }
    )
    
    if (response.data.code === 1) {
      alert('已拒绝审核')
      closeRejectDialog()
      selectedArticle.value = null
      markdownContent.value = ''
      refreshPending()
    } else {
      alert('操作失败：' + response.data.msg)
    }
  } catch (error) {
    console.error('拒绝审核失败：', error)
    alert('操作失败')
  }
}

// 刷新待审核列表
const refreshPending = () => {
  fetchPendingArticles()
}

// 刷新已发布列表
const refreshPublished = () => {
  fetchPublishedArticles()
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  
  try {
    // 处理各种时间格式
    let time
    if (Array.isArray(timeStr)) {
      // [2024, 10, 21, 18, 30, 45]
      time = new Date(timeStr[0], timeStr[1] - 1, timeStr[2], timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0)
    } else if (typeof timeStr === 'string') {
      time = new Date(timeStr)
    } else {
      time = new Date(timeStr)
    }
    
    if (isNaN(time.getTime())) {
      console.error('无效的时间格式：', timeStr)
      return '未知时间'
    }
    
    const now = new Date()
    const diff = now - time
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    
    return time.toLocaleDateString('zh-CN')
  } catch (e) {
    console.error('时间格式化错误：', e, timeStr)
    return '未知时间'
  }
}

// 格式化完整时间
const formatFullTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  
  try {
    let time
    if (Array.isArray(timeStr)) {
      time = new Date(timeStr[0], timeStr[1] - 1, timeStr[2], timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0)
    } else if (typeof timeStr === 'string') {
      time = new Date(timeStr)
    } else {
      time = new Date(timeStr)
    }
    
    if (isNaN(time.getTime())) {
      return '未知时间'
    }
    
    return time.toLocaleString('zh-CN')
  } catch (e) {
    return '未知时间'
  }
}

// 初始化
onMounted(() => {
  fetchPendingArticles()
  fetchPublishedArticles()
})
</script>

<style scoped>
.article-review {
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 头部 */
.review-header {
  background: white;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

/* 内容区 */
.review-content {
  flex: 1;
  display: grid;
  grid-template-columns: 300px 1fr 300px;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* 文章列表列 */
.article-column {
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.column-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.btn-refresh {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-refresh:hover {
  background: #e0e0e0;
}

.btn-refresh svg {
  width: 16px;
  height: 16px;
  color: #666;
}

/* 文章项 */
.article-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}

.article-item:hover {
  background: #f8f9fa;
}

.article-item.active {
  background: #e8f0fe;
}

.article-thumb {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
}

.article-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-item .article-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-item .article-summary {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.article-meta .dot {
  margin: 0 4px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.article-tags .tag {
  padding: 2px 6px;
  font-size: 10px;
  color: #666;
  background: #f0f0f0;
  border-radius: 3px;
}

/* 空状态 */
.empty-state,
.no-selection,
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.empty-state svg,
.no-selection svg {
  width: 48px;
  height: 48px;
  color: #d0d0d0;
  margin-bottom: 12px;
}

.empty-state p,
.no-selection p,
.loading-state p {
  margin: 0;
  font-size: 14px;
}

/* 文章详情 */
.article-detail {
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title-area h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.detail-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.btn-approve,
.btn-reject {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-approve {
  background: #10b981;
  color: white;
}

.btn-approve:hover {
  background: #059669;
}

.btn-reject {
  background: #ef4444;
  color: white;
}

.btn-reject:hover {
  background: #dc2626;
}

.btn-approve svg,
.btn-reject svg {
  width: 16px;
  height: 16px;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* Markdown查看器 */
.markdown-viewer {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

.markdown-viewer :deep(h1),
.markdown-viewer :deep(h2),
.markdown-viewer :deep(h3),
.markdown-viewer :deep(h4) {
  margin: 24px 0 16px;
  font-weight: 600;
}

.markdown-viewer :deep(h1) { font-size: 28px; }
.markdown-viewer :deep(h2) { font-size: 24px; }
.markdown-viewer :deep(h3) { font-size: 20px; }

.markdown-viewer :deep(p) {
  margin: 12px 0;
}

.markdown-viewer :deep(code) {
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #e83e8c;
}

.markdown-viewer :deep(pre) {
  background: #282c34;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-viewer :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
}

.markdown-viewer :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

/* 加载状态 */
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f0f0f0;
  border-top-color: #666;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 拒绝对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
}

.dialog h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.reject-textarea {
  width: 100%;
  height: 120px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
  transition: border-color 0.3s;
}

.reject-textarea:focus {
  border-color: #666;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-confirm {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: #ef4444;
  color: white;
}

.btn-confirm:hover {
  background: #dc2626;
}
</style>

