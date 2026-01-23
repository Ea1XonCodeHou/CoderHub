<template>
  <nav class="navbar">
    <div class="nav-content">
      <!-- Logo区域 -->
      <div class="nav-logo" @click="goHome">
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
        <li :class="{ active: currentRoute === 'home' }" @click="goHome">
          <a href="#">博客首页</a>
        </li>
        <li :class="{ active: currentRoute === 'tutorial' }" @click="goToTutorial">
          <a href="#">教程</a>
        </li>
        <li :class="{ active: currentRoute === 'projects' }" @click="goToProjects">
          <a href="#">项目</a>
        </li>
        <li :class="{ active: currentRoute === 'ai' }" @click="goToAIChat">
          <a href="#">智能体</a>
        </li>
      </ul>

      <!-- 搜索框 -->
      <div v-if="showSearch" class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 21L16.65 16.65M19 11C19 15.4183 15.4183 19 11 19C6.58172 19 3 15.4183 3 11C3 6.58172 6.58172 3 11 3C15.4183 3 19 6.58172 19 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <input 
          type="text" 
          placeholder="搜索文章..." 
          v-model="searchKeyword"
          @input="handleSearch"
        />
      </div>

      <!-- 右侧操作区 -->
      <div class="nav-right">
        <button v-if="showWriteBtn" class="btn-write" @click="goToEditor">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          写文章
        </button>

        <!-- 用户头像 -->
        <div class="user-avatar" @click="toggleUserMenu">
          <img :src="userAvatar" alt="avatar" />
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
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

// Props
const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true
  },
  showWriteBtn: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['search'])

const router = useRouter()
const route = useRoute()

const searchKeyword = ref('')
const showUserMenu = ref(false)

// 计算当前路由
const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('/home')) return 'home'
  if (path.includes('/tutorial')) return 'tutorial'
  if (path.includes('/ai')) return 'ai'
  return ''
})

// 用户头像
const userAvatar = computed(() => {
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      return user.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=coderhub'
    } catch {
      return 'https://api.dicebear.com/7.x/avataaars/svg?seed=coderhub'
    }
  }
  return 'https://api.dicebear.com/7.x/avataaars/svg?seed=coderhub'
})

// 方法
const goHome = () => router.push('/home')
const goToTutorial = () => router.push('/tutorial')
const goToProjects = () => router.push('/projects')
const goToAIChat = () => router.push('/ai/assistant')
const goToEditor = () => router.push('/article/editor')
const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  showUserMenu.value = false
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

const handleSearch = () => {
  emit('search', searchKeyword.value)
}

// 点击外部关闭菜单
const handleClickOutside = (e) => {
  if (!e.target.closest('.user-avatar')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 48px; /* 增加与导航菜单的间距 */
}

.nav-logo svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
}

.nav-menu {
  display: flex;
  gap: 32px;
  list-style: none;
  margin: 0;
  padding: 0;
  margin-left: 16px; /* 额外左侧间距 */
}

.nav-menu li {
  position: relative;
  cursor: pointer;
}

.nav-menu li a {
  display: block;
  padding: 8px 0;
  font-size: 15px;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}

.nav-menu li.active a {
  color: #2c3e50;
  font-weight: 600;
}

.nav-menu li.active::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 0;
  right: 0;
  height: 3px;
  background: #2c3e50;
  border-radius: 2px;
}

.nav-menu li:hover a {
  color: #2c3e50;
}

/* 搜索框 */
.search-box {
  position: relative;
  flex: 1;
  max-width: 400px;
  margin: 0 24px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #94a3b8;
  pointer-events: none;
}

.search-box input {
  width: 100%;
  height: 40px;
  padding: 0 16px 0 42px;
  font-size: 14px;
  color: #2c3e50;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 20px;
  outline: none;
  transition: all 0.3s;
}

.search-box input::placeholder {
  color: #94a3b8;
}

.search-box input:focus {
  background: white;
  border-color: #2c3e50;
  box-shadow: 0 0 0 3px rgba(44, 62, 80, 0.1);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-left: auto;
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
  transition: all 0.2s;
}

.btn-write svg {
  width: 18px;
  height: 18px;
}

.btn-write:hover {
  background: #34495e;
  transform: translateY(-1px);
}

.user-avatar {
  position: relative;
  cursor: pointer;
}

.user-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.user-menu {
  position: absolute;
  top: 48px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 150px;
  z-index: 200;
}

.menu-item {
  display: block;
  padding: 10px 16px;
  font-size: 14px;
  color: #2c3e50;
  text-decoration: none;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f5f7fa;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .search-box {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
}
</style>

