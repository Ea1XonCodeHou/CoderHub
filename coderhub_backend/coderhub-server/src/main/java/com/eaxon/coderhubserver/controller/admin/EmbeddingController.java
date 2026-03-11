package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.config.ChromaDBConfig;
import com.eaxon.coderhubserver.service.AIService;
import com.eaxon.coderhubserver.service.ArticleEmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化管理接口（管理员）
 * 支持批量向量化、清空重建索引、语义检索测试
 */
@RestController
@RequestMapping("/admin/embedding")
@Slf4j
@Tag(name = "管理端-向量化管理接口")
public class EmbeddingController {

    @Autowired
    private ArticleEmbeddingService articleEmbeddingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ChromaDBConfig chromaDBConfig;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${langchain4j.chroma.base-url:http://127.0.0.1:8000}")
    private String chromaBaseUrl;

    @Value("${langchain4j.chroma.collection-name:coderhub_articles}")
    private String collectionName;

    /**
     * 获取当前有效的 EmbeddingStore（可能已被重建）
     */
    private EmbeddingStore<TextSegment> getStore() {
        return chromaDBConfig.getStoreRef().get();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量向量化所有已发布文章")
    public Result<Map<String, Object>> batchEmbedAllArticles() {
        log.info("开始批量向量化所有文章（Chunking模式）");

        long startTime = System.currentTimeMillis();
        int successCount = articleEmbeddingService.batchEmbedAllArticles();
        long endTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("timeMs", endTime - startTime);

        return Result.success(result);
    }

    @PostMapping("/article/{articleId}")
    @Operation(summary = "向量化单篇文章")
    public Result<String> embedSingleArticle(
            @PathVariable String articleId,
            @RequestParam(defaultValue = "false") boolean force) {
        log.info("向量化单篇文章: articleId={}, force={}", articleId, force);

        boolean success = articleEmbeddingService.embedArticle(articleId, force);
        if (success) {
            return Result.success("向量化成功");
        } else {
            return Result.error("向量化失败，请检查文章是否存在或已发布");
        }
    }

    @DeleteMapping("/article/{articleId}")
    @Operation(summary = "删除文章向量")
    public Result<String> removeArticleEmbedding(@PathVariable String articleId) {
        log.info("删除文章向量: articleId={}", articleId);
        articleEmbeddingService.removeArticleEmbedding(articleId);
        return Result.success("删除成功");
    }

    /**
     * 清空 ChromaDB collection 并重置 Redis 向量化记录，然后重建
     */
    @PostMapping("/rebuild")
    @Operation(summary = "清空向量库并重建索引", description = "清空ChromaDB + 清空Redis记录 + 重新批量向量化")
    public Result<Map<String, Object>> rebuildIndex() {
        log.info("开始重建向量索引...");
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();

        // 1. 清空 ChromaDB collection 并刷新 Bean 引用
        try {
            deleteChromaCollection();
            ChromaEmbeddingStore newStore = chromaDBConfig.rebuildStore();
            articleEmbeddingService.refreshEmbeddingStore(newStore);
            aiService.refreshEmbeddingStore(newStore);
            result.put("chromaCleared", true);
            log.info("ChromaDB collection 已清空并重建");
        } catch (Exception e) {
            log.error("清空 ChromaDB 失败: {}", e.getMessage(), e);
            result.put("chromaCleared", false);
            result.put("chromaError", e.getMessage());
            return Result.error("清空向量库失败: " + e.getMessage());
        }

        // 2. 清空 Redis 向量化记录
        articleEmbeddingService.clearEmbeddingRecords();
        result.put("redisCleared", true);

        // 3. 重新批量向量化
        int successCount = articleEmbeddingService.batchEmbedAllArticles();
        long endTime = System.currentTimeMillis();

        result.put("successCount", successCount);
        result.put("timeMs", endTime - startTime);

        log.info("向量索引重建完成: 成功={}篇, 耗时={}ms", successCount, endTime - startTime);
        return Result.success(result);
    }

    /**
     * 仅清空 ChromaDB + Redis 记录（不重建）
     */
    @PostMapping("/clear")
    @Operation(summary = "清空向量库", description = "仅清空ChromaDB和Redis记录，不重新向量化")
    public Result<String> clearIndex() {
        log.info("清空向量索引...");
        try {
            deleteChromaCollection();
            ChromaEmbeddingStore newStore = chromaDBConfig.rebuildStore();
            articleEmbeddingService.refreshEmbeddingStore(newStore);
            aiService.refreshEmbeddingStore(newStore);
            articleEmbeddingService.clearEmbeddingRecords();
            log.info("向量索引已清空");
            return Result.success("向量库已清空");
        } catch (Exception e) {
            log.error("清空失败: {}", e.getMessage(), e);
            return Result.error("清空失败: " + e.getMessage());
        }
    }

    /**
     * 通过 ChromaDB HTTP API 删除 collection
     */
    private void deleteChromaCollection() {
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.Request deleteReq = new okhttp3.Request.Builder()
                    .url(chromaBaseUrl + "/api/v1/collections/" + collectionName)
                    .delete()
                    .build();
            try (okhttp3.Response resp = client.newCall(deleteReq).execute()) {
                log.info("删除 ChromaDB collection 响应: {}", resp.code());
            }
        } catch (Exception e) {
            log.warn("删除 collection 异常（可能不存在）: {}", e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "测试语义检索")
    public Result<List<Map<String, Object>>> searchSimilarArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("语义检索: query={}, topK={}", query, topK);

        TextSegment querySegment = TextSegment.from(query);
        Embedding queryEmbedding = embeddingModel.embed(querySegment).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = getStore().search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        List<Map<String, Object>> results = matches.stream().map(match -> {
            Map<String, Object> item = new HashMap<>();
            item.put("score", match.score());
            item.put("text", match.embedded().text());

            if (match.embedded().metadata() != null) {
                item.put("articleId", match.embedded().metadata().getString("articleId"));
                item.put("title", match.embedded().metadata().getString("title"));
                item.put("authorName", match.embedded().metadata().getString("authorName"));
                item.put("categoryName", match.embedded().metadata().getString("categoryName"));
                item.put("chunkIndex", match.embedded().metadata().getString("chunkIndex"));
            }

            return item;
        }).collect(Collectors.toList());

        return Result.success(results);
    }

    @GetMapping("/health")
    @Operation(summary = "测试ChromaDB连接")
    public Result<String> healthCheck() {
        try {
            TextSegment testSegment = TextSegment.from("健康检查测试");
            Embedding testEmbedding = embeddingModel.embed(testSegment).content();

            log.info("ChromaDB连接正常，Embedding维度: {}", testEmbedding.dimension());
            return Result.success("ChromaDB连接正常，Embedding维度: " + testEmbedding.dimension());
        } catch (Exception e) {
            log.error("ChromaDB连接失败: {}", e.getMessage(), e);
            return Result.error("ChromaDB连接失败: " + e.getMessage());
        }
    }
}
