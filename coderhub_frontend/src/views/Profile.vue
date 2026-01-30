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

      <!-- 预留功能区域 -->
      <div class="feature-preview">
        <h2 class="section-title">更多功能</h2>
        <div class="feature-grid">
          <div class="feature-card disabled">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 15C15.866 15 19 11.866 19 8C19 4.13401 15.866 1 12 1C8.13401 1 5 4.13401 5 8C5 11.866 8.13401 15 12 15Z" stroke="currentColor" stroke-width="2"/>
              <path d="M8.21 13.89L7 23L12 20L17 23L15.79 13.88" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>会员升级</h3>
            <p>即将开放</p>
          </div>
          <div class="feature-card disabled">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
              <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>浏览历史</h3>
            <p>即将开放</p>
          </div>
          <div class="feature-card disabled">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 21L12 16L5 21V5C5 4.46957 5.21071 3.96086 5.58579 3.58579C5.96086 3.21071 6.46957 3 7 3H17C17.5304 3 18.0391 3.21071 18.4142 3.58579C18.7893 3.96086 19 4.46957 19 5V21Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>我的收藏</h3>
            <p>即将开放</p>
          </div>
          <div class="feature-card disabled">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10.325 4.317C10.751 2.561 13.249 2.561 13.675 4.317C13.7389 4.5808 13.8642 4.82578 14.0407 5.032C14.2172 5.23822 14.4399 5.39985 14.6907 5.50375C14.9414 5.60764 15.2132 5.65085 15.4838 5.62987C15.7544 5.60889 16.0162 5.5243 16.248 5.383C17.791 4.443 19.558 6.209 18.618 7.753C18.4769 7.98466 18.3924 8.24634 18.3715 8.51677C18.3506 8.78721 18.3938 9.05877 18.4975 9.30938C18.6013 9.55999 18.7627 9.78258 18.9687 9.95905C19.1747 10.1355 19.4194 10.2609 19.683 10.325C21.439 10.751 21.439 13.249 19.683 13.675C19.4192 13.7389 19.1742 13.8642 18.968 14.0407C18.7618 14.2172 18.6001 14.4399 18.4963 14.6907C18.3924 14.9414 18.3491 15.2132 18.3701 15.4838C18.3911 15.7544 18.4757 16.0162 18.617 16.248C19.557 17.791 17.791 19.558 16.247 18.618C16.0153 18.4769 15.7537 18.3924 15.4832 18.3715C15.2128 18.3506 14.9412 18.3938 14.6906 18.4975C14.44 18.6013 14.2174 18.7627 14.0409 18.9687C13.8645 19.1747 13.7391 19.4194 13.675 19.683C13.249 21.439 10.751 21.439 10.325 19.683C10.2611 19.4192 10.1358 19.1742 9.95929 18.968C9.7828 18.7618 9.56011 18.6001 9.30935 18.4963C9.05859 18.3924 8.78683 18.3491 8.51621 18.3701C8.24559 18.3911 7.98375 18.4757 7.752 18.617C6.209 19.557 4.442 17.791 5.382 16.247C5.5231 16.0153 5.60755 15.7537 5.62848 15.4832C5.64942 15.2128 5.60624 14.9412 5.50247 14.6906C5.3987 14.44 5.23726 14.2174 5.03127 14.0409C4.82529 13.8645 4.58056 13.7391 4.317 13.675C2.561 13.249 2.561 10.751 4.317 10.325C4.5808 10.2611 4.82578 10.1358 5.032 9.95929C5.23822 9.7828 5.39985 9.56011 5.50375 9.30935C5.60764 9.05859 5.65085 8.78683 5.62987 8.51621C5.60889 8.24559 5.5243 7.98375 5.383 7.752C4.443 6.209 6.209 4.442 7.753 5.382C8.753 5.99 10.049 5.452 10.325 4.317Z" stroke="currentColor" stroke-width="2"/>
              <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>账号安全</h3>
            <p>即将开放</p>
          </div>
        </div>
      </div>
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
import { updateUserInfo, uploadFile } from '@/api/user'
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
  max-width: 1000px;
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

.profile-card {
  background: white;
  border-radius: 26px;
  padding: 40px;
  border: 1px solid var(--border-warm);
  box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);
  margin-bottom: 32px;
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

/* 响应式 */
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

  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

