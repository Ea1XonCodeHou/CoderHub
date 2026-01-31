<template>
  <div class="home-container font-display">
    <NavBar @search="handleNavSearch" />

    <main class="main-wrapper">
      <!-- Hero -->
      <section class="hero-section">
        <div class="hero-inner">
          <h2 class="hero-title">
            你的专属技术知识库<br />
            <span class="hero-highlight">开启编程之旅</span>
          </h2>
          <p class="hero-subtitle">
            探索精选技术文章，与开发者社区深度交流
          </p>

          <div class="hero-search">
            <span class="material-symbols-outlined search-icon">auto_awesome</span>
            <input
              v-model="searchKeyword"
              class="search-input"
              type="text"
              placeholder="向 AI 咨询技术问题，或搜索知识库..."
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>

          <div class="trending-tags">
            <span class="trending-label">热门搜索</span>
            <a
              v-for="tag in trendingTags"
              :key="tag"
              class="trending-tag"
              href="#"
              @click.prevent="searchByTag(tag)"
            >#{{ tag }}</a>
          </div>
        </div>
      </section>

      <!-- Content -->
      <div class="content-grid">
        <section class="articles-section">
          <div class="section-header">
            <h3 class="section-title">精选文章</h3>
            <div class="sort-tabs">
              <button
                :class="['sort-tab', { active: sortType === 'latest' }]"
                @click="sortType = 'latest'; fetchArticles()"
              >最新</button>
              <button
                :class="['sort-tab', { active: sortType === 'hot' }]"
                @click="sortType = 'hot'; fetchArticles()"
              >热门</button>
            </div>
          </div>

          <div class="articles-list">
            <div v-if="articles.length === 0" class="empty-state">
              <span class="material-symbols-outlined">article</span>
              <p>暂无文章</p>
            </div>

            <article
              v-for="article in articles"
              :key="article.id"
              class="article-card"
              @click="goToArticle(article.id)"
            >
              <div class="article-cover">
                <img
                  :src="article.coverImage || 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=600'"
                  :alt="article.title"
                />
              </div>

              <div class="article-body">
                <div class="article-meta-top">
                  <span class="category-badge">{{ article.categoryName || '技术' }}</span>
                  <span class="read-time">{{ estimateReadTime(article.summary) }} 分钟阅读</span>
                </div>

                <h4 class="article-title">{{ article.title }}</h4>
                <p class="article-summary">{{ article.summary }}</p>

                <div class="article-footer">
                  <div class="author-info">
                    <div
                      class="author-avatar"
                      :style="{ backgroundImage: `url(${article.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + article.username})` }"
                    ></div>
                    <span class="author-name">{{ article.username }}</span>
                  </div>
                  <button class="read-more-btn" @click.stop="goToArticle(article.id)">
                    阅读全文 <span class="material-symbols-outlined">arrow_forward</span>
                  </button>
                </div>
              </div>
            </article>
          </div>

          <button class="load-more-btn" @click="loadMoreArticles">
            加载更多文章
          </button>
        </section>

        <aside class="sidebar">
          <div class="ai-card">
            <div class="ai-card-header">
              <span class="material-symbols-outlined">auto_awesome</span>
              <h3>智能助手</h3>
            </div>
            <p class="ai-card-text">
              "发现你正在探索技术文章，也许你会对
              <span class="ai-highlight">分布式缓存策略</span>
              感兴趣？"
            </p>
            <button class="ai-card-btn" @click="goToAIChat">
              与 AI 助手对话
            </button>
          </div>

          <div class="sidebar-card">
            <div class="card-header">
              <span class="material-symbols-outlined">trending_up</span>
              <h3>热门话题</h3>
            </div>
            <div class="topics-list">
              <a
                v-for="(topic, index) in trendingTopics"
                :key="index"
                class="topic-item"
                href="#"
                @click.prevent="searchByTag(topic.name)"
              >
                <span class="topic-name">{{ topic.name }}</span>
                <span class="topic-count">{{ topic.count }}</span>
              </a>
            </div>
          </div>

          <div class="sidebar-card">
            <div class="card-header">
              <span class="material-symbols-outlined">forum</span>
              <h3>社区动态</h3>
            </div>
            <div class="community-list">
              <div v-for="(activity, index) in communityActivities" :key="index" class="activity-item">
                <div class="activity-avatar" :style="{ backgroundImage: `url(${activity.avatar})` }"></div>
                <div class="activity-content">
                  <p class="activity-name">{{ activity.name }}</p>
                  <p class="activity-text">{{ activity.text }}</p>
                  <a href="#" class="activity-link">查看详情</a>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </main>

    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo">
              <span class="material-symbols-outlined">book_4</span>
              <h4>CoderHub</h4>
            </div>
            <p class="footer-desc">
              一个为开发者精心打造的技术交流平台，学习、讨论、共同成长。
            </p>
          </div>

          <div class="footer-links">
            <h5>知识库</h5>
            <ul>
              <li><a href="#">全部文章</a></li>
              <li><a href="#">技术教程</a></li>
              <li><a href="#">架构设计</a></li>
              <li><a href="#">开源项目</a></li>
            </ul>
          </div>

          <div class="footer-links">
            <h5>社区</h5>
            <ul>
              <li><a href="#">讨论区</a></li>
              <li><a href="#">活动</a></li>
              <li><a href="#">代码审查</a></li>
            </ul>
          </div>

          <div class="footer-newsletter">
            <h5>订阅周刊</h5>
            <p>获取每周精选技术文章</p>
            <div class="newsletter-form">
              <input type="email" placeholder="输入邮箱地址" />
              <button>订阅</button>
            </div>
          </div>
        </div>

        <div class="footer-bottom">
          <p>© 2024 CoderHub. 用心为开发者打造</p>
          <div class="footer-bottom-links">
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
            <a href="#">联系我们</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedCategory = ref('all')
