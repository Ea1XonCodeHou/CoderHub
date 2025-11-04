<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <!-- Logo区域 -->
        <div class="nav-logo">
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
          <li class="active">
            <a href="#">博客首页</a>
          </li>
          <li @click="goToTutorial">
            <a href="#">教程</a>
          </li>
          <li>
            <a href="#">项目</a>
          </li>
          <li>
            <a href="#">智能体</a>
          </li>
        </ul>

        <!-- 搜索框 -->
        <div class="search-box">
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
          <button class="btn-write" @click="goToEditor">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            写文章
          </button>

          <!-- 用户头像 -->
          <div class="user-avatar" @click="toggleUserMenu">
            <img :src="userInfo.avatar" alt="avatar" />
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

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 左侧分类导航 -->
      <aside class="sidebar-left">
        <div class="category-list">
          <!-- 全部分类 -->
          <div 
            :class="['category-item', { 'active': selectedCategory === 'all' }]"
            @click="selectedCategory = 'all'"
          >
            <svg class="category-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 7H21M3 12H21M3 17H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>全部</span>
          </div>
          
          <!-- 一级分类 -->
          <template v-for="rootCategory in rootCategories" :key="rootCategory.id">
            <div 
              :class="['category-item', 'root-category', { 'active': selectedCategory === rootCategory.id }]"
              @click="handleCategoryClick(rootCategory.id)"
            >
              <!-- 展开/收起按钮 -->
              <svg 
                v-if="hasSubCategories(rootCategory.id)"
                @click.stop="toggleExpand(rootCategory.id)" 
                class="expand-icon" 
                :class="{ 'expanded': isExpanded(rootCategory.id) }"
                viewBox="0 0 24 24" 
                fill="none" 
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              
              <!-- 分类图标 -->
              <svg class="category-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 3H5C3.89543 3 3 3.89543 3 5V9C3 10.1046 3.89543 11 5 11H9C10.1046 11 11 10.1046 11 9V5C11 3.89543 10.1046 3 9 3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 3H15C13.8954 3 13 3.89543 13 5V9C13 10.1046 13.8954 11 15 11H19C20.1046 11 21 10.1046 21 9V5C21 3.89543 20.1046 3 19 3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9 13H5C3.89543 13 3 13.8954 3 15V19C3 20.1046 3.89543 21 5 21H9C10.1046 21 11 20.1046 11 19V15C11 13.8954 10.1046 13 9 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 13H15C13.8954 13 13 13.8954 13 15V19C13 20.1046 13.8954 21 15 21H19C20.1046 21 21 20.1046 21 19V15C21 13.8954 20.1046 13 19 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              
              <span>{{ rootCategory.categoryName }}</span>
              <span class="article-count">{{ rootCategory.articleCount }}</span>
            </div>
            
            <!-- 二级分类 -->
            <div 
              v-if="isExpanded(rootCategory.id)"
              v-for="subCategory in getSubCategories(rootCategory.id)" 
              :key="subCategory.id"
              :class="['category-item', 'sub-category', { 'active': selectedCategory === subCategory.id }]"
              @click.stop="handleCategoryClick(subCategory.id)"
            >
              <svg class="category-icon sub-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M8 4H6C5.46957 4 4.96086 4.21071 4.58579 4.58579C4.21071 4.96086 4 5.46957 4 6V8C4 8.53043 4.21071 9.03914 4.58579 9.41421C4.96086 9.78929 5.46957 10 6 10H8C8.53043 10 9.03914 9.78929 9.41421 9.41421C9.78929 9.03914 10 8.53043 10 8V6C10 5.46957 9.78929 4.96086 9.41421 4.58579C9.03914 4.21071 8.53043 4 8 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="16" cy="7" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M4 16H6C6.53043 16 7.03914 16.2107 7.41421 16.5858C7.78929 16.9609 8 17.4696 8 18V20C8 20.5304 7.78929 21.0391 7.41421 21.4142C7.03914 21.7893 6.53043 22 6 22H4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 18C14 17.4696 14.2107 16.9609 14.5858 16.5858C14.9609 16.2107 15.4696 16 16 16H18C18.5304 16 19.0391 16.2107 19.4142 16.5858C19.7893 16.9609 20 17.4696 20 18V20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>{{ subCategory.categoryName }}</span>
              <span class="article-count">{{ subCategory.articleCount }}</span>
            </div>
          </template>
        </div>
      </aside>

      <!-- 中间文章列表 -->
      <main class="article-list">
        <div class="list-header">
          <h2>推荐文章</h2>
          <div class="sort-tabs">
            <span :class="{ 'active': sortType === 'hot' }" @click="sortType = 'hot'">热门</span>
            <span :class="{ 'active': sortType === 'latest' }" @click="sortType = 'latest'">最新</span>
          </div>
        </div>

        <!-- 文章卡片 -->
        <div v-if="articles.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 12H15M9 16H15M17 21H7C5.89543 21 5 20.1046 5 19V5C5 3.89543 5.89543 3 7 3H12.5858C12.851 3 13.1054 3.10536 13.2929 3.29289L18.7071 8.70711C18.8946 8.89464 19 9.149 19 9.41421V19C19 20.1046 18.1046 21 17 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>暂无文章</p>
        </div>
        
        <div v-for="article in articles" :key="article.id" class="article-card" @click="goToArticle(article.id)">
          <!-- 封面图 -->
          <div v-if="article.coverImage" class="article-cover">
            <img :src="article.coverImage" alt="cover" />
          </div>
          
          <div class="article-content">
            <div class="article-header">
              <img :src="article.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" class="author-avatar" alt="avatar" />
              <div class="author-info">
                <div class="author-name">{{ article.username }}</div>
                <div class="article-meta">
                  <span>{{ formatTime(article.createTime) }}</span>
                  <span class="dot">·</span>
                  <span>{{ article.viewCount }} 阅读</span>
                </div>
              </div>
              
              <!-- 原创标签 -->
              <span v-if="article.isOriginal === 1" class="original-badge">原创</span>
            </div>

            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary }}</p>

            <div class="article-tags">
              <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.tagName }}</span>
            </div>

            <div class="article-footer">
              <div class="category-badge">{{ article.categoryName }}</div>
              <div class="action-group">
                <button class="action-btn">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M7 22V11M2 13V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H17.28C17.7623 22.0055 18.2304 21.8364 18.5979 21.524C18.9654 21.2116 19.2077 20.7769 19.28 20.3L20.66 11.3C20.7035 11.0134 20.6842 10.7207 20.6033 10.4423C20.5225 10.1638 20.3821 9.90629 20.1919 9.68751C20.0016 9.46873 19.7661 9.29393 19.5016 9.17522C19.2371 9.0565 18.9499 8.99672 18.66 9H14V4C14 3.46957 13.7893 2.96086 13.4142 2.58579C13.0391 2.21071 12.5304 2 12 2L7 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ article.likeCount }}
                </button>
                <button class="action-btn">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ article.commentCount }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧边栏 -->
      <aside class="sidebar-right">
        <!-- 个人信息卡片 -->
        <div class="info-card">
          <div class="user-profile">
            <!-- 头像容器（带VIP特效） -->
            <div class="avatar-container" :class="getAvatarClass(userInfo.userLevel)">
              <img :src="userInfo.avatar" alt="avatar" class="profile-avatar" />
              <!-- VIP 光环 (userLevel === 1) -->
              <div v-if="userInfo.userLevel === 1" class="vip-ring"></div>
              <!-- SVIP 彩虹光环 (userLevel === 2) -->
              <div v-if="userInfo.userLevel === 2" class="svip-ring"></div>
            </div>
            <h3 class="profile-name">
              {{ userInfo.username }}
              <!-- VIP/SVIP 徽章 -->
              <span v-if="userInfo.userLevel === 1" class="level-badge vip-badge">VIP</span>
              <span v-if="userInfo.userLevel === 2" class="level-badge svip-badge">SVIP</span>
            </h3>
            <p class="profile-level">{{ levelText }}</p>
          </div>
          <div class="stats">
            <div class="stat-item">
              <span class="stat-value">{{ userStats.articleCount }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.followingCount }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.followersCount }}</span>
              <span class="stat-label">粉丝</span>
            </div>
          </div>
        </div>

        <!-- 热门标签 -->
        <div class="hot-tags">
          <h4>热门标签</h4>
          <div class="tags-container">
            <span v-for="tag in hotTags" :key="tag" class="hot-tag">{{ tag }}</span>
          </div>
        </div>
      </aside>
    </div>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>CoderHub</h4>
            <p>专业的技术分享社区</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <a href="#">关于我们</a>
            <a href="#">联系方式</a>
            <a href="#">使用协议</a>
          </div>
          <div class="footer-section">
            <h4>社区</h4>
            <a href="#">写文章</a>
            <a href="#">问答</a>
            <a href="#">活动</a>
          </div>
        </div>
        <div class="footer-copyright">
          © 2024 CoderHub. All rights reserved.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedCategory = ref('all')
