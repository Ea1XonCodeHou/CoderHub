package com.eaxon.coderhubserver.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubpojo.DTO.ChatStreamEvent.RecommendItem;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubserver.agent.skills.ArticleContentReaderSkill;
import com.eaxon.coderhubserver.agent.skills.ArticleSearchSkill;
import com.eaxon.coderhubserver.agent.skills.SmartAssistantSkill;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.TagMapper;
import com.eaxon.coderhubserver.mapper.TutorialMapper;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * CoderHub AI 工具集
 * 提供教程搜索、文章搜索等工具供AI Agent调用
 * 
 * @author CoderHub
 */
@Component
@Slf4j
public class CoderHubTools {

    @Autowired
    private TutorialMapper tutorialMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;
    
    // Skills需要暴露给AIService注册
    @Autowired
    public ArticleSearchSkill articleSearchSkill;
    
    @Autowired
    public ArticleContentReaderSkill articleContentReaderSkill;
    
    @Autowired
    public SmartAssistantSkill smartAssistantSkill;

    /**
     * 搜索教程
     * 当用户想学习某个技术或主题时，使用此工具搜索平台上的相关教程
     */
    @Tool("【系统教程专用工具】搜索CoderHub平台上的视频教程和课程。" +
          "🔴触发条件（必须包含以下关键词之一）：'教程'、'课程'、'视频'、'学习路线'、'系统学习'、'怎么学'、'入门指南'。" +
          "🚫禁止触发：如果用户问'博客'、'文章'、'博文'，请使用ArticleSearchSkill工具。" +
          "✅典型问题示例：'SpringBoot教程'、'想学Vue课程'、'Redis学习路线'。")
    public String searchTutorials(
            @P("搜索关键词，如：Spring Boot、Vue3、Redis等") String keyword,
            @P("返回结果数量，默认5") int limit) {
        
        log.info("【工具调用】searchTutorials - 关键词: {}, 数量: {}", keyword, limit);
        
        if (limit <= 0 || limit > 10) {
            limit = 5;
        }
        
        List<Tutorial> tutorials = tutorialMapper.searchByKeyword(keyword, limit);
        
        if (tutorials == null || tutorials.isEmpty()) {
            log.info("【工具调用】searchTutorials - 未找到相关教程");
            return "未找到与「" + keyword + "」相关的教程。";
        }
        
        log.info("【工具调用】searchTutorials - 找到 {} 个教程", tutorials.size());
        
        // 构建返回结果
        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(tutorials.size()).append(" 个与「").append(keyword).append("」相关的教程：\n\n");
        
        for (int i = 0; i < tutorials.size(); i++) {
            Tutorial t = tutorials.get(i);
            result.append(i + 1).append(". **").append(t.getTitle()).append("**\n");
            result.append("   - 讲师：").append(t.getInstructorName() != null ? t.getInstructorName() : "未知").append("\n");
            result.append("   - 难度：").append(getDifficultyText(t.getDifficulty())).append("\n");
            result.append("   - 评分：").append(t.getRating() != null ? t.getRating() : "暂无").append(" ⭐\n");
            result.append("   - 学习人数：").append(t.getStudentCount() != null ? t.getStudentCount() : 0).append(" 人\n");
            if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                String desc = t.getDescription().length() > 100 ? 
                    t.getDescription().substring(0, 100) + "..." : t.getDescription();
                result.append("   - 简介：").append(desc).append("\n");
            }
            result.append("   - 链接：/tutorial/").append(t.getId()).append("\n\n");
        }
        
