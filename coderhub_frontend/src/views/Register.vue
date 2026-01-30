<template>
  <div class="auth-container">
    <!-- 背景动画 -->
    <div class="animated-background">
      <!-- 大型浮动形状 -->
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      
      <!-- 几何图形 -->
      <div class="geometry geo-1"></div>
      <div class="geometry geo-2"></div>
      <div class="geometry geo-3"></div>
      <div class="geometry geo-4"></div>
      
      <!-- 粒子效果 -->
      <div class="particles">
        <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
      </div>
      
      <!-- 网格线 -->
      <div class="grid-lines">
        <div v-for="i in 8" :key="'v' + i" class="grid-line vertical" :style="{ left: (i * 12.5) + '%' }"></div>
        <div v-for="i in 6" :key="'h' + i" class="grid-line horizontal" :style="{ top: (i * 16.6) + '%' }"></div>
      </div>
    </div>

    <div class="auth-shell">
      <!-- 左侧标语区 -->
      <section class="auth-hero">
        <span class="hero-kicker">Join CoderHub</span>
        <h2 class="hero-title">从记录到分享，打造你的技术作品集</h2>
        <p class="hero-subtitle">
          无论是文章、教程还是项目，CoderHub 都能帮你沉淀成长路径。
        </p>
        <div class="hero-badges">
          <span class="hero-badge">📝 博客发布</span>
          <span class="hero-badge">🧩 项目展示</span>
          <span class="hero-badge">🚀 能力成长</span>
        </div>
        <ul class="hero-list">
          <li>沉浸式知识管理，记录你的每一步</li>
          <li>团队协作与社区互动，找到同频伙伴</li>
          <li>AI 智能体辅助学习与检索</li>
        </ul>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-label">寻找开发者</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">学习项目</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">技术灵感阅读</span>
          </div>
        </div>
        <div class="hero-quote">
          “在这里，把你的技术实力变成能被看见的作品。”
        </div>
      </section>

      <!-- 注册卡片 -->
      <div class="auth-card" :class="{ 'shake': isShaking }">
      <!-- Logo和标题 -->
      <div class="header">
        <div class="logo">
          <span class="material-symbols-outlined logo-icon">book_4</span>
        </div>
        <h1 class="title">加入 CoderHub</h1>
        <p class="subtitle">开启你的技术分享之旅</p>
      </div>

      <!-- 注册表单 -->
      <form @submit.prevent="handleRegister" class="form">
        <div class="form-group">
          <label for="phone">手机号</label>
          <div class="input-wrapper">
            <input
              id="phone"
              v-model="registerForm.phone"
              type="tel"
              placeholder="请输入手机号"
              pattern="[0-9]{11}"
              required
              :disabled="loading"
            />
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M5 4H9L11 9L8.5 10.5C9.57096 12.6715 11.3285 14.429 13.5 15.5L15 13L20 15V19C20 19.5304 19.7893 20.0391 19.4142 20.4142C19.0391 20.7893 18.5304 21 18 21C14.0993 20.763 10.4202 19.1065 7.65683 16.3432C4.8935 13.5798 3.23705 9.90074 3 6C3 5.46957 3.21071 4.96086 3.58579 4.58579C3.96086 4.21071 4.46957 4 5 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <div class="input-wrapper">
            <input
              id="email"
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱"
              required
              :disabled="loading"
            />
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 8L10.89 13.26C11.54 13.67 12.46 13.67 13.11 13.26L21 8M5 19H19C20.1046 19 21 18.1046 21 17V7C21 5.89543 20.1046 5 19 5H5C3.89543 5 3 5.89543 3 7V17C3 18.1046 3.89543 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-wrapper">
            <input
              id="password"
              v-model="registerForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码（至少6位）"
              minlength="6"
              required
              :disabled="loading"
            />
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M19 12C19 14 17 17 12 17C7 17 5 14 5 12C5 10 7 7 12 7C17 7 19 10 19 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
              :disabled="loading"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                <path d="M12 5C7 5 3 12 3 12C3 12 7 19 12 19C17 19 21 12 21 12C21 12 17 5 12 5Z" stroke="currentColor" stroke-width="2"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 3L21 21M12 9C13.6569 9 15 10.3431 15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12M19 12C19 12 17 16 12 16M5 12C5 12 7 8 12 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <div class="input-wrapper">
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              required
              :disabled="loading"
            />
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21M12.5 7C12.5 8.933 10.933 10.5 9 10.5C7.067 10.5 5.5 8.933 5.5 7C5.5 5.067 7.067 3.5 9 3.5C10.933 3.5 12.5 5.067 12.5 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <button
              type="button"
              class="toggle-password"
              @click="showConfirmPassword = !showConfirmPassword"
              :disabled="loading"
            >
              <svg v-if="!showConfirmPassword" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                <path d="M12 5C7 5 3 12 3 12C3 12 7 19 12 19C17 19 21 12 21 12C21 12 17 5 12 5Z" stroke="currentColor" stroke-width="2"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 3L21 21M12 9C13.6569 9 15 10.3431 15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12M19 12C19 12 17 16 12 16M5 12C5 12 7 8 12 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <p v-if="passwordMismatch" class="error-text">两次密码输入不一致</p>
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading || passwordMismatch">
          <span v-if="!loading">注册</span>
          <span v-else class="loading-spinner"></span>
        </button>
      </form>

      <!-- 底部链接 -->
      <div class="footer">
        <span class="footer-text">已有账号？</span>
        <router-link to="/" class="footer-link">立即登录</router-link>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'

