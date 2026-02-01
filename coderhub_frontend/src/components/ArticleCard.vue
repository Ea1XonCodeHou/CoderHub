<template>
  <div class="article-card" :class="{ 'dark-mode': isDarkMode }">
    <div class="card-cover" v-if="article.coverUrl">
      <img :src="article.coverUrl" :alt="article.title" @error="handleImageError">
      <div class="cover-overlay">
        <span class="category-tag">{{ article.categoryName }}</span>
      </div>
    </div>
    
    <div class="card-content">
      <h3 class="card-title">{{ article.title }}</h3>
      
      <p class="card-summary" v-if="article.summary">
        {{ truncateSummary(article.summary) }}
      </p>
      
      <div class="card-meta">
        <div class="meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          <span>{{ article.authorName || '匿名' }}</span>
        </div>
        
        <div class="meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
          <span>{{ formatCount(article.viewCount) }}</span>
        </div>
        
        <div class="meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
          </svg>
          <span>{{ formatCount(article.likeCount) }}</span>
        </div>
      </div>
      
      <div class="card-actions">
        <button class="btn-secondary" @click="$emit('view-article', article.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
            <polyline points="15 3 21 3 21 9"></polyline>
            <line x1="10" y1="14" x2="21" y2="3"></line>
          </svg>
          查看详情
        </button>
        <button class="btn-primary" @click="$emit('ask-about-article', article)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
          继续问
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>

const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  isDarkMode: {
    type: Boolean,
    default: false
  }
})

defineEmits(['view-article', 'ask-about-article'])

// 处理图片加载失败
function handleImageError(e) {
  e.target.src = '/placeholder-article.png'
}

// 截断摘要
function truncateSummary(summary) {
  if (!summary) return ''
  return summary.length > 120 ? summary.substring(0, 120) + '...' : summary
}

// 格式化数量
function formatCount(count) {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}
</script>

<style scoped>
.article-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.article-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.article-card.dark-mode {
  background: #1e1e1e;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.article-card.dark-mode:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

/* 封面图 */
.card-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), transparent);
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 16px;
}

.category-tag {
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.dark-mode .category-tag {
  background: rgba(102, 126, 234, 0.2);
  color: #a5b4fc;
  border: 1px solid rgba(165, 180, 252, 0.3);
}

/* 卡片内容 */
.card-content {
  padding: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .card-title {
  color: #e0e0e0;
}

.card-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .card-summary {
  color: #a0a0a0;
}

/* 元数据 */
.card-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.dark-mode .card-meta {
  border-bottom-color: #333;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
}

.meta-item svg {
  width: 16px;
  height: 16px;
  stroke: #888;
}

.dark-mode .meta-item {
  color: #999;
}

.dark-mode .meta-item svg {
  stroke: #999;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 12px;
}

.card-actions button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.card-actions button svg {
  width: 18px;
  height: 18px;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.btn-secondary:hover {
  background: #e8e8e8;
  color: #333;
}

.dark-mode .btn-secondary {
  background: #2a2a2a;
  color: #a0a0a0;
}

.dark-mode .btn-secondary:hover {
  background: #333;
  color: #e0e0e0;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.dark-mode .btn-primary {
  background: linear-gradient(135deg, #5568d3 0%, #6a3d8f 100%);
}

/* 响应式 */
@media (max-width: 768px) {
  .card-cover {
    height: 160px;
  }
  
  .card-title {
    font-size: 16px;
  }
  
  .card-actions {
    flex-direction: column;
  }
  
  .card-actions button {
    width: 100%;
  }
}
</style>
