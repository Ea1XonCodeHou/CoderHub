<template>
  <div class="aq-wrap">
    <!-- 页面标题区 -->
    <div class="aq-header">
      <div class="aq-header-left">
        <div class="aq-icon-box">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <polyline points="12 6 12 12 16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h2 class="aq-title">AI 提问额度管理</h2>
          <p class="aq-subtitle">管理普通用户（user_level=0）的免费提问次数，基于 Redis 存储</p>
        </div>
      </div>
    </div>

    <!-- 批量初始化卡片 -->
    <div class="aq-card">
      <div class="aq-card-header">
        <div class="aq-card-title-row">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <span>批量初始化全部用户额度</span>
        </div>
        <p class="aq-card-desc">为所有普通用户在 Redis 中写入初始额度，<strong>已有记录的用户自动跳过</strong>，不会覆盖现有额度。</p>
      </div>

      <div class="aq-form-row">
        <div class="aq-input-group">
          <label class="aq-label">初始额度（次）</label>
          <input
            v-model.number="batchQuota"
            type="number"
            min="1"
            max="9999"
            class="aq-input"
            placeholder="默认 10 次"
          />
        </div>
        <button
          class="aq-btn primary"
          :disabled="batchLoading"
          @click="handleBatchInit"
        >
          <svg v-if="!batchLoading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-4.5"/>
          </svg>
          <span class="aq-spinner" v-else></span>
          {{ batchLoading ? '初始化中...' : '一键初始化' }}
        </button>
      </div>

      <!-- 批量结果 -->
      <transition name="fade">
        <div v-if="batchResult" class="aq-result" :class="batchResult.ok ? 'ok' : 'err'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline v-if="batchResult.ok" points="20 6 9 17 4 12"/>
            <circle v-else cx="12" cy="12" r="10"/><line v-else x1="15" y1="9" x2="9" y2="15"/><line v-else x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          <span v-if="batchResult.ok">
            初始化完成：共 <strong>{{ batchResult.total }}</strong> 名用户，
            新初始化 <strong>{{ batchResult.initialized }}</strong> 人，
            已跳过 <strong>{{ batchResult.skipped }}</strong> 人（已有额度）
          </span>
          <span v-else>{{ batchResult.msg }}</span>
        </div>
      </transition>
    </div>

    <!-- 单用户查询/修改卡片 -->
    <div class="aq-card">
      <div class="aq-card-header">
        <div class="aq-card-title-row">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <span>查询 / 修改单个用户额度</span>
        </div>
        <p class="aq-card-desc">输入用户 ID 查询当前剩余额度，也可直接覆盖设置新值。</p>
      </div>

      <div class="aq-form-row">
        <div class="aq-input-group flex1">
          <label class="aq-label">用户 ID</label>
          <input
            v-model.trim="queryUserId"
            type="text"
            class="aq-input"
            placeholder="输入用户 UUID"
            @keyup.enter="handleQuery"
          />
        </div>
        <button class="aq-btn secondary" :disabled="queryLoading || !queryUserId" @click="handleQuery">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          {{ queryLoading ? '查询中...' : '查询' }}
        </button>
      </div>

      <!-- 查询结果展示 + 修改 -->
      <transition name="fade">
        <div v-if="queryResult !== null" class="aq-user-result">
          <div class="aq-user-quota-row">
            <span class="aq-quota-label">当前剩余额度</span>
            <span class="aq-quota-value" :class="{ zero: queryResult.quota === 0 }">
              {{ queryResult.exists ? queryResult.quota + ' 次' : '未初始化' }}
            </span>
          </div>

          <div class="aq-form-row mt12">
            <div class="aq-input-group">
              <label class="aq-label">设置新额度（次）</label>
              <input
                v-model.number="newQuota"
                type="number"
                min="0"
                max="9999"
                class="aq-input"
                placeholder="输入新的额度值"
              />
            </div>
            <button
              class="aq-btn primary"
              :disabled="setLoading || newQuota === null || newQuota === ''"
              @click="handleSetQuota"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
              </svg>
              {{ setLoading ? '保存中...' : '保存修改' }}
            </button>
          </div>

          <transition name="fade">
            <div v-if="setResult" class="aq-result" :class="setResult.ok ? 'ok' : 'err'">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline v-if="setResult.ok" points="20 6 9 17 4 12"/>
                <circle v-else cx="12" cy="12" r="10"/><line v-else x1="15" y1="9" x2="9" y2="15"/><line v-else x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              <span>{{ setResult.msg }}</span>
            </div>
          </transition>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const token = () => localStorage.getItem('token')

// ==================== 批量初始化 ====================
const batchQuota = ref(10)
const batchLoading = ref(false)
const batchResult = ref(null)