const sortType = ref('latest')
const userInfo = ref({})
const searchKeyword = ref('')

// ==================== 新增：Stitch设计所需的数据 ====================

// 热门搜索标签（从后端热门标签获取）
const trendingTags = ref(['Spring Boot', '微服务', 'Vue3'])

// 热门话题（默认占位，优先使用后端热门标签）
const trendingTopics = ref([
  { name: '微前端架构 2024', count: '1.2k' },
  { name: 'TypeScript 最佳实践', count: '840' },
  { name: '向量数据库对比', count: '650' }
])

// 社区动态（模拟数据，暂无后端接口）
const communityActivities = ref([
  {
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=mike',
    name: '张三',
    text: '"刚完成了一个 WebSocket 调试，花了10小时..."'
  },
  {
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=liam',
    name: '李四',
    text: '"分享了我的新开源 Go 日志库！"'
  }
])

// ==================== 保留原有后端接口相关逻辑 ====================

// 处理导航栏搜索
const handleNavSearch = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

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
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

// 获取分类列表（保留原有接口）
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/category/list')
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

// 获取所有相关分类ID（包括子分类）
const getAllRelatedCategoryIds = (categoryId) => {
  if (categoryId === 'all') return null
  
  const subCategories = getSubCategories(categoryId)
  if (subCategories.length > 0) {
    return [categoryId, ...subCategories.map(c => c.id)]
  }
  
  return [categoryId]
}

// 获取文章列表（保留原有接口）
const fetchArticles = async () => {
  try {
    const token = localStorage.getItem('token')
    const params = {}
    
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
      let articleList = response.data.data
      
      if (selectedCategory.value !== 'all') {
        const subCategories = getSubCategories(selectedCategory.value)
        if (subCategories.length > 0) {
          for (const subCat of subCategories) {
            const subResponse = await axios.get('/api/article/list', {
              params: { categoryId: subCat.id },
              headers: { authentication: token }
            })
            if (subResponse.data.code === 1) {
              articleList = [...articleList, ...subResponse.data.data]
            }
          }
        }
      }
      
      articles.value = articleList
    }
  } catch (error) {
    console.error('获取文章列表失败：', error)
  }
}

