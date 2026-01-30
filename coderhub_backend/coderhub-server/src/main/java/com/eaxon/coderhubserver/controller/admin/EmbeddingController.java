package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubserver.service.ArticleEmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化管理接口（管理员）
 * 
 * 用于批量向量化、测试检索等操作
 */
@RestController
@RequestMapping("/admin/embedding")
@Slf4j
@Tag(name = "管理端-向量化管理接口")
public class EmbeddingController {

    @Autowired
    private ArticleEmbeddingService articleEmbeddingService;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 批量向量化所有已发布文章
     */
    @PostMapping("/batch")
    @Operation(summary = "批量向量化所有已发布文章")
    public Result<Map<String, Object>> batchEmbedAllArticles() {
        log.info("开始批量向量化所有文章");
        
        long startTime = System.currentTimeMillis();
        int successCount = articleEmbeddingService.batchEmbedAllArticles();
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("timeMs", endTime - startTime);
        
        return Result.success(result);
    }

    /**
     * 向量化单篇文章
     * 
     * @param articleId 文章ID
     * @param force 是否强制更新（忽略去重检查）
     */
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

    /**
     * 删除文章向量
     */
    @DeleteMapping("/article/{articleId}")
    @Operation(summary = "删除文章向量")
    public Result<String> removeArticleEmbedding(@PathVariable String articleId) {
        log.info("删除文章向量: articleId={}", articleId);
        
        articleEmbeddingService.removeArticleEmbedding(articleId);
        return Result.success("删除成功");
    }

    /**
     * 测试语义检索（输入查询文本，返回相似文章）
     */
    @GetMapping("/search")
    @Operation(summary = "测试语义检索")
    public Result<List<Map<String, Object>>> searchSimilarArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("语义检索: query={}, topK={}", query, topK);
        
        // 1. 将查询文本向量化
        TextSegment querySegment = TextSegment.from(query);
        Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
        
        // 2. 检索相似文章（使用新的search API）
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        
        // 3. 构建返回结果
        List<Map<String, Object>> results = matches.stream().map(match -> {
            Map<String, Object> item = new HashMap<>();
            item.put("score", match.score());
            item.put("text", match.embedded().text());
            
            // 提取元数据
            if (match.embedded().metadata() != null) {
                item.put("articleId", match.embedded().metadata().getString("articleId"));
                item.put("title", match.embedded().metadata().getString("title"));
                item.put("authorName", match.embedded().metadata().getString("authorName"));
                item.put("categoryName", match.embedded().metadata().getString("categoryName"));
            }
            
            return item;
        }).collect(Collectors.toList());
        
        return Result.success(results);
    }

    /**
     * 测试ChromaDB连接
     */
    @GetMapping("/health")
    @Operation(summary = "测试ChromaDB连接")
    public Result<String> healthCheck() {
        try {
            // 简单的向量化测试
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

