<template>
  <header class="navbar">
    <div class="nav-content">
      <!-- Logo区域 -->
      <div class="nav-left">
        <div class="nav-logo" @click="goHome">
          <span class="material-symbols-outlined logo-icon">book_4</span>
          <h1 class="logo-text">CoderHub</h1>
        </div>
        
        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <a 
            :class="['nav-link', { active: currentRoute === 'home' }]" 
            href="#" 
            @click.prevent="goHome"
          >博客首页</a>
          <a 
            :class="['nav-link', { active: currentRoute === 'tutorial' }]" 
            href="#" 
            @click.prevent="goToTutorial"
          >教程</a>
          <a 
            :class="['nav-link', { active: currentRoute === 'projects' }]" 
            href="#" 
            @click.prevent="goToProjects"
          >项目</a>
          <a 
            :class="['nav-link', { active: currentRoute === 'ai' }]" 
            href="#" 
            @click.prevent="goToAIChat"
          >智能体</a>
        </nav>
      </div>

      <!-- 右侧操作区 -->
      <div class="nav-right">
        <!-- 消息通知 -->
        <NotificationBell />
        
        <!-- 我的收藏 -->
        <button class="nav-btn-shelf" @click="goToProfile">
          <span class="material-symbols-outlined">auto_stories</span>
          <span class="btn-text">我的收藏</span>
        </button>
        
        <div class="nav-divider"></div>
        
        <!-- 写文章按钮 -->
        <button v-if="showWriteBtn" class="nav-btn-primary" @click="goToEditor">
          写文章
        </button>

        <!-- 用户头像 -->
        <div class="user-avatar" @click="toggleUserMenu">
          <img :src="userAvatar" alt="avatar" />
          <div v-if="showUserMenu" class="user-menu">
            <a href="#" class="menu-item" @click.prevent="goToProfile">
              <span class="material-symbols-outlined">person</span>
              个人主页
            </a>
            <a href="#" class="menu-item" @click.prevent="goToMyArticles">
              <span class="material-symbols-outlined">article</span>
              我的文章
            </a>
            <a href="#" class="menu-item" @click.prevent="goToSettings">
              <span class="material-symbols-outlined">settings</span>
              设置
            </a>
            <div class="menu-divider"></div>
            <a href="#" class="menu-item logout" @click.prevent="handleLogout">
              <span class="material-symbols-outlined">logout</span>
              退出登录
            </a>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import NotificationBell from './notification/NotificationBell.vue'

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
  if (path.includes('/projects')) return 'projects'
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
const goToMyArticles = () => {
  showUserMenu.value = false
  // TODO: 暂无我的文章页面
  router.push('/profile')
}
const goToSettings = () => {
  showUserMenu.value = false
  // TODO: 暂无设置页面
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
/* ========== Stitch Design NavBar ========== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  border-bottom: 1px solid var(--border-warm, #e8e2d9);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.nav-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

/* Logo */
.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.nav-logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 32px;
  color: var(--primary, #c2410c);
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.logo-text {
  font-family: 'Crimson Pro', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
  margin: 0;
}

/* Navigation Menu */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
  padding: 8px 0;
  position: relative;
}

.nav-link:hover {
  color: var(--primary, #c2410c);
}

.nav-link.active {
  color: var(--primary, #c2410c);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -24px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary, #c2410c);
  border-radius: 1px;
}

/* Right Side */
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-btn-shelf {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-btn-shelf:hover {
  color: var(--primary, #c2410c);
  background: rgba(194, 65, 12, 0.05);
}

.nav-btn-shelf .material-symbols-outlined {
  font-size: 20px;
}

.nav-divider {
  width: 1px;
  height: 24px;
  background: var(--border-warm, #e8e2d9);
}

.nav-btn-primary {
  background: var(--primary, #c2410c);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 9999px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.nav-btn-primary:hover {
  background: #9a3412;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

/* User Avatar */
.user-avatar {
  position: relative;
  cursor: pointer;
}

.user-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(194, 65, 12, 0.1);
  object-fit: cover;
  transition: border-color 0.2s;
}

.user-avatar:hover img {
  border-color: rgba(194, 65, 12, 0.3);
}

/* User Menu Dropdown */
.user-menu {
  position: absolute;
  top: 52px;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
  padding: 8px;
  min-width: 180px;
  z-index: 200;
  border: 1px solid var(--border-warm, #e8e2d9);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.15s;
}

.menu-item:hover {
  background: var(--surface-warm, #f3eee5);
  color: var(--primary, #c2410c);
}

.menu-item .material-symbols-outlined {
  font-size: 18px;
  opacity: 0.7;
}

.menu-divider {
  height: 1px;
  background: var(--border-warm, #e8e2d9);
  margin: 6px 0;
}

.menu-item.logout {
  color: #dc2626;
}

.menu-item.logout:hover {
  background: #fef2f2;
  color: #dc2626;
}

/* Responsive */
@media (max-width: 1024px) {
  .nav-content {
    padding: 0 24px;
  }
  
  .nav-left {
    gap: 24px;
  }
  
  .nav-menu {
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .nav-btn-shelf {
    display: none;
  }
  
  .nav-divider {
    display: none;
  }
  
  .btn-text {
    display: none;
  }
}
</style>
