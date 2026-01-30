<template>
  <div class="document-viewer">
    <!-- 顶部导航栏 - 使用统一的NavBar组件 -->
    <NavBar :showWriteBtn="false" />
    
    <!-- 文档工具栏 -->
    <div class="document-toolbar">
      <div class="toolbar-content">
        <div class="document-title">
          <span class="material-symbols-outlined">description</span>
          <span class="title-text">{{ document?.documentTitle }}</span>
        </div>

        <div class="toolbar-actions">
          <!-- 缩放控制 -->
          <div class="zoom-controls" v-if="totalPages > 0">
            <button @click="zoomOut" :disabled="scale <= 0.5" class="zoom-btn" title="缩小">
              <span class="material-symbols-outlined">zoom_out</span>
            </button>
            <span class="zoom-value">{{ Math.round(scale * 100) }}%</span>
            <button @click="zoomIn" :disabled="scale >= 3" class="zoom-btn" title="放大">
              <span class="material-symbols-outlined">zoom_in</span>
            </button>
            <button @click="fitToWidth" class="zoom-btn" title="适应宽度">
              <span class="material-symbols-outlined">fit_screen</span>
            </button>
            <button @click="rotateLeft" class="zoom-btn" title="逆时针旋转">
              <span class="material-symbols-outlined">rotate_left</span>
            </button>
          </div>
          
          <!-- 页码控制 -->
          <div class="page-controls" v-if="totalPages > 0">
            <button @click="prevPage" :disabled="currentPage <= 1" class="page-btn">
              <span class="material-symbols-outlined">chevron_left</span>
            </button>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <button @click="nextPage" :disabled="currentPage >= totalPages" class="page-btn">
              <span class="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
          <button class="action-btn" @click="downloadDocument" title="下载文档">
            <span class="material-symbols-outlined">download</span>
            <span>下载</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="viewer-content">
      <!-- 左侧PDF预览区 -->
      <div class="pdf-section">
        <div class="pdf-container">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载文档中... {{ loadingProgress }}%</p>
        </div>
        <div v-else-if="error" class="error-state">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>{{ error }}</p>
          <button @click="retryLoad" class="retry-btn">重新加载</button>
        </div>
        <div v-else class="pdf-render-area">
          <canvas ref="pdfCanvas" class="pdf-canvas"></canvas>
        </div>
        </div>
      </div>

      <!-- 右侧信息栏 -->
      <aside class="info-sidebar">
        <!-- 文档信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">
            <span class="material-symbols-outlined">info</span>
            文档信息
          </h3>
          <div class="info-item">
            <span class="info-label">文档类型：</span>
            <span class="info-value">{{ document?.documentType?.toUpperCase() }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">文件大小：</span>
            <span class="info-value">{{ formatFileSize(document?.fileSize) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所属章节：</span>
            <span class="info-value">第{{ chapter?.chapterOrder }}章</span>
          </div>
          <div class="info-item" v-if="totalPages > 0">
            <span class="info-label">总页数：</span>
            <span class="info-value">{{ totalPages }} 页</span>
          </div>
        </div>

        <!-- 教程信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">
            <span class="material-symbols-outlined">school</span>
            教程信息
          </h3>
          <div class="tutorial-info">
            <img :src="tutorial?.coverImage" alt="cover" class="tutorial-cover" />
            <h4 class="tutorial-name">{{ tutorial?.title }}</h4>
            <p class="tutorial-desc">{{ tutorial?.description }}</p>
          </div>
        </div>

        <!-- 讲师信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">
            <span class="material-symbols-outlined">person</span>
            讲师信息
          </h3>
          <div class="instructor-card">
            <img :src="tutorial?.instructorAvatar" :alt="tutorial?.instructorName" class="instructor-avatar" />
            <div class="instructor-details">
              <span class="instructor-name">{{ tutorial?.instructorName }}</span>
              <span class="instructor-title">{{ tutorial?.instructorDesc }}</span>
            </div>
          </div>
        </div>

        <!-- 本章其他资源 -->
        <div class="info-card">
          <h3 class="card-title">
            <span class="material-symbols-outlined">folder</span>
            本章其他资源
          </h3>
          
          <!-- 其他文档 -->
          <div v-if="chapterDocuments.length > 1" class="resource-list">
            <h4 class="resource-subtitle">文档资料</h4>
            <div
              v-for="doc in chapterDocuments.filter(d => d.id !== document?.id)"
              :key="doc.id"
              class="resource-item"
              @click="viewDocument(doc)"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7 21H17C18.1046 21 19 20.1046 19 19V9.41421C19 9.149 18.8946 8.89464 18.7071 8.70711L13.2929 3.29289C13.1054 3.10536 12.851 3 12.5858 3H7C5.89543 3 5 3.89543 5 5V19C5 20.1046 5.89543 21 7 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="resource-name">{{ doc.documentTitle }}</span>
            </div>
          </div>

          <!-- 视频资源 -->
          <div v-if="chapterVideos.length > 0" class="resource-list">
            <h4 class="resource-subtitle">视频资源</h4>
            <div
              v-for="video in chapterVideos"
              :key="video.id"
              class="resource-item"
            >
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 10L19.5528 7.72361C20.2177 7.39116 21 7.87465 21 8.61803V15.382C21 16.1253 20.2177 16.6088 19.5528 16.2764L15 14M5 18H13C14.1046 18 15 17.1046 15 16V8C15 6.89543 14.1046 6 13 6H5C3.89543 6 3 6.89543 3 8V16C3 17.1046 3.89543 18 5 18Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span class="resource-name">{{ video.videoTitle }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { getChapterDocuments, getChapterVideos, getTutorialChapters, getTutorialDetail } from '@/api/user'
import * as pdfjsLib from 'pdfjs-dist'
import { onMounted, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

// 配置PDF.js worker（使用CDN）
pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js`

const route = useRoute()
const router = useRouter()

const document = ref(null)
const chapter = ref(null)
const tutorial = ref(null)
const chapterDocuments = ref([])
const chapterVideos = ref([])

// PDF.js相关状态
const pdfCanvas = ref(null)
const pdfDoc = shallowRef(null)  // 使用shallowRef避免深度响应式导致的Proxy问题
const currentPage = ref(1)
const totalPages = ref(0)
const loading = ref(false)
const loadingProgress = ref(0)
const error = ref('')
const scale = ref(1.5) // PDF缩放比例
const rotation = ref(0) // PDF旋转角度（0, 90, 180, 270）

// 从路由参数获取数据
onMounted(async () => {
  const { documentId, chapterId, tutorialId } = route.query
  
  if (!documentId || !chapterId || !tutorialId) {
    error.value = '缺少必要参数'
    return
  }
  
  try {
    // 加载教程信息
    await loadTutorialInfo(tutorialId)
    
    // 加载章节信息
    await loadChapterInfo(tutorialId, chapterId)
    
    // 加载章节资源
    await loadChapterResources(chapterId)
    
    // 从章节资源中找到当前文档
    const currentDoc = chapterDocuments.value.find(d => d.id === documentId)
    if (currentDoc) {
      document.value = currentDoc
      // 加载PDF
      await loadPDF(currentDoc.documentUrl)
    } else {
      error.value = '文档不存在'
    }
  } catch (err) {
    console.error('加载数据失败：', err)
    error.value = '加载失败，请重试'
  }
})

// 加载PDF文档
const loadPDF = async (url) => {
  loading.value = true
  error.value = ''
  loadingProgress.value = 0
  
  try {
    // 方案：先用fetch下载完整PDF到Blob，避免CORS和Range请求问题
    console.log('开始下载PDF：', url)
    
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`HTTP错误: ${response.status}`)
    }
    
    // 获取总大小
    const contentLength = response.headers.get('content-length')
    const total = parseInt(contentLength, 10)
    let loaded = 0
    
    // 读取数据流
    const reader = response.body.getReader()
    const chunks = []
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      chunks.push(value)
      loaded += value.length
      
      if (total) {
        loadingProgress.value = Math.round((loaded / total) * 100)
      }
    }
    
    // 合并所有chunks为Uint8Array
    const chunksAll = new Uint8Array(loaded)
    let position = 0
    for (const chunk of chunks) {
      chunksAll.set(chunk, position)
      position += chunk.length
    }
    
    console.log('PDF下载完成，大小：', loaded, '字节')
    
    // 使用二进制数据加载PDF（不使用URL）
    const loadingTask = pdfjsLib.getDocument({
      data: chunksAll,  // 直接使用二进制数据，不通过URL
      cMapUrl: 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/cmaps/',
      cMapPacked: true,
    })
    
    pdfDoc.value = await loadingTask.promise
    totalPages.value = pdfDoc.value.numPages
    currentPage.value = 1
    
    console.log('PDF加载成功，总页数：', totalPages.value)
    
    // 渲染第一页
    await renderPage(1)
  } catch (err) {
    console.error('PDF加载失败：', err)
    error.value = 'PDF加载失败：' + (err.message || '未知错误')
  } finally {
    loading.value = false
  }
}

// 渲染指定页面
const renderPage = async (pageNum) => {
  if (!pdfDoc.value || !pdfCanvas.value) return
  
  try {
    const page = await pdfDoc.value.getPage(pageNum)
    const viewport = page.getViewport({ scale: scale.value, rotation: rotation.value })
    
    const canvas = pdfCanvas.value
    const context = canvas.getContext('2d')
    
    // 设置canvas尺寸
    canvas.height = viewport.height
    canvas.width = viewport.width
    
    // 渲染PDF页面
    const renderContext = {
      canvasContext: context,
      viewport: viewport
    }
    
    await page.render(renderContext).promise
  } catch (err) {
    console.error('页面渲染失败：', err)
    error.value = '页面渲染失败'
  }
}

// 缩放相关函数
const zoomIn = async () => {
  if (scale.value >= 3) return
  scale.value = Math.min(3, scale.value + 0.25)
  await renderPage(currentPage.value)
}

const zoomOut = async () => {
  if (scale.value <= 0.5) return
  scale.value = Math.max(0.5, scale.value - 0.25)
  await renderPage(currentPage.value)
}

const fitToWidth = async () => {
  if (!pdfDoc.value || !pdfCanvas.value) return
  
  try {
    const page = await pdfDoc.value.getPage(currentPage.value)
    const viewport = page.getViewport({ scale: 1, rotation: rotation.value })
    
    // 获取容器宽度
    const container = pdfCanvas.value.parentElement
    const containerWidth = container.clientWidth - 40 // 减去padding
    
    // 计算适应宽度的缩放比例
    scale.value = containerWidth / viewport.width
    await renderPage(currentPage.value)
  } catch (err) {
    console.error('适应宽度失败：', err)
  }
}

const rotateLeft = async () => {
  rotation.value = (rotation.value - 90 + 360) % 360
  await renderPage(currentPage.value)
}

// 上一页
const prevPage = async () => {
  if (currentPage.value <= 1) return
  currentPage.value--
  await renderPage(currentPage.value)
}

// 下一页
const nextPage = async () => {
  if (currentPage.value >= totalPages.value) return
  currentPage.value++
  await renderPage(currentPage.value)
}

// 重新加载
const retryLoad = async () => {
  if (document.value?.documentUrl) {
    await loadPDF(document.value.documentUrl)
  }
}

// 监听文档变化
watch(() => document.value?.documentUrl, async (newUrl) => {
  if (newUrl) {
    await loadPDF(newUrl)
  }
})

// 加载教程信息
const loadTutorialInfo = async (tutorialId) => {
  try {
    const res = await getTutorialDetail(tutorialId)
    if (res.code === 1) {
      tutorial.value = res.data
    }
  } catch (error) {
    console.error('加载教程信息失败：', error)
  }
}

// 加载章节信息
const loadChapterInfo = async (tutorialId, chapterId) => {
  try {
    const res = await getTutorialChapters(tutorialId)
    if (res.code === 1 && res.data) {
      const currentChapter = res.data.find(c => c.id === chapterId)
      if (currentChapter) {
        chapter.value = currentChapter
      }
    }
  } catch (error) {
    console.error('加载章节信息失败：', error)
  }
}

// 加载章节资源
const loadChapterResources = async (chapterId) => {
  try {
    const [docsRes, videosRes] = await Promise.all([
      getChapterDocuments(chapterId),
      getChapterVideos(chapterId)
    ])
    if (docsRes.code === 1) {
      chapterDocuments.value = docsRes.data || []
    }
    if (videosRes.code === 1) {
      chapterVideos.value = videosRes.data || []
    }
  } catch (error) {
    console.error('加载章节资源失败：', error)
  }
}

// 返回教程页面
const goBack = () => {
  router.back()
}

// 下载文档
const downloadDocument = async () => {
  if (!document.value?.documentUrl) return
  
  try {
    const response = await fetch(document.value.documentUrl)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    
    const a = window.document.createElement('a')
    a.href = url
    a.download = document.value.documentTitle || '课件.pdf'
    window.document.body.appendChild(a)
    a.click()
    window.document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败：', error)
    window.open(document.value.documentUrl, '_blank')
  }
}

// 查看其他文档
const viewDocument = (doc) => {
  document.value = doc
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}
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

.document-viewer {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

/* 文档工具栏 */
.document-toolbar {
  background: white;
  border-bottom: 1px solid var(--border-warm);
  box-shadow: 0 2px 8px rgba(45, 42, 38, 0.05);
  position: sticky;
  top: 72px;
  z-index: 40;
}

.toolbar-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 16px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.document-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.document-title .material-symbols-outlined {
  font-size: 24px;
  color: var(--primary);
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 缩放控制 */
.zoom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--surface);
  border-radius: 12px;
  border: 1px solid var(--border-warm);
}

.zoom-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: white;
  border: 1px solid var(--border-warm);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
}

.zoom-btn:hover:not(:disabled) {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-btn .material-symbols-outlined {
  font-size: 20px;
  font-variation-settings: 'FILL' 0, 'wght' 600;
}

.zoom-value {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-main);
  min-width: 50px;
  text-align: center;
  font-family: 'Inter', sans-serif;
}

/* 页码控制 */
.page-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: var(--surface);
  border-radius: 12px;
  border: 1px solid var(--border-warm);
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: white;
  border: 1px solid var(--border-warm);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
}

.page-btn:hover:not(:disabled) {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn .material-symbols-outlined {
  font-size: 20px;
  font-variation-settings: 'FILL' 0, 'wght' 600;
}

.page-info {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
  min-width: 80px;
  text-align: center;
  font-family: 'Inter', sans-serif;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.action-btn:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

.action-btn .material-symbols-outlined {
  font-size: 20px;
  font-variation-settings: 'FILL' 0, 'wght' 600;
}

/* 主内容区 */
.viewer-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 32px;
  padding: 32px 40px;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
  overflow: hidden;
}

/* PDF渲染区域 */
.pdf-section {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.pdf-container {
  background: white;
  border-radius: 26px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.pdf-render-area {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 32px;
  background: var(--surface);
}

.pdf-canvas {
  box-shadow: 0 8px 24px rgba(45, 42, 38, 0.12);
  background: white;
  border-radius: 8px;
}

/* 加载状态 */
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  color: var(--text-muted);
  padding: 80px 40px;
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
  to {
    transform: rotate(360deg);
  }
}

.loading-state p {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: var(--text-muted);
}

/* 错误状态 */
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  color: #dc2626;
  padding: 80px 40px;
}

.error-state svg {
  width: 64px;
  height: 64px;
  color: #dc2626;
}

.error-state p {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: #dc2626;
}

.retry-btn {
  padding: 12px 28px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.retry-btn:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

/* 右侧信息栏 */
.info-sidebar {
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: sticky;
  top: 120px;
  max-height: calc(100vh - 140px);
}

.info-card {
  background: white;
  border-radius: 26px;
  padding: 28px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Crimson Pro', serif;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-warm);
}

.card-title .material-symbols-outlined {
  font-size: 22px;
  color: var(--primary);
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 14px;
  border-bottom: 1px solid var(--border-warm);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: var(--text-muted);
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.info-value {
  color: #1f2937;
  font-weight: 700;
  font-family: 'Inter', sans-serif;
}

/* 教程信息 */
.tutorial-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tutorial-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 16px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 4px 12px rgba(45, 42, 38, 0.1);
}

.tutorial-name {
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-top: 4px;
}

.tutorial-desc {
  font-family: 'Crimson Pro', serif;
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.7;
  margin-top: 8px;
}

/* 讲师信息 */
.instructor-card {
  display: flex;
  gap: 12px;
  align-items: center;
}

.instructor-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-warm);
}

.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instructor-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
}

.instructor-title {
  font-size: 13px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
  font-weight: 600;
}

/* 资源列表 */
.resource-list {
  margin-top: 12px;
}

.resource-subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  margin-bottom: 10px;
  background: var(--surface);
  border: 1px solid var(--border-warm);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.resource-item:hover {
  background: white;
  border-color: var(--primary);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.1);
}

.resource-item svg {
  width: 22px;
  height: 22px;
  color: var(--primary);
  flex-shrink: 0;
}

.resource-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Inter', sans-serif;
}

/* 响应式 */
@media (max-width: 1200px) {
  .viewer-content {
    grid-template-columns: 1fr;
    padding: 24px 20px;
    gap: 24px;
  }
  
  .info-sidebar {
    position: static;
    max-height: none;
  }

  .toolbar-content {
    padding: 16px 24px;
  }
}

@media (max-width: 768px) {
  .toolbar-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .zoom-controls,
  .page-controls {
    padding: 6px 10px;
  }

  .zoom-btn,
  .page-btn {
    width: 32px;
    height: 32px;
  }
}
</style>