const sortType = ref('hot')
const showUserMenu = ref(false)
const userInfo = ref({})
const searchKeyword = ref('')

// 用户统计信息
const userStats = ref({
  articleCount: 0,
  followingCount: 0,
  followersCount: 0
})

// 分类列表（从后端获取）
const categoryList = ref([])

// 展开的分类ID列表（默认全部收起）
const expandedCategories = ref([])

// 计算属性：一级分类
const rootCategories = computed(() => {
  return categoryList.value.filter(c => !c.parentId && c.status === 1)
})

// 获取子分类
const getSubCategories = (parentId) => {
  return categoryList.value.filter(c => c.parentId === parentId && c.status === 1)
}

// 检查是否有子分类
const hasSubCategories = (parentId) => {
  return getSubCategories(parentId).length > 0
}

// 检查分类是否展开
const isExpanded = (categoryId) => {
  return expandedCategories.value.includes(categoryId)
}

// 切换展开/收起
const toggleExpand = (categoryId) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    // 已展开，则收起
    expandedCategories.value.splice(index, 1)
  } else {
    // 未展开，则展开
    expandedCategories.value.push(categoryId)
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/admin/category/list')
    if (response.data.code === 1) {
      categoryList.value = response.data.data
    }
  } catch (error) {
    console.error('获取分类列表失败：', error)
  }
}

