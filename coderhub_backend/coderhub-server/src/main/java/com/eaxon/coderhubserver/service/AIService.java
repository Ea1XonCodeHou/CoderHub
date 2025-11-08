package com.eaxon.coderhubserver.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * AI 服务层 - 提供流式对话功能
 */
@Service
@Slf4j
public class AIService {

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.streaming-chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.streaming-chat-model.model-name}")
    private String modelName;

    private OpenAiStreamingChatModel streamingChatModel;

    @PostConstruct
    public void init() {
        log.info("初始化 AI 流式模型，baseUrl: {}, modelName: {}", baseUrl, modelName);
        streamingChatModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .maxTokens(2000)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 流式对话 - 返回 Flux 流
     *
     * @param userMessage 用户消息
     * @return Flux<String> 流式响应
     */
    public Flux<String> streamChat(String userMessage) {
        log.info("接收到用户消息: {}", userMessage);

        // 创建一个 Sink 用于发送流式数据
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 使用 LangChain4j 的流式响应处理器
        streamingChatModel.generate(userMessage, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                // 每次接收到 token 就推送到 sink
                log.debug("收到 token: {}", token);
                sink.tryEmitNext(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                log.info("流式响应完成");
                sink.tryEmitComplete();
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式响应出错", error);
                sink.tryEmitError(error);
            }
        });

        return sink.asFlux();
    }

    /**
     * 流式对话 - 返回 SSE 格式
     *
     * @param userMessage 用户消息
     * @return Flux<String> SSE 格式的流式响应
     */
    public Flux<String> streamChatSSE(String userMessage) {
        return streamChat(userMessage)
                .map(token -> {
                    // 确保正确处理中文字符
                    // Spring 的 SSE 会自动添加 "data:" 前缀，这里直接返回内容即可
                    log.debug("发送 token (length: {}): {}", token.length(), token);
                    return token;
                })
                .doOnError(e -> log.error("SSE 流式输出错误", e))
                .doFinally(signal -> log.info("SSE 流式输出结束，信号: {}", signal));
    }
}
