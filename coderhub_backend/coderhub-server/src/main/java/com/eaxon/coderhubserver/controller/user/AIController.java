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

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.ChatRequestDTO;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.AIService;
import com.eaxon.coderhubserver.service.RedisService;
import com.eaxon.coderhubpojo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
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
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

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

        // 检查当前用户是否为免费用户（user_level=0）并扣减额度
        try {
            String userId = BaseContext.getCurrentId();
            if (userId != null) {
                User user = userMapper.getUserById(userId);
                if (user != null && Integer.valueOf(0).equals(user.getUserLevel())) {
                    // 确保 Redis 有该用户的额度记录（新用户注册时可能还未初始化）
                    redisService.initAiQuota(userId, RedisConstant.AI_QUOTA_FREE);

                    Integer quota = redisService.getAiQuota(userId);
                    if (quota != null && quota <= 0) {
                        // 额度已耗尽，返回系统提示流，不消费真实 AI
                        log.info("用户 {} 免费额度已耗尽，返回提示消息", userId);
                        return buildQuotaExhaustedStream(sessionId);
                    }
                    // 扣减 1 次
                    redisService.decrementAiQuota(userId);
                    log.info("用户 {} 扣减 1 次 AI 额度，剩余: {}", userId,
                            redisService.getAiQuota(userId));
                }
            }
        } catch (Exception e) {
            log.warn("AI 额度检查失败（不阻断请求）: {}", e.getMessage());
        }

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
     * 构建额度耗尽时的流式提示响应（不调用真实 AI，直接返回系统提示文本流）
     */
    private Flux<ServerSentEvent<String>> buildQuotaExhaustedStream(String sessionId) {
        String notice = """
                🎉 感谢您使用 CoderHub AI 助手！

                您的免费提问额度已全部使用完毕。作为我们平台的免费用户，您已享有了一定数量的 AI 智能问答服务。

                如果您希望继续使用 AI 助手，有以下几种方式：

                📩 **联系管理员**：您可以通过平台消息或邮件联系我们的管理员，申请额度增加或了解更多使用权限。

                🌟 **支持我们**：如果您觉得 CoderHub 平台做得不错，欢迎分享给更多的朋友，您的支持是我们持续改进的最大动力！

                💡 **继续探索**：在等待额度恢复的过程中，您依然可以浏览平台上丰富的技术教程、阅读博客文章，以及参与社区互动。我们的内容团队持续更新优质内容，相信一定有您感兴趣的内容等待发现。

                感谢您的理解与支持，期待在 CoderHub 与您继续同行！ 🚀
                """;

        ChatStreamEvent startEvent = ChatStreamEvent.thinking(sessionId, "system");
        ChatStreamEvent msgEvent = ChatStreamEvent.message(notice, sessionId);
        ChatStreamEvent doneEvent = ChatStreamEvent.done(sessionId,
                ChatStreamEvent.TokenUsage.builder().outputTokens(notice.length()).build());

        return Flux.just(startEvent, msgEvent, doneEvent)
                .map(event -> createSSE(event, sessionId));
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
                )
        );

        return Result.success(models);
    }

    /**
     * 查询当前用户的 AI 提问剩余额度
     * 非免费用户（user_level>0）返回 -1 表示无限制
     */
    @GetMapping("/quota")
    @Operation(summary = "查询 AI 提问剩余额度", description = "返回当前登录用户的免费提问剩余次数，-1 表示无限制")
    public Result<Map<String, Object>> getQuota() {
        String userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("未登录");
        }

        User user = userMapper.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        if (!Integer.valueOf(0).equals(user.getUserLevel())) {
            // 高级用户：无限制
            data.put("quota", -1);
            data.put("unlimited", true);
        } else {
            // 初始化额度（若首次查询）
            redisService.initAiQuota(userId, RedisConstant.AI_QUOTA_FREE);
            Integer quota = redisService.getAiQuota(userId);
            data.put("quota", quota != null ? Math.max(0, quota) : 0);
            data.put("unlimited", false);
        }
        return Result.success(data);
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

    /**
     * RAG增强的流式对话接口
     * 
     * 用于延伸问题场景：
     * 1. 用户在文章详情页点击延伸问题
     * 2. 跳转到AI助手，自动发送问题
     * 3. 后端检索知识库，基于博客内容生成回答
     */
    @PostMapping(value = "/chat/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "RAG增强对话接口", description = "基于知识库检索的智能对话，用于延伸问题场景")
    public Flux<ServerSentEvent<String>> streamChatWithRAG(@RequestBody ChatRequestDTO request) {
        log.info("收到RAG增强对话请求，消息: {}, 模型: {}", 
                request.getMessage(), request.getModel());

        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : java.util.UUID.randomUUID().toString();

        // 同样进行额度检查
        try {
            String userId = BaseContext.getCurrentId();
            if (userId != null) {
                User user = userMapper.getUserById(userId);
                if (user != null && Integer.valueOf(0).equals(user.getUserLevel())) {
                    redisService.initAiQuota(userId, RedisConstant.AI_QUOTA_FREE);
                    Integer quota = redisService.getAiQuota(userId);
                    if (quota != null && quota <= 0) {
                        return buildQuotaExhaustedStream(sessionId);
                    }
                    redisService.decrementAiQuota(userId);
                }
            }
        } catch (Exception e) {
            log.warn("RAG 接口额度检查失败（不阻断请求）: {}", e.getMessage());
        }

        return aiService.streamChatWithRAG(request)
                .map(event -> createSSE(event, sessionId))
                .onErrorResume(e -> {
                    log.error("RAG对话出错: {}", e.getMessage());
                    ChatStreamEvent errorEvent = ChatStreamEvent.error(e.getMessage(), sessionId);
                    return Flux.just(createSSE(errorEvent, sessionId));
                });
    }

    /**
     * RAG对话接口（GET - 兼容 EventSource）
     * 用于从文章详情页跳转时，通过URL参数传递问题
     */
    @GetMapping(value = "/chat/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "RAG增强对话接口（GET）", description = "兼容URL参数的RAG对话接口")
    public Flux<ServerSentEvent<String>> streamChatWithRAGGet(
            @RequestParam("message") String message,
            @RequestParam(value = "model", required = false, defaultValue = "qwen-plus") String model) {
        
        log.info("收到RAG增强对话请求 [GET]，消息: {}", message);
        
        String sessionId = java.util.UUID.randomUUID().toString();
        
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message(message)
                .model(model)
                .sessionId(sessionId)
                .build();
        
        return aiService.streamChatWithRAG(request)
                .map(event -> createSSE(event, sessionId))
                .onErrorResume(e -> {
                    log.error("RAG对话出错: {}", e.getMessage());
                    ChatStreamEvent errorEvent = ChatStreamEvent.error(e.getMessage(), sessionId);
                    return Flux.just(createSSE(errorEvent, sessionId));
                });
    }
}