// 处理分类点击
const handleCategoryClick = (categoryId) => {
  selectedCategory.value = categoryId
  fetchArticles()
}

// 文章列表
const articles = ref([])

// 获取文章列表
const fetchArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const params = {}
    
    // 如果选择了分类，添加分类ID参数
    if (selectedCategory.value !== 'all') {
      params.categoryId = selectedCategory.value
    }
    
    const response = await axios.get('/api/article/list', {
      params,
      headers: {
        authentication: token
      }
    })
    
    if (response.data.code === 1) {
      articles.value = response.data.data
    }
  } catch (error) {
    console.error('获取文章列表失败：', error)
  }
}

// 跳转到文章详情
const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
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

// 热门标签
const hotTags = ref([])

// 获取热门标签
const fetchHotTags = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/article/tags/hot', {
      params: { limit: 10 },
      headers: {
        authentication: token
      }
    })
    if (response.data.code === 1) {
      hotTags.value = response.data.data.map(tag => tag.tagName)
    }
  } catch (error) {
    console.error('获取热门标签失败：', error)
  }
}

// 用户等级文本（userLevel: 0-普通 1-VIP 2-SVIP）
const levelText = computed(() => {
  const level = userInfo.value.userLevel
  if (level === 2) return '超级会员'
  if (level === 1) return '尊贵会员'
  return '普通用户'
})

// 获取头像容器样式类（userLevel: 0-普通 1-VIP 2-SVIP）
const getAvatarClass = (userLevel) => {
  if (userLevel === 2) return 'svip-avatar'
  if (userLevel === 1) return 'vip-avatar'
  return 'normal-avatar'
}

// 切换用户菜单
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 跳转到个人主页
const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

// 跳转到文章编辑器
const goToEditor = () => {
  router.push('/article/editor')
}

// 跳转到教程页面
const goToTutorial = () => {
  router.push('/tutorial')
}