// 跳转到文章详情
const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 跳转到AI助手
const goToAIChat = () => {
  router.push('/ai/assistant')
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  
  try {
    let time
    if (Array.isArray(timeStr)) {
      time = new Date(timeStr[0], timeStr[1] - 1, timeStr[2], timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0)
    } else if (typeof timeStr === 'string') {
      time = new Date(timeStr)
    } else {
      time = new Date(timeStr)
    }
    
    if (isNaN(time.getTime())) {
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
    return '未知时间'
  }
}

// 估算阅读时间
const estimateReadTime = (summary) => {
  if (!summary) return 5
  const words = summary.length
  return Math.max(3, Math.ceil(words / 200))
}

// 热门标签（后端返回）
const hotTags = ref([])

const formatTopicCount = (count) => {
  if (count === null || count === undefined) return '0'
  const value = Number(count)
  if (Number.isNaN(value)) return '0'
  if (value >= 10000) return `${(value / 1000).toFixed(1)}k`
  if (value >= 1000) return `${(value / 1000).toFixed(1)}k`
  return `${value}`
}

// 获取热门标签（保留原有接口）
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
      hotTags.value = response.data.data || []
      const hotTagNames = hotTags.value
        .map(tag => tag.tagName || tag.name)
        .filter(Boolean)
      if (hotTagNames.length > 0) {
        trendingTags.value = hotTagNames.slice(0, 5)
      }
      if (hotTags.value.length > 0) {
        trendingTopics.value = hotTags.value.slice(0, 6).map(tag => ({
          name: tag.tagName || tag.name || '热门话题',
          count: formatTopicCount(tag.usageCount ?? tag.useCount ?? tag.referenceCount ?? tag.viewCount ?? 0)
        }))
      }
    }
  } catch (error) {
    console.error('获取热门标签失败：', error)
  }
}

// 用户等级文本
const levelText = computed(() => {
  const level = userInfo.value.userLevel
  if (level === 2) return '超级会员'
  if (level === 1) return '尊贵会员'
  return '普通用户'
})

// 获取头像容器样式类
const getAvatarClass = (userLevel) => {
  if (userLevel === 2) return 'svip-avatar'
  if (userLevel === 1) return 'vip-avatar'
  return 'normal-avatar'
}

// 搜索功能
const handleSearch = () => {
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

// 按标签搜索
const searchByTag = (tag) => {
  searchKeyword.value = tag
  handleSearch()
}

// 加载更多文章（暂无后端分页接口支持）
const loadMoreArticles = () => {
  // TODO: 需要后端支持分页接口
  console.log('加载更多文章 - 需要后端分页支持')
}

// 获取用户统计信息（保留原有接口）
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

// 页面加载时获取用户信息和数据
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  } else {
    router.push('/')
  }
  
  // 获取数据（保留所有原有接口调用）
  fetchCategories()
  fetchArticles()
  fetchHotTags()
  fetchUserStats()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:ital,wght@0,400;0,600;0,700;1,400&family=Inter:wght@400;500;600;700&family=JetBrains+Mono&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

:global(:root) {
  --primary: #c2410c;
  --accent: #d97706;
  --background: #faf7f2;
  --surface: #f3eee5;
  --text-main: #2d2a26;
  --text-muted: #7c7267;
  --border-warm: #e8e2d9;
  --golden-glow: rgba(251, 191, 36, 0.25);
}

:global(.material-symbols-outlined) {
  font-variation-settings: 'FILL' 0, 'wght' 500, 'GRAD' 0, 'opsz' 24;
}

.font-display {
  font-family: 'Inter', sans-serif;
}

.font-serif-warm {
  font-family: 'Crimson Pro', serif;
}

