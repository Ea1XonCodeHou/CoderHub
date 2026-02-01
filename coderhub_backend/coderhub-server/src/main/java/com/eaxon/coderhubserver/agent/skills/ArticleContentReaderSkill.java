package com.eaxon.coderhubserver.agent.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubpojo.DTO.ArticleContentResult;
import com.eaxon.coderhubpojo.DTO.ArticleContentResult.RelevantSegment;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.service.ArticleEmbeddingService;
import com.eaxon.coderhubserver.service.OssContentService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;

/**
 * 文章内容读取技能
 * 下载并解析文章完整内容，结合RAG检索提供深度理解
 * 
 * @author CoderHub
 */
@Component
@Slf4j
public class ArticleContentReaderSkill {

    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private OssContentService ossContentService;
    
    @Autowired
    private ArticleEmbeddingService embeddingService;
    
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    /** RAG检索返回的段落数 */
    private static final int TOP_K_SEGMENTS = 3;
    
    /** RAG最低相似度阈值 */
    private static final double MIN_SIMILARITY = 0.6;

    /**
     * 读取文章完整内容并进行RAG深度检索
     * 当用户点击"继续了解"或明确要求深入阅读某篇文章时调用
     */
    @Tool("读取指定文章的完整内容，并使用RAG技术提取关键信息。" +
          "当用户说'继续了解这篇文章'、'深入阅读'、'详细讲解某篇文章'时调用此工具。")
    public ArticleContentResult readArticleContent(
            @P("文章ID（从搜索结果中获取）") String articleId,
            @P("用户关注的焦点问题（可选，用于精准检索）") String focusQuery) {
        
        log.info("【ArticleContentReaderSkill】读取文章: articleId={}, focusQuery={}", 
                 articleId, focusQuery);
        
        try {
            // 1. 获取文章元数据
            Article article = articleMapper.getById(articleId);
            if (article == null) {
                log.warn("文章不存在: {}", articleId);
                return ArticleContentResult.error("文章不存在");
            }
            
            // 2. 检查文章是否已向量化（若未向量化则立即执行）
            if (!embeddingService.isArticleEmbedded(articleId)) {
                log.info("文章未向量化，开始向量化: {}", articleId);
                embeddingService.embedArticle(articleId, true);
            }
            
            // 3. 下载文章内容
            String contentUrl = article.getContentUrl();
            if (contentUrl == null || contentUrl.isEmpty()) {
                log.warn("文章内容URL为空: {}", articleId);
                return ArticleContentResult.error("文章内容不可用");
            }
            
            String markdownContent = ossContentService.downloadMarkdownContent(contentUrl);
            if (markdownContent == null || markdownContent.isEmpty()) {
                log.error("文章内容下载失败: {}", articleId);
                return ArticleContentResult.error("文章内容下载失败，请稍后重试");
            }
            
            log.info("文章内容下载成功，长度: {} 字符", markdownContent.length());
            
            // 4. RAG检索相关段落（基于用户焦点问题）
            List<RelevantSegment> relevantSegments = new ArrayList<>();
            if (focusQuery != null && !focusQuery.isEmpty()) {
                relevantSegments = searchRelevantSegments(articleId, focusQuery);
            }
            
            // 5. 构建返回结果
            ArticleContentResult result = ArticleContentResult.success(
                articleId, 
                article.getTitle(), 
                markdownContent
            );
            result.setRelevantSegments(relevantSegments);
            
            // 生成简短摘要提示
            String summaryHint = String.format(
                "已成功获取《%s》的完整内容（约%d字），现在可以为你详细解读文章内容。",
                article.getTitle(),
                markdownContent.length()
            );
            result.setSummary(summaryHint);
            
            log.info("【ArticleContentReaderSkill】文章读取成功: {}", article.getTitle());
            return result;
            
        } catch (Exception e) {
            log.error("读取文章内容时发生异常: articleId={}", articleId, e);
            return ArticleContentResult.error("系统异常：" + e.getMessage());
        }
    }
    
    /**
     * RAG检索相关段落
     */
    private List<RelevantSegment> searchRelevantSegments(String articleId, String query) {
        try {
            // 1. 将查询向量化
            TextSegment querySegment = TextSegment.from(query);
            Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
            
            // 2. 构建检索请求（仅在当前文章内检索）
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(TOP_K_SEGMENTS)
                    .minScore(MIN_SIMILARITY)
                    .build();
            
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
            
            // 3. 过滤出属于当前文章的段落
            List<RelevantSegment> segments = matches.stream()
                    .filter(match -> {
                        if (match.embedded().metadata() == null) {
                            return false;
                        }
                        String matchArticleId = match.embedded().metadata().getString("articleId");
                        return articleId.equals(matchArticleId);
                    })
                    .map(match -> {
                        RelevantSegment segment = new RelevantSegment();
                        segment.setContent(match.embedded().text());
                        segment.setScore(match.score());
                        return segment;
                    })
                    .collect(Collectors.toList());
            
            log.info("RAG检索到 {} 个相关段落", segments.size());
            return segments;
            
        } catch (Exception e) {
            log.error("RAG检索失败: query={}", query, e);
            return new ArrayList<>();
        }
    }
}
