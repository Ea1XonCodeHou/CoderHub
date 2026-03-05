<template>
  <div class="sn-wrap">
    <!-- 页面标题区 -->
    <div class="sn-header">
      <div class="sn-header-left">
        <div class="sn-icon-box">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h2 class="sn-title">发布系统通知</h2>
          <p class="sn-subtitle">消息将通过 RabbitMQ 异步投递至全体用户信箱</p>
        </div>
      </div>
    </div>

    <!-- 主卡片 -->
    <div class="sn-card">
      <div class="sn-form">

        <!-- Emoji 选择区 -->
        <div class="form-item">
          <label class="form-label">
            <span class="label-dot"></span>
            Emoji 前缀
            <span class="label-hint">（可选，附加在通知内容前）</span>
          </label>
          <div class="emoji-row">
            <button
              v-for="e in emojiOptions"
              :key="e"
              :class="['emoji-btn', { active: selectedEmoji === e }]"
              @click="toggleEmoji(e)"
              type="button"
            >{{ e }}</button>
            <button
              :class="['emoji-btn', 'emoji-clear', { active: selectedEmoji === '' }]"
              @click="selectedEmoji = ''"
              type="button"
            >无</button>
          </div>
        </div>

        <!-- 通知内容 -->
        <div class="form-item">
          <label class="form-label">
            <span class="label-dot required"></span>
            通知内容
          </label>
          <div class="content-input-wrap" :class="{ 'focused': contentFocused, 'has-error': showError }">
            <div v-if="selectedEmoji" class="emoji-preview">{{ selectedEmoji }}</div>
            <textarea
              v-model="content"
              class="content-textarea"
              :placeholder="'例如：平台将于今晚 22:00 进行系统维护，预计停服 30 分钟，敬请知悉。'"
              rows="4"
              maxlength="200"
              @focus="contentFocused = true; showError = false"
              @blur="contentFocused = false"
            />
          </div>
          <div class="input-footer">
            <span v-if="showError" class="error-tip">请填写通知内容</span>
            <span v-else class="char-count">{{ content.length }} / 200</span>
          </div>
        </div>

        <!-- 发送范围说明 -->
        <div class="scope-tip">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>当前发送范围：<strong>全体正常用户</strong>（状态正常的所有 USER 角色账户）</span>
        </div>

        <!-- 预览区 -->
        <div v-if="content.trim()" class="preview-box">
          <div class="preview-label">通知预览</div>
          <div class="preview-content">
            <div class="preview-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#2c3e50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="#2c3e50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="preview-text">
              <div class="preview-type">系统通知</div>
              <div class="preview-msg">{{ previewText }}</div>
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button class="btn-reset" @click="resetForm" :disabled="sending">清空</button>
          <button class="btn-send" @click="handleSend" :disabled="sending">
            <span v-if="sending" class="btn-spinner"></span>
            <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="22" y1="2" x2="11" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
            {{ sending ? '发送中...' : '一键发布' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 发送结果反馈卡片 -->
    <transition name="fade-slide">
      <div v-if="lastResult" :class="['result-card', lastResult.success ? 'result-success' : 'result-error']">
        <div class="result-icon">
          <svg v-if="lastResult.success" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="result-info">
          <div class="result-title">{{ lastResult.success ? '通知已成功提交' : '发送失败' }}</div>
          <div class="result-detail">{{ lastResult.message }}</div>
        </div>
        <button class="result-close" @click="lastResult = null">×</button>
      </div>
    </transition>

    <!-- 使用说明 -->
    <div class="guide-card">
      <h4 class="guide-title">📋 使用说明</h4>
      <ul class="guide-list">
        <li>点击「一键发布」后接口立即返回，通知在后台通过 RabbitMQ 异步投递</li>
        <li>消息类型为 <code>SYSTEM_WARNING</code>，用户可在信箱「系统消息」栏查看</li>
        <li>前端通知铃铛轮询间隔 30 秒，用户最多 30 秒后会看到未读角标变化</li>
        <li>每条通知内容上限 200 字，支持添加 Emoji 前缀增强辨识度</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import axios from 'axios'

// ==================== 状态 ====================
const selectedEmoji = ref('')
const content = ref('')
const contentFocused = ref(false)
const showError = ref(false)
const sending = ref(false)
const lastResult = ref(null)

// 常用 emoji 列表
const emojiOptions = ['📢', '⚠️', '🔔', '🛠️', '🎉', '📌', '💡', '🚀']

// 通知预览文字
const previewText = computed(() => {
  const prefix = selectedEmoji.value ? selectedEmoji.value + ' ' : ''
  return prefix + content.value
})

// ==================== 方法 ====================
const toggleEmoji = (e) => {
  selectedEmoji.value = selectedEmoji.value === e ? '' : e
}

const resetForm = () => {
  selectedEmoji.value = ''
  content.value = ''
  showError.value = false
  lastResult.value = null
}

const handleSend = async () => {
  if (!content.value.trim()) {
    showError.value = true
    return
  }

  sending.value = true
  lastResult.value = null

  try {
    const token = localStorage.getItem('token')
    const res = await axios.post('/api/admin/notification/broadcast', {
      emoji: selectedEmoji.value || null,
      content: content.value.trim()
    }, {
      headers: { authentication: token }
    })

    const data = res.data
    if (data.code === 1) {
      lastResult.value = {
        success: true,
        message: `已向 ${data.data.targetCount} 名用户发起通知，${data.data.message}`
      }
      // 发送成功后清空内容，保留 emoji 选择
      content.value = ''
    } else {
      lastResult.value = { success: false, message: data.msg || '发送失败，请重试' }
    }
  } catch (err) {
    lastResult.value = {
      success: false,
      message: err.response?.data?.msg || '网络异常，请检查后端服务'
    }
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.sn-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 760px;
}

/* 页面标题 */
.sn-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sn-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sn-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sn-icon-box svg {
  width: 24px;
  height: 24px;
  color: #fff;
}

.sn-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 4px 0;
}

.sn-subtitle {
  font-size: 13px;
  color: #718096;
  margin: 0;
}

/* 主卡片 */
.sn-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 28px 32px;
}

.sn-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 表单项 */
.form-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #cbd5e0;
  flex-shrink: 0;
}

