package com.eaxon.coderhubserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubpojo.DTO.ChatRequestDTO;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent.RecommendItem;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent.ToolCall;
import com.eaxon.coderhubpojo.entity.AIMessage;
import com.eaxon.coderhubserver.agent.CoderHubTools;
import com.eaxon.coderhubserver.mapper.AIMessageMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * AI 对话服务层
 * 使用 LangChain4j 实现流式对话，支持工具调用
 * 
 * 核心功能：
 * 1. 流式响应 - 使用 Reactor Flux 实现真正的响应式流
 * 2. 工具调用 - 支持搜索教程、文章等工具
 * 3. 多模型支持 - 可动态切换不同的AI模型
 * 4. 上下文管理 - 支持对话历史
 * 
 * @author CoderHub
 */
@Service
@Slf4j
public class AIService {

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.streaming-chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.streaming-chat-model.model-name:qwen-plus}")
    private String defaultModelName;

    @Autowired
    private CoderHubTools coderHubTools;

    @Autowired
    private AIMessageMapper messageMapper;

    /** 上下文窗口最大消息数 */
    private static final int MAX_CONTEXT_MESSAGES = 20;

    /**
     * 流式模型缓存
     */
    private final Map<String, OpenAiStreamingChatModel> streamingModelCache = new ConcurrentHashMap<>();

    /**
     * 同步模型缓存（用于工具调用判断）
     */
    private final Map<String, OpenAiChatModel> syncModelCache = new ConcurrentHashMap<>();

    /**
     * 工具调用Agent接口
     */
    private CoderHubAgent coderHubAgent;

    /**
     * 带工具的系统提示词
     */
    private static final String SYSTEM_PROMPT_WITH_TOOLS = """
            你是 CoderHub AI 助手，一个专业的编程技术顾问。你可以帮助用户学习编程技术，并推荐平台上的相关教程和文章。
            
            你具有以下特点：
            1. 专业知识：精通各种编程语言、框架和最佳实践
            2. 代码能力：能够编写清晰、高效、可维护的代码
            3. 资源推荐：可以搜索和推荐 CoderHub 平台上的教程和文章
            4. 沟通技巧：用清晰简洁的语言解释复杂概念
            
            当用户询问想要学习某个技术时，你应该：
            1. 使用工具搜索相关的教程和文章
            2. 基于搜索结果给出推荐
            3. 同时提供一些学习建议
            
            在回答时请注意：
            - 提供准确、实用的技术建议
            - 代码示例要完整且可运行
            - 适当使用 Markdown 格式组织回答
            - 对于代码块，请标注编程语言以便语法高亮
            - 当推荐资源时，请告知用户这些都来自 CoderHub 平台
            """;

    /**
     * Agent接口定义
     */
    public interface CoderHubAgent {
        String chat(String message);
    }

    @PostConstruct
    public void init() {
        log.info("初始化 AI 服务，默认模型: {}, baseUrl: {}", defaultModelName, baseUrl);
        
        // 创建同步模型用于工具调用
        OpenAiChatModel syncModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(defaultModelName)
                .temperature(0.7)
                .maxTokens(4096)
                .logRequests(false)
                .logResponses(false)
                .build();
        
        // 使用AiServices构建Agent
        coderHubAgent = AiServices.builder(CoderHubAgent.class)
                .chatLanguageModel(syncModel)
                .tools(coderHubTools)
                .build();
        
        // 预热流式模型
        getOrCreateStreamingModel(defaultModelName, 0.7, 4096);
        
        log.info("AI 服务初始化完成，已加载工具: searchTutorials, searchArticles, getHotContent, getHotTags");
    }

    @PreDestroy
    public void destroy() {
        log.info("清理 AI 服务资源");
        streamingModelCache.clear();
        syncModelCache.clear();
    }

    /**
     * 流式对话 - 核心方法（支持工具调用）
     */
    public Flux<ChatStreamEvent> streamChat(ChatRequestDTO request) {
        String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : java.util.UUID.randomUUID().toString();
        String model = request.getModel() != null ? request.getModel() : defaultModelName;
        double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
        int maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;
        
        log.info("开始流式对话 - sessionId: {}, model: {}", sessionId, model);
        String userMessage = request.getMessage();

        return Flux.create(sink -> {
            try {
                // 1. 发送思考中事件
                sink.next(ChatStreamEvent.thinking(sessionId, model));
                
                // 2. 检测是否需要工具调用（基于关键词）
                boolean needsToolCall = detectToolCallIntent(userMessage);
                List<RecommendItem> recommendations = new ArrayList<>();
                String toolResult = null;

                if (needsToolCall) {
                    log.info("检测到工具调用意图，开始执行工具调用");
                    
                    // 发送工具调用状态
                    ToolCall tutorialToolCall = ToolCall.builder()
                            .toolName("searchTutorials")
                            .displayName("搜索教程")
                            .icon("📚")
                            .parameters("关键词: " + extractKeyword(userMessage))
                            .status("calling")
                            .build();
                    sink.next(ChatStreamEvent.toolCalling(sessionId, tutorialToolCall));

                    try {
                        // 使用Agent执行工具调用
                        toolResult = coderHubAgent.chat(userMessage);
                        
                        // 提取关键词获取推荐列表
                        String keyword = extractKeyword(userMessage);
                        recommendations = coderHubTools.searchAndGetRecommendations(keyword, 3, 3);

                        // 发送工具调用完成状态
                        tutorialToolCall.setStatus("success");
                        tutorialToolCall.setResultCount(recommendations.size());
                        sink.next(ChatStreamEvent.toolResult(sessionId, tutorialToolCall, recommendations));
                        
                        log.info("工具调用完成，获取到 {} 个推荐结果", recommendations.size());
                        
                    } catch (Exception e) {
                        log.error("工具调用失败: {}", e.getMessage());
                        tutorialToolCall.setStatus("failed");
                        sink.next(ChatStreamEvent.toolResult(sessionId, tutorialToolCall, null));
                    }
                }

                // 3. 获取流式模型
                OpenAiStreamingChatModel streamingModel = getOrCreateStreamingModel(model, temperature, maxTokens);
                
                // 4. 构建消息列表
                List<ChatMessage> messages = buildMessages(request, toolResult);
                
                // 5. 执行流式生成
                AtomicInteger tokenCount = new AtomicInteger(0);
                StringBuilder fullResponse = new StringBuilder();
                List<RecommendItem> finalRecommendations = recommendations;
                
                streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    
                    @Override
                    public void onNext(String token) {
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            tokenCount.incrementAndGet();
                            sink.next(ChatStreamEvent.message(token, sessionId));
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        log.info("流式响应完成 - sessionId: {}, 总字符数: {}", 
                                sessionId, fullResponse.length());
                        
                        ChatStreamEvent.TokenUsage usage = ChatStreamEvent.TokenUsage.builder()
                                .outputTokens(tokenCount.get())
                                .build();
                        
                        // 如果有推荐内容，附带在完成事件中
                        if (!finalRecommendations.isEmpty()) {
                            sink.next(ChatStreamEvent.doneWithRecommendations(sessionId, usage, finalRecommendations));
                        } else {
                        sink.next(ChatStreamEvent.done(sessionId, usage));
                        }
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式响应出错 - sessionId: {}, error: {}", sessionId, error.getMessage());
                        sink.next(ChatStreamEvent.error(parseErrorMessage(error), sessionId));
                        sink.complete();
                    }
                });
                
            } catch (Exception e) {
                log.error("创建流式对话失败: {}", e.getMessage(), e);
                sink.next(ChatStreamEvent.error("服务初始化失败: " + e.getMessage(), sessionId));
                sink.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 检测是否需要工具调用
     */
    private boolean detectToolCallIntent(String message) {
        if (message == null) return false;
        
        String lowerMessage = message.toLowerCase();
        
        // 学习意图关键词
        String[] learnKeywords = {
            "想学", "学习", "入门", "教程", "推荐", "怎么学", "如何学", 
            "有什么", "教我", "帮我找", "搜索", "查找", "了解", "掌握",
            "课程", "资源", "资料", "视频", "文章", "博客"
        };
        
        for (String keyword : learnKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        // 技术关键词检测
        String[] techKeywords = {
            "java", "spring", "vue", "react", "python", "redis", "mysql",
            "docker", "kubernetes", "微服务", "分布式", "算法", "数据结构"
        };
        
        for (String tech : techKeywords) {
            if (lowerMessage.contains(tech)) {
                // 如果包含技术关键词，再检查是否有疑问或请求意图
                if (lowerMessage.contains("?") || lowerMessage.contains("？") ||
                    lowerMessage.contains("吗") || lowerMessage.contains("呢") ||
                    lowerMessage.contains("有") || lowerMessage.contains("推荐")) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 从消息中提取搜索关键词
     */
    private String extractKeyword(String message) {
        if (message == null) return "";
        
        // 移除常见的意图词，保留技术关键词
        String[] removePatterns = {
            "我想学习?", "想学", "想了解", "帮我找", "帮我搜索", 
            "有什么", "有没有", "推荐一些", "推荐", "教程", "课程",
            "如何学", "怎么学", "入门", "请问", "请", "吗", "呢", "？", "?"
        };
        
        String result = message;
        for (String pattern : removePatterns) {
            result = result.replace(pattern, " ");
        }
        
        // 清理并返回
        return result.trim().replaceAll("\\s+", " ");
    }

    /**
     * 获取或创建流式模型
     */
    private OpenAiStreamingChatModel getOrCreateStreamingModel(String modelName, double temperature, int maxTokens) {
        String cacheKey = modelName + "_" + temperature + "_" + maxTokens;
        
        return streamingModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建新的流式模型实例: {}", cacheKey);
            
            return OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .logRequests(false)
                    .logResponses(false)
                    .build();
        });
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(ChatRequestDTO request, String toolResult) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 1. 添加系统提示词
        String systemPrompt = request.getSystemPrompt() != null ? 
                request.getSystemPrompt() : SYSTEM_PROMPT_WITH_TOOLS;
        messages.add(SystemMessage.from(systemPrompt));
        
        // 2. 从数据库加载历史对话（如果有 conversationId）
        String conversationId = request.getConversationId();
        if (conversationId != null && !conversationId.isEmpty()) {
            List<AIMessage> dbMessages = messageMapper.getRecentMessages(conversationId, MAX_CONTEXT_MESSAGES);
            for (AIMessage dbMsg : dbMessages) {
                if ("user".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(UserMessage.from(dbMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(dbMsg.getRole())) {
                    messages.add(AiMessage.from(dbMsg.getContent()));
                }
            }
            log.debug("从数据库加载了 {} 条历史消息", dbMessages.size());
        } 
        // 兼容旧的 history 参数
        else if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (ChatRequestDTO.ChatMessage historyMsg : request.getHistory()) {
                if ("user".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(UserMessage.from(historyMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(AiMessage.from(historyMsg.getContent()));
                }
            }
        }
        
        // 3. 如果有工具调用结果，添加为上下文
        if (toolResult != null && !toolResult.isEmpty()) {
            String contextMessage = "【平台资源检索结果】\n" + toolResult + 
                    "\n\n请基于以上检索结果，结合你的专业知识，为用户提供学习建议和推荐。";
            messages.add(SystemMessage.from(contextMessage));
        }
        
        // 4. 添加当前用户消息
        messages.add(UserMessage.from(request.getMessage()));
        
        log.debug("构建消息列表完成，共 {} 条消息", messages.size());
        return messages;
    }

    /**
     * 解析错误消息
     */
    private String parseErrorMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null) {
            return "未知错误";
        }
        
        if (message.contains("rate limit")) {
            return "请求频率过高，请稍后重试";
        }
        if (message.contains("timeout")) {
            return "请求超时，请稍后重试";
        }
        if (message.contains("unauthorized") || message.contains("401")) {
            return "API 认证失败，请检查配置";
        }
        if (message.contains("quota")) {
            return "API 配额已用完";
        }
        
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        
        return message;
    }
}
