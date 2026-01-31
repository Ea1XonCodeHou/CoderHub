package com.eaxon.coderhubserver.service;

import java.util.Map;

/**
 * 文章评论点赞业务接口
 */
public interface ArticleCommentLikeService {

    /**
     * 点赞/取消点赞评论
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return Map包含liked(是否已点赞)和likeCount(点赞数)
     */
    Map<String, Object> toggleLike(String userId, String commentId);

    /**
     * 检查用户是否已点赞评论
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return true表示已点赞，false表示未点赞
     */
    Boolean isLiked(String userId, String commentId);
}