const handleBatchInit = async () => {
  batchLoading.value = true
  batchResult.value = null
  try {
    const res = await fetch('/api/admin/ai-quota/init-all', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token()}`
      },
      body: JSON.stringify({ quota: batchQuota.value || 10 })
    })
    const json = await res.json()
    if (json.code === 1 && json.data) {
      batchResult.value = { ok: true, ...json.data }
    } else {
      batchResult.value = { ok: false, msg: json.msg || '初始化失败' }
    }
  } catch (e) {
    batchResult.value = { ok: false, msg: '网络错误：' + e.message }
  } finally {
    batchLoading.value = false
  }
}

// ==================== 单用户查询 + 修改 ====================
const queryUserId = ref('')
const queryLoading = ref(false)
const queryResult = ref(null)
const newQuota = ref('')
const setLoading = ref(false)
const setResult = ref(null)

const handleQuery = async () => {
  if (!queryUserId.value) return
  queryLoading.value = true
  queryResult.value = null
  setResult.value = null
  newQuota.value = ''
  try {
    const res = await fetch(`/api/admin/ai-quota/user/${queryUserId.value}`, {
      headers: { Authorization: `Bearer ${token()}` }
    })
    const json = await res.json()
    if (json.code === 1 && json.data) {
      queryResult.value = json.data
    } else {
      queryResult.value = { exists: false, quota: null }
    }
  } catch (e) {
    queryResult.value = { exists: false, quota: null }
  } finally {
    queryLoading.value = false
  }
}

const handleSetQuota = async () => {
  if (newQuota.value === '' || newQuota.value === null) return
  setLoading.value = true
  setResult.value = null
  try {
    const res = await fetch(`/api/admin/ai-quota/user/${queryUserId.value}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token()}`
      },
      body: JSON.stringify({ quota: newQuota.value })
    })
    const json = await res.json()
    if (json.code === 1) {
      setResult.value = { ok: true, msg: `已将用户额度设置为 ${newQuota.value} 次` }
      // 刷新查询结果
      queryResult.value = { ...queryResult.value, exists: true, quota: newQuota.value }
    } else {
      setResult.value = { ok: false, msg: json.msg || '保存失败' }
    }
  } catch (e) {
    setResult.value = { ok: false, msg: '网络错误：' + e.message }
  } finally {
    setLoading.value = false
  }
}
</script>

<style scoped>
.aq-wrap {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ==================== 标题区 ==================== */
.aq-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.aq-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.aq-icon-box {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.aq-icon-box svg {
  width: 24px;
  height: 24px;
  color: white;
  stroke: white;
}

.aq-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px;
}

.aq-subtitle {
  font-size: 13px;
  color: #8892a4;
  margin: 0;
}

/* ==================== 卡片 ==================== */
.aq-card {
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.aq-card-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.aq-card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
}

.aq-card-title-row svg {
  width: 18px;
  height: 18px;
  color: #667eea;
  stroke: #667eea;
  flex-shrink: 0;
}

.aq-card-desc {
  font-size: 13px;
  color: #8892a4;
  margin: 0;
  line-height: 1.6;
}

/* ==================== 表单行 ==================== */
.aq-form-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.aq-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.aq-input-group.flex1 {
  flex: 1;
  min-width: 200px;
}

.aq-label {
  font-size: 12px;
  font-weight: 600;
  color: #5a6478;
  letter-spacing: 0.3px;
}

.aq-input {
  height: 40px;
  padding: 0 12px;
  border: 1.5px solid #dde1ea;
  border-radius: 10px;
  font-size: 14px;
  color: #2c3e50;
  background: #fafbfc;
  outline: none;
  transition: border-color 0.2s;
  min-width: 120px;
}

.aq-input:focus {
  border-color: #667eea;
  background: #fff;
}

/* ==================== 按钮 ==================== */
.aq-btn {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 7px;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.aq-btn svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.aq-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.aq-btn.primary:hover:not(:disabled) {
  opacity: 0.88;
  transform: translateY(-1px);
}

.aq-btn.secondary {
  background: #f0f2f8;
  color: #4a5568;
}

.aq-btn.secondary:hover:not(:disabled) {
  background: #e2e6f0;
}

.aq-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.aq-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ==================== 结果提示 ==================== */
.aq-result {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
}

.aq-result svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.aq-result.ok {
  background: #f0faf4;
  color: #2d7a4f;
  border: 1px solid #b7e4c7;
}

.aq-result.ok svg {
  stroke: #2d7a4f;
}

.aq-result.err {
  background: #fff5f5;
  color: #c0392b;
  border: 1px solid #f5b7b1;
}

.aq-result.err svg {
  stroke: #c0392b;
}

/* ==================== 单用户查询结果 ==================== */
.aq-user-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f7f8fc;
  border-radius: 12px;
  border: 1px solid #eef0f4;
}

.aq-user-quota-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.aq-quota-label {
  font-size: 13px;
  color: #8892a4;
  font-weight: 500;
}

.aq-quota-value {
  font-size: 22px;
  font-weight: 700;
  color: #667eea;
}

.aq-quota-value.zero {
  color: #e74c3c;
}

.mt12 {
  margin-top: 4px;
}

/* ==================== 过渡动画 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
