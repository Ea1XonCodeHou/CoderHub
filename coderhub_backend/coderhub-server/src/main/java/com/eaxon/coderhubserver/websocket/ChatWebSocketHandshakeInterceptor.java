package com.eaxon.coderhubserver.websocket;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.eaxon.coderhubcommon.constant.JwtClaimsConstant;
import com.eaxon.coderhubcommon.properties.JwtProperties;
import com.eaxon.coderhubcommon.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 握手拦截器
 * 从 URL 参数 ?token=xxx 中提取 JWT，验证后将 userId 存入 WebSocket session 属性
 */
@Component
@Slf4j
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 URL query string 提取 token: /ws/chat?token=xxx
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("WebSocket 握手被拒绝：缺少 token 参数");
            return false;
        }

        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }

        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 握手被拒绝：token 为空");
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            attributes.put("userId", userId);
            log.info("WebSocket 握手成功，userId={}", userId);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 握手被拒绝：JWT 无效 - {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // 握手后无需额外处理
    }
}
