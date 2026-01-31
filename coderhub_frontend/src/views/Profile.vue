<template>
  <div class="profile-container">
    <!-- 顶部导航栏 - 使用统一的NavBar组件 -->
    <NavBar :showWriteBtn="false" />

    <!-- 主体内容 -->
    <main class="profile-main">
      <!-- 数据统计卡片 -->
      <div class="stats-section">
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(194, 65, 12, 0.12);">
            <span class="material-symbols-outlined" style="color: var(--primary);">article</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ userStats.articleCount || 0 }}</div>
            <div class="stat-label">我的文章</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(217, 119, 6, 0.12);">
            <span class="material-symbols-outlined" style="color: var(--accent);">favorite</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ userStats.followingCount || 0 }}</div>
            <div class="stat-label">我的关注</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(16, 185, 129, 0.12);">
            <span class="material-symbols-outlined" style="color: #10b981;">group</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ userStats.followersCount || 0 }}</div>
            <div class="stat-label">我的粉丝</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(59, 130, 246, 0.12);">
            <span class="material-symbols-outlined" style="color: #3b82f6;">folder</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ userStats.projectCount || 0 }}</div>
            <div class="stat-label">我的项目</div>
          </div>
        </div>
      </div>

      <!-- 左右布局容器 -->
      <div class="profile-layout">
        <!-- 左侧：个人信息卡片 -->
        <div class="profile-card">
          <h1 class="page-title">个人信息</h1>
        
        <!-- 头像上传区域 -->
        <div class="avatar-section">
          <div class="avatar-preview">
            <img :src="formData.avatar || userInfo.avatar" alt="头像" />
            <div class="avatar-overlay" @click="triggerFileInput">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 5V19M5 12H19" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>更换头像</span>
            </div>
          </div>
          <input 
            ref="fileInput" 
            type="file" 
            accept="image/*" 
            @change="handleAvatarChange" 
            style="display: none"
          />
          <p class="avatar-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>
        </div>

        <!-- 信息编辑表单 -->
        <div class="form-section">
          <div class="form-group">
            <label class="form-label">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21M16 7C16 9.20914 14.2091 11 12 11C9.79086 11 8 9.20914 8 7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              用户名
            </label>
            <input 
              v-model="formData.username" 
              type="text" 
              class="form-input" 
              placeholder="请输入用户名"
            />
          </div>

          <div class="form-group">
            <label class="form-label">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 5C3 3.89543 3.89543 3 5 3H8.27924C8.70967 3 9.09181 3.27543 9.22792 3.68377L10.7257 8.17721C10.8831 8.64932 10.6694 9.16531 10.2243 9.38787L7.96701 10.5165C9.06925 12.9612 11.0388 14.9308 13.4835 16.033L14.6121 13.7757C14.8347 13.3306 15.3507 13.1169 15.8228 13.2743L20.3162 14.7721C20.7246 14.9082 21 15.2903 21 15.7208V19C21 20.1046 20.1046 21 19 21H18C9.71573 21 3 14.2843 3 6V5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              手机号
            </label>
            <input 
              v-model="formData.phone" 
              type="text" 
              class="form-input" 
              placeholder="请输入手机号"
            />
          </div>

          <div class="form-group">
            <label class="form-label">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 8L10.89 13.26C11.2187 13.4793 11.6049 13.5963 12 13.5963C12.3951 13.5963 12.7813 13.4793 13.11 13.26L21 8M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              邮箱
            </label>
            <input 
              v-model="formData.email" 
              type="email" 
              class="form-input" 
              placeholder="请输入邮箱"
            />
          </div>

          <div class="form-group readonly">
            <label class="form-label">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7 7H17M7 12H17M7 17H11M3 5C3 3.89543 3.89543 3 5 3H19C20.1046 3 21 3.89543 21 5V19C21 20.1046 20.1046 21 19 21H5C3.89543 21 3 20.1046 3 19V5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              账户编号
            </label>
            <input 
              :value="userInfo.account" 
              type="text" 
              class="form-input" 
              disabled
            />
          </div>

          <div class="form-group readonly">
            <label class="form-label">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 15C15.866 15 19 11.866 19 8C19 4.13401 15.866 1 12 1C8.13401 1 5 4.13401 5 8C5 11.866 8.13401 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8.21 13.89L7 23L12 20L17 23L15.79 13.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              会员等级
            </label>
            <input 
              :value="getUserLevel(userInfo.userLevel)" 
              type="text" 
              class="form-input" 
              disabled
            />
          </div>
        </div>

          <!-- 按钮区域 -->
          <div class="button-group">
            <button class="btn btn-cancel" @click="resetForm">重置</button>
            <button class="btn btn-submit" @click="handleSubmit" :disabled="loading">
              <span v-if="!loading">保存修改</span>
              <span v-else>保存中...</span>
            </button>
          </div>
        </div>

        <!-- 右侧：内容标签页 -->
        <div class="content-tabs-section">
        <div class="tabs-header">
          <button 
            v-for="tab in contentTabs" 
            :key="tab.id"
            class="tab-btn"
            :class="{ active: activeTab === tab.id }"
            @click="switchTab(tab.id)"
          >
            <span class="material-symbols-outlined">{{ tab.icon }}</span>
            {{ tab.label }}
          </button>
        </div>
        
        <!-- 我的博客 -->
        <div v-show="activeTab === 'articles'" class="tab-content">
          <div v-if="articlesLoading" class="content-loading">
            <div class="mini-spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="myArticles.length === 0" class="empty-state">
            <span class="material-symbols-outlined">article</span>
            <p>暂无发布的文章</p>
            <button class="btn-primary-small" @click="goToWrite">去发布</button>
          </div>
          <div v-else class="articles-grid">
            <div v-for="article in myArticles" :key="article.id" class="article-card" @click="goToArticle(article.id)">
              <img :src="article.coverImage || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=200&fit=crop'" :alt="article.title" class="article-cover" />
              <div class="article-info">
                <h4 class="article-title">{{ article.title }}</h4>
                <div class="article-meta">
                  <span><span class="material-symbols-outlined">visibility</span> {{ article.viewCount || 0 }}</span>
                  <span>{{ formatDate(article.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 我的关注 -->
        <div v-show="activeTab === 'following'" class="tab-content">
          <div v-if="followingLoading" class="content-loading">
            <div class="mini-spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="myFollowing.length === 0" class="empty-state">
            <span class="material-symbols-outlined">group</span>
            <p>暂无关注的用户</p>
            <button class="btn-primary-small" @click="goToHome">去发现</button>
          </div>
          <div v-else class="following-grid">
            <div v-for="user in myFollowing" :key="user.userId" class="user-card">
              <img :src="user.avatar || 'https://i.pravatar.cc/150?img=0'" :alt="user.username" class="user-avatar" />
              <div class="user-info">
                <h4 class="user-name">{{ user.username }}</h4>
                <p class="user-desc">{{ user.description || '这个人很懒，什么都没写' }}</p>
              </div>
              <button class="btn-unfollow" @click.stop="unfollowUser(user.userId)">取消关注</button>
            </div>
          </div>
        </div>
        
        <!-- 我的项目 -->
        <div v-show="activeTab === 'projects'" class="tab-content">
          <div v-if="projectsLoading" class="content-loading">
            <div class="mini-spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="myProjects.length === 0" class="empty-state">
            <span class="material-symbols-outlined">folder</span>
            <p>暂无发布的项目</p>
            <button class="btn-primary-small" @click="goToProjects">去发布</button>
          </div>
          <div v-else class="projects-grid">
            <div v-for="project in myProjects" :key="project.id" class="project-card" @click="goToProject(project.id)">
              <img :src="project.coverImage || 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&h=200&fit=crop'" :alt="project.projectName" class="project-cover" />
              <div class="project-info">
                <h4 class="project-title">{{ project.projectName }}</h4>
                <p class="project-desc">{{ project.shortDescription }}</p>
                <div class="project-meta">
                  <span><span class="material-symbols-outlined">visibility</span> {{ project.viewCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 设置 -->
        <div v-show="activeTab === 'settings'" class="tab-content settings-content">
          <div class="settings-nav">
            <button 
              v-for="setting in settingsTabs" 
              :key="setting.id"
              class="settings-nav-btn"
              :class="{ active: activeSettingsTab === setting.id }"
              @click="activeSettingsTab = setting.id"
            >
              <span class="material-symbols-outlined">{{ setting.icon }}</span>
              {{ setting.label }}
            </button>
          </div>
          
          <!-- 基础资料设置 -->
          <div v-show="activeSettingsTab === 'basic'" class="settings-panel">
            <h3 class="settings-title">基础资料</h3>
            <p class="settings-desc">修改您的个人基本信息</p>
            <div class="settings-form">
              <div class="form-row">
                <label>用户名</label>
                <input v-model="settingsForm.username" type="text" placeholder="请输入用户名" />
              </div>
              <div class="form-row">
                <label>个人简介</label>
                <textarea v-model="settingsForm.description" placeholder="介绍一下自己吧" rows="3"></textarea>
              </div>
              <button class="btn-save-settings" @click="saveBasicSettings" :disabled="savingSettings">
                {{ savingSettings ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </div>
          
          <!-- 账号安全设置 -->
          <div v-show="activeSettingsTab === 'security'" class="settings-panel">
            <h3 class="settings-title">账号安全</h3>
            <p class="settings-desc">管理您的账号安全设置</p>
            <div class="settings-form">
              <div class="form-row">
                <label>当前密码</label>
                <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
              </div>
              <div class="form-row">
                <label>新密码</label>
                <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
              </div>
              <div class="form-row">
                <label>确认新密码</label>
                <input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
              </div>
              <button class="btn-save-settings" @click="changePassword" :disabled="savingSettings">
                {{ savingSettings ? '修改中...' : '修改密码' }}
              </button>
            </div>
          </div>
          
          <!-- 通知设置 -->
          <div v-show="activeSettingsTab === 'notification'" class="settings-panel">
            <h3 class="settings-title">通知设置</h3>
            <p class="settings-desc">管理您的消息通知偏好</p>
            <div class="settings-form notification-form">
              <div class="notification-item">
                <div class="notification-info">
                  <span class="notification-label">评论通知</span>
                  <span class="notification-desc">当有人评论您的内容时通知您</span>
                </div>
                <label class="toggle-switch">
                  <input type="checkbox" v-model="notificationSettings.comment" />
                  <span class="toggle-slider"></span>
                </label>
              </div>
              <div class="notification-item">
                <div class="notification-info">
                  <span class="notification-label">关注通知</span>
                  <span class="notification-desc">当有新用户关注您时通知您</span>
                </div>
                <label class="toggle-switch">
                  <input type="checkbox" v-model="notificationSettings.follow" />
                  <span class="toggle-slider"></span>
                </label>
              </div>
              <div class="notification-item">
                <div class="notification-info">
                  <span class="notification-label">点赞通知</span>
                  <span class="notification-desc">当有人点赞您的内容时通知您</span>
                </div>
                <label class="toggle-switch">
                  <input type="checkbox" v-model="notificationSettings.like" />
                  <span class="toggle-slider"></span>
                </label>
              </div>
              <div class="notification-item">
                <div class="notification-info">
                  <span class="notification-label">系统通知</span>
                  <span class="notification-desc">接收系统公告和更新通知</span>
                </div>
                <label class="toggle-switch">
                  <input type="checkbox" v-model="notificationSettings.system" />
                  <span class="toggle-slider"></span>
                </label>
              </div>
              <p class="notification-tip">* 通知设置功能即将上线，敬请期待</p>
            </div>
          </div>
        </div>
      </div>
      </div> <!-- 关闭 profile-layout -->
    </main>

    <!-- 提示信息 -->
    <div v-if="showMessage" class="message-toast" :class="messageType">
      {{ messageText }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { updateUserInfo, uploadFile, getMyArticles, getMyFollowing, getMyProjects, changePassword as apiChangePassword } from '@/api/user'
import NavBar from '@/components/NavBar.vue'
import axios from 'axios'

const router = useRouter()
const userInfo = ref({})
const formData = reactive({
  username: '',
  phone: '',
  email: '',
  avatar: ''
})

const loading = ref(false)
const fileInput = ref(null)
const showMessage = ref(false)
const messageText = ref('')
const messageType = ref('success')

// 用户统计数据
const userStats = ref({
  articleCount: 0,
  followingCount: 0,
  followersCount: 0,
  projectCount: 0
})

// ==================== 标签页相关 ====================
const activeTab = ref('articles')
const contentTabs = [
  { id: 'articles', label: '我的博客', icon: 'article' },
  { id: 'following', label: '我的关注', icon: 'group' },
  { id: 'projects', label: '我的项目', icon: 'folder' },
  { id: 'settings', label: '设置', icon: 'settings' }
]

// 设置子标签
const activeSettingsTab = ref('basic')
const settingsTabs = [
  { id: 'basic', label: '基础资料', icon: 'person' },
  { id: 'security', label: '账号安全', icon: 'lock' },
  { id: 'notification', label: '通知设置', icon: 'notifications' }
]

// ==================== 内容数据 ====================
const myArticles = ref([])
const myFollowing = ref([])
const myProjects = ref([])
const articlesLoading = ref(false)
const followingLoading = ref(false)
const projectsLoading = ref(false)

// ==================== 设置表单 ====================
const settingsForm = reactive({
  username: '',
  description: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const notificationSettings = reactive({
  comment: true,
  follow: true,
  like: true,
  system: true
})

const savingSettings = ref(false)

// 切换标签页
const switchTab = (tabId) => {
  activeTab.value = tabId
  
  // 按需加载数据
  if (tabId === 'articles' && myArticles.value.length === 0) {
    loadMyArticles()
  } else if (tabId === 'following' && myFollowing.value.length === 0) {
    loadMyFollowing()
  } else if (tabId === 'projects' && myProjects.value.length === 0) {
    loadMyProjects()
  } else if (tabId === 'settings') {
    // 初始化设置表单
    settingsForm.username = userInfo.value.username || ''
    settingsForm.description = userInfo.value.description || ''
  }
}

// 加载我的文章
const loadMyArticles = async () => {
  articlesLoading.value = true
  try {
    const res = await getMyArticles()
    if (res.code === 1) {
      myArticles.value = res.data || []
    }
  } catch (error) {
    console.error('加载文章失败：', error)
  } finally {
    articlesLoading.value = false
  }
}

// 加载我的关注
const loadMyFollowing = async () => {
  followingLoading.value = true
  try {
    const res = await getMyFollowing()
    if (res.code === 1) {
      myFollowing.value = res.data || []
    }
  } catch (error) {
    console.error('加载关注列表失败：', error)
  } finally {
    followingLoading.value = false
  }
}

// 加载我的项目
const loadMyProjects = async () => {
  projectsLoading.value = true
  try {
    const res = await getMyProjects()
    if (res.code === 1) {
      myProjects.value = res.data || []
    }
  } catch (error) {
    console.error('加载项目失败：', error)
  } finally {
    projectsLoading.value = false
  }
}

// 取消关注
const unfollowUser = async (userId) => {
  if (!confirm('确定要取消关注吗？')) return
  
  try {
    const token = localStorage.getItem('token')
    // 使用 toggleFollow 接口（POST /user/{userId}/follow）
    const res = await axios.post(`/api/user/${userId}/follow`, {}, {
      headers: { authentication: token }
    })
    if (res.data.code === 1) {
      // 如果 isFollowing 为 false 表示取消关注成功
      if (!res.data.data.isFollowing) {
        myFollowing.value = myFollowing.value.filter(u => u.userId !== userId)
        userStats.value.followingCount = Math.max(0, userStats.value.followingCount - 1)
        showMessageToast('已取消关注', 'success')
      }
    }
  } catch (error) {
    console.error('取消关注失败：', error)
    showMessageToast('操作失败', 'error')
  }
}

// 保存基础设置
const saveBasicSettings = async () => {
  if (!settingsForm.username.trim()) {
    showMessageToast('用户名不能为空', 'error')
    return
  }
  
  savingSettings.value = true
  try {
    const res = await updateUserInfo({
      username: settingsForm.username,
      description: settingsForm.description
    })
    
    if (res.code === 1) {
      const newUserInfo = { ...userInfo.value, ...res.data }
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
      userInfo.value = newUserInfo
      showMessageToast('保存成功', 'success')
    } else {
      showMessageToast(res.msg || '保存失败', 'error')
    }
  } catch (error) {
    console.error('保存设置失败：', error)
    showMessageToast('保存失败', 'error')
  } finally {
    savingSettings.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordForm.oldPassword) {
    showMessageToast('请输入当前密码', 'error')
    return
  }
  if (!passwordForm.newPassword) {
    showMessageToast('请输入新密码', 'error')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    showMessageToast('新密码长度不能少于6位', 'error')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showMessageToast('两次输入的密码不一致', 'error')
    return
  }
  
  savingSettings.value = true
  try {
    const res = await apiChangePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res.code === 1) {
      showMessageToast('密码修改成功', 'success')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      showMessageToast(res.msg || '密码修改失败', 'error')
    }
  } catch (error) {
    console.error('修改密码失败：', error)
    showMessageToast(error.response?.data?.msg || '密码修改失败', 'error')
  } finally {
    savingSettings.value = false
  }
}

// 格式化日期
const formatDate = (time) => {
  if (!time) return ''
  
  // 处理Java LocalDateTime数组格式
  if (Array.isArray(time)) {
    const [year, month, day] = time
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  
  const date = new Date(time)
  if (isNaN(date.getTime())) return ''
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 导航方法
const goToArticle = (id) => router.push(`/article/${id}`)
const goToProject = (id) => router.push(`/project/${id}`)
const goToWrite = () => router.push('/write')
const goToHome = () => router.push('/home')
const goToProjects = () => router.push('/projects')

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const token = localStorage.getItem('token')
    const userId = userInfo.value.id || userInfo.value.userId
    if (!userId) return
    
    const response = await axios.get(`/api/user/${userId}/stats`, {
      headers: { authentication: token }
    })
    
    if (response.data.code === 1) {
      const stats = response.data.data
      userStats.value = {
        articleCount: stats.articleCount || 0,
        followingCount: stats.followingCount || 0,
        followersCount: stats.followersCount || 0,
        projectCount: stats.projectCount || 0
      }
    }
  } catch (error) {
    console.error('获取用户统计数据失败：', error)
  }
}

// 初始化
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
    resetForm()
    fetchUserStats()
    // 默认加载文章列表
    loadMyArticles()
  } else {
    router.push('/')
  }
})

// 获取会员等级文本
const getUserLevel = (level) => {
  const levels = { 0: '普通用户', 1: 'VIP会员', 2: 'SVIP会员' }
  return levels[level] || '普通用户'
}

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理头像上传
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件大小
  if (file.size > 2 * 1024 * 1024) {
    showMessageToast('文件大小不能超过 2MB', 'error')
    return
  }

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    showMessageToast('只能上传图片文件', 'error')
    return
  }

  try {
    loading.value = true
    const response = await uploadFile(file)
    formData.avatar = response.data
    showMessageToast('头像上传成功，请点击保存修改', 'success')
  } catch (error) {
    console.error('头像上传失败：', error)
    showMessageToast(error.message || '头像上传失败', 'error')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.username = userInfo.value.username
  formData.phone = userInfo.value.phone
  formData.email = userInfo.value.email
  formData.avatar = userInfo.value.avatar
}

// 提交表单
const handleSubmit = async () => {
  // 验证表单
  if (!formData.username || !formData.username.trim()) {
    showMessageToast('用户名不能为空', 'error')
    return
  }

  if (!formData.phone || !formData.phone.trim()) {
    showMessageToast('手机号不能为空', 'error')
    return
  }

  if (!formData.email || !formData.email.trim()) {
    showMessageToast('邮箱不能为空', 'error')
    return
  }

  // 手机号格式验证
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(formData.phone)) {
    showMessageToast('手机号格式不正确', 'error')
    return
  }

  // 邮箱格式验证
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(formData.email)) {
    showMessageToast('邮箱格式不正确', 'error')
    return
  }

  try {
    loading.value = true
    
    // 构建更新数据（只提交修改过的字段）
    const updateData = {}
    if (formData.username !== userInfo.value.username) {
      updateData.username = formData.username
    }
    if (formData.phone !== userInfo.value.phone) {
      updateData.phone = formData.phone
    }
    if (formData.email !== userInfo.value.email) {
      updateData.email = formData.email
    }
    if (formData.avatar !== userInfo.value.avatar) {
      updateData.avatar = formData.avatar
    }

    // 如果没有修改任何内容
    if (Object.keys(updateData).length === 0) {
      showMessageToast('没有任何修改', 'info')
      return
    }

    const response = await updateUserInfo(updateData)
    
    // 更新本地存储和当前数据
    const newUserInfo = {
      ...userInfo.value,
      ...response.data
    }
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    userInfo.value = newUserInfo
    resetForm()
    
    showMessageToast('个人信息修改成功', 'success')
  } catch (error) {
    console.error('修改失败：', error)
    showMessageToast(error.message || '修改失败，请重试', 'error')
  } finally {
    loading.value = false
  }
}

// 显示提示消息
const showMessageToast = (text, type = 'success') => {
  messageText.value = text
  messageType.value = type
  showMessage.value = true
  
  setTimeout(() => {
    showMessage.value = false
  }, 3000)
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

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.profile-container {
  min-height: 100vh;
  background: var(--background);
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
}

/* 主体内容 */
.profile-main {
  max-width: 1150px;
  margin: 0 auto;
  padding: 40px;
}

/* 数据统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 26px;
  padding: 28px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(194, 65, 12, 0.15);
  border-color: var(--primary);
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-wrapper .material-symbols-outlined {
  font-size: 28px;
  font-variation-settings: 'FILL' 1, 'wght' 700;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-family: 'Crimson Pro', serif;
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
}

/* 左右布局 */
.profile-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.profile-card {
  width: 380px;
  flex-shrink: 0;
  background: white;
  border-radius: 26px;
  padding: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
  position: sticky;
  top: 96px;
}

.page-title {
  font-family: 'Crimson Pro', serif;
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 40px;
  text-align: center;
  letter-spacing: -0.02em;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
}

.avatar-preview:hover {
  transform: scale(1.05);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  gap: 8px;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay svg {
  width: 24px;
  height: 24px;
}

.avatar-overlay span {
  font-size: 12px;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #95a5a6;
}

/* 表单区域 */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.form-label svg {
  width: 18px;
  height: 18px;
  color: #34495e;
}

.form-input {
  padding: 14px 18px;
  border: 1px solid var(--border-warm);
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.2s;
  background: white;
  font-family: 'Inter', sans-serif;
  color: var(--text-main);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.12);
}

.form-input:disabled {
  background: var(--surface);
  color: var(--text-muted);
  cursor: not-allowed;
}

.form-group.readonly .form-input {
  border-style: dashed;
  background: var(--surface);
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: var(--surface);
  color: var(--text-main);
  border: 1px solid var(--border-warm);
  font-weight: 600;
  font-family: 'Inter', sans-serif;
}

.btn-cancel:hover {
  background: #e8e2d9;
  border-color: var(--text-muted);
}

.btn-submit {
  background: var(--primary);
  color: white;
  border: none;
  font-weight: 700;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 14px rgba(194, 65, 12, 0.25);
}

.btn-submit:hover:not(:disabled) {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(194, 65, 12, 0.35);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 功能预览区域 */
.feature-preview {
  background: white;
  border-radius: 26px;
  padding: 40px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.section-title {
  font-family: 'Crimson Pro', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 28px;
  letter-spacing: -0.02em;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 28px;
  border: 1px solid var(--border-warm);
  border-radius: 20px;
  transition: all 0.3s;
  text-align: center;
  background: var(--surface);
}

.feature-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.feature-card:not(.disabled):hover {
  border-color: var(--primary);
  box-shadow: 0 8px 20px rgba(194, 65, 12, 0.15);
  transform: translateY(-4px);
  background: white;
}

.feature-card svg {
  width: 44px;
  height: 44px;
  color: var(--primary);
}

.feature-card h3 {
  font-family: 'Crimson Pro', serif;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.feature-card p {
  font-size: 14px;
  color: var(--text-muted);
  font-family: 'Inter', sans-serif;
  font-weight: 600;
}

/* 提示消息 */
.message-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translate(-50%, -20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}

.message-toast.success {
  background: #27ae60;
  color: white;
}

.message-toast.error {
  background: #e74c3c;
  color: white;
}

.message-toast.info {
  background: #3498db;
  color: white;
}

/* ==================== 内容标签页 ==================== */
.content-tabs-section {
  flex: 1;
  min-width: 0;
  background: white;
  border-radius: 26px;
  padding: 32px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
}

.tabs-header {
  display: flex;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 2px solid var(--border-warm);
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--surface);
  border: 1px solid var(--border-warm);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
}

.tab-btn .material-symbols-outlined {
  font-size: 20px;
}

.tab-btn:hover {
  background: white;
  border-color: var(--primary);
  color: var(--primary);
}

.tab-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.25);
}

/* 标签页内容 */
.tab-content {
  min-height: 300px;
}

.content-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px;
  color: var(--text-muted);
}

.mini-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--border-warm);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-muted);
  text-align: center;
}

