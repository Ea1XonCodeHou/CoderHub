package com.eaxon.coderhubserver.service;

/**
 * 文章点赞Service接口
 */
public interface ArticleLikeService {

    /**
     * 点赞/取消点赞（toggle）
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 点赞状态（true=已点赞，false=已取消点赞）
     */
    Boolean toggleLike(String userId, String articleId);

    /**
     * 获取文章点赞数
     * @param articleId 文章ID
     * @return 点赞数
     */
    Integer getLikeCount(String articleId);

    /**
     * 检查用户是否已点赞
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return true=已点赞，false=未点赞
     */
    Boolean isLiked(String userId, String articleId);
}

