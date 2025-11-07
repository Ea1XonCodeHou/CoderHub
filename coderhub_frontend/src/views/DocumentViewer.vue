<template>
  <div class="document-viewer">
    <!-- 顶部导航栏 -->
    <nav class="viewer-navbar">
      <div class="nav-content">
        <button class="back-btn" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回教程
        </button>
        
        <div class="document-title">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 12H15M9 16H15M17 21H7C5.89543 21 5 20.1046 5 19V5C5 3.89543 5.89543 3 7 3H12.5858C12.851 3 13.1054 3.10536 13.2929 3.29289L18.7071 8.70711C18.8946 8.89464 19 9.149 19 9.41421V19C19 20.1046 18.1046 21 17 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>{{ document?.documentTitle }}</span>
        </div>

        <div class="nav-actions">
          <!-- 缩放控制 -->
          <div class="zoom-controls" v-if="totalPages > 0">
            <button @click="zoomOut" :disabled="scale <= 0.5" class="zoom-btn" title="缩小">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="M21 21L16.65 16.65M8 11H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
            <span class="zoom-value">{{ Math.round(scale * 100) }}%</span>
            <button @click="zoomIn" :disabled="scale >= 3" class="zoom-btn" title="放大">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="M21 21L16.65 16.65M11 8V14M8 11H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
            <button @click="fitToWidth" class="zoom-btn" title="适应宽度">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M9 12H15M12 9V15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
            <button @click="rotateLeft" class="zoom-btn" title="逆时针旋转">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z" stroke="currentColor" stroke-width="2"/>
                <path d="M12 7V12L9 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          
          <!-- 页码控制 -->
          <div class="page-controls" v-if="totalPages > 0">
            <button @click="prevPage" :disabled="currentPage <= 1" class="page-btn">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <button @click="nextPage" :disabled="currentPage >= totalPages" class="page-btn">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <button class="action-btn" @click="downloadDocument" title="下载文档">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15M7 10L12 15M12 15L17 10M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            下载
          </button>
        </div>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="viewer-content">
      <!-- 左侧PDF预览区 -->
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

      <!-- 右侧信息栏 -->
      <aside class="info-sidebar">
        <!-- 文档信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">文档信息</h3>
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
          <h3 class="card-title">教程信息</h3>
          <div class="tutorial-info">
            <img :src="tutorial?.coverImage" alt="cover" class="tutorial-cover" />
            <h4 class="tutorial-name">{{ tutorial?.title }}</h4>
            <p class="tutorial-desc">{{ tutorial?.description }}</p>
          </div>
        </div>

        <!-- 讲师信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">讲师信息</h3>
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
          <h3 class="card-title">本章其他资源</h3>
          
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
.document-viewer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部导航栏 */
.viewer-navbar {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 100%;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.document-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.document-title svg {
  width: 24px;
  height: 24px;
  color: #64748b;
}

.nav-actions {
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
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.zoom-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.zoom-btn:hover:not(:disabled) {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-btn svg {
  width: 16px;
  height: 16px;
}

.zoom-value {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  min-width: 45px;
  text-align: center;
}

/* 页码控制 */
.page-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #2c3e50;
  border-color: #2c3e50;
  color: white;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn svg {
  width: 16px;
  height: 16px;
}

.page-info {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  min-width: 80px;
  text-align: center;
}

.action-btn {
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
  transition: all 0.2s;
}

.action-btn:hover {
  background: #1a252f;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.2);
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

/* 主内容区 */
.viewer-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* PDF渲染区域 */
.pdf-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.pdf-render-area {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  background: #f5f7fa;
}

.pdf-canvas {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: white;
}

/* 加载状态 */
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #64748b;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #64748b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 错误状态 */
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #ef4444;
}

.error-state svg {
  width: 64px;
  height: 64px;
}

.retry-btn {
  padding: 10px 24px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: #1a252f;
}

/* 右侧信息栏 */
.info-sidebar {
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #64748b;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.info-label {
  color: #64748b;
}

.info-value {
  color: #2c3e50;
  font-weight: 500;
}

/* 教程信息 */
.tutorial-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tutorial-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.tutorial-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.tutorial-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

/* 讲师信息 */
.instructor-card {
  display: flex;
  gap: 12px;
  align-items: center;
}

.instructor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instructor-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.instructor-title {
  font-size: 12px;
  color: #64748b;
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
  padding: 12px;
  margin-bottom: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.resource-item:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateX(4px);
}

.resource-item svg {
  width: 20px;
  height: 20px;
  color: #64748b;
  flex-shrink: 0;
}

.resource-name {
  font-size: 13px;
  color: #2c3e50;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 1200px) {
  .viewer-content {
    grid-template-columns: 1fr;
  }
  
  .info-sidebar {
    display: none;
  }
}
</style>
