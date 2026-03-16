package com.eaxon.coderhubserver.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.eaxon.coderhubpojo.VO.ChatMsgVO;
import com.eaxon.coderhubserver.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 消息处理器
 *
 * 消息协议（JSON）：
 *   客户端发送：{ "type": "SEND", "receiverId": "...", "content": "..." }
 *   服务端推送：{ "type": "MESSAGE", ...ChatMsgVO字段... }
 *   服务端ACK： { "type": "ACK", "messageId": "...", "conversationId": "..." }
 */
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** userId → WebSocketSession（在线用户映射） */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ==================== 连接生命周期 ====================

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket 连接建立 userId={}, 当前在线: {}", userId, SESSIONS.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("WebSocket 连接关闭 userId={}, 当前在线: {}", userId, SESSIONS.size());
        }
    }

    // ==================== 消息处理 ====================

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String senderId = getUserId(session);
        if (senderId == null) return;

        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.path("type").asText("");

            if ("SEND".equalsIgnoreCase(type)) {
                String receiverId = json.path("receiverId").asText(null);
                String content = json.path("content").asText("").trim();

                if (receiverId == null || content.isEmpty()) {
                    log.warn("收到无效消息，senderId={}", senderId);
                    return;
                }

                // 保存消息并获取 VO
                ChatMsgVO msgVO = chatService.saveAndPushMessage(senderId, receiverId, content);

                // 给发送方 ACK
                sendToSession(session, wrapType("ACK", msgVO));

                // 推送给接收方（如果在线）
                WebSocketSession receiverSession = SESSIONS.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    sendToSession(receiverSession, wrapType("MESSAGE", msgVO));
                    log.info("消息已实时推送给接收方 receiverId={}", receiverId);
                } else {
                    log.info("接收方不在线，消息已持久化，receiverId={}", receiverId);
                }
            }

        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败，senderId={}: {}", senderId, e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误 userId={}: {}", getUserId(session), exception.getMessage());
    }

    /**
     * 查询指定用户是否在线（有活跃的 WebSocket 连接）
     */
    public static boolean isUserOnline(String userId) {
        if (userId == null) return false;
        WebSocketSession session = SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    // ==================== 工具方法 ====================

    private String getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    private void sendToSession(WebSocketSession session, Object payload) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("WebSocket 发送失败: {}", e.getMessage());
        }
    }

    private Map<String, Object> wrapType(String type, Object data) {
        Map<String, Object> wrapper = new java.util.LinkedHashMap<>();
        wrapper.put("type", type);
        // 将 data 展开到同层级
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            JsonNode node = objectMapper.readTree(dataJson);
            node.fields().forEachRemaining(entry -> wrapper.put(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            wrapper.put("data", data);
        }
        return wrapper;
    }
}
