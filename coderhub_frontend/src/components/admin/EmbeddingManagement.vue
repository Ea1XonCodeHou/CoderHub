<template>
  <div class="embedding-management">
    <div class="section-header">
      <h2>向量化管理 (RAG)</h2>
      <p class="section-desc">管理文章向量化入库，支持ChromaDB语义检索</p>
    </div>

    <!-- 操作卡片区 -->
    <div class="action-cards">
      <!-- 健康检查 -->
      <div class="action-card">
        <div class="card-header">
          <h3>🔗 连接状态</h3>
        </div>
        <div class="card-body">
          <p class="status-text" :class="healthStatus">{{ healthMessage }}</p>
          <button @click="checkHealth" class="btn-primary" :disabled="loading.health">
            {{ loading.health ? '检测中...' : '检测连接' }}
          </button>
        </div>
      </div>

      <!-- 批量向量化 -->
      <div class="action-card">
        <div class="card-header">
          <h3>📦 批量向量化</h3>
        </div>
        <div class="card-body">
          <p>将所有已发布文章向量化并存入ChromaDB</p>
          <button @click="batchEmbed" class="btn-primary" :disabled="loading.batch">
            {{ loading.batch ? '处理中...' : '开始批量向量化' }}
          </button>
          <p v-if="batchResult" class="result-text success">
            ✅ 成功向量化 {{ batchResult.successCount }} 篇，耗时 {{ batchResult.timeMs }}ms
          </p>
        </div>
      </div>

      <!-- 单篇向量化 -->
      <div class="action-card">
        <div class="card-header">
          <h3>📄 单篇向量化</h3>
        </div>
        <div class="card-body">
          <input 
            v-model="singleArticleId" 
            type="text" 
            placeholder="输入文章ID" 
            class="form-input"
          />
          <div class="btn-group">
            <button @click="embedSingle" class="btn-primary" :disabled="loading.single || !singleArticleId">
              {{ loading.single ? '处理中...' : '向量化' }}
            </button>
            <button @click="deleteSingle" class="btn-danger" :disabled="loading.delete || !singleArticleId">
              {{ loading.delete ? '删除中...' : '删除向量' }}
            </button>
          </div>
          <p v-if="singleResult" class="result-text" :class="singleResult.success ? 'success' : 'error'">
            {{ singleResult.message }}
          </p>
        </div>
      </div>
    </div>

    <!-- 语义检索测试 -->
    <div class="search-section">
      <div class="section-header">
        <h3>🔍 语义检索测试</h3>
      </div>
      <div class="search-form">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="输入查询文本（如：Spring Boot入门教程）" 
          class="form-input search-input"
          @keyup.enter="searchSimilar"
        />
        <select v-model="searchTopK" class="form-select">
          <option :value="3">Top 3</option>
          <option :value="5">Top 5</option>
          <option :value="10">Top 10</option>
        </select>
        <button @click="searchSimilar" class="btn-primary" :disabled="loading.search || !searchQuery">
          {{ loading.search ? '检索中...' : '检索' }}
        </button>
      </div>

      <!-- 检索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <h4>检索结果（{{ searchResults.length }} 条）</h4>
        <div v-for="(item, index) in searchResults" :key="index" class="result-item">
          <div class="result-header">
            <span class="result-rank">#{{ index + 1 }}</span>
            <span class="result-title">{{ item.title || '无标题' }}</span>
            <span class="result-score">相似度: {{ (item.score * 100).toFixed(1) }}%</span>
          </div>
          <div class="result-meta">
            <span>文章ID: {{ item.articleId }}</span>
            <span v-if="item.authorName">作者: {{ item.authorName }}</span>
            <span v-if="item.categoryName">分类: {{ item.categoryName }}</span>
          </div>
          <p class="result-text-preview">{{ truncateText(item.text, 200) }}</p>
        </div>
      </div>
      <p v-else-if="searchPerformed && !loading.search" class="empty-text">未找到相关文章</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

// 获取带认证的请求配置
const getAuthConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      authentication: token
    }
  }
}

// 状态
const healthStatus = ref('')
const healthMessage = ref('点击检测连接状态')
const batchResult = ref(null)
const singleArticleId = ref('')
const singleResult = ref(null)
const searchQuery = ref('')
const searchTopK = ref(5)
const searchResults = ref([])
const searchPerformed = ref(false)

// 加载状态
const loading = ref({
  health: false,
  batch: false,
  single: false,
  delete: false,
  search: false
})

