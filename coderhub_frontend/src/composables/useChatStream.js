/**
 * useChatStream - 处理 AI 对话流式响应的 Composable
 * 
 * 功能：
 * 1. 使用 fetch + ReadableStream 接收 SSE 流
 * 2. 解析事件类型（thinking, tool_calling, tool_result, message, done, error）
 * 3. 提供响应式的流式内容状态
 * 4. 支持取消/中断流式响应
 * 5. 支持工具调用状态和推荐内容
 * 
 * @author CoderHub
 */
import { ref, shallowRef } from 'vue'

/**
 * 后端 API 地址
 */
const API_BASE_URL = '/api'

/**
 * 解析 SSE 事件
 */
function parseSSEEvent(eventString) {
  const lines = eventString.split('\n')
  const event = {
    id: null,
    type: 'message',
    data: null
  }

  for (const line of lines) {
    if (line.startsWith('id:')) {
      event.id = line.slice(3).trim()
    } else if (line.startsWith('event:')) {
      event.type = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      const dataStr = line.slice(5).trim()
      try {
        event.data = JSON.parse(dataStr)
      } catch {
        event.data = dataStr
      }
    }
  }

  return event
}

/**
 * useChatStream Composable
 */
export function useChatStream() {
  // 状态
  const isStreaming = ref(false)
  const streamingContent = ref('')
  const currentError = ref(null)
  const isThinking = ref(false)
  const abortController = shallowRef(null)
  
  // 工具调用状态
  const isToolCalling = ref(false)
  const toolCallStatus = ref(null) // { toolName, displayName, icon, parameters, status, resultCount }
  const recommendations = ref([]) // 推荐内容列表

  /**
   * 发送消息并接收流式响应
   * 
   * @param {Object} options - 请求选项
   * @param {string} options.message - 用户消息
   * @param {string} options.model - AI 模型
   * @param {number} options.temperature - 温度参数
   * @param {Array} options.history - 对话历史
   * @param {Function} options.onToken - 收到 token 的回调
   * @param {Function} options.onComplete - 完成的回调
   * @param {Function} options.onError - 错误的回调
   * @param {Function} options.onToolCall - 工具调用的回调
   * @param {Function} options.onToolResult - 工具结果的回调
   * @returns {Promise<string>} - 完整的响应内容
   */
  async function sendMessage({
    message,
    model = 'qwen-plus',
    temperature = 0.7,
    history = [],
    conversationId = null, // 新增：持久化会话ID
    onToken = null,
    onComplete = null,
    onError = null,
    onToolCall = null,
    onToolResult = null
  }) {
    // 重置状态
    isStreaming.value = true
    streamingContent.value = ''
    currentError.value = null
    isThinking.value = true
    isToolCalling.value = false
    toolCallStatus.value = null
    recommendations.value = []

    // 创建 AbortController 用于取消请求
    abortController.value = new AbortController()

    try {
      const requestBody = {
        message,
        model,
        temperature,
        conversationId, // 持久化会话ID
        history: history.map(msg => ({
          role: msg.role,
          content: msg.content
        })),
        sessionId: generateSessionId()
      }

      // 获取 token
      const token = localStorage.getItem('token')
      
      const response = await fetch(`${API_BASE_URL}/ai/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
          'authentication': token || ''  // 添加 token 到请求头
        },
        body: JSON.stringify(requestBody),
        signal: abortController.value.signal
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          break
        }

        // 解码数据块
        buffer += decoder.decode(value, { stream: true })

        // 按事件分割处理
        const events = buffer.split('\n\n')
        buffer = events.pop() || '' // 保留不完整的事件

        for (const eventStr of events) {
          if (!eventStr.trim()) continue

          const event = parseSSEEvent(eventStr)
          
          switch (event.type) {
            case 'thinking':
              isThinking.value = true
              isToolCalling.value = false
              break
            
            // 工具调用中
            case 'tool_calling':
              isThinking.value = false
              isToolCalling.value = true
              if (event.data?.toolCall) {
                toolCallStatus.value = event.data.toolCall
              }
              if (onToolCall) {
                onToolCall(event.data?.toolCall)
              }
              break
            
            // 工具调用完成
            case 'tool_result':
              isToolCalling.value = false
              if (event.data?.toolCall) {
                toolCallStatus.value = event.data.toolCall
              }
              if (event.data?.recommendations) {
                recommendations.value = event.data.recommendations
              }
              if (onToolResult) {
                onToolResult(event.data?.toolCall, event.data?.recommendations)
              }
              break
              
            case 'message':
              isThinking.value = false
              isToolCalling.value = false
              if (event.data?.content) {
                streamingContent.value += event.data.content
                if (onToken) {
                  onToken(event.data.content)
                }
              }
              break
              
            case 'done':
              isStreaming.value = false
              isThinking.value = false
              isToolCalling.value = false
              // 如果完成事件带有推荐内容
              if (event.data?.recommendations) {
                recommendations.value = event.data.recommendations
              }
              if (onComplete) {
                onComplete(streamingContent.value, event.data?.tokenUsage, recommendations.value)
              }
              break
              
            case 'error':
              const errorMsg = event.data?.error || '未知错误'
              currentError.value = errorMsg
              isStreaming.value = false
              isThinking.value = false
              isToolCalling.value = false
              if (onError) {
                onError(errorMsg)
              }
              break
          }
        }
      }

      return streamingContent.value

    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('请求被取消')
        return streamingContent.value
      }

      const errorMessage = error.message || '网络请求失败'
      currentError.value = errorMessage
      isStreaming.value = false
      isThinking.value = false
      isToolCalling.value = false
      
      if (onError) {
        onError(errorMessage)
      }
      
      throw error
    }
  }

  /**
   * 发送RAG增强的消息（基于知识库检索）
   * 用于延伸问题场景
   * 
   * @param {Object} options - 请求选项
   * @param {string} options.message - 用户消息
   * @param {string} options.model - AI 模型
   * @param {Function} options.onToken - 收到 token 的回调
   * @param {Function} options.onComplete - 完成的回调
   * @param {Function} options.onError - 错误的回调
   * @param {Function} options.onToolCall - 工具调用（RAG检索）的回调
   * @param {Function} options.onToolResult - 工具结果的回调
   * @returns {Promise<string>} - 完整的响应内容
   */
  async function sendMessageWithRAG({
    message,
    model = 'qwen-plus',
    temperature = 0.7,
    conversationId = null,
    onToken = null,
    onComplete = null,
    onError = null,
    onToolCall = null,
    onToolResult = null
  }) {
    // 重置状态
    isStreaming.value = true
    streamingContent.value = ''
    currentError.value = null
    isThinking.value = true
    isToolCalling.value = false
    toolCallStatus.value = null
    recommendations.value = []

    // 创建 AbortController 用于取消请求
    abortController.value = new AbortController()

    try {
      const requestBody = {
        message,
        model,
        temperature,
        conversationId,
        sessionId: generateSessionId()
      }

      // 获取 token
      const token = localStorage.getItem('token')
      
      // 使用 RAG 接口
      const response = await fetch(`${API_BASE_URL}/ai/chat/rag`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
          'authentication': token || ''
        },
        body: JSON.stringify(requestBody),
        signal: abortController.value.signal
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          break
        }

        buffer += decoder.decode(value, { stream: true })

        const events = buffer.split('\n\n')
        buffer = events.pop() || ''

        for (const eventStr of events) {
          if (!eventStr.trim()) continue

          const event = parseSSEEvent(eventStr)
          
          switch (event.type) {
            case 'thinking':
              isThinking.value = true
              isToolCalling.value = false
              break
            
            case 'tool_calling':
              isThinking.value = false
              isToolCalling.value = true
              if (event.data?.toolCall) {
                toolCallStatus.value = event.data.toolCall
              }
              if (onToolCall) {
                onToolCall(event.data?.toolCall)
              }
              break
            
            case 'tool_result':
              isToolCalling.value = false
              if (event.data?.toolCall) {
                toolCallStatus.value = event.data.toolCall
              }
              if (event.data?.recommendations) {
                recommendations.value = event.data.recommendations
              }
              if (onToolResult) {
                onToolResult(event.data?.toolCall, event.data?.recommendations)
              }
              break
              
            case 'message':
              isThinking.value = false
              isToolCalling.value = false
              if (event.data?.content) {
                streamingContent.value += event.data.content
                if (onToken) {
                  onToken(event.data.content)
                }
              }
              break
              
            case 'done':
              isStreaming.value = false
              isThinking.value = false
              isToolCalling.value = false
              if (event.data?.recommendations) {
                recommendations.value = event.data.recommendations
              }
              if (onComplete) {
                onComplete(streamingContent.value, event.data?.tokenUsage, recommendations.value)
              }
              break
              
            case 'error':
              const errorMsg = event.data?.error || '未知错误'
              currentError.value = errorMsg
              isStreaming.value = false
              isThinking.value = false
              isToolCalling.value = false
              if (onError) {
                onError(errorMsg)
              }
              break
          }
        }
      }

      return streamingContent.value

    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('RAG请求被取消')
        return streamingContent.value
      }

      const errorMessage = error.message || '网络请求失败'
      currentError.value = errorMessage
      isStreaming.value = false
      isThinking.value = false
      isToolCalling.value = false
      
      if (onError) {
        onError(errorMessage)
      }
      
      throw error
    }
  }

  /**
   * 使用 GET 方式发送消息（兼容 EventSource）
   * 适用于简单场景，不支持历史对话
   */
  async function sendMessageGet({
    message,
    model = 'qwen-plus',
    temperature = 0.7,
    onToken = null,
    onComplete = null,
    onError = null
  }) {
    isStreaming.value = true
    streamingContent.value = ''
    currentError.value = null
    isThinking.value = true

    return new Promise((resolve, reject) => {
      const url = new URL(`${API_BASE_URL}/ai/chat/stream`)
      url.searchParams.set('message', message)
      url.searchParams.set('model', model)
      url.searchParams.set('temperature', temperature.toString())

      const eventSource = new EventSource(url.toString())

      eventSource.addEventListener('thinking', () => {
        isThinking.value = true
      })

      eventSource.addEventListener('message', (event) => {
        isThinking.value = false
        try {
          const data = JSON.parse(event.data)
          if (data.content) {
            streamingContent.value += data.content
            if (onToken) {
              onToken(data.content)
            }
          }
        } catch {
          // 如果不是 JSON，直接作为内容
          streamingContent.value += event.data
          if (onToken) {
            onToken(event.data)
          }
        }
      })

      eventSource.addEventListener('done', (event) => {
        isStreaming.value = false
        isThinking.value = false
        eventSource.close()
        
        let tokenUsage = null
        try {
          const data = JSON.parse(event.data)
          tokenUsage = data.tokenUsage
        } catch {}
        
        if (onComplete) {
          onComplete(streamingContent.value, tokenUsage)
        }
        resolve(streamingContent.value)
      })

      eventSource.addEventListener('error', (event) => {
        let errorMsg = '连接错误'
        if (event.data) {
          try {
            const data = JSON.parse(event.data)
            errorMsg = data.error || errorMsg
          } catch {}
        }
        
        currentError.value = errorMsg
        isStreaming.value = false
        isThinking.value = false
        eventSource.close()
        
        if (onError) {
          onError(errorMsg)
        }
        reject(new Error(errorMsg))
      })

      eventSource.onerror = () => {
        if (eventSource.readyState === EventSource.CLOSED) {
          // 正常关闭
          isStreaming.value = false
          isThinking.value = false
          if (onComplete) {
            onComplete(streamingContent.value)
          }
          resolve(streamingContent.value)
        }
      }

      // 保存 eventSource 引用用于取消
      abortController.value = { abort: () => eventSource.close() }
    })
  }

  /**
   * 取消当前流式响应
   */
  function cancelStream() {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    isStreaming.value = false
    isThinking.value = false
    isToolCalling.value = false
  }

  /**
   * 重置状态
   */
  function reset() {
    cancelStream()
    streamingContent.value = ''
    currentError.value = null
    toolCallStatus.value = null
    recommendations.value = []
  }

  /**
   * 生成会话ID
   */
  function generateSessionId() {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  return {
    // 状态
    isStreaming,
    streamingContent,
    currentError,
    isThinking,
    
    // 工具调用状态
    isToolCalling,
    toolCallStatus,
    recommendations,
    
    // 方法
    sendMessage,
    sendMessageWithRAG,
    sendMessageGet,
    cancelStream,
    reset
  }
}

export default useChatStream
