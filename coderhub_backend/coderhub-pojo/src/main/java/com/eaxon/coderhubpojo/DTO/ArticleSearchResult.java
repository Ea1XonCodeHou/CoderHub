package com.eaxon.coderhubpojo.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章搜索结果DTO
 * 用于AI工具返回结构化的文章列表数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSearchResult {
    
    /** 搜索关键词 */
    private String keyword;
    
    /** 找到的文章数量 */
    private Integer count;
    
    /** 文章卡片列表 */
    private List<ArticleCard> articles;
    
    /** 搜索建议 */
    private String suggestion;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleCard {
        /** 文章ID */
        private String id;
        
        /** 文章标题 */
        private String title;
        
        /** 文章摘要 */
        private String summary;
        
        /** 封面图URL */
        private String coverImage;
        
        /** 阅读量 */
        private Long viewCount;
        
        /** 点赞数 */
        private Integer likeCount;
        
        /** 分类名称 */
        private String categoryName;
        
        /** 作者名称 */
        private String authorName;
        
        /** 发布时间 */
        private String publishTime;
    }
}
