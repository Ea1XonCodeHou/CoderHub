package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 文章AI智能摘要VO
 * 
 * 包含AI生成的文章摘要和延伸学习问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 文章ID
     */
    private String articleId;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * AI生成的智能摘要（150-200字）
     */
    private String summary;
    
    /**
     * 延伸学习问题列表（3-5个）
     * 用于生成可点击的问题气泡，跳转到AI助手深入学习
     */
    private List<String> extendedQuestions;
    
    /**
     * 生成时间戳
     */
    private Long generateTime;
    
    /**
     * 是否来自缓存
     */
    private Boolean fromCache;
}