// 搜索功能
const handleSearch = () => {
  // 简单的前端搜索过滤（如果需要后端搜索，可以调用API）
  if (!searchKeyword.value.trim()) {
    fetchArticles()
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  articles.value = articles.value.filter(article => 
    article.title.toLowerCase().includes(keyword) ||
    article.summary.toLowerCase().includes(keyword) ||
    article.username.toLowerCase().includes(keyword)
  )
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token || !userInfo.value.id) return
    
    const response = await axios.get(`/api/user/${userInfo.value.id}/stats`, {
      headers: { authentication: token }
    })
    
    if (response.data.code === 1) {
      const stats = response.data.data
      userStats.value = {
        articleCount: stats.articleCount || 0,
        followingCount: stats.followingCount || 0,
        followersCount: stats.followersCount || 0
      }
    }
  } catch (error) {
    console.error('获取用户统计信息失败：', error)
  }
}

// 退出登录
const handleLogout = () => {
  showUserMenu.value = false
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

// 页面加载时获取用户信息和数据
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  } else {
    router.push('/')
  }
  
  // 获取数据
  fetchCategories()
  fetchArticles()
  fetchHotTags()
  fetchUserStats()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏 */
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

/* 主体内容 */
.main-content {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 200px 1fr 280px;
  gap: 24px;
}

