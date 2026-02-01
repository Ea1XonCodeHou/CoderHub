package com.eaxon.coderhubserver.agent.skills;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eaxon.coderhubpojo.DTO.SmartActionRecommendation;
import com.eaxon.coderhubpojo.DTO.SmartActionRecommendation.Action;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.TagMapper;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 智能助手技能
 * 根据用户学习行为推荐智能辅助操作
 * 
 * @author CoderHub
 */
@Component
@Slf4j
public class SmartAssistantSkill {

    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private TagMapper tagMapper;

    /**
     * 智能推荐辅助操作
     * 当用户正在阅读文章或完成文章学习后，AI应调用此工具推荐后续操作
     */
    @Tool("当用户正在学习文章时，智能推荐相关的辅助操作（如生成笔记、查找教程等）。" +
          "在用户深入阅读文章后，AI应主动调用此工具提供智能建议。")
    public SmartActionRecommendation suggestActions(
            @P("文章ID") String articleId,
            @P("用户当前行为：reading-正在阅读, completed-已完成") String userAction) {
        
        log.info("【SmartAssistantSkill】推荐操作: articleId={}, userAction={}", 
                 articleId, userAction);
        
        SmartActionRecommendation recommendation = new SmartActionRecommendation();
        recommendation.setArticleId(articleId);
        
        try {
            // 获取文章信息
            Article article = articleMapper.getById(articleId);
            if (article == null) {
                recommendation.setReason("文章不存在");
                return recommendation;
            }
            
            // 1. 通用操作：生成学习笔记
            Action noteAction = new Action();
            noteAction.setActionId("generate_note");
            noteAction.setLabel("📝 生成Markdown笔记");
            noteAction.setDescription("根据文章内容生成结构化学习笔记，方便复习和分享");
            noteAction.setIcon("📝");
            noteAction.setPriority(5); // 最高优先级
            recommendation.addAction(noteAction);
            
            // 2. 通用操作：检索相关教程
            Action tutorialAction = new Action();
            tutorialAction.setActionId("find_tutorials");
            tutorialAction.setLabel("📚 查找相关教程");
            tutorialAction.setDescription("在CoderHub平台检索该技术的系统化教程");
            tutorialAction.setIcon("📚");
            tutorialAction.setPriority(4);
            recommendation.addAction(tutorialAction);
            
            // 3. 根据文章标签推荐特殊操作
            List<Tag> tags = tagMapper.getByArticleId(articleId);
            boolean hasCodeTag = tags.stream()
                    .anyMatch(tag -> tag.getTagName().contains("代码") || 
                                   tag.getTagName().contains("实战") ||
                                   tag.getTagName().contains("项目"));
            
            if (hasCodeTag) {
                // 代码类文章：推荐生成Demo
                Action demoAction = new Action();
                demoAction.setActionId("generate_demo");
                demoAction.setLabel("💻 生成可运行Demo");
                demoAction.setDescription("基于文章内容生成完整的代码示例（未来功能）");
                demoAction.setIcon("💻");
                demoAction.setPriority(3);
                recommendation.addAction(demoAction);
            }
            
            // 4. 根据文章分类推荐
            if (article.getCategoryId() != null) {
                // 推荐同分类文章
                Action relatedAction = new Action();
                relatedAction.setActionId("find_related");
                relatedAction.setLabel("🔗 查找相关文章");
                relatedAction.setDescription("发现同一技术领域的其他优质文章");
                relatedAction.setIcon("🔗");
                relatedAction.setPriority(2);
                recommendation.addAction(relatedAction);
            }
            
            // 5. 设置推荐原因
            String reason = String.format(
                "根据你正在学习的《%s》，为你推荐 %d 个智能辅助操作，帮助你更高效地掌握知识。",
                article.getTitle(),
                recommendation.getActions().size()
            );
            recommendation.setReason(reason);
            
            log.info("【SmartAssistantSkill】推荐了 {} 个操作", recommendation.getActions().size());
            return recommendation;
            
        } catch (Exception e) {
            log.error("生成智能推荐时发生异常", e);
            recommendation.setReason("系统异常，暂时无法生成推荐");
            return recommendation;
        }
    }
    
    /**
     * 生成Markdown笔记（占位，后续实现）
     */
    @Tool("根据文章内容生成结构化的Markdown学习笔记")
    public String generateMarkdownNote(
            @P("文章ID") String articleId,
            @P("笔记风格：brief-简洁版, detailed-详细版") String style) {
        
        log.info("【SmartAssistantSkill】生成笔记: articleId={}, style={}", articleId, style);
        
        // TODO: 后续实现完整的笔记生成逻辑
        return "笔记生成功能开发中，敬请期待！";
    }
}
