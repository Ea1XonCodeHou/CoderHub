package com.eaxon.coderhubserver.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 手动配置 OpenAI 模型，兼容 Spring Boot 2.7.3
 */
@Configuration
@Slf4j
public class LangChainConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.timeout:60}")
    private Integer timeout;
    
    @Value("${langchain4j.open-ai.chat-model.log-requests:false}")
    private Boolean logRequests;
    
    @Value("${langchain4j.open-ai.chat-model.log-responses:false}")
    private Boolean logResponses;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("========================================");
        log.info("初始化 LangChain4j OpenAI ChatLanguageModel");
        log.info("Base URL: {}", baseUrl);
        log.info("Model Name: {}", modelName);
        log.info("Temperature: {}", temperature);
        log.info("Timeout: {}s", timeout);
        log.info("Log Requests: {}", logRequests);
        log.info("Log Responses: {}", logResponses);
        log.info("========================================");
        
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}