/* 左侧边栏 */
.sidebar-left {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.category-list {
  background: white;
  border-radius: 12px;
  padding: 12px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.category-item:hover {
  background: #f5f7fa;
  color: #2c3e50;
}

.category-item.active {
  background: #f5f7fa;
  color: #2c3e50;
  font-weight: 600;
}

/* 一级分类样式 */
.category-item.root-category {
  font-weight: 600;
  margin-top: 8px;
  color: #2c3e50;
}

/* 二级分类样式 */
.category-item.sub-category {
  padding-left: 48px;
  font-size: 13px;
  color: #64748b;
}

.category-item.sub-category:hover {
  background: #f8f9fa;
}

/* 展开/收起按钮 */
.expand-icon {
  width: 14px;
  height: 14px;
  color: #94a3b8;
  cursor: pointer;
  transition: transform 0.2s;
  transform: rotate(0deg);
  flex-shrink: 0;
}

.expand-icon:hover {
  color: #2c3e50;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.category-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.sub-icon {
  width: 16px;
  height: 16px;
  opacity: 0.6;
  flex-shrink: 0;
}

/* 文章数量标签 */
.article-count {
  margin-left: auto;
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;
}

/* 文章列表 */
.article-list {
  background: transparent;
  padding: 0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.list-header h2 {
  font-size: 20px;
  color: #2c3e50;
  font-weight: 600;
}

.sort-tabs {
  display: flex;
  gap: 8px;
  background: #f5f7fa;
  padding: 4px;
  border-radius: 8px;
}

.sort-tabs span {
  padding: 6px 16px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  font-weight: 500;
}

.sort-tabs span.active {
  background: white;
  color: #2c3e50;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sort-tabs span:hover:not(.active) {
  color: #2c3e50;
  background: rgba(255, 255, 255, 0.6);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  color: #cbd5e1;
}

.empty-state p {
  font-size: 14px;
}

/* 文章卡片 */
.article-card {
  display: flex;
  gap: 20px;
  padding: 24px;
  margin-bottom: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid transparent;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  border-color: #e2e8f0;
}

.article-card:last-child {
  margin-bottom: 0;
}

/* 封面图 */
.article-cover {
  flex-shrink: 0;
  width: 220px;
  height: 150px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.article-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s;
}

.article-card:hover .article-cover::before {
  opacity: 1;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.article-card:hover .article-cover img {
  transform: scale(1.1);
}

/* 文章内容 */
.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.article-meta {
  font-size: 12px;
  color: #94a3b8;
}

.dot {
  margin: 0 6px;
}

/* 原创标签 */
.original-badge {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #f59e0b;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.2);
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.article-card:hover .article-title {
  color: #667eea;
}

.article-summary {
  font-size: 14px;
  color: #64748b;
  line-height: 1.7;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.tag {
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 500;
  color: #667eea;
  background: linear-gradient(135deg, #f5f7ff 0%, #e8ebff 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

/* 分类标签 */
.category-badge {
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #2c3e50;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  border-radius: 6px;
  transition: all 0.2s;
}

.action-group {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  color: #64748b;
  background: #f5f7fa;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.action-btn svg {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;
}

.action-btn:hover {
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.action-btn:hover svg {
  transform: scale(1.1);
}

/* 右侧边栏 */
.sidebar-right {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.info-card,
.hot-tags {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
  border: 1px solid #f5f7fa;
  transition: all 0.3s;
}

.info-card:hover,
.hot-tags:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.user-profile {
  text-align: center;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头像容器（与ArticleDetail保持一致） */
.avatar-container {
  position: relative;
  width: 64px;
  height: 64px;
  margin-bottom: 12px;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  position: relative;
  z-index: 2;
}

/* VIP 头像特效 */
.vip-avatar .profile-avatar {
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.6);
}

.vip-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 3px solid transparent;
  background: linear-gradient(45deg, #FFD700, #FFA500, #FFD700) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: vipRotate 3s linear infinite;
  z-index: 1;
}

@keyframes vipRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* SVIP 头像特效 */
.svip-avatar .profile-avatar {
  box-shadow: 0 0 30px rgba(138, 43, 226, 0.8);
  animation: svipPulse 2s ease-in-out infinite;
}

@keyframes svipPulse {
  0%, 100% {
    box-shadow: 0 0 30px rgba(138, 43, 226, 0.8);
  }
  50% {
    box-shadow: 0 0 50px rgba(255, 20, 147, 1);
  }
}

.svip-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border-radius: 50%;
  border: 4px solid transparent;
  background: linear-gradient(90deg, 
    #FF1493, 
    #FF69B4, 
    #FF6347, 
    #FFD700, 
    #00FA9A, 
    #00CED1, 
    #1E90FF, 
    #9370DB, 
    #FF1493
  ) border-box;
  background-size: 300% 300%;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: svipRainbow 3s linear infinite;
  z-index: 1;
}

@keyframes svipRainbow {
  0% {
    background-position: 0% 50%;
    transform: rotate(0deg);
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
    transform: rotate(360deg);
  }
}

.profile-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 等级徽章 */
.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

.vip-badge {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
  animation: vipBadgeGlow 2s ease-in-out infinite;
}

@keyframes vipBadgeGlow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(255, 215, 0, 0.8);
  }
}

.svip-badge {
  background: linear-gradient(135deg, #FF1493 0%, #9370DB 50%, #1E90FF 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 20, 147, 0.4);
  animation: svipBadgeGlow 2s ease-in-out infinite;
}

@keyframes svipBadgeGlow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 20, 147, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(138, 43, 226, 0.8);
  }
}

.profile-level {
  font-size: 13px;
  color: #94a3b8;
}

.stats {
  display: flex;
  justify-content: space-around;
  padding-top: 20px;
  border-top: 2px solid #f5f7fa;
  margin-top: 4px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transition: transform 0.2s;
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-3px);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.hot-tags h4 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 18px;
  font-weight: 600;
  position: relative;
  padding-left: 12px;
}

.hot-tags h4::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hot-tag {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 500;
  color: #667eea;
  background: linear-gradient(135deg, #f5f7ff 0%, #e8ebff 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.hot-tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr 280px;
  }
  
  .sidebar-left {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .sidebar-right {
    display: none;
  }
  
  .nav-menu {
    display: none;
  }
}

/* ==================== 页脚 ==================== */
.page-footer {
  background: #2c3e50;
  color: white;
  padding: 40px 0 20px;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 32px;
}

.footer-section h4 {
  font-size: 16px;
  margin: 0 0 16px 0;
}

.footer-section p {
  font-size: 14px;
  color: #bdc3c7;
  margin: 0;
}

.footer-section a {
  display: block;
  font-size: 14px;
  color: #bdc3c7;
  text-decoration: none;
  margin-bottom: 8px;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: white;
}

.footer-copyright {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  font-size: 14px;
  color: #bdc3c7;
}
</style>

