package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubcommon.constant.RedisConstant;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.VO.ArticleSummaryVO;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章智能摘要服务
 * 
 * 核心逻辑：
 * 1. Redis缓存优先（键 abstract:{id}，14天过期）
 * 2. 缓存未命中则调用阿里百炼LLM生成
 * 3. 基于博客纯文本生成150-200字摘要 + 3-5个延伸问题
 * 4. 存入Redis后返回结构化数据
 */
@Service
@Slf4j
public class ArticleSummaryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 摘要生成的Prompt模板
     */
    private static final String SUMMARY_PROMPT_TEMPLATE = """
            你是一个专业的技术文章分析助手。请根据以下技术博客文章内容，完成两个任务：

            【任务1】生成一段150-200字的中文摘要，要求：
            - 准确概括文章的核心内容和技术要点
            - 语言简洁专业，适合技术人员阅读
            - 突出文章的实用价值和学习收获

            【任务2】基于文章内容，生成3-5个延伸学习问题，要求：
            - 问题应该帮助读者深入理解文章主题
            - 问题要具体、有深度，能引发思考
            - 问题应该是读者可能想进一步了解的方向

            请严格按照以下JSON格式输出（不要输出其他任何内容）：
            {
              "summary": "这里是150-200字的摘要内容",
              "questions": ["问题1", "问题2", "问题3", "问题4", "问题5"]
            }

            以下是文章标题和内容：
            
            【标题】%s
            
            【内容】
            %s
            """;

    /**
     * 获取文章智能摘要
     * 
     * @param articleId 文章ID
     * @return 摘要VO，包含摘要和延伸问题
     */
    public ArticleSummaryVO getArticleSummary(String articleId) {
        // 1. 先查Redis缓存
        String cacheKey = RedisConstant.ARTICLE_SUMMARY + articleId;
        ArticleSummaryVO cachedSummary = getFromCache(cacheKey);
        if (cachedSummary != null) {
            log.info("从缓存获取文章摘要: articleId={}", articleId);
            cachedSummary.setFromCache(true);
            return cachedSummary;
        }

        // 2. 缓存未命中，查询文章信息
        Article article = articleMapper.getById(articleId);
        if (article == null) {
            log.warn("文章不存在: articleId={}", articleId);
            return null;
        }

        // 3. 获取文章完整内容
        String fullContent = getArticleContent(article);
        if (!StringUtils.hasText(fullContent)) {
            log.warn("文章内容为空: articleId={}", articleId);
            return null;
        }

        // 4. 调用LLM生成摘要
        ArticleSummaryVO summaryVO = generateSummaryByLLM(article, fullContent);
        if (summaryVO == null) {
            log.error("LLM生成摘要失败: articleId={}", articleId);
            return null;
        }

        // 5. 存入Redis缓存
        saveToCache(cacheKey, summaryVO);
        summaryVO.setFromCache(false);
        
        log.info("生成并缓存文章摘要成功: articleId={}", articleId);
        return summaryVO;
    }

    /**
     * 从缓存获取摘要
     */
    private ArticleSummaryVO getFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                if (cached instanceof String) {
                    return objectMapper.readValue((String) cached, ArticleSummaryVO.class);
                } else if (cached instanceof ArticleSummaryVO) {
                    return (ArticleSummaryVO) cached;
                }
            }
        } catch (Exception e) {
            log.error("从缓存读取摘要失败: key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 保存摘要到缓存
     */
    private void saveToCache(String cacheKey, ArticleSummaryVO summaryVO) {
        try {
            String jsonValue = objectMapper.writeValueAsString(summaryVO);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, 
                    RedisConstant.EXPIRE_14_DAYS, TimeUnit.SECONDS);
            log.debug("摘要已缓存: key={}", cacheKey);
        } catch (JsonProcessingException e) {
            log.error("序列化摘要失败: {}", e.getMessage());
        }
    }

    /**
     * 获取文章内容（从OSS下载）
     */
    private String getArticleContent(Article article) {
        if (article == null || !StringUtils.hasText(article.getContentUrl())) {
            return null;
        }
        try {
            String fileName = extractFileNameFromUrl(article.getContentUrl());
            return aliOssUtil.downloadAsString(fileName);
        } catch (Exception e) {
            log.error("获取文章内容失败: articleId={}, error={}", 
                    article.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 从URL提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        int index = url.lastIndexOf(".com/");
        if (index != -1) {
            return url.substring(index + 5);
        }
        return url;
    }

    /**
     * 调用LLM生成摘要和延伸问题
     */
    private ArticleSummaryVO generateSummaryByLLM(Article article, String content) {
        try {
            // 限制内容长度，避免Token超限（取前6000字）
            String truncatedContent = content.length() > 6000 
                    ? content.substring(0, 6000) + "..." 
                    : content;

            // 构建Prompt
            String prompt = String.format(SUMMARY_PROMPT_TEMPLATE, 
                    article.getTitle(), truncatedContent);

            // 调用LLM
            log.info("调用LLM生成摘要: articleId={}, title={}", 
                    article.getId(), article.getTitle());
            String response = chatLanguageModel.generate(prompt);
            log.debug("LLM响应: {}", response);

            // 解析JSON响应
            return parseLLMResponse(article, response);

        } catch (Exception e) {
            log.error("LLM生成摘要异常: articleId={}, error={}", 
                    article.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析LLM返回的JSON响应
     */
    private ArticleSummaryVO parseLLMResponse(Article article, String response) {
        try {
            // 提取JSON部分（LLM可能返回额外的文字）
            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                log.warn("无法从LLM响应中提取JSON: {}", response);
                // 尝试使用原始响应作为摘要
                return buildFallbackSummary(article, response);
            }

            // 解析JSON
            LLMSummaryResponse llmResponse = objectMapper.readValue(jsonStr, LLMSummaryResponse.class);

            return ArticleSummaryVO.builder()
                    .articleId(article.getId())
                    .title(article.getTitle())
                    .summary(llmResponse.getSummary())
                    .extendedQuestions(llmResponse.getQuestions())
                    .generateTime(System.currentTimeMillis())
                    .fromCache(false)
                    .build();

        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage());
            return buildFallbackSummary(article, response);
        }
    }

    /**
     * 从响应中提取JSON字符串
     */
    private String extractJson(String response) {
        // 尝试找到JSON对象
        Pattern pattern = Pattern.compile("\\{[^{}]*\"summary\"[^{}]*\"questions\"[^{}]*\\}", 
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group();
        }
        
        // 尝试更宽松的匹配
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return null;
    }

    /**
     * 构建降级摘要（当JSON解析失败时）
     */
    private ArticleSummaryVO buildFallbackSummary(Article article, String response) {
        // 使用响应的前200字作为摘要
        String summary = response.length() > 200 
                ? response.substring(0, 200) + "..." 
                : response;
        
        // 生成默认问题
        List<String> defaultQuestions = new ArrayList<>();
        defaultQuestions.add("这篇文章的核心观点是什么？");
        defaultQuestions.add("文章中提到的技术方案有哪些优缺点？");
        defaultQuestions.add("如何在实际项目中应用这些知识？");

        return ArticleSummaryVO.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .summary(summary)
                .extendedQuestions(defaultQuestions)
                .generateTime(System.currentTimeMillis())
                .fromCache(false)
                .build();
    }

    /**
     * 清除文章摘要缓存
     * 
     * @param articleId 文章ID
     */
    public void clearSummaryCache(String articleId) {
        String cacheKey = RedisConstant.ARTICLE_SUMMARY + articleId;
        redisTemplate.delete(cacheKey);
        log.info("清除文章摘要缓存: articleId={}", articleId);
    }

    /**
     * LLM响应的内部类
     */
    @lombok.Data
    private static class LLMSummaryResponse {
        private String summary;
        private List<String> questions;
    }
}