const router = useRouter()

const registerForm = ref({
  phone: '',
  email: '',
  password: ''
})

const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const isShaking = ref(false)

const passwordMismatch = computed(() => {
  return confirmPassword.value && registerForm.value.password !== confirmPassword.value
})

const getParticleStyle = (index) => {
  const size = Math.random() * 4 + 2
  const left = Math.random() * 100
  const animationDuration = Math.random() * 20 + 15
  const animationDelay = Math.random() * 5
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    top: `${Math.random() * 100}%`,
    animationDuration: `${animationDuration}s`,
    animationDelay: `${animationDelay}s`
  }
}

const handleRegister = async () => {
  // 验证两次密码是否一致
  if (registerForm.value.password !== confirmPassword.value) {
    alert('两次密码输入不一致')
    return
  }

  loading.value = true
  
  try {
    const response = await register(registerForm.value)
    
    console.log('注册成功！', response)
    alert('注册成功！即将跳转到登录页面')
    
    // 跳转到登录页
    router.push('/')
  } catch (error) {
    console.error('注册失败：', error)
    isShaking.value = true
    setTimeout(() => {
      isShaking.value = false
    }, 500)
    alert(error.message || '注册失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

:global(:root) {
  --primary: #c2410c;
  --primary-muted: #ea580c;
  --background: #fdfaf6;
  --surface: #f7f2eb;
  --text-main: #2d2a26;
  --text-muted: #8c8273;
  --border-warm: #eaddd3;
  --selection: #fef3c7;
}

:global(::selection) {
  background-color: var(--selection);
  color: var(--primary);
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #ecf0f1 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景动画 */
.animated-background {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(45deg, #bdc3c7, #95a5a6);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #dfe6e9, #b2bec3);
  bottom: -150px;
  right: -150px;
  animation-delay: 5s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(225deg, #ecf0f1, #bdc3c7);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 30px) scale(0.9);
  }
}

/* 几何图形 */
.geometry {
  position: absolute;
  opacity: 0.15;
  animation: rotate-float 25s infinite ease-in-out;
}

.geo-1 {
  width: 100px;
  height: 100px;
  border: 2px solid #2c3e50;
  border-radius: 20px;
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.geo-2 {
  width: 80px;
  height: 80px;
  border: 2px solid #34495e;
  border-radius: 50%;
  top: 65%;
  right: 15%;
  animation-delay: 5s;
}

.geo-3 {
  width: 60px;
  height: 60px;
  border: 2px solid #2c3e50;
  transform: rotate(45deg);
  top: 40%;
  left: 85%;
  animation-delay: 10s;
}

.geo-4 {
  width: 120px;
  height: 120px;
  border: 2px solid #95a5a6;
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  bottom: 20%;
  left: 20%;
  animation-delay: 15s;
}

@keyframes rotate-float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 0.15;
  }
  25% {
    transform: translate(20px, -30px) rotate(90deg);
    opacity: 0.25;
  }
  50% {
    transform: translate(-15px, 20px) rotate(180deg);
    opacity: 0.2;
  }
  75% {
    transform: translate(30px, 15px) rotate(270deg);
    opacity: 0.25;
  }
}

/* 粒子效果 */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.particle {
  position: absolute;
  background: #2c3e50;
  border-radius: 50%;
  opacity: 0.4;
  animation: particle-float infinite ease-in-out;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-100px) translateX(50px);
    opacity: 0.8;
  }
}

/* 网格线 */
.grid-lines {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0.08;
}

.grid-line {
  position: absolute;
  background: #2c3e50;
}

.grid-line.vertical {
  width: 1px;
  height: 100%;
  animation: grid-pulse-v 8s infinite ease-in-out;
}

.grid-line.horizontal {
  width: 100%;
  height: 1px;
  animation: grid-pulse-h 8s infinite ease-in-out;
}

@keyframes grid-pulse-v {
  0%, 100% {
    opacity: 0.08;
    transform: scaleY(1);
  }
  50% {
    opacity: 0.15;
    transform: scaleY(1.05);
  }
}

@keyframes grid-pulse-h {
  0%, 100% {
    opacity: 0.08;
    transform: scaleX(1);
  }
  50% {
    opacity: 0.15;
    transform: scaleX(1.05);
  }
}

/* 注册卡片 */
.auth-card {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.shake {
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
  20%, 40%, 60%, 80% { transform: translateX(10px); }
}

/* Header */
.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.logo svg {
  width: 100%;
  height: 100%;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

/* 表单 */
.form {
  margin-bottom: 32px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  height: 52px;
  padding: 0 48px 0 48px;
  font-size: 15px;
  color: #2d3748;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  outline: none;
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  background: #ffffff;
  border-color: #2c3e50;
  box-shadow: 0 0 0 4px rgba(44, 62, 80, 0.1);
}

.input-wrapper input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #a0aec0;
  pointer-events: none;
  transition: color 0.3s ease;
}

.input-wrapper input:focus + .input-icon {
  color: #2c3e50;
}

.toggle-password {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.toggle-password:hover {
  color: #2c3e50;
}

.toggle-password svg {
  width: 100%;
  height: 100%;
}

.error-text {
  font-size: 12px;
  color: #f56565;
  margin-top: 6px;
  margin-left: 4px;
}

/* 按钮 */
.btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(44, 62, 80, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(44, 62, 80, 0.4);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Footer */
.footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.footer-text {
  font-size: 14px;
  color: #718096;
  margin-right: 8px;
}

.footer-link {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: #34495e;
}

/* ==================== Stitch 暖色风格覆盖 ==================== */
.auth-container {
  background: linear-gradient(135deg, #fff7ee 0%, #f8eada 100%);
  font-family: 'Inter', sans-serif;
}

.auth-shell {
  width: min(1100px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: 48px;
  align-items: center;
  position: relative;
  z-index: 2;
}

.auth-hero {
  padding: 12px 8px;
  color: var(--text-main);
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--primary);
  background: #fff1e6;
  border-radius: 999px;
  margin-bottom: 16px;
}

.hero-title {
  font-family: 'Crimson Pro', serif;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 16px;
  color: #1f2937;
}

.hero-subtitle {
  font-family: 'Crimson Pro', serif;
  font-size: 16px;
  line-height: 1.7;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.hero-badge {
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  background: #fffaf5;
  border: 1px solid var(--border-warm);
  color: #7c6d60;
}

.hero-list {
  list-style: none;
  padding: 0;
  margin: 0 0 22px 0;
  display: grid;
  gap: 10px;
  color: var(--text-main);
  font-size: 14px;
}

.hero-list li {
  padding-left: 20px;
  position: relative;
}

.hero-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--primary);
}

.hero-stats {
  display: flex;
  gap: 28px;
  margin-bottom: 20px;
}

.hero-stats .stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-stats .stat-number {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}

.hero-stats .stat-label {
  font-size: 12px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-quote {
  font-family: 'Crimson Pro', serif;
  font-style: italic;
  color: #7c6d60;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border-warm);
  padding: 14px 18px;
  border-radius: 16px;
}

.shape-1,
.shape-2,
.shape-3 {
  opacity: 0.35;
  filter: blur(100px);
}

.shape-1 {
  background: linear-gradient(135deg, #ffd9c7, #ffc9b0);
}

.shape-2 {
  background: linear-gradient(135deg, #ffe8d6, #ffd9c7);
}

.shape-3 {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.geometry {
  border-color: #d9cfc4;
}

.particle {
  background: #c4b5a5;
}

.grid-line {
  background: #d9cfc4;
}

.auth-card {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid var(--border-warm);
  box-shadow: 0 20px 50px rgba(45, 42, 38, 0.2);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff1e6;
  border: 1px solid var(--border-warm);
  border-radius: 18px;
  animation: none;
}

.logo-icon {
  font-size: 32px;
  color: var(--primary);
}

.title {
  font-family: 'Crimson Pro', serif;
  color: #1f2937;
}

.subtitle {
  color: var(--text-muted);
}

.form-group label {
  color: #3f3a35;
}

.input-wrapper input {
  background: #ffffff;
  border-color: var(--border-warm);
}

.input-wrapper input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(217, 119, 6, 0.12);
}

.input-icon {
  color: #b7a99b;
}

.toggle-password {
  color: #b7a99b;
}

.toggle-password:hover {
  color: var(--primary);
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-muted) 100%);
  box-shadow: 0 6px 18px rgba(194, 65, 12, 0.3);
}

.btn-primary:hover:not(:disabled) {
  box-shadow: 0 10px 26px rgba(194, 65, 12, 0.35);
}

.footer {
  border-top: 1px solid var(--border-warm);
}

.footer-text {
  color: var(--text-muted);
}

.footer-link {
  color: var(--primary);
}

.footer-link:hover {
  color: var(--primary-muted);
}

/* 响应式 */
@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px;
  }
  
  .title {
    font-size: 28px;
  }
  
  .form-group {
    margin-bottom: 18px;
  }
}

@media (max-width: 960px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    text-align: center;
  }

  .hero-badges,
  .hero-stats {
    justify-content: center;
  }
}
</style>