.label-dot.required {
  background: #e53e3e;
}

.label-hint {
  font-weight: 400;
  color: #a0aec0;
  font-size: 12px;
}

/* Emoji 行 */
.emoji-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.emoji-btn {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  border: 1.5px solid #e2e8f0;
  background: #f7fafc;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-btn:hover {
  border-color: #4a5568;
  background: #edf2f7;
  transform: scale(1.1);
}

.emoji-btn.active {
  border-color: #2c3e50;
  background: #2c3e50;
  box-shadow: 0 2px 8px rgba(44, 62, 80, 0.25);
}

.emoji-clear {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
  width: auto;
  padding: 0 14px;
}

.emoji-clear.active {
  color: #fff;
}

/* 内容输入区 */
.content-input-wrap {
  display: flex;
  align-items: flex-start;
  gap: 0;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #fafafa;
  transition: border-color 0.2s, box-shadow 0.2s;
  overflow: hidden;
}

.content-input-wrap.focused {
  border-color: #4a5568;
  box-shadow: 0 0 0 3px rgba(74, 85, 104, 0.1);
  background: #fff;
}

.content-input-wrap.has-error {
  border-color: #e53e3e;
  box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1);
}

.emoji-preview {
  font-size: 22px;
  padding: 14px 0 14px 16px;
  line-height: 1.5;
  flex-shrink: 0;
  user-select: none;
}

.content-textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  padding: 14px 16px;
  font-size: 14px;
  font-family: inherit;
  color: #2d3748;
  line-height: 1.6;
  resize: none;
}

.content-textarea::placeholder {
  color: #bdc3cb;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  min-height: 20px;
}

.char-count {
  font-size: 12px;
  color: #a0aec0;
}

.error-tip {
  font-size: 12px;
  color: #e53e3e;
  font-weight: 500;
}

/* 范围说明 */
.scope-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f0f4ff;
  border-radius: 10px;
  border-left: 3px solid #4a5568;
  font-size: 13px;
  color: #4a5568;
}

.scope-tip svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #4a5568;
}

.scope-tip strong {
  font-weight: 700;
}

/* 预览区 */
.preview-box {
  border: 1.5px dashed #cbd5e0;
  border-radius: 12px;
  padding: 16px;
  background: #f7fafc;
}

.preview-label {
  font-size: 11px;
  font-weight: 700;
  color: #a0aec0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 12px;
}

.preview-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.preview-icon {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.preview-icon svg {
  width: 18px;
  height: 18px;
}

.preview-text {
  flex: 1;
}

.preview-type {
  font-size: 11px;
  font-weight: 700;
  color: #718096;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.preview-msg {
  font-size: 14px;
  color: #2d3748;
  line-height: 1.55;
  word-break: break-all;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 4px;
}

.btn-reset {
  padding: 10px 24px;
  border-radius: 10px;
  border: 1.5px solid #e2e8f0;
  background: #fff;
  color: #718096;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-reset:hover:not(:disabled) {
  border-color: #4a5568;
  color: #2d3748;
  background: #f7fafc;
}

.btn-send {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(44, 62, 80, 0.3);
}

.btn-send:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(44, 62, 80, 0.4);
}

.btn-send:disabled,
.btn-reset:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.btn-send svg {
  width: 16px;
  height: 16px;
}

/* 加载动画 */
.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 结果反馈卡片 */
.result-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border-radius: 12px;
  position: relative;
}

.result-success {
  background: #f0fff4;
  border: 1.5px solid #9ae6b4;
  color: #276749;
}

.result-error {
  background: #fff5f5;
  border: 1.5px solid #feb2b2;
  color: #c53030;
}

.result-icon svg {
  width: 22px;
  height: 22px;
}

.result-info {
  flex: 1;
}

.result-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 3px;
}

.result-detail {
  font-size: 13px;
  opacity: 0.85;
}

.result-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.5;
  color: inherit;
  padding: 0 4px;
  line-height: 1;
}

.result-close:hover {
  opacity: 1;
}

/* 使用说明 */
.guide-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.guide-title {
  font-size: 14px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 12px 0;
}

.guide-list {
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.guide-list li {
  font-size: 13px;
  color: #718096;
  line-height: 1.55;
}

.guide-list code {
  background: #edf2f7;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: #4a5568;
  font-family: 'JetBrains Mono', monospace;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