.home-container {
  min-height: 100vh;
  background: var(--background);
  color: var(--text-main);
}

.main-wrapper {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px 80px;
}

.hero-section {
  margin: 0 -40px 48px;
  padding: 64px 40px 72px;
  background: linear-gradient(135deg, #fdfcfb 0%, #f5eadd 100%);
  border-bottom: 1px solid var(--border-warm);
  border-radius: 0 0 48px 48px;
}

.hero-inner {
  max-width: 880px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-family: 'Crimson Pro', serif;
  font-size: 48px;
  line-height: 1.2;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
}

.hero-highlight {
  color: var(--primary);
  font-style: italic;
}

.hero-subtitle {
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  color: #6b7280;
  margin-bottom: 32px;
}

.hero-search {
  position: relative;
  max-width: 680px;
  margin: 0 auto;
}

.search-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--accent);
}

.search-input {
  width: 100%;
  height: 64px;
  padding: 0 128px 0 52px;
  border-radius: 20px;
  border: 2px solid var(--border-warm);
  background: white;
  font-size: 16px;
  font-family: 'Crimson Pro', serif;
  outline: none;
  box-shadow: 0 16px 30px rgba(148, 163, 184, 0.25);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input:focus {
  border-color: var(--accent);
  box-shadow: 0 18px 40px rgba(217, 119, 6, 0.15);
}

.search-btn {
  position: absolute;
  right: 12px;
  top: 12px;
  bottom: 12px;
  padding: 0 24px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: 14px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 8px 20px rgba(217, 119, 6, 0.25);
}

.search-btn:hover {
  background: #c76a0a;
}

.trending-tags {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
}

.trending-label {
  color: #9ca3af;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.16em;
}

.trending-tag {
  color: var(--primary);
  font-weight: 700;
  text-decoration: none;
}

.trending-tag:hover {
  text-decoration: underline;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 40px;
}

.articles-section {
  grid-column: span 8;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-warm);
}

