<template>
  <div class="video-player-page">
    <!-- 顶部导航栏 - 使用统一的NavBar组件 -->
    <NavBar :showWriteBtn="false" />
    
    <!-- 视频信息工具栏 -->
    <div class="video-toolbar">
      <div class="toolbar-content">
        <div class="video-breadcrumb">
          <router-link :to="`/tutorial/${tutorial?.id}`" class="breadcrumb-link">{{ tutorial?.title }}</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">{{ chapter?.chapterTitle }}</span>
        </div>
        <button class="download-btn" @click="downloadVideo" title="下载视频">
          <span class="material-symbols-outlined">download</span>
          <span>下载</span>
        </button>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧视频区域 -->
      <div class="video-section">
        <!-- 顶部视频信息区 -->
        <div class="video-info-header">
          <h1 class="video-title">{{ video?.videoTitle }}</h1>
          <div class="video-meta">
            <div class="meta-item">
              <span class="material-symbols-outlined">schedule</span>
              <span>{{ video?.duration }}</span>
            </div>
            <div class="meta-item">
              <span class="material-symbols-outlined">visibility</span>
              <span>{{ formatNumber(viewCount) }} 次观看</span>
            </div>
            <div class="meta-item" v-if="video?.createTime">
              <span class="material-symbols-outlined">calendar_today</span>
              <span>{{ formatDate(video?.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 中间核心播放区 -->
        <div class="video-player-container">
          <video 
            ref="videoPlayer"
            :src="video?.videoUrl"
            controls
            controlslist="nodownload"
            class="video-element"
            @loadedmetadata="onVideoLoaded"
            @timeupdate="onTimeUpdate"
            @ended="onVideoEnded"
            @error="onVideoError"
          >
            您的浏览器不支持视频播放
          </video>
        </div>

        <!-- 讲师信息卡片 -->
        <div class="instructor-card">
          <div class="instructor-avatar">
            <img :src="tutorial?.instructorAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor'" alt="讲师头像">
          </div>
          <div class="instructor-info">
            <h3 class="instructor-name">{{ tutorial?.instructorName || '讲师' }}</h3>
            <p class="instructor-title">{{ tutorial?.instructorDesc || '课程讲师' }}</p>
          </div>
        </div>
      </div>

      <!-- 右侧相关视频推荐区 -->
      <aside class="playlist-sidebar">
        <div class="playlist-header">
          <h2>
            <span class="material-symbols-outlined">playlist_play</span>
            课程目录
          </h2>
          <span class="chapter-count">{{ chapters.length }} 章节</span>
        </div>

        <div class="playlist-content">
          <div 
            v-for="chapterItem in chapters" 
            :key="chapterItem.id"
            class="chapter-item"
          >
            <div class="chapter-header" @click="toggleChapter(chapterItem.id)">
              <div class="chapter-info">
                <svg 
                  class="chapter-icon" 
                  :class="{ 'expanded': expandedChapters.includes(chapterItem.id) }"
                  viewBox="0 0 24 24" 
                  fill="none" 
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span class="chapter-title">{{ chapterItem.chapterOrder }}. {{ chapterItem.chapterTitle }}</span>
              </div>
              <span class="chapter-duration">{{ chapterItem.duration }}</span>
            </div>

            <!-- 章节视频列表 -->
            <transition name="slide">
              <div v-if="expandedChapters.includes(chapterItem.id)" class="video-list">
                <div 
                  v-for="videoItem in chapterVideos[chapterItem.id]" 
                  :key="videoItem.id"
                  class="video-item"
                  :class="{ 'active': videoItem.id === video?.id, 'playing': isPlaying && videoItem.id === video?.id }"
                  @click="switchVideo(videoItem, chapterItem)"
                >
                  <div class="video-item-icon">
                    <span v-if="videoItem.id === video?.id && isPlaying" class="material-symbols-outlined">pause</span>
                    <span v-else class="material-symbols-outlined">play_arrow</span>
                  </div>
                  <div class="video-item-info">
                    <span class="video-item-title">{{ videoItem.videoTitle }}</span>
                    <div class="video-item-meta">
                      <span class="video-item-duration">
                        <span class="material-symbols-outlined">schedule</span>
                        {{ videoItem.duration }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- 空状态 -->
          <div v-if="chapters.length === 0" class="empty-playlist">
            <span class="material-symbols-outlined">playlist_play</span>
            <p>暂无课程目录</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { getChapterVideos, getTutorialChapters, getTutorialDetail } from '@/api/user'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()

// 数据状态
const videoPlayer = ref(null)
const video = ref(null)
const tutorial = ref(null)
const chapter = ref(null)
const chapters = ref([])
const chapterVideos = ref({})
const expandedChapters = ref([])
const isPlaying = ref(false)
const viewCount = ref(1234) // 模拟观看次数

// 加载数据
const loadData = async () => {
  try {
    const { videoId, chapterId, tutorialId } = route.query
    
    if (!videoId || !chapterId || !tutorialId) {
      alert('参数错误')
      goBack()
      return
    }

    // 加载教程信息
    const tutorialRes = await getTutorialDetail(tutorialId)
    tutorial.value = tutorialRes.data

    // 加载章节列表
    const chaptersRes = await getTutorialChapters(tutorialId)
    chapters.value = chaptersRes.data || []

    // 加载所有章节的视频
    for (const chapterItem of chapters.value) {
      const videosRes = await getChapterVideos(chapterItem.id)
      chapterVideos.value[chapterItem.id] = videosRes.data || []
      
      // 找到当前章节和视频
      if (chapterItem.id === chapterId) {
        chapter.value = chapterItem
        expandedChapters.value.push(chapterItem.id)
        
        const currentVideo = videosRes.data?.find(v => v.id === videoId)
        if (currentVideo) {
          video.value = currentVideo
        }
      }
    }
  } catch (error) {
    console.error('加载数据失败：', error)
    alert('加载失败，请重试')
  }
}

// 视频事件处理
const onVideoLoaded = () => {
  console.log('视频加载完成')
}

const onTimeUpdate = () => {
  // 可以在这里记录播放进度
}

const onVideoEnded = () => {
  isPlaying.value = false
  console.log('视频播放结束')
  // 自动播放下一个视频
  playNextVideo()
}

const onVideoError = (error) => {
  console.error('视频加载失败：', error)
  alert('视频加载失败，请检查网络连接')
}

// 切换章节展开/收起
const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

// 切换视频
const switchVideo = (videoItem, chapterItem) => {
  video.value = videoItem
  chapter.value = chapterItem
  isPlaying.value = true
  
  // 更新URL
  router.replace({
    path: '/video/player',
    query: {
      videoId: videoItem.id,
      chapterId: chapterItem.id,
      tutorialId: tutorial.value.id
    }
  })
}

// 播放下一个视频
const playNextVideo = () => {
  // 查找当前视频的下一个视频
  const currentChapterVideos = chapterVideos.value[chapter.value.id] || []
  const currentIndex = currentChapterVideos.findIndex(v => v.id === video.value.id)
  
  if (currentIndex < currentChapterVideos.length - 1) {
    // 播放当前章节的下一个视频
    switchVideo(currentChapterVideos[currentIndex + 1], chapter.value)
  } else {
    // 查找下一个章节的第一个视频
    const currentChapterIndex = chapters.value.findIndex(c => c.id === chapter.value.id)
    if (currentChapterIndex < chapters.value.length - 1) {
      const nextChapter = chapters.value[currentChapterIndex + 1]
      const nextChapterVideos = chapterVideos.value[nextChapter.id] || []
      if (nextChapterVideos.length > 0) {
        switchVideo(nextChapterVideos[0], nextChapter)
      }
    }
  }
}

// 下载视频
const downloadVideo = () => {
  if (!video.value) return
  
  const link = document.createElement('a')
  link.href = video.value.videoUrl
  link.download = video.value.videoTitle + '.mp4'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 返回课程页面
const goBack = () => {
  router.back()
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 监听播放状态
onMounted(() => {
  loadData()
  
  // 监听视频播放事件
  const player = videoPlayer.value
  if (player) {
    player.addEventListener('play', () => {
      isPlaying.value = true
    })
    player.addEventListener('pause', () => {
      isPlaying.value = false
    })
  }
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

.video-player-page {
  min-height: 100vh;
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

/* 视频信息工具栏 */
.video-toolbar {
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

.video-breadcrumb {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.breadcrumb-link {
  color: var(--primary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb-link:hover {
  color: #9a3412;
}

.breadcrumb-separator {
  color: var(--border-warm);
}

.breadcrumb-current {
  color: var(--text-muted);
}

.download-btn {
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

.download-btn:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

.download-btn .material-symbols-outlined {
  font-size: 20px;
  font-variation-settings: 'FILL' 0, 'wght' 600;
}

/* 主要内容区 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 32px;
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px 40px;
  min-height: calc(100vh - 140px);
}

/* 左侧视频区域 */
.video-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

/* 顶部视频信息区 */
.video-info-header {
  background: white;
  border-radius: 26px;
  padding: 28px 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.video-title {
  font-family: 'Crimson Pro', serif;
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.video-meta {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.meta-item .material-symbols-outlined {
  font-size: 18px;
  color: var(--primary);
  font-variation-settings: 'FILL' 1, 'wght' 600;
}

/* 中间核心播放区 */
.video-player-container {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 26px;
  overflow: hidden;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 32px rgba(45, 42, 38, 0.15);
  aspect-ratio: 16 / 9;
}

.video-element {
  width: 100%;
  height: 100%;
  background: #000;
  display: block;
}

/* 讲师信息卡片 */
.instructor-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
  background: white;
  border-radius: 26px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.instructor-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--border-warm);
  flex-shrink: 0;
}

.instructor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.instructor-info {
  flex: 1;
  min-width: 0;
}

.instructor-name {
  font-size: 17px;
  font-weight: 700;
  margin: 0 0 6px 0;
  color: #1f2937;
  font-family: 'Inter', sans-serif;
}

.instructor-title {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
}

/* 右侧相关视频推荐区 */
.playlist-sidebar {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: sticky;
  top: 120px;
  max-height: calc(100vh - 140px);
  overflow: hidden;
}

.playlist-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: white;
  border-bottom: 2px solid var(--border-warm);
  border-radius: 26px 26px 0 0;
  border: 1px solid var(--border-warm);
  border-bottom-width: 2px;
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.playlist-header h2 {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Crimson Pro', serif;
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: #1f2937;
}

.playlist-header .material-symbols-outlined {
  font-size: 24px;
  color: var(--primary);
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.chapter-count {
  font-size: 13px;
  font-weight: 700;
  padding: 6px 14px;
  background: var(--surface);
  border-radius: 999px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
  border: 1px solid var(--border-warm);
}

.playlist-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: white;
  border-radius: 0 0 26px 26px;
  border: 1px solid var(--border-warm);
  border-top: none;
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

/* 章节项 */
.chapter-item {
  margin-bottom: 12px;
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border-warm);
  transition: all 0.2s;
}

.chapter-item:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.1);
}

.chapter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.chapter-header:hover {
  background: rgba(194, 65, 12, 0.05);
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.chapter-icon {
  width: 20px;
  height: 20px;
  color: var(--primary);
  transition: transform 0.3s;
  flex-shrink: 0;
}

.chapter-icon.expanded {
  transform: rotate(90deg);
}

.chapter-title {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-duration {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
  white-space: nowrap;
}

/* 视频列表 */
.video-list {
  border-top: 1px solid var(--border-warm);
  background: white;
}

.video-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px 16px 52px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid var(--border-warm);
}

.video-item:last-child {
  border-bottom: none;
}

.video-item:hover {
  background: var(--surface);
}

.video-item.active {
  background: rgba(194, 65, 12, 0.08);
  border-left: 4px solid var(--primary);
}

.video-item.playing .video-item-icon {
  color: var(--primary);
}

.video-item-icon {
  width: 24px;
  height: 24px;
  color: var(--text-muted);
  flex-shrink: 0;
  margin-top: 2px;
}

.video-item-icon .material-symbols-outlined {
  font-size: 24px;
  font-variation-settings: 'FILL' 1, 'wght' 600;
}

.video-item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.video-item-title {
  display: block;
  font-family: 'Crimson Pro', serif;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.video-item.active .video-item-title {
  color: var(--primary);
}

.video-item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.video-item-duration {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

.video-item-duration .material-symbols-outlined {
  font-size: 14px;
  font-variation-settings: 'FILL' 0, 'wght' 600;
}

/* 空状态 */
.empty-playlist {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  color: var(--text-muted);
  background: var(--surface);
  border-radius: 20px;
  border: 1px dashed var(--border-warm);
  margin: 20px;
}

.empty-playlist .material-symbols-outlined {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.4;
  color: var(--primary);
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.empty-playlist p {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  margin: 0;
  color: var(--text-muted);
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr 360px;
    padding: 24px 20px;
    gap: 24px;
  }

  .toolbar-content {
    padding: 16px 24px;
  }
}

@media (max-width: 968px) {
  .main-content {
    grid-template-columns: 1fr;
    padding: 24px 20px;
  }
  
  .playlist-sidebar {
    position: static;
    max-height: 500px;
  }

  .video-title {
    font-size: 26px;
  }

  .video-meta {
    gap: 20px;
  }
}
</style>