.empty-state .material-symbols-outlined {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.4;
  color: var(--primary);
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.btn-primary-small {
  padding: 10px 24px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary-small:hover {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.3);
}

/* 文章网格 */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.article-card {
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border-warm);
  cursor: pointer;
  transition: all 0.3s;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(194, 65, 12, 0.15);
  border-color: var(--primary);
}

.article-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.article-info {
  padding: 16px;
}

.article-title {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-muted);
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-meta .material-symbols-outlined {
  font-size: 16px;
}

/* 关注列表 */
.following-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--surface);
  border-radius: 16px;
  border: 1px solid var(--border-warm);
  transition: all 0.2s;
}

.user-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.1);
}

.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-warm);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-desc {
  font-size: 14px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-unfollow {
  padding: 8px 16px;
  background: white;
  border: 1px solid var(--border-warm);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-unfollow:hover {
  border-color: #dc2626;
  color: #dc2626;
  background: rgba(220, 38, 38, 0.05);
}

/* 项目网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.project-card {
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border-warm);
  cursor: pointer;
  transition: all 0.3s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(194, 65, 12, 0.15);
  border-color: var(--primary);
}

.project-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.project-info {
  padding: 16px;
}

.project-title {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.project-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  display: flex;
  font-size: 13px;
  color: var(--text-muted);
}

.project-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.project-meta .material-symbols-outlined {
  font-size: 16px;
}

/* ==================== 设置面板 ==================== */
.settings-content {
  display: flex;
  gap: 32px;
  min-width: 0;
  overflow: hidden;
}

.settings-nav {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-nav-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  font-family: 'Inter', sans-serif;
}

.settings-nav-btn .material-symbols-outlined {
  font-size: 20px;
}

.settings-nav-btn:hover {
  background: var(--surface);
  color: var(--text-main);
}

.settings-nav-btn.active {
  background: var(--surface);
  border-color: var(--primary);
  color: var(--primary);
}

.settings-panel {
  flex: 1;
  min-width: 0;
  padding: 24px;
  background: var(--surface);
  border-radius: 20px;
  border: 1px solid var(--border-warm);
  overflow: hidden;
}

.settings-title {
  font-family: 'Crimson Pro', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.settings-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-warm);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  overflow: hidden;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.form-row label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

.form-row input,
.form-row textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 14px 18px;
  border: 1px solid var(--border-warm);
  border-radius: 12px;
  font-size: 15px;
  background: white;
  color: var(--text-main);
  font-family: 'Inter', sans-serif;
  transition: all 0.2s;
}

.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.12);
}

