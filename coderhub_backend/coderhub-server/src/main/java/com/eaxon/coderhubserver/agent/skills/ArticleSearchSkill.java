package com.eaxon.coderhubserver.agent.skills;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubpojo.DTO.ArticleSearchResult;
import com.eaxon.coderhubpojo.DTO.ArticleSearchResult.ArticleCard;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Category;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.CategoryMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 文章搜索技能
 * 提供结构化的文章搜索结果，返回文章卡片数据供前端展示
 * 
 * @author CoderHub
 */
@Component
@Slf4j
public class ArticleSearchSkill {

    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 搜索平台文章，返回带封面图的文章卡片数据
     * 当用户想查询、查看、学习某个技术领域的文章时调用
     */
    @Tool("【博客文章专用工具】搜索CoderHub平台上的技术博客和文章，返回JSON格式的文章卡片数据。" +
          "🔴触发条件（必须包含以下关键词之一）：'博客'、'文章'、'技术文档'、'博文'、'blog'、'post'、'阅读'。" +
          "🚫禁止触发：如果用户问'教程'、'课程'、'视频'、'学习路线'，请使用searchTutorials工具。" +
          "✅典型问题示例：'找找SpringBoot的博客'、'有Redis相关的文章吗'、'推荐微服务博文'。" +
          "返回JSON包含：articles数组（id/title/summary/coverImage/viewCount/likeCount/categoryName/authorName），前端会渲染为精美卡片。")
    public String searchArticles(
            @P("搜索关键词，如：Agent、SpringBoot、Redis、微服务等") String keyword,
            @P("返回结果数量，默认5，最多10") int limit) {
        
        log.info("【ArticleSearchSkill】搜索关键词: {}, 数量: {}", keyword, limit);
        
        // 参数校验
        if (limit <= 0 || limit > 10) {
            limit = 5;
        }
        
        // 搜索文章
        List<Article> articles = articleMapper.searchByKeyword(keyword, limit);
        
        if (articles == null || articles.isEmpty()) {
            log.info("【ArticleSearchSkill】未找到相关文章");
            ArticleSearchResult result = new ArticleSearchResult();
            result.setKeyword(keyword);
            result.setCount(0);
            result.setArticles(List.of());
            result.setSuggestion("未找到与「" + keyword + "」相关的文章，建议尝试其他关键词或查看平台热门文章。");
            try {
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("序列化ArticleSearchResult失败", e);
                return "{\"error\":\"序列化失败\"}";
            }
        }
        
        log.info("【ArticleSearchSkill】找到 {} 篇文章", articles.size());
        
        // 构建文章卡片列表
        List<ArticleCard> cards = articles.stream()
                .map(this::buildArticleCard)
                .collect(Collectors.toList());
        
        // 构建返回结果
        ArticleSearchResult result = new ArticleSearchResult();
        result.setKeyword(keyword);
        result.setCount(cards.size());
        result.setArticles(cards);
        result.setSuggestion("找到 " + cards.size() + " 篇与「" + keyword + "」相关的优质文章，点击「继续深入学习」可下载完整内容并进行深度阅读。");
        
        try {
            String json = objectMapper.writeValueAsString(result);
            log.info("【ArticleSearchSkill】返回JSON: {}", json.substring(0, Math.min(200, json.length())));
            
            // 存储原始JSON到ThreadLocal（供AIService读取）
            try {
                Class<?> aiServiceClass = Class.forName("com.eaxon.coderhubserver.service.AIService");
                ThreadLocal<String> jsonThreadLocal = (ThreadLocal<String>) aiServiceClass.getField("TOOL_RESULT_JSON").get(null);
                ThreadLocal<String> nameThreadLocal = (ThreadLocal<String>) aiServiceClass.getField("TOOL_NAME").get(null);
                jsonThreadLocal.set(json);
                nameThreadLocal.set("文章搜索");
                log.info("【ArticleSearchSkill】已存储JSON到ThreadLocal");
            } catch (Exception e) {
                log.warn("存储JSON到ThreadLocal失败: {}", e.getMessage());
            }
            
            // 返回包含真实文章标题的详细信息给LLM，并明确约束
            StringBuilder response = new StringBuilder();
            response.append("⚠️ 重要提示：以下是平台真实存在的所有文章，你只能介绍这 ").append(cards.size()).append(" 篇，不要编造或扩展其他文章！\n\n");
            response.append("平台实际有 ").append(cards.size()).append(" 篇与「").append(keyword).append("」相关的文章：\n\n");
            for (int i = 0; i < cards.size(); i++) {
                ArticleCard card = cards.get(i);
                response.append(i + 1).append(". 标题：《").append(card.getTitle()).append("》\n");
                response.append("   分类：").append(card.getCategoryName()).append("\n");
                response.append("   作者：").append(card.getAuthorName()).append("\n");
                response.append("   简介：").append(card.getSummary().substring(0, Math.min(80, card.getSummary().length()))).append("...\n\n");
            }
            response.append("\n⛔ 严禁发挥想象：上述 ").append(cards.size()).append(" 篇就是平台的所有相关文章，不存在其他文章！");
            return response.toString();
        } catch (Exception e) {
            log.error("序列化ArticleSearchResult失败", e);
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 构建文章卡片
     */
    private ArticleCard buildArticleCard(Article article) {
        ArticleCard card = new ArticleCard();
        card.setId(article.getId());
        card.setTitle(article.getTitle());
        
        // 摘要处理（限制长度）
        String summary = article.getSummary();
        if (summary != null && summary.length() > 200) {
            summary = summary.substring(0, 200) + "...";
        }
        card.setSummary(summary);
        
        // 封面图
        card.setCoverImage(article.getCoverImage());
        
        // 统计数据
        card.setViewCount(article.getViewCount() != null ? article.getViewCount() : 0L);
        card.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0);
        
        // 分类信息
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.findById(article.getCategoryId());
            if (category != null) {
                card.setCategoryName(category.getCategoryName());
            }
        }
        
        // 作者信息
        if (article.getUserId() != null) {
            User author = userMapper.getUserById(article.getUserId());
            if (author != null) {
                card.setAuthorName(author.getUsername());
            }
        }
        
        // 发布时间
        if (article.getPublishTime() != null) {
            card.setPublishTime(article.getPublishTime().format(FORMATTER));
        }
        
        return card;
    }
}
