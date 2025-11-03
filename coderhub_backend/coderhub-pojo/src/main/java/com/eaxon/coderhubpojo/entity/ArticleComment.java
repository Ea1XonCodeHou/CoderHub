package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章评论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String articleId;
    private String userId;
    private String content;
    private String replyId;  // 回复的评论ID（NULL表示顶级评论）
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

