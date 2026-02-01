package com.eaxon.coderhubpojo.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章内容读取结果DTO
 * 包含完整内容和RAG检索的关键段落
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleContentResult {
    
    /** 文章ID */
    private String articleId;
    
    /** 文章标题 */
    private String title;
    
    /** 完整Markdown内容 */
    private String fullContent;
    
    /** RAG检索的相关段落 */
    private List<RelevantSegment> relevantSegments;
    
    /** 是否成功 */
    private Boolean success;
    
    /** 错误信息（失败时） */
    private String errorMessage;
    
    /** 内容概括（AI生成） */
    private String summary;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelevantSegment {
        /** 段落内容 */
        private String content;
        
        /** 相似度得分 */
        private Double score;
        
        /** 段落位置 */
        private Integer position;
    }
    
    /**
     * 创建成功结果
     */
    public static ArticleContentResult success(String articleId, String title, String content) {
        ArticleContentResult result = new ArticleContentResult();
        result.setArticleId(articleId);
        result.setTitle(title);
        result.setFullContent(content);
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static ArticleContentResult error(String errorMessage) {
        ArticleContentResult result = new ArticleContentResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