        return result.toString();
    }

    /**
     * 获取平台热门内容
     * 推荐平台上最受欢迎的教程或文章
     */
    @Tool("获取CoderHub平台上的热门内容。当用户没有明确目标、想看看有什么好内容、或者询问推荐时，应该调用此工具。")
    public String getHotContent(
            @P("内容类型：tutorial-教程，article-文章，all-全部") String type,
            @P("返回结果数量，默认5") int limit) {
        
        log.info("【工具调用】getHotContent - 类型: {}, 数量: {}", type, limit);
        
        if (limit <= 0 || limit > 10) {
            limit = 5;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("🔥 CoderHub 热门推荐\n\n");
        
        // 获取热门教程
        if ("tutorial".equals(type) || "all".equals(type)) {
            List<Tutorial> tutorials = tutorialMapper.getHotTutorials(limit);
            if (tutorials != null && !tutorials.isEmpty()) {
                result.append("📚 **热门教程**\n");
                for (int i = 0; i < tutorials.size(); i++) {
                    Tutorial t = tutorials.get(i);
                    result.append(i + 1).append(". ").append(t.getTitle());
                    result.append(" (").append(t.getStudentCount()).append("人学习, ");
                    result.append(t.getRating()).append("⭐)\n");
                    result.append("   链接：/tutorial/").append(t.getId()).append("\n");
                }
                result.append("\n");
            }
        }
        
        // 获取热门文章
        if ("article".equals(type) || "all".equals(type)) {
            List<Article> articles = articleMapper.getHotArticles(limit);
            if (articles != null && !articles.isEmpty()) {
                result.append("📝 **热门文章**\n");
                for (int i = 0; i < articles.size(); i++) {
                    Article a = articles.get(i);
                    result.append(i + 1).append(". ").append(a.getTitle());
                    result.append(" (").append(a.getViewCount()).append("阅读, ");
                    result.append(a.getLikeCount()).append("👍)\n");
                    result.append("   链接：/article/").append(a.getId()).append("\n");
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 获取热门标签
     */
    @Tool("获取CoderHub平台上的热门技术标签。当用户想了解平台有哪些技术话题时，应该调用此工具。")
    public String getHotTags(@P("返回结果数量，默认10") int limit) {
        log.info("【工具调用】getHotTags - 数量: {}", limit);
        
        if (limit <= 0 || limit > 20) {
            limit = 10;
        }
        
        List<Tag> tags = tagMapper.getHotTags(limit);
        
        if (tags == null || tags.isEmpty()) {
            return "暂无热门标签";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("🏷️ **CoderHub 热门技术标签**\n\n");
        
        for (Tag tag : tags) {
            result.append("• ").append(tag.getTagName());
            result.append(" (").append(tag.getUsageCount()).append("篇文章)\n");
        }
        
        return result.toString();
    }

    // ============ 辅助方法 ============

    /**
     * 将教程列表转换为推荐项列表（供前端展示卡片用）
     */
    public List<RecommendItem> convertTutorialsToRecommendItems(List<Tutorial> tutorials) {
        if (tutorials == null) return new ArrayList<>();
        
        return tutorials.stream().map(t -> RecommendItem.builder()
                .id(t.getId())
                .type("tutorial")
                .title(t.getTitle())
                .description(t.getDescription())
                .coverImage(t.getCoverImage())
                .author(t.getInstructorName())
                .viewCount(Long.valueOf(t.getStudentCount() != null ? t.getStudentCount() : 0))
                .rating(t.getRating() != null ? t.getRating().doubleValue() : 0.0)
                .link("/tutorial/" + t.getId())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 将文章列表转换为推荐项列表
     */
    public List<RecommendItem> convertArticlesToRecommendItems(List<Article> articles) {
        if (articles == null) return new ArrayList<>();
        
        return articles.stream().map(a -> {
            // 获取文章标签
            List<Tag> tags = tagMapper.getByArticleId(a.getId());
            List<String> tagNames = tags != null ? 
                tags.stream().map(Tag::getTagName).collect(Collectors.toList()) : 
                new ArrayList<>();
            
            return RecommendItem.builder()
                    .id(a.getId())
                    .type("article")
                    .title(a.getTitle())
                    .description(a.getSummary())
                    .coverImage(a.getCoverImage())
                    .viewCount(a.getViewCount())
                    .tags(tagNames)
                    .link("/article/" + a.getId())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 获取难度文本
     */
    private String getDifficultyText(Integer difficulty) {
        if (difficulty == null) return "未知";
        return switch (difficulty) {
            case 0 -> "入门";
            case 1 -> "进阶";
            case 2 -> "高级";
            default -> "未知";
        };
    }

    /**
     * 直接搜索并返回推荐项列表（供AIService使用）
     */
    public List<RecommendItem> searchAndGetRecommendations(String keyword, int tutorialLimit, int articleLimit) {
        List<RecommendItem> recommendations = new ArrayList<>();
        
        // 搜索教程
        List<Tutorial> tutorials = tutorialMapper.searchByKeyword(keyword, tutorialLimit);
        if (tutorials != null) {
            recommendations.addAll(convertTutorialsToRecommendItems(tutorials));
        }
        
        // 搜索文章
        List<Article> articles = articleMapper.searchByKeyword(keyword, articleLimit);
        if (articles != null) {
            recommendations.addAll(convertArticlesToRecommendItems(articles));
        }
        
        return recommendations;
    }
}

