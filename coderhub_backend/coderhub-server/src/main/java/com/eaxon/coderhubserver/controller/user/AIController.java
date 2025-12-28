package com.eaxon.coderhubserver.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.ChatRequestDTO;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent;
import com.eaxon.coderhubserver.service.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI 智能对话控制器
 * 使用 WebFlux Flux + SSE 实现真正的流式输出
 * 
 * 升级到 Spring Boot 3.x，使用响应式编程模型
 * 
 * @author CoderHub
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@Tag(name = "AI智能对话接口", description = "提供流式AI对话、模型管理等功能")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AIController {

    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 流式对话接口（POST - 推荐）
     * 支持完整的请求参数：模型选择、温度、历史对话等
     * 
     * 返回 SSE 流，前端使用 EventSource 或 fetch 接收
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话接口（POST）", description = "发送消息并获取AI流式响应，支持conversationId持久化")
    public Flux<ServerSentEvent<String>> streamChatPost(@RequestBody ChatRequestDTO request) {
        log.info("收到流式对话请求 [POST]，消息: {}, 模型: {}, conversationId: {}", 
                request.getMessage(), request.getModel(), request.getConversationId());
        
        String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : java.util.UUID.randomUUID().toString();
        
        return aiService.streamChat(request)
                .map(event -> createSSE(event, sessionId))
                .onErrorResume(e -> {
                    log.error("流式对话出错: {}", e.getMessage());
                    ChatStreamEvent errorEvent = ChatStreamEvent.error(e.getMessage(), sessionId);
                    return Flux.just(createSSE(errorEvent, sessionId));
                });
    }

    /**
     * 流式对话接口（GET - 兼容 EventSource）
     * EventSource 只支持 GET 请求，提供此接口作为兼容方案
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话接口（GET）", description = "兼容 EventSource 的流式对话接口")
    public Flux<ServerSentEvent<String>> streamChatGet(
            @RequestParam("message") String message,
            @RequestParam(value = "model", required = false, defaultValue = "qwen-plus") String model,
            @RequestParam(value = "temperature", required = false, defaultValue = "0.7") Double temperature) {
        
        log.info("收到流式对话请求 [GET]，消息: {}, 模型: {}", message, model);
        
        String sessionId = java.util.UUID.randomUUID().toString();
        
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message(message)
                .model(model)
                .temperature(temperature)
                .sessionId(sessionId)
                .build();
        
        return aiService.streamChat(request)
                .map(event -> createSSE(event, sessionId))
                .onErrorResume(e -> {
                    log.error("流式对话出错: {}", e.getMessage());
                    ChatStreamEvent errorEvent = ChatStreamEvent.error(e.getMessage(), sessionId);
                    return Flux.just(createSSE(errorEvent, sessionId));
                });
    }

    /**
     * 创建 SSE 事件
     */
    private ServerSentEvent<String> createSSE(ChatStreamEvent event, String sessionId) {
        try {
            String data = objectMapper.writeValueAsString(event);
            return ServerSentEvent.<String>builder()
                    .id(sessionId)
                    .event(event.getType().name().toLowerCase())
                    .data(data)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("序列化SSE事件失败: {}", e.getMessage());
            return ServerSentEvent.<String>builder()
                    .id(sessionId)
                    .event("error")
                    .data("{\"error\":\"序列化失败\"}")
                    .build();
        }
    }

    /**
     * 获取可用的AI模型列表
     */
    @GetMapping("/models")
    @Operation(summary = "获取可用模型列表", description = "返回系统支持的所有AI模型")
    public Result<List<Map<String, Object>>> getAvailableModels() {
        log.info("获取可用模型列表");
        
        List<Map<String, Object>> models = List.of(
                Map.of(
                        "id", "qwen-plus",
                        "name", "通义千问 Plus",
                        "description", "最强大的通用模型，适合复杂任务",
                        "maxTokens", 8192,
                        "icon", "🌟"
                ),
                Map.of(
                        "id", "qwen-turbo",
                        "name", "通义千问 Turbo",
                        "description", "快速响应模型，适合日常对话",
                        "maxTokens", 8192,
                        "icon", "⚡"
                ),
                Map.of(
                        "id", "qwen-max",
                        "name", "通义千问 Max",
                        "description", "超大规模模型，适合专业分析",
                        "maxTokens", 32768,
                        "icon", "🚀"
                ),
                Map.of(
                        "id", "deepseek-chat",
                        "name", "DeepSeek Chat",
                        "description", "深度求索对话模型",
                        "maxTokens", 16384,
                        "icon", "🔮"
                )
        );
        
        return Result.success(models);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "AI服务健康检查", description = "检查AI服务是否正常运行")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "service", "AI Chat Service",
                "version", "2.0",
                "framework", "Spring Boot 3.x + WebFlux",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * SSE 连接测试接口
     * 用于前端测试 SSE 连接是否正常
     */
    @GetMapping(value = "/test/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE连接测试", description = "测试SSE流式连接")
    public Flux<ServerSentEvent<String>> testStream() {
        log.info("测试SSE连接");
        
        return Flux.interval(Duration.ofMillis(500))
                .take(10)
                .map(i -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(i))
                        .event("message")
                        .data("{\"content\":\"测试消息 " + (i + 1) + "\"}")
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("{\"type\":\"DONE\"}")
                                .build()
                ));
    }
}