// 检测ChromaDB连接
const checkHealth = async () => {
  loading.value.health = true
  healthStatus.value = ''
  try {
    const response = await axios.get('/api/admin/embedding/health', getAuthConfig())
    if (response.data.code === 1) {
      healthStatus.value = 'success'
      healthMessage.value = response.data.data
    } else {
      healthStatus.value = 'error'
      healthMessage.value = response.data.msg || '连接失败'
    }
  } catch (error) {
    healthStatus.value = 'error'
    healthMessage.value = '连接失败: ' + (error.message || '未知错误')
  } finally {
    loading.value.health = false
  }
}

// 批量向量化
const batchEmbed = async () => {
  if (!confirm('确定要批量向量化所有已发布文章吗？这可能需要一些时间。')) return
  
  loading.value.batch = true
  batchResult.value = null
  try {
    const response = await axios.post('/api/admin/embedding/batch', {}, getAuthConfig())
    if (response.data.code === 1) {
      batchResult.value = response.data.data
    } else {
      alert('批量向量化失败: ' + (response.data.msg || '未知错误'))
    }
  } catch (error) {
    alert('批量向量化失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value.batch = false
  }
}

// 单篇向量化
const embedSingle = async () => {
  if (!singleArticleId.value) return
  
  loading.value.single = true
  singleResult.value = null
  try {
    const response = await axios.post(`/api/admin/embedding/article/${singleArticleId.value}`, {}, getAuthConfig())
    singleResult.value = {
      success: response.data.code === 1,
      message: response.data.code === 1 ? '✅ 向量化成功' : ('❌ ' + (response.data.msg || '向量化失败'))
    }
  } catch (error) {
    singleResult.value = {
      success: false,
      message: '❌ ' + (error.message || '向量化失败')
    }
  } finally {
    loading.value.single = false
  }
}

// 删除向量
const deleteSingle = async () => {
  if (!singleArticleId.value) return
  if (!confirm(`确定要删除文章 ${singleArticleId.value} 的向量吗？`)) return
  
  loading.value.delete = true
  singleResult.value = null
  try {
    const response = await axios.delete(`/api/admin/embedding/article/${singleArticleId.value}`, getAuthConfig())
    singleResult.value = {
      success: response.data.code === 1,
      message: response.data.code === 1 ? '✅ 删除成功' : ('❌ ' + (response.data.msg || '删除失败'))
    }
  } catch (error) {
    singleResult.value = {
      success: false,
      message: '❌ ' + (error.message || '删除失败')
    }
  } finally {
    loading.value.delete = false
  }
}

// 语义检索
const searchSimilar = async () => {
  if (!searchQuery.value) return
  
  loading.value.search = true
  searchResults.value = []
  searchPerformed.value = true
  try {
    const response = await axios.get('/api/admin/embedding/search', {
      ...getAuthConfig(),
      params: {
        query: searchQuery.value,
        topK: searchTopK.value
      }
    })
    if (response.data.code === 1) {
      searchResults.value = response.data.data || []
    } else {
      alert('检索失败: ' + (response.data.msg || '未知错误'))
    }
  } catch (error) {
    alert('检索失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value.search = false
  }
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style scoped>
.embedding-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.section-header h2 {
  font-size: 20px;
  color: #2c3e50;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 14px;
  color: #64748b;
}

/* 操作卡片 */
.action-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.action-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.card-header h3 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 12px;
}

.card-body p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.status-text {
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 6px;
  background: #f1f5f9;
}

.status-text.success {
  background: #d1fae5;
  color: #065f46;
}

.status-text.error {
  background: #fee2e2;
  color: #991b1b;
}

.result-text {
  margin-top: 12px;
  font-size: 13px;
}

.result-text.success {
  color: #059669;
}

.result-text.error {
  color: #dc2626;
}

/* 表单控件 */
.form-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  color: #2c3e50;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  outline: none;
  margin-bottom: 12px;
}

.form-input:focus {
  border-color: #2c3e50;
}

.btn-group {
  display: flex;
  gap: 8px;
}

.btn-primary {
  padding: 10px 16px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #34495e;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger {
  padding: 10px 16px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 搜索区域 */
.search-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.search-section h3 {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  margin-bottom: 0;
}

.form-select {
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  min-width: 100px;
}

/* 检索结果 */
.search-results h4 {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 16px;
}

.result-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.result-rank {
  background: #2c3e50;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.result-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  flex: 1;
}

.result-score {
  font-size: 13px;
  color: #059669;
  font-weight: 500;
}

.result-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.result-text-preview {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  background: white;
  padding: 10px;
  border-radius: 4px;
}

.empty-text {
  text-align: center;
  color: #94a3b8;
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .action-cards {
    grid-template-columns: 1fr;
  }
}
</style>

