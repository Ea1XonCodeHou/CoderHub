package com.eaxon.coderhubserver.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChromaDB 向量数据库配置
 * 
 * 用于文章语义检索（RAG）
 */
@Configuration
@Slf4j
public class ChromaDBConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${langchain4j.chroma.base-url:http://127.0.0.1:8000}")
    private String chromaBaseUrl;

    @Value("${langchain4j.chroma.collection-name:coderhub_articles}")
    private String collectionName;

    /**
     * ChromaDB 向量存储 Bean
     */
    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        log.info("初始化ChromaDB向量存储: baseUrl={}, collection={}", chromaBaseUrl, collectionName);
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(collectionName)
                .build();
    }

    /**
     * Embedding 模型 Bean（阿里百炼 text-embedding-v3）
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("初始化Embedding模型: text-embedding-v3");
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName("text-embedding-v3")
                .build();
    }
}

