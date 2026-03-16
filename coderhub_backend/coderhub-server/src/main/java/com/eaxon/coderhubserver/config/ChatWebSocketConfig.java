package com.eaxon.coderhubserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.eaxon.coderhubserver.websocket.ChatWebSocketHandler;
import com.eaxon.coderhubserver.websocket.ChatWebSocketHandshakeInterceptor;

/**
 * WebSocket 配置
 * 使用原生 WebSocket（不依赖 STOMP/SockJS 额外库）
 * 前端通过 ws(s)://host/ws/chat?token=xxx 连接
 */
@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private ChatWebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
