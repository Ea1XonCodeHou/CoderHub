<template>
  <div class="project-detail-container">
    <!-- 导航栏 -->
    <NavBar :showWriteBtn="false" />
    
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    
    <!-- 项目不存在 -->
    <div v-else-if="!project" class="error-state">
      <h2>😕 项目不存在或无权访问</h2>
      <button @click="router.push('/projects')">返回项目列表</button>
    </div>
    
    <!-- 项目详情 -->
    <div v-else class="project-content">
      <!-- 项目头部 -->
      <header class="project-header">
        <div class="header-main">
          <div class="project-meta">
            <div class="breadcrumb">
              <router-link to="/projects">项目</router-link>
              <span class="separator">/</span>
              <span class="current">{{ project.projectName }}</span>
            </div>
            <h1 class="project-title">
              <span class="project-icon">📦</span>
              {{ project.projectName }}
              <span v-if="project.isOpenSource" class="open-source-badge">开源</span>
            </h1>
            <p class="project-desc">{{ project.shortDescription }}</p>
          </div>
          
          <div class="project-actions">
            <a v-if="project.gitUrl" :href="project.gitUrl" target="_blank" class="btn-action github">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              GitHub
            </a>
            <a v-if="project.demoUrl" :href="project.demoUrl" target="_blank" class="btn-action demo">
              🌐 在线演示
            </a>
            <a v-if="project.projectFileUrl" :href="project.projectFileUrl" class="btn-action download">
              📥 下载源码
            </a>
          </div>
        </div>
        
        <!-- 统计信息 -->
        <div class="project-stats">
          <div class="stat-item">
            <span class="stat-icon">👁️</span>
            <span class="stat-value">{{ formatNumber(project.viewCount || 0) }}</span>
            <span class="stat-label">浏览</span>
          </div>
          <div class="stat-item" v-if="project.githubStars">
            <span class="stat-icon">⭐</span>
            <span class="stat-value">{{ formatNumber(project.githubStars) }}</span>
            <span class="stat-label">Stars</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">📂</span>
            <span class="stat-value">{{ project.categoryName }}</span>
            <span class="stat-label">分类</span>
          </div>
        </div>
      </header>
      
      <!-- 主体内容 -->
      <div class="project-body">
        <!-- 左侧：README -->
        <main class="readme-section">
          <div class="readme-header">
            <span class="readme-icon">📄</span>
            <span class="readme-title">README.md</span>
          </div>
          <div class="readme-content" v-html="renderedReadme"></div>
        </main>
        
        <!-- 右侧：信息栏 -->
        <aside class="info-sidebar">
          <!-- 封面图 -->
          <div class="cover-section" v-if="project.coverImage">
            <img :src="project.coverImage" :alt="project.projectName" class="cover-image" />
          </div>
          
          <!-- 关于 -->
          <div class="info-card">
            <h3>关于项目</h3>
            <p class="about-desc">{{ project.shortDescription }}</p>
            
            <div class="info-links">
              <a v-if="project.gitUrl" :href="project.gitUrl" target="_blank" class="info-link">
                <svg viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                  <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
                Git 仓库
              </a>
              <a v-if="project.demoUrl" :href="project.demoUrl" target="_blank" class="info-link">
                🔗 在线演示
              </a>
            </div>
          </div>
          
          <!-- 技术栈 -->
          <div class="info-card">
            <h3>技术栈</h3>
            <div class="tech-stack">
              <span v-for="tech in project.techStacks" :key="tech.id" class="tech-tag" :style="{ background: getTechColor(tech.techName) }">
                {{ tech.techName }}
              </span>
            </div>
          </div>
          
          <!-- 作者信息 -->
          <div class="info-card">
            <h3>作者</h3>
            <div class="author-info">
              <img :src="project.author?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" :alt="project.author?.username" class="author-avatar" />
              <div class="author-details">
                <span class="author-name">{{ project.author?.username || '匿名用户' }}</span>
                <span class="author-role">开发者</span>
              </div>
            </div>
          </div>
          
          <!-- 项目截图 -->
          <div class="info-card" v-if="project.screenshots && project.screenshots.length > 0">
            <h3>项目截图</h3>
            <div class="screenshots-grid">
              <img 
                v-for="(img, index) in project.screenshots" 
                :key="index" 
                :src="img" 
                :alt="`截图 ${index + 1}`"
                class="screenshot-thumb"
                @click="openLightbox(index)"
              />
            </div>
          </div>
          
          <!-- 发布信息 -->
          <div class="info-card">
            <h3>发布信息</h3>
            <div class="publish-info">
              <div class="info-row">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatDate(project.createdAt) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">更新时间</span>
                <span class="info-value">{{ formatDate(project.updatedAt) }}</span>
              </div>
              <div class="info-row" v-if="project.fileSize">
                <span class="info-label">源码大小</span>
                <span class="info-value">{{ formatFileSize(project.fileSize) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">开源状态</span>
                <span class="info-value">{{ project.isOpenSource ? '✅ 开源' : '🔒 私有' }}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
      
      <!-- 评论区预留 -->
      <section class="comments-section">
        <h2>💬 评论区</h2>
        <div class="comments-placeholder">
          <p>评论功能即将上线，敬请期待...</p>
        </div>
      </section>
    </div>
    
    <!-- 图片灯箱 -->
    <Transition name="fade">
      <div v-if="lightboxVisible" class="lightbox" @click="closeLightbox">
        <button class="lightbox-close" @click="closeLightbox">×</button>
        <button class="lightbox-prev" @click.stop="prevImage">‹</button>
        <img :src="currentLightboxImage" alt="preview" @click.stop />
        <button class="lightbox-next" @click.stop="nextImage">›</button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import NavBar from '@/components/NavBar.vue'
import { projectApi } from '@/api/projectApi'

const route = useRoute()
const router = useRouter()

// 响应式数据
const project = ref(null)
const isLoading = ref(true)
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)

// 渲染后的README
const renderedReadme = computed(() => {
  if (!project.value?.detailedDescription) {
    return '<p>暂无详细描述</p>'
  }
  return marked(project.value.detailedDescription)
})

// 当前灯箱图片
const currentLightboxImage = computed(() => {
  if (!project.value?.screenshots) return ''
  return project.value.screenshots[lightboxIndex.value] || ''
})

// 加载项目详情
async function loadProject() {
  isLoading.value = true
  try {
    const id = route.params.id
    const data = await projectApi.getProjectDetail(id)
    project.value = data
  } catch (e) {
    console.error('加载项目失败:', e)
    project.value = null
  } finally {
    isLoading.value = false
  }
}

// 格式化数字
function formatNumber(num) {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化日期（支持Java LocalDateTime数组格式）
function formatDate(dateInput) {
  if (!dateInput) return '-'
  
  try {
    let date
    
    // 处理Java LocalDateTime数组格式 [2024, 12, 29, 15, 23, 42, 756000000]
    if (Array.isArray(dateInput)) {
      const [year, month, day, hour, minute, second] = dateInput
      date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
    }
    // 处理ISO字符串或时间戳
    else {
      date = new Date(dateInput)
    }
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '-'
    }
    
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch (e) {
    console.error('日期格式化失败:', e, dateInput)
    return '-'
  }
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

// 技术栈颜色
function getTechColor(tech) {
  const colorMap = {
    'Vue': 'linear-gradient(135deg, #42b883 0%, #35495e 100%)',
    'Vue 3': 'linear-gradient(135deg, #42b883 0%, #35495e 100%)',
    'React': 'linear-gradient(135deg, #61dafb 0%, #20232a 100%)',
    'Spring Boot': 'linear-gradient(135deg, #6db33f 0%, #4a7c2f 100%)',
    'Node.js': 'linear-gradient(135deg, #68a063 0%, #3c6e2f 100%)',
    'Python': 'linear-gradient(135deg, #3776ab 0%, #ffd343 100%)',
    'TypeScript': 'linear-gradient(135deg, #3178c6 0%, #235a97 100%)',
    'MySQL': 'linear-gradient(135deg, #4479a1 0%, #00758f 100%)',
    'Redis': 'linear-gradient(135deg, #dc382d 0%, #a02e24 100%)',
    'Docker': 'linear-gradient(135deg, #2496ed 0%, #1d78b5 100%)',
  }
  for (const [key, color] of Object.entries(colorMap)) {
    if (tech.includes(key) || key.includes(tech)) return color
  }
  return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 灯箱功能
function openLightbox(index) {
  lightboxIndex.value = index
  lightboxVisible.value = true
}

function closeLightbox() {
  lightboxVisible.value = false
}

function prevImage() {
  if (lightboxIndex.value > 0) {
    lightboxIndex.value--
  } else {
    lightboxIndex.value = project.value.screenshots.length - 1
  }
}

function nextImage() {
  if (lightboxIndex.value < project.value.screenshots.length - 1) {
    lightboxIndex.value++
  } else {
    lightboxIndex.value = 0
  }
}

onMounted(() => {
  loadProject()
})
</script>

<style scoped>
.project-detail-container {
  min-height: 100vh;
  background: #f6f8fa;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e1e4e8;
  border-top-color: #0366d6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state h2 {
  margin-bottom: 20px;
  color: #586069;
}

.error-state button {
  padding: 12px 24px;
  background: #0366d6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.project-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
}

/* 头部 */
.project-header {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  flex-wrap: wrap;
}

.breadcrumb {
  font-size: 14px;
  color: #586069;
  margin-bottom: 8px;
}

.breadcrumb a {
  color: #0366d6;
  text-decoration: none;
}

.breadcrumb .separator {
  margin: 0 8px;
}

.project-title {
  font-size: 28px;
  font-weight: 600;
  color: #24292e;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.project-icon {
  font-size: 32px;
}

.open-source-badge {
  font-size: 12px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #28a745 0%, #22863a 100%);
  color: white;
  border-radius: 20px;
  font-weight: 500;
}

.project-desc {
  color: #586069;
  font-size: 16px;
  max-width: 600px;
}

.project-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}

.btn-action.github {
  background: #24292e;
  color: white;
}

.btn-action.github:hover {
  background: #2f363d;
}

.btn-action.github svg {
  width: 18px;
  height: 18px;
}

.btn-action.demo {
  background: #0366d6;
  color: white;
}

.btn-action.demo:hover {
  background: #0256b9;
}

.btn-action.download {
  background: #28a745;
  color: white;
}

.btn-action.download:hover {
  background: #22863a;
}

.project-stats {
  display: flex;
  gap: 32px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e1e4e8;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-icon {
  font-size: 18px;
}

.stat-value {
  font-weight: 600;
  color: #24292e;
}

.stat-label {
  color: #586069;
  font-size: 14px;
}

/* 主体 */
.project-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

@media (max-width: 960px) {
  .project-body {
    grid-template-columns: 1fr;
  }
}

/* README */
.readme-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.readme-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: #f6f8fa;
  border-bottom: 1px solid #e1e4e8;
  font-weight: 500;
}

.readme-content {
  padding: 32px;
  line-height: 1.8;
  color: #24292e;
}

.readme-content :deep(h1),
.readme-content :deep(h2),
.readme-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.readme-content :deep(h1) { font-size: 28px; }
.readme-content :deep(h2) { font-size: 22px; }
.readme-content :deep(h3) { font-size: 18px; }

.readme-content :deep(p) {
  margin-bottom: 16px;
}

.readme-content :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 90%;
}

.readme-content :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.readme-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

.readme-content :deep(ul),
.readme-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.readme-content :deep(li) {
  margin-bottom: 8px;
}

.readme-content :deep(blockquote) {
  border-left: 4px solid #0366d6;
  padding-left: 16px;
  margin: 16px 0;
  color: #586069;
}

.readme-content :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.readme-content :deep(a:hover) {
  text-decoration: underline;
}

.readme-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

/* 侧边栏 */
.info-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cover-section {
  border-radius: 12px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  display: block;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.info-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: #24292e;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e1e4e8;
}

.about-desc {
  color: #586069;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

.info-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0366d6;
  font-size: 14px;
  text-decoration: none;
}

.info-link:hover {
  text-decoration: underline;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: white;
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
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #24292e;
}

.author-role {
  font-size: 13px;
  color: #586069;
}

.screenshots-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.screenshot-thumb {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.2s;
}

.screenshot-thumb:hover {
  transform: scale(1.05);
}

.publish-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.info-label {
  color: #586069;
}

.info-value {
  color: #24292e;
  font-weight: 500;
}

/* 评论区 */
.comments-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-top: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.comments-section h2 {
  font-size: 20px;
  margin-bottom: 24px;
}

.comments-placeholder {
  text-align: center;
  padding: 48px;
  background: #f6f8fa;
  border-radius: 8px;
  color: #586069;
}

/* 灯箱 */
.lightbox {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.lightbox img {
  max-width: 90%;
  max-height: 90%;
  border-radius: 8px;
}

.lightbox-close, .lightbox-prev, .lightbox-next {
  position: absolute;
  background: rgba(255,255,255,0.2);
  color: white;
  border: none;
  font-size: 32px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.lightbox-close:hover, .lightbox-prev:hover, .lightbox-next:hover {
  background: rgba(255,255,255,0.3);
}

.lightbox-close {
  top: 20px;
  right: 20px;
}

.lightbox-prev {
  left: 20px;
}

.lightbox-next {
  right: 20px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

