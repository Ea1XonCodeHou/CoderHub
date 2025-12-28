/**
 * useLocalStorage - 本地存储 Composable
 * 
 * 功能：
 * 1. 响应式的 localStorage 封装
 * 2. 自动序列化/反序列化
 * 3. 支持默认值
 * 4. 支持过期时间
 * 
 * @author CoderHub
 */
import { ref, watch } from 'vue'

/**
 * useLocalStorage Composable
 * 
 * @param {string} key - localStorage 键名
 * @param {any} defaultValue - 默认值
 * @param {Object} options - 选项
 * @param {number} options.expireMs - 过期时间（毫秒）
 */
export function useLocalStorage(key, defaultValue = null, options = {}) {
  const { expireMs = null } = options

  /**
   * 从 localStorage 读取值
   */
  function readValue() {
    try {
      const item = localStorage.getItem(key)
      if (item === null) {
        return defaultValue
      }

      const parsed = JSON.parse(item)

      // 检查是否有过期时间
      if (parsed && typeof parsed === 'object' && parsed.__expireAt) {
        if (Date.now() > parsed.__expireAt) {
          // 已过期，删除并返回默认值
          localStorage.removeItem(key)
          return defaultValue
        }
        return parsed.value
      }

      return parsed
    } catch {
      return defaultValue
    }
  }

  /**
   * 写入 localStorage
   */
  function writeValue(value) {
    try {
      let toStore = value

      // 如果设置了过期时间，包装值
      if (expireMs) {
        toStore = {
          value,
          __expireAt: Date.now() + expireMs
        }
      }

      localStorage.setItem(key, JSON.stringify(toStore))
    } catch (error) {
      console.error(`写入 localStorage 失败 [${key}]:`, error)
    }
  }

  // 创建响应式引用
  const storedValue = ref(readValue())

  // 监听变化，自动同步到 localStorage
  watch(
    storedValue,
    (newValue) => {
      if (newValue === null || newValue === undefined) {
        localStorage.removeItem(key)
      } else {
        writeValue(newValue)
      }
    },
    { deep: true }
  )

  /**
   * 删除存储的值
   */
  function remove() {
    localStorage.removeItem(key)
    storedValue.value = defaultValue
  }

  /**
   * 刷新值（从 localStorage 重新读取）
   */
  function refresh() {
    storedValue.value = readValue()
  }

  return {
    value: storedValue,
    remove,
    refresh
  }
}

/**
 * 对话历史存储
 */
export function useChatHistory() {
  const STORAGE_KEY = 'coderhub_chat_history'
  const MAX_CONVERSATIONS = 50

  const { value: conversations, remove: clearAll } = useLocalStorage(STORAGE_KEY, [])

  /**
   * 保存对话
   */
  function saveConversation(conversation) {
    const now = new Date()
    const newConversation = {
      id: conversation.id || `chat_${Date.now()}`,
      title: conversation.title || generateTitle(conversation.messages),
      messages: conversation.messages || [],
      model: conversation.model || 'qwen-plus',
      createdAt: conversation.createdAt || now.toISOString(),
      updatedAt: now.toISOString()
    }

    // 查找是否存在
    const index = conversations.value.findIndex(c => c.id === newConversation.id)
    
    if (index >= 0) {
      // 更新现有对话
      conversations.value[index] = newConversation
    } else {
      // 添加新对话到开头
      conversations.value.unshift(newConversation)
    }

    // 限制数量
    if (conversations.value.length > MAX_CONVERSATIONS) {
      conversations.value = conversations.value.slice(0, MAX_CONVERSATIONS)
    }
  }

  /**
   * 获取对话
   */
  function getConversation(id) {
    return conversations.value.find(c => c.id === id)
  }

  /**
   * 删除对话
   */
  function deleteConversation(id) {
    const index = conversations.value.findIndex(c => c.id === id)
    if (index >= 0) {
      conversations.value.splice(index, 1)
    }
  }

  /**
   * 生成对话标题
   */
  function generateTitle(messages) {
    if (!messages || messages.length === 0) {
      return '新对话'
    }
    
    const firstUserMessage = messages.find(m => m.role === 'user')
    if (firstUserMessage) {
      const content = firstUserMessage.content
      return content.length > 30 ? content.substring(0, 30) + '...' : content
    }
    
    return '新对话'
  }

  /**
   * 格式化时间
   */
  function formatTime(isoString) {
    const date = new Date(isoString)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 7) return `${diffDays}天前`
    
    return date.toLocaleDateString('zh-CN')
  }

  return {
    conversations,
    saveConversation,
    getConversation,
    deleteConversation,
    clearAll,
    formatTime
  }
}

/**
 * 用户设置存储
 */
export function useUserSettings() {
  const STORAGE_KEY = 'coderhub_user_settings'

  const defaultSettings = {
    theme: 'light', // 'light' | 'dark' | 'system'
    model: 'qwen-plus',
    temperature: 0.7,
    sendOnEnter: true,
    showTimestamp: true,
    fontSize: 'medium' // 'small' | 'medium' | 'large'
  }

  const { value: settings } = useLocalStorage(STORAGE_KEY, defaultSettings)

  /**
   * 更新设置
   */
  function updateSetting(key, value) {
    settings.value = {
      ...settings.value,
      [key]: value
    }
  }

  /**
   * 重置设置
   */
  function resetSettings() {
    settings.value = { ...defaultSettings }
  }

  return {
    settings,
    updateSetting,
    resetSettings
  }
}

export default useLocalStorage

