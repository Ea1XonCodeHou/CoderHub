package com.eaxon.coderhubpojo.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 发布评论DTO
 */
@Data
public class CommentPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String articleId;  // 文章ID
    private String content;    // 评论内容
    private String replyId;    // 回复的评论ID（可选，如果是回复评论则必填）
}

