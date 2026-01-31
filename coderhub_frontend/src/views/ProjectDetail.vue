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
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&family=JetBrains+Mono&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

:global(:root) {
  --primary: #c2410c;
  --accent: #d97706;
  --background: #fdfaf6;
  --surface: #f7f2eb;
  --text-main: #2d2a26;
  --text-muted: #7c7267;
  --border-warm: #eaddd3;
  --golden-glow: rgba(245, 158, 11, 0.25);
}

.project-detail-container {
  min-height: 100vh;
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
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
  border: 4px solid var(--border-warm);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state h2 {
  margin-bottom: 20px;
  font-family: 'Crimson Pro', serif;
  color: var(--text-muted);
}

.error-state button {
  padding: 12px 28px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.error-state button:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

.project-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 40px;
}

/* 头部 */
.project-header {
  background: white;
  border-radius: 26px;
  padding: 40px;
  margin-bottom: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
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
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 12px;
  font-family: 'Inter', sans-serif;
}

.breadcrumb a {
  color: var(--primary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: #9a3412;
}

.breadcrumb .separator {
  margin: 0 10px;
  color: var(--border-warm);
}

.project-title {
  font-family: 'Crimson Pro', serif;
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.project-icon {
  font-size: 36px;
}

.open-source-badge {
  font-size: 12px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 999px;
  font-weight: 700;
  font-family: 'Inter', sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.project-desc {
  font-family: 'Crimson Pro', serif;
  color: var(--text-muted);
  font-size: 17px;
  max-width: 700px;
  line-height: 1.7;
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
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.btn-action.github:hover {
  background: #2f363d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(36, 41, 46, 0.3);
}

.btn-action.github svg {
  width: 18px;
  height: 18px;
}

.btn-action.demo {
  background: var(--primary);
  color: white;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.btn-action.demo:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.3);
}

.btn-action.download {
  background: #10b981;
  color: white;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.btn-action.download:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.project-stats {
  display: flex;
  gap: 36px;
  margin-top: 28px;
  padding-top: 28px;
  border-top: 1px solid var(--border-warm);
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  font-size: 20px;
}

.stat-value {
  font-weight: 700;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
  font-size: 16px;
}

.stat-label {
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
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
  border-radius: 26px;
  overflow: hidden;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.readme-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 28px;
  background: var(--surface);
  border-bottom: 1px solid var(--border-warm);
  font-weight: 700;
  font-family: 'Inter', sans-serif;
  color: #1f2937;
}

.readme-content {
  padding: 40px;
  line-height: 1.8;
  color: var(--text-main);
  font-family: 'Crimson Pro', serif;
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
  background: var(--surface);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, monospace;
  font-size: 90%;
  color: var(--primary);
  border: 1px solid var(--border-warm);
}

.readme-content :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 20px 0;
  border: 1px solid #2d2d2d;
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
  border-left: 4px solid var(--primary);
  padding-left: 20px;
  margin: 20px 0;
  color: var(--text-muted);
  background: var(--surface);
  padding: 16px 20px;
  border-radius: 8px;
}

.readme-content :deep(a) {
  color: var(--primary);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.readme-content :deep(a:hover) {
  color: #9a3412;
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
  border-radius: 26px;
  padding: 28px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.info-card h3 {
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-warm);
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
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  color: white;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
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
  font-weight: 700;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
  font-size: 15px;
}

.author-role {
  font-size: 13px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
  font-weight: 600;
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
  border-radius: 26px;
  padding: 40px;
  margin-top: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.comments-section h2 {
  font-family: 'Crimson Pro', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 28px;
}

.comments-placeholder {
  text-align: center;
  padding: 80px 40px;
  background: var(--surface);
  border-radius: 20px;
  color: var(--text-muted);
  border: 1px dashed var(--border-warm);
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
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

