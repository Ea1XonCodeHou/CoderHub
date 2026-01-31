package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教程评论展示VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 评论基本信息
    private String id;
    private String tutorialId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    
    // 评论用户信息
    private String userId;
    private String username;
    private String userAvatar;
    private Integer userLevel;
    
    // 回复相关信息
    private String parentId;              // 父评论ID
    private String replyToUsername;       // 被回复的用户名
    
    // 当前用户是否已点赞
    private Boolean isLiked;
    
    // 是否是当前用户发布的
    private Boolean isOwner;
    
    // 子评论列表（只用于顶级评论）
    private List<TutorialCommentVO> replies;
}

