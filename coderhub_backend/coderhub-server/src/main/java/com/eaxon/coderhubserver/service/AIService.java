package com.eaxon.coderhubserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubpojo.DTO.ChatRequestDTO;
import com.eaxon.coderhubpojo.DTO.ChatStreamEvent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * AI 对话服务层
 * 使用 LangChain4j 实现流式对话，支持多模型、温度参数、历史对话
 * 
 * 核心功能：
 * 1. 流式响应 - 使用 Reactor Flux 实现真正的响应式流
 * 2. 多模型支持 - 可动态切换不同的AI模型
 * 3. 上下文管理 - 支持对话历史
 * 4. 参数配置 - 支持温度、最大token等参数
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

    /**
     * 模型缓存，避免重复创建
     */
    private final Map<String, OpenAiStreamingChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 默认系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是 CoderHub AI 助手，一个专业的编程技术顾问。你具有以下特点：
            
            1. 专业知识：精通各种编程语言、框架和最佳实践
            2. 代码能力：能够编写清晰、高效、可维护的代码
            3. 问题解决：善于分析和解决技术难题
            4. 沟通技巧：用清晰简洁的语言解释复杂概念
            
            在回答时请注意：
            - 提供准确、实用的技术建议
            - 代码示例要完整且可运行
            - 适当使用 Markdown 格式组织回答
            - 对于代码块，请标注编程语言以便语法高亮
            """;

    @PostConstruct
    public void init() {
        log.info("初始化 AI 服务，默认模型: {}, baseUrl: {}", defaultModelName, baseUrl);
        // 预热默认模型
        getOrCreateModel(defaultModelName, 0.7, 4096);
        log.info("AI 服务初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("清理 AI 服务资源");
        modelCache.clear();
    }

    /**
     * 流式对话 - 核心方法
     * 返回 Flux<ChatStreamEvent> 实现真正的响应式流
     *
     * @param request 对话请求
     * @return 流式事件
     */
    public Flux<ChatStreamEvent> streamChat(ChatRequestDTO request) {
        String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : java.util.UUID.randomUUID().toString();
        String model = request.getModel() != null ? request.getModel() : defaultModelName;
        double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
        int maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;
        
        log.info("开始流式对话 - sessionId: {}, model: {}, temperature: {}", 
                sessionId, model, temperature);

        return Flux.create(sink -> {
            try {
                // 1. 发送思考中事件
                sink.next(ChatStreamEvent.thinking(sessionId, model));
                
                // 2. 获取或创建模型
                OpenAiStreamingChatModel chatModel = getOrCreateModel(model, temperature, maxTokens);
                
                // 3. 构建消息列表
                List<ChatMessage> messages = buildMessages(request);
                
                // 4. 执行流式生成
                AtomicInteger tokenCount = new AtomicInteger(0);
                StringBuilder fullResponse = new StringBuilder();
                
                chatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    
                    @Override
                    public void onNext(String token) {
                        if (token != null && !token.isEmpty()) {
                            fullResponse.append(token);
                            tokenCount.incrementAndGet();
                            
                            // 发送内容片段事件
                            sink.next(ChatStreamEvent.message(token, sessionId));
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        log.info("流式响应完成 - sessionId: {}, 总字符数: {}, token估计: {}", 
                                sessionId, fullResponse.length(), tokenCount.get());
                        
                        // 发送完成事件
                        ChatStreamEvent.TokenUsage usage = ChatStreamEvent.TokenUsage.builder()
                                .outputTokens(tokenCount.get())
                                .build();
                        sink.next(ChatStreamEvent.done(sessionId, usage));
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式响应出错 - sessionId: {}, error: {}", sessionId, error.getMessage());
                        
                        String errorMessage = parseErrorMessage(error);
                        sink.next(ChatStreamEvent.error(errorMessage, sessionId));
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
     * 获取或创建模型实例
     */
    private OpenAiStreamingChatModel getOrCreateModel(String modelName, double temperature, int maxTokens) {
        String cacheKey = modelName + "_" + temperature + "_" + maxTokens;
        
        return modelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建新的模型实例: {}", cacheKey);
            
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
    private List<ChatMessage> buildMessages(ChatRequestDTO request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 1. 添加系统提示词
        String systemPrompt = request.getSystemPrompt() != null ? 
                request.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;
        messages.add(SystemMessage.from(systemPrompt));
        
        // 2. 添加历史对话
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (ChatRequestDTO.ChatMessage historyMsg : request.getHistory()) {
                if ("user".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(UserMessage.from(historyMsg.getContent()));
                } else if ("assistant".equalsIgnoreCase(historyMsg.getRole())) {
                    messages.add(AiMessage.from(historyMsg.getContent()));
                }
            }
        }
        
        // 3. 添加当前用户消息
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
        
        // 处理常见错误
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
        
        // 截断过长的错误消息
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        
        return message;
    }
}