.section-title {
  font-family: 'Crimson Pro', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.sort-tabs {
  display: flex;
  gap: 16px;
}

.sort-tab {
  background: transparent;
  border: none;
  font-size: 13px;
  font-weight: 700;
  color: #9ca3af;
  cursor: pointer;
  padding-bottom: 6px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.sort-tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.sort-tab:hover {
  color: #6b7280;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-state .material-symbols-outlined {
  font-size: 48px;
  margin-bottom: 12px;
}

.article-card {
  display: flex;
  gap: 28px;
  background: white;
  border: 1px solid var(--border-warm);
  border-radius: 32px;
  padding: 24px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 36px rgba(194, 65, 12, 0.12);
}

.article-cover {
  width: 260px;
  height: 180px;
  border-radius: 24px;
  overflow: hidden;
  flex-shrink: 0;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.article-meta-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.category-badge {
  padding: 6px 14px;
  background: #fef3c7;
  color: #b45309;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.read-time {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 600;
}

.article-title {
  font-family: 'Crimson Pro', serif;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 12px;
  line-height: 1.4;
  transition: color 0.2s;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-card:hover .article-title {
  color: var(--primary);
}

.article-summary {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: #6b7280;
  line-height: 1.7;
  margin-bottom: 24px;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background-size: cover;
  background-position: center;
  border: 1px solid var(--border-warm);
}

.author-name {
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
}

.read-more-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: none;
  color: var(--primary);
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}

.read-more-btn:hover {
  color: #9a3412;
}

.load-more-btn {
  width: 100%;
  padding: 18px;
  border: 2px dashed var(--border-warm);
  background: white;
  color: #6b7280;
  font-weight: 700;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.sidebar {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ai-card {
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(251, 191, 36, 0.4);
  box-shadow: 0 0 50px var(--golden-glow);
}

.ai-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--accent);
  margin-bottom: 16px;
  font-weight: 700;
  letter-spacing: 0.2em;
  font-size: 12px;
  text-transform: uppercase;
}

.ai-card-text {
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  color: #1f2937;
  line-height: 1.6;
  margin-bottom: 20px;
  font-style: italic;
}

.ai-highlight {
  color: var(--primary);
  font-weight: 700;
}

.ai-card-btn {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  background: var(--accent);
  color: white;
  border: none;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(217, 119, 6, 0.25);
  transition: filter 0.2s;
}

.ai-card-btn:hover {
  filter: brightness(1.05);
}

.sidebar-card {
  background: white;
  border-radius: 28px;
  border: 1px solid var(--border-warm);
  padding: 24px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 12px;
  font-weight: 700;
  color: #1f2937;
}

.card-header .material-symbols-outlined {
  color: var(--primary);
}

.topics-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.topic-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-decoration: none;
  color: inherit;
}

.topic-name {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  color: #374151;
  transition: color 0.2s;
}

.topic-item:hover .topic-name {
  color: var(--primary);
}

.topic-count {
  font-size: 11px;
  font-weight: 700;
  background: #f3f4f6;
  padding: 4px 8px;
  border-radius: 6px;
  color: #6b7280;
  font-family: 'JetBrains Mono', monospace;
}

.community-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.activity-item {
  display: flex;
  gap: 12px;
}

.activity-avatar {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background-size: cover;
  background-position: center;
  background-color: var(--surface);
}

.activity-name {
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
}

.activity-text {
  font-family: 'Crimson Pro', serif;
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
  margin: 2px 0 6px;
}

.activity-link {
  font-size: 10px;
  font-weight: 700;
  color: var(--primary);
  text-transform: uppercase;
  text-decoration: none;
}

.activity-link:hover {
  text-decoration: underline;
}

.page-footer {
  margin-top: 80px;
  background: var(--surface);
  border-top: 1px solid var(--border-warm);
  padding: 60px 0 40px;
}

.footer-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 32px;
  margin-bottom: 32px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--primary);
  margin-bottom: 14px;
}

.footer-logo h4 {
  font-family: 'Crimson Pro', serif;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.footer-desc {
  font-family: 'Crimson Pro', serif;
  color: #6b7280;
  line-height: 1.7;
}

.footer-links h5,
.footer-newsletter h5 {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14px;
}

.footer-links ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
}

.footer-links a {
  color: #6b7280;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
}

.footer-links a:hover {
  color: var(--primary);
}

.footer-newsletter p {
  font-family: 'Crimson Pro', serif;
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 12px;
  font-style: italic;
}

.newsletter-form {
  display: flex;
  gap: 8px;
}

.newsletter-form input {
  flex: 1;
  border: 1px solid var(--border-warm);
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 12px;
  outline: none;
}

.newsletter-form button {
  padding: 8px 16px;
  border-radius: 10px;
  background: var(--primary);
  color: white;
  border: none;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-bottom p {
  font-size: 12px;
  color: #9ca3af;
  font-style: italic;
}

.footer-bottom-links {
  display: flex;
  gap: 16px;
}

.footer-bottom-links a {
  font-size: 12px;
  color: #9ca3af;
  text-decoration: none;
  text-transform: uppercase;
  font-weight: 700;
}

.footer-bottom-links a:hover {
  color: var(--primary);
}

@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .articles-section,
  .sidebar {
    grid-column: span 12;
  }

  .article-card {
    flex-direction: column;
  }

  .article-cover {
    width: 100%;
    height: 220px;
  }
}

@media (max-width: 768px) {
  .main-wrapper {
    padding: 0 20px 60px;
  }

  .hero-section {
    margin: 0 -20px 40px;
    padding: 48px 20px 56px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-search {
    max-width: 100%;
  }

  .search-input {
    padding-right: 96px;
    height: 56px;
  }

  .search-btn {
    padding: 0 16px;
  }

  .footer-grid {
    grid-template-columns: 1fr;
  }
}
</style>

