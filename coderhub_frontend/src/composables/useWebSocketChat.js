/**
 * useWebSocketChat — 原生 WebSocket 私信通讯 Composable
 *
 * 协议：
 *   发送: { type: "SEND", receiverId, content }
 *   收到: { type: "MESSAGE"|"ACK", ...ChatMsgVO }
 */
import { ref } from 'vue'

export function useWebSocketChat() {
  const connected = ref(false)
  const error = ref(null)
  let ws = null
  let onMessageCallback = null
  let reconnectTimer = null
  let manualClose = false

  function connect(token, onMessage) {
    onMessageCallback = onMessage
    manualClose = false
    _connect(token)
  }

  function _connect(token) {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws/chat?token=${encodeURIComponent(token)}`

    ws = new WebSocket(url)

    ws.onopen = () => {
      connected.value = true
      error.value = null
      if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
      console.log('[WS] 连接已建立')
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (onMessageCallback) onMessageCallback(data)
      } catch (e) {
        console.error('[WS] 消息解析失败', e)
      }
    }

    ws.onerror = (e) => {
      error.value = '连接出错'
      console.error('[WS] 错误', e)
    }

    ws.onclose = () => {
      connected.value = false
      if (!manualClose) {
        // 5 秒后自动重连
        reconnectTimer = setTimeout(() => _connect(token), 5000)
        console.log('[WS] 连接断开，5s 后重连...')
      }
    }
  }

  function sendMessage(receiverId, content) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      error.value = '连接未就绪，请稍后重试'
      return false
    }
    ws.send(JSON.stringify({ type: 'SEND', receiverId, content }))
    return true
  }

  function disconnect() {
    manualClose = true
    if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
    if (ws) { ws.close(); ws = null }
    connected.value = false
  }

  return { connected, error, connect, sendMessage, disconnect }
}
