<template>
  <div class="smart-action-panel" :class="{ 'dark-mode': isDarkMode }">
    <div class="panel-header">
      <div class="header-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path>
        </svg>
      </div>
      <div class="header-content">
        <h4>智能助手建议</h4>
        <p v-if="recommendation.reason">{{ recommendation.reason }}</p>
      </div>
    </div>
    
    <div class="actions-list">
      <div 
        v-for="action in recommendation.actions" 
        :key="action.actionId"
        class="action-item"
        @click="$emit('execute-action', action)"
      >
        <div class="action-icon">{{ action.icon }}</div>
        <div class="action-content">
          <div class="action-label">{{ action.label }}</div>
          <div class="action-description">{{ action.description }}</div>
        </div>
        <div class="action-arrow">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"></polyline>
          </svg>
        </div>
      </div>
    </div>
    
    <div class="panel-footer">
      <button class="dismiss-btn" @click="$emit('dismiss')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
        暂不需要
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  recommendation: {
    type: Object,
    required: true
  },
  isDarkMode: {
    type: Boolean,
    default: false
  }
})

defineEmits(['execute-action', 'dismiss'])
</script>

<style scoped>
.smart-action-panel {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  margin: 16px 0;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.smart-action-panel.dark-mode {
  background: linear-gradient(135deg, #5568d3 0%, #6a3d8f 100%);
  box-shadow: 0 8px 32px rgba(85, 104, 211, 0.4);
}

/* 面板头部 */
.panel-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  color: white;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-icon svg {
  width: 28px;
  height: 28px;
  stroke: white;
}

.header-content h4 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: white;
}

.header-content p {
  font-size: 14px;
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
}

/* 操作列表 */
.actions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.action-item {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: white;
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.dark-mode .action-item {
  background: rgba(30, 30, 30, 0.9);
}

.dark-mode .action-item:hover {
  background: rgba(40, 40, 40, 0.95);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.action-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
}

.action-label {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.dark-mode .action-label {
  color: #e0e0e0;
}

.action-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.dark-mode .action-description {
  color: #a0a0a0;
}

.action-arrow {
  flex-shrink: 0;
}

.action-arrow svg {
  width: 20px;
  height: 20px;
  stroke: #999;
  transition: transform 0.2s ease;
}

.action-item:hover .action-arrow svg {
  stroke: #667eea;
  transform: translateX(4px);
}

.dark-mode .action-item:hover .action-arrow svg {
  stroke: #a5b4fc;
}

/* 面板底部 */
.panel-footer {
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.dismiss-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.dismiss-btn svg {
  width: 16px;
  height: 16px;
  stroke: white;
}

.dismiss-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 响应式 */
@media (max-width: 768px) {
  .smart-action-panel {
    padding: 20px;
  }
  
  .panel-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
  }
  
  .header-icon svg {
    width: 24px;
    height: 24px;
  }
  
  .action-icon {
    font-size: 28px;
    width: 40px;
    height: 40px;
  }
  
  .action-label {
    font-size: 15px;
  }
  
  .action-description {
    font-size: 12px;
  }
}
</style>
