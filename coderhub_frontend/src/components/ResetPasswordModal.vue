<template>
  <Teleport to="body">
    <div v-if="visible" class="reset-overlay" @click.self="close">
      <div class="reset-modal">
        <!-- Header -->
        <div class="reset-header">
          <h3>找回密码</h3>
          <button class="close-btn" @click="close">
            <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <!-- Step 1: 输入邮箱 + 发送验证码 -->
        <div v-if="step === 1" class="reset-body">
          <p class="step-hint">请输入注册时使用的邮箱，我们将发送验证码</p>
          <div class="field">
            <label>邮箱地址</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              :disabled="sending"
            />
          </div>
          <button
            class="action-btn primary"
            :disabled="!form.email || sending || countdown > 0"
            @click="handleSendCode"
          >
            <span v-if="sending" class="btn-spinner"></span>
            <span v-else-if="countdown > 0">{{ countdown }}s 后重新发送</span>
            <span v-else>发送验证码</span>
          </button>
          <p v-if="message" :class="['msg', msgType]">{{ message }}</p>
        </div>

        <!-- Step 2: 输入验证码 + 新密码 -->
        <div v-if="step === 2" class="reset-body">
          <p class="step-hint">验证码已发送至 <strong>{{ form.email }}</strong></p>
          <div class="field">
            <label>验证码</label>
            <input
              v-model="form.code"
              type="text"
              maxlength="6"
              placeholder="请输入6位验证码"
              :disabled="resetting"
            />
          </div>
          <div class="field">
            <label>新密码</label>
            <div class="pwd-wrapper">
              <input
                v-model="form.newPassword"
                :type="showPwd ? 'text' : 'password'"
                placeholder="请输入新密码（至少6位）"
                :disabled="resetting"
              />
              <button class="toggle-pwd" @click="showPwd = !showPwd" type="button">
                <svg v-if="!showPwd" viewBox="0 0 24 24" fill="none" width="16" height="16">
                  <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 5C7 5 3 12 3 12s4 7 9 7 9-7 9-7-4-7-9-7z" stroke="currentColor" stroke-width="2"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" width="16" height="16">
                  <path d="M3 3l18 18M12 9a3 3 0 013 3m-6 0a3 3 0 013-3M19 12s-2 4-7 4M5 12s2-4 7-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
          <div class="field">
            <label>确认密码</label>
            <input
              v-model="form.confirmPassword"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请再次输入新密码"
              :disabled="resetting"
            />
          </div>
          <div class="btn-row">
            <button class="action-btn secondary" @click="step = 1" :disabled="resetting">上一步</button>
            <button
              class="action-btn primary"
              :disabled="!canSubmit || resetting"
              @click="handleReset"
            >
              <span v-if="resetting" class="btn-spinner"></span>
              <span v-else>重置密码</span>
            </button>
          </div>
          <p v-if="message" :class="['msg', msgType]">{{ message }}</p>
        </div>

        <!-- Step 3: 成功 -->
        <div v-if="step === 3" class="reset-body success-body">
          <div class="success-icon">
            <svg viewBox="0 0 24 24" fill="none" width="48" height="48">
              <circle cx="12" cy="12" r="10" stroke="#10b981" stroke-width="2"/>
              <path d="M8 12l3 3 5-5" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h4>密码重置成功</h4>
          <p class="step-hint">请使用新密码登录</p>
          <button class="action-btn primary" @click="close">返回登录</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'
import { sendResetCode, resetPassword } from '@/api/user'

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['update:visible'])

const step = ref(1)
const form = ref({ email: '', code: '', newPassword: '', confirmPassword: '' })
const sending = ref(false)
const resetting = ref(false)
const countdown = ref(0)
const message = ref('')
const msgType = ref('error')
const showPwd = ref(false)
let timer = null

const canSubmit = computed(() => {
  return form.value.code.length === 6
    && form.value.newPassword.length >= 6
    && form.value.newPassword === form.value.confirmPassword
})

function close() {
  emit('update:visible', false)
  // 重置状态
  setTimeout(() => {
    step.value = 1
    form.value = { email: '', code: '', newPassword: '', confirmPassword: '' }
    message.value = ''
    sending.value = false
    resetting.value = false
  }, 300)
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleSendCode() {
  if (!form.value.email) return
  sending.value = true
  message.value = ''
  try {
    await sendResetCode(form.value.email)
    message.value = '验证码已发送，请查收邮箱'
    msgType.value = 'success'
    startCountdown()
    step.value = 2
  } catch (e) {
    message.value = e.message || '发送失败'
    msgType.value = 'error'
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  if (!canSubmit.value) return
  resetting.value = true
  message.value = ''
  try {
    await resetPassword({
      email: form.value.email,
      code: form.value.code,
      newPassword: form.value.newPassword
    })
    step.value = 3
  } catch (e) {
    message.value = e.message || '重置失败'
    msgType.value = 'error'
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped>
.reset-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.reset-modal {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.reset-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px 0;
}

.reset-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.reset-body {
  padding: 24px 32px 32px;
  display: flex;
  flex-direction: column;
}

.step-hint {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 18px;
  line-height: 1.5;
}

.step-hint strong {
  color: #c2410c;
}

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  font-size: 14px;
  color: #1f2937;
  background: #f9fafb;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.field input:focus {
  border-color: #c2410c;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.1);
}

.field input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pwd-wrapper {
  position: relative;
}

.pwd-wrapper input {
  padding-right: 40px;
}

.toggle-pwd {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 2px;
}

.toggle-pwd:hover {
  color: #c2410c;
}

.btn-row {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  height: 44px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* step1/step3 的单独按钮：宽度自适应并居中 */
.reset-body > .action-btn {
  flex: none;
  width: 100%;
  max-width: 200px;
  margin: 0 auto;
  display: flex;
}

.action-btn.primary {
  background: linear-gradient(135deg, #c2410c, #ea580c);
  color: #fff;
  box-shadow: 0 3px 12px rgba(194, 65, 12, 0.25);
}

.action-btn.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 5px 16px rgba(194, 65, 12, 0.3);
}

.action-btn.secondary {
  background: #f3f4f6;
  color: #374151;
}

.action-btn.secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.msg {
  font-size: 13px;
  margin: 12px 0 0;
  text-align: center;
}

.msg.error {
  color: #ef4444;
}

.msg.success {
  color: #10b981;
}

.success-body {
  text-align: center;
  padding: 32px 24px;
}

.success-icon {
  margin-bottom: 16px;
}

.success-body h4 {
  font-size: 18px;
  color: #1f2937;
  margin: 0 0 8px;
}

.success-body .action-btn {
  margin-top: 20px;
}
</style>
