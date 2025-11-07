<template>
  <div class="video-player-page">
    <!-- 顶部导航栏 -->
    <nav class="top-navbar">
      <button class="btn-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回课程
      </button>
      <div class="nav-title">
        <h3>{{ video?.videoTitle }}</h3>
        <span class="tutorial-breadcrumb">
          {{ tutorial?.title }} / {{ chapter?.chapterTitle }}
        </span>
      </div>
      <div class="nav-actions">
        <button class="btn-download" @click="downloadVideo" title="下载视频">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V13M12 13L9 10M12 13L15 10M5 16L5 17C5 18.1046 5.89543 19 7 19L17 19C18.1046 19 19 18.1046 19 17V16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          下载
        </button>
      </div>
    </nav>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧视频区域 -->
      <div class="video-section">
        <!-- 视频播放器 -->
        <div class="video-container">
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

        <!-- 视频信息 -->
        <div class="video-info-section">
          <div class="video-header">
            <h1 class="video-title">{{ video?.videoTitle }}</h1>
            <div class="video-stats">
              <span class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
                </svg>
                {{ video?.duration }}
              </span>
              <span class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M2.45817 12C3.73199 7.94288 7.52289 5 12.0004 5C16.4778 5 20.2687 7.94291 21.5426 12C20.2687 16.0571 16.4778 19 12.0003 19C7.52286 19 3.73195 16.0571 2.45817 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                {{ formatNumber(viewCount) }} 次观看
              </span>
              <span class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 7C3 5.89543 3.89543 5 5 5H19C20.1046 5 21 5.89543 21 7V17C21 18.1046 20.1046 19 19 19H5C3.89543 19 3 18.1046 3 17V7Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 2V5M16 2V5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                {{ formatDate(video?.createTime) }}
              </span>
            </div>
          </div>

          <!-- 讲师信息 -->
          <div class="instructor-card">
            <div class="instructor-avatar">
              <img :src="tutorial?.instructorAvatar || '/default-avatar.png'" alt="讲师头像">
            </div>
            <div class="instructor-info">
              <h3 class="instructor-name">{{ tutorial?.instructorName || '讲师' }}</h3>
              <p class="instructor-title">{{ tutorial?.instructorDesc || '课程讲师' }}</p>
            </div>
            <button class="btn-follow">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              关注
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧课程目录 -->
      <aside class="playlist-sidebar">
        <div class="playlist-header">
          <h2>
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15" stroke="currentColor" stroke-width="2"/>
              <path d="M9 5C9 6.10457 9.89543 7 11 7H13C14.1046 7 15 6.10457 15 5V4C15 2.89543 14.1046 2 13 2H11C9.89543 2 9 2.89543 9 4V5Z" stroke="currentColor" stroke-width="2"/>
            </svg>
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
                    <svg v-if="videoItem.id === video?.id && isPlaying" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <rect x="6" y="5" width="4" height="14" rx="1" fill="currentColor"/>
                      <rect x="14" y="5" width="4" height="14" rx="1" fill="currentColor"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M5 3L19 12L5 21V3Z" fill="currentColor"/>
                    </svg>
                  </div>
                  <div class="video-item-info">
                    <span class="video-item-title">{{ videoItem.videoTitle }}</span>
                    <span class="video-item-duration">{{ videoItem.duration }}</span>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- 空状态 -->
          <div v-if="chapters.length === 0" class="empty-playlist">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15" stroke="currentColor" stroke-width="2"/>
            </svg>
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
.video-player-page {
  min-height: 100vh;
  background: #0f0f0f;
  color: white;
}

/* 顶部导航栏 */
.top-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(15, 15, 15, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.btn-back svg {
  width: 18px;
  height: 18px;
}

.nav-title {
  flex: 1;
  margin: 0 24px;
}

.nav-title h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: white;
}

.tutorial-breadcrumb {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.btn-download {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-download:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.btn-download svg {
  width: 16px;
  height: 16px;
}

/* 主要内容区 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 0;
  min-height: calc(100vh - 60px);
}

/* 左侧视频区域 */
.video-section {
  background: #0f0f0f;
}

.video-container {
  position: relative;
  width: 100%;
  background: #000;
  aspect-ratio: 16 / 9;
}

.video-element {
  width: 100%;
  height: 100%;
  background: black;
}

/* 视频信息区 */
.video-info-section {
  padding: 24px;
}

.video-header {
  margin-bottom: 24px;
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: white;
  line-height: 1.4;
}

.video-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-item svg {
  width: 16px;
  height: 16px;
}

/* 讲师卡片 */
.instructor-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 24px;
}

.instructor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
}

.instructor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.instructor-info {
  flex: 1;
}

.instructor-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: white;
}

.instructor-title {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.btn-follow {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  border: none;
  border-radius: 6px;
  color: #0f0f0f;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-follow:hover {
  background: #f0f0f0;
  transform: translateY(-1px);
}

.btn-follow svg {
  width: 16px;
  height: 16px;
}

/* 右侧播放列表 */
.playlist-sidebar {
  background: #181818;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  overflow-y: auto;
  max-height: calc(100vh - 60px);
}

.playlist-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #181818;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.playlist-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.playlist-header svg {
  width: 20px;
  height: 20px;
}

.chapter-count {
  font-size: 12px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.playlist-content {
  padding: 12px;
}

/* 章节项 */
.chapter-item {
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  overflow: hidden;
}

.chapter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.chapter-header:hover {
  background: rgba(255, 255, 255, 0.05);
}

.chapter-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chapter-icon {
  width: 16px;
  height: 16px;
  color: rgba(255, 255, 255, 0.6);
  transition: transform 0.3s;
}

.chapter-icon.expanded {
  transform: rotate(90deg);
}

.chapter-title {
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.chapter-duration {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 视频列表 */
.video-list {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.video-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px 12px 42px;
  cursor: pointer;
  transition: all 0.2s;
}

.video-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.video-item.active {
  background: rgba(103, 126, 234, 0.15);
}

.video-item.playing .video-item-icon {
  color: #677eea;
}

.video-item-icon {
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.video-item-icon svg {
  width: 100%;
  height: 100%;
}

.video-item-info {
  flex: 1;
  min-width: 0;
}

.video-item-title {
  display: block;
  font-size: 13px;
  color: white;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-item.active .video-item-title {
  color: #677eea;
  font-weight: 500;
}

.video-item-duration {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

/* 空状态 */
.empty-playlist {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.3);
}

.empty-playlist svg {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-playlist p {
  font-size: 14px;
  margin: 0;
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
    grid-template-columns: 1fr 320px;
  }
}

@media (max-width: 968px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .playlist-sidebar {
    max-height: 400px;
  }
}
</style>
