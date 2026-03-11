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

import java.util.concurrent.atomic.AtomicReference;

/**
 * ChromaDB 向量数据库配置
 *
 * 使用 AtomicReference 包装 EmbeddingStore，清空 collection 后可刷新引用。
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

    private final AtomicReference<ChromaEmbeddingStore> storeRef = new AtomicReference<>();

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        log.info("初始化ChromaDB向量存储: baseUrl={}, collection={}", chromaBaseUrl, collectionName);
        ChromaEmbeddingStore store = ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(collectionName)
                .build();
        storeRef.set(store);
        return store;
    }

    /**
     * 重建 ChromaEmbeddingStore（删除旧 collection 后调用）
     * 返回新的 store 实例，同时更新 AtomicReference
     */
    public ChromaEmbeddingStore rebuildStore() {
        log.info("重建 ChromaEmbeddingStore: baseUrl={}, collection={}", chromaBaseUrl, collectionName);
        ChromaEmbeddingStore newStore = ChromaEmbeddingStore.builder()
                .baseUrl(chromaBaseUrl)
                .collectionName(collectionName)
                .build();
        storeRef.set(newStore);
        return newStore;
    }

    public AtomicReference<ChromaEmbeddingStore> getStoreRef() {
        return storeRef;
    }

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