.form-row textarea {
  resize: vertical;
  min-height: 80px;
}

.btn-save-settings {
  align-self: flex-start;
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
  margin-top: 12px;
}

.btn-save-settings:hover:not(:disabled) {
  background: #9a3412;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(194, 65, 12, 0.3);
}

.btn-save-settings:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 通知设置 */
.notification-form {
  gap: 0;
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-warm);
}

.notification-item:last-of-type {
  border-bottom: none;
}

.notification-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
}

.notification-desc {
  font-size: 13px;
  color: var(--text-muted);
}

/* Toggle Switch */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 28px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-warm);
  transition: 0.3s;
  border-radius: 28px;
}

.toggle-slider::before {
  position: absolute;
  content: "";
  height: 22px;
  width: 22px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-switch input:checked + .toggle-slider {
  background-color: var(--primary);
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(22px);
}

.notification-tip {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-warm);
  font-size: 13px;
  color: var(--text-muted);
  font-style: italic;
}

/* 响应式 */
@media (max-width: 1200px) {
  .profile-layout {
    flex-direction: column;
  }

  .profile-card {
    width: 100%;
    position: static;
    margin-bottom: 24px;
  }
}

@media (max-width: 768px) {
  .profile-main {
    padding: 24px 16px;
  }

  .profile-card {
    padding: 24px;
  }

  .page-title {
    font-size: 24px;
  }

  .button-group {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .tabs-header {
    flex-direction: column;
  }

  .tab-btn {
    width: 100%;
    justify-content: center;
  }

  .settings-content {
    flex-direction: column;
  }

  .settings-nav {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .settings-nav-btn {
    flex: 1;
    justify-content: center;
    min-width: auto;
    padding: 12px;
  }

  .articles-grid,
  .projects-grid {
    grid-template-columns: 1fr;
  }
}
</style>

