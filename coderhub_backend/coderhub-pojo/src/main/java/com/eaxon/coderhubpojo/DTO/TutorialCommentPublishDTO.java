package com.eaxon.coderhubpojo.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 教程评论发布DTO
 */
@Data
public class TutorialCommentPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tutorialId;  // 教程ID
    private String content;     // 评论内容
    private String parentId;    // 父评论ID（可选，如果是回复评论则必填）
}

