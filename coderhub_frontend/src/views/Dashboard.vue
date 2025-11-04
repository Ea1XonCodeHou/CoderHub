<template>
  <div class="dashboard-container">
    <!-- 侧边导航栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
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
        <span class="admin-badge">管理后台</span>
      </div>

      <nav class="sidebar-nav">
        <div 
          v-for="item in menuItems" 
          :key="item.key"
          :class="['nav-item', { 'active': activeMenu === item.key }]"
          @click="switchMenu(item.key)"
        >
          <svg class="nav-icon" v-html="item.icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"></svg>
          <span>{{ item.label }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button @click="backToHome" class="back-btn">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回首页
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <h1>{{ currentTitle }}</h1>
        <div class="user-info">
          <span class="welcome-text">欢迎回来，{{ userInfo.username }}</span>
          <img :src="userInfo.avatar" alt="avatar" class="avatar" />
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 仪表盘总览 -->
        <div v-if="activeMenu === 'overview'" class="overview-section">
          <div class="stats-grid">
            <div v-for="stat in stats" :key="stat.label" class="stat-card">
              <div class="stat-icon" :style="{ background: stat.color }">
                <svg v-html="stat.icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"></svg>
              </div>
              <div class="stat-content">
                <p class="stat-label">{{ stat.label }}</p>
                <h3 class="stat-value">{{ stat.value }}</h3>
                <p class="stat-trend" :class="stat.trendType">
                  {{ stat.trend }}
                </p>
              </div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts-section">
            <div class="chart-card">
              <h3>最近7天访问趋势</h3>
              <div class="chart-placeholder">
                <p>图表区域（可接入 ECharts）</p>
              </div>
            </div>
            <div class="chart-card">
              <h3>文章分类分布</h3>
              <div class="chart-placeholder">
                <p>图表区域（可接入 ECharts）</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户管理 -->
        <UserManagement v-if="activeMenu === 'users'" />

        <!-- 文章审核 -->
        <ArticleReview v-if="activeMenu === 'articles'" />

        <!-- 分类管理 -->
        <CategoryManagement v-if="activeMenu === 'categories'" />

        <!-- 教程管理 -->
        <TutorialManagement 
          v-if="activeMenu === 'tutorials' && !selectedTutorial" 
          @view-detail="handleViewTutorialDetail"
        />

        <!-- 教程详情 -->
        <TutorialDetail 
          v-if="activeMenu === 'tutorials' && selectedTutorial" 
          :tutorial="selectedTutorial"
          @back="handleBackToTutorialList"
        />

        <!-- 通用设置 -->
        <div v-if="activeMenu === 'settings'" class="section">
          <div class="section-header">
            <h2>通用设置</h2>
          </div>
          <div class="settings-form">
            <div class="form-group">
              <label>网站标题</label>
              <input type="text" value="CoderHub 技术博客社区" class="form-input" />
            </div>
            <div class="form-group">
              <label>网站描述</label>
              <textarea class="form-textarea" rows="4">一个专注于技术分享的博客社区平台</textarea>
            </div>
            <div class="form-group">
              <label>每页文章数</label>
              <input type="number" value="20" class="form-input" />
            </div>
            <div class="form-group">
              <label>是否开启注册</label>
              <label class="switch">
                <input type="checkbox" checked />
                <span class="slider"></span>
              </label>
            </div>
            <button class="btn-primary">保存设置</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import ArticleReview from '@/components/admin/ArticleReview.vue'
import CategoryManagement from '@/components/admin/CategoryManagement.vue'
import TutorialDetail from '@/components/admin/TutorialDetail.vue'
import TutorialManagement from '@/components/admin/TutorialManagement.vue'
import UserManagement from '@/components/admin/UserManagement.vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeMenu = ref('overview')
const userInfo = ref({})
const selectedTutorial = ref(null)

// 菜单项
const menuItems = ref([
  {
    key: 'overview',
    label: '仪表盘',
    icon: '<path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    key: 'users',
    label: '用户管理',
    icon: '<path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 11C11.2091 11 13 9.20914 13 7C13 4.79086 11.2091 3 9 3C6.79086 3 5 4.79086 5 7C5 9.20914 6.79086 11 9 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    key: 'articles',
    label: '文章审核',
    icon: '<path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    key: 'categories',
    label: '分类管理',
    icon: '<path d="M4 7H20M4 12H20M4 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    key: 'tutorials',
    label: '教程管理',
    icon: '<path d="M2 3H8C9.06087 3 10.0783 3.42143 10.8284 4.17157C11.5786 4.92172 12 5.93913 12 7V21C12 20.2044 11.6839 19.4413 11.1213 18.8787C10.5587 18.3161 9.79565 18 9 18H2V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 3H16C14.9391 3 13.9217 3.42143 13.1716 4.17157C12.4214 4.92172 12 5.93913 12 7V21C12 20.2044 12.3161 19.4413 12.8787 18.8787C13.4413 18.3161 14.2044 18 15 18H22V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    key: 'settings',
    label: '通用设置',
    icon: '<circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5843 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6643 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.258 9.77251 19.9887C9.5799 19.7194 9.31074 19.5143 9 19.4C8.69838 19.2669 8.36381 19.2272 8.03941 19.286C7.71502 19.3448 7.41568 19.4995 7.18 19.73L7.12 19.79C6.93425 19.976 6.71368 20.1235 6.47088 20.2241C6.22808 20.3248 5.96783 20.3766 5.705 20.3766C5.44217 20.3766 5.18192 20.3248 4.93912 20.2241C4.69632 20.1235 4.47575 19.976 4.29 19.79C4.10405 19.6043 3.95653 19.3837 3.85588 19.1409C3.75523 18.8981 3.70343 18.6378 3.70343 18.375C3.70343 18.1122 3.75523 17.8519 3.85588 17.6091C3.95653 17.3663 4.10405 17.1457 4.29 16.96L4.35 16.9C4.58054 16.6643 4.73519 16.365 4.794 16.0406C4.85282 15.7162 4.81312 15.3816 4.68 15.08C4.55324 14.7842 4.34276 14.532 4.07447 14.3543C3.80618 14.1766 3.49179 14.0813 3.17 14.08H3C2.46957 14.08 1.96086 13.8693 1.58579 13.4942C1.21071 13.1191 1 12.6104 1 12.08C1 11.5496 1.21071 11.0409 1.58579 10.6658C1.96086 10.2907 2.46957 10.08 3 10.08H3.09C3.42099 10.0723 3.742 9.96512 4.0113 9.77251C4.28059 9.5799 4.48572 9.31074 4.6 9C4.73312 8.69838 4.77282 8.36381 4.714 8.03941C4.65519 7.71502 4.50054 7.41568 4.27 7.18L4.21 7.12C4.02405 6.93425 3.87653 6.71368 3.77588 6.47088C3.67523 6.22808 3.62343 5.96783 3.62343 5.705C3.62343 5.44217 3.67523 5.18192 3.77588 4.93912C3.87653 4.69632 4.02405 4.47575 4.21 4.29C4.39575 4.10405 4.61632 3.95653 4.85912 3.85588C5.10192 3.75523 5.36217 3.70343 5.625 3.70343C5.88783 3.70343 6.14808 3.75523 6.39088 3.85588C6.63368 3.95653 6.85425 4.10405 7.04 4.29L7.1 4.35C7.33568 4.58054 7.63502 4.73519 7.95941 4.794C8.28381 4.85282 8.61838 4.81312 8.92 4.68H9C9.29577 4.55324 9.54802 4.34276 9.72569 4.07447C9.90337 3.80618 9.99872 3.49179 10 3.17V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72618 14.2743 3.99447C14.452 4.26276 14.7042 4.47324 15 4.6C15.3016 4.73312 15.6362 4.77282 15.9606 4.714C16.285 4.65519 16.5843 4.50054 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61632 20.1441 4.85912C20.2448 5.10192 20.2966 5.36217 20.2966 5.625C20.2966 5.88783 20.2448 6.14808 20.1441 6.39088C20.0435 6.63368 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33568 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72569C20.1938 9.90337 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  }
])

// 统计数据
const stats = ref([
  {
    label: '总用户数',
    value: '1,234',
    trend: '↑ 12.5%',
    trendType: 'up',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    icon: '<path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="9" cy="7" r="4" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    label: '文章总数',
    value: '856',
    trend: '↑ 8.2%',
    trendType: 'up',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    icon: '<path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    label: '今日访问',
    value: '3,456',
    trend: '↑ 23.1%',
    trendType: 'up',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    icon: '<path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="12" r="3" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  },
  {
    label: '待审核文章',
    value: '12',
    trend: '↓ 5.4%',
    trendType: 'down',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    icon: '<path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.7088 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18457 2.99721 7.13633 4.39828 5.49707C5.79935 3.85782 7.69279 2.71538 9.79619 2.24015C11.8996 1.76491 14.1003 1.9823 16.07 2.85999" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M22 4L12 14.01L9 11.01" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>'
  }
])

// Mock 文章数据
const mockArticles = ref([
  {
    id: 1,
    title: 'Spring Boot 3.0 实战教程',
    summary: '详细介绍Spring Boot 3.0的新特性和实战应用...',
    author: 'CoderHub',
    createTime: '2小时前',
    status: 'pending',
    statusText: '待审核'
  },
  {
    id: 2,
    title: 'Vue 3 Composition API 最佳实践',
    summary: '深入探讨Vue 3组合式API的使用技巧和最佳实践...',
    author: '前端开发者',
    createTime: '5小时前',
    status: 'approved',
    statusText: '已通过'
  }
])

const currentTitle = computed(() => {
  const item = menuItems.value.find(item => item.key === activeMenu.value)
  return item ? item.label : '仪表盘'
})

const handleViewTutorialDetail = (tutorial) => {
  selectedTutorial.value = tutorial
}

const handleBackToTutorialList = () => {
  selectedTutorial.value = null
}

const switchMenu = (menuKey) => {
  activeMenu.value = menuKey
  selectedTutorial.value = null
}

const backToHome = () => {
  router.push('/home')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
    if (userInfo.value.role !== 'ADMIN') {
      alert('您没有权限访问管理后台')
      router.push('/home')
    }
  } else {
    router.push('/')
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: #2c3e50;
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header svg {
  width: 40px;
  height: 40px;
  margin-bottom: 12px;
}

.logo-text {
  display: block;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.admin-badge {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  font-size: 12px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-left: 3px solid white;
}

.nav-icon {
  width: 20px;
  height: 20px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.back-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 260px;
}

.top-bar {
  background: white;
  padding: 20px 32px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar h1 {
  font-size: 24px;
  color: #2c3e50;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 14px;
  color: #64748b;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.logout-btn {
  padding: 8px 16px;
  background: #f5f7fa;
  color: #2c3e50;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #e2e8f0;
}

.content-area {
  padding: 32px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 6px;
}

.stat-trend {
  font-size: 13px;
  font-weight: 600;
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.down {
  color: #ef4444;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chart-card h3 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #94a3b8;
}

/* 通用section */
.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.section-header h2 {
  font-size: 20px;
  color: #2c3e50;
}

.btn-primary {
  padding: 10px 20px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #34495e;
  transform: translateY(-1px);
}

/* 数据表格 */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: #f5f7fa;
}

.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.data-table td {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
  font-size: 14px;
  color: #2c3e50;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge.success {
  background: #d1fae5;
  color: #065f46;
}

.badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

.badge.level-0 {
  background: #e5e7eb;
  color: #374151;
}

.badge.level-1 {
  background: #fef3c7;
  color: #92400e;
}

.badge.level-2 {
  background: #ddd6fe;
  color: #5b21b6;
}

.btn-action {
  padding: 6px 12px;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 13px;
  color: #2c3e50;
  cursor: pointer;
  margin-right: 8px;
  transition: all 0.2s;
}

.btn-action:hover {
  background: #e2e8f0;
}

.btn-action.success {
  background: #d1fae5;
  color: #065f46;
  border-color: #a7f3d0;
}

.btn-action.danger {
  background: #fee2e2;
  color: #991b1b;
  border-color: #fecaca;
}

/* 文章列表 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #2c3e50;
}

.article-item h4 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 8px;
}

.article-item p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.article-meta {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 12px;
}

.article-meta .status {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.article-meta .status.pending {
  background: #fef3c7;
  color: #92400e;
}

.article-meta .status.approved {
  background: #d1fae5;
  color: #065f46;
}

.article-actions {
  display: flex;
  gap: 10px;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.category-card {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-info h4 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 4px;
}

.category-info p {
  font-size: 13px;
  color: #94a3b8;
}

.category-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  padding: 6px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon:hover {
  border-color: #2c3e50;
  color: #2c3e50;
}

.btn-icon.danger:hover {
  border-color: #ef4444;
  color: #ef4444;
}

/* 设置表单 */
.settings-form {
  max-width: 600px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.form-input,
.form-textarea,
.select-filter {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  color: #2c3e50;
  background: #f5f7fa;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  outline: none;
  transition: all 0.2s;
}

.form-input:focus,
.form-textarea:focus,
.select-filter:focus {
  background: white;
  border-color: #2c3e50;
}

/* 开关按钮 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #2c3e50;
}

input:checked + .slider:before {
  transform: translateX(24px);
}

.empty-text {
  text-align: center;
  color: #94a3b8;
  padding: 60px 0;
  font-size: 15px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-section,
  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>

