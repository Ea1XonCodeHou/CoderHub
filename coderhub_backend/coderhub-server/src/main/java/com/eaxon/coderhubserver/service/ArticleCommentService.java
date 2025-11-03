package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.CommentPublishDTO;
import com.eaxon.coderhubpojo.VO.CommentVO;

import java.util.List;

/**
 * 文章评论业务接口
 */
public interface ArticleCommentService {

    /**
     * 发布评论（包括顶级评论和回复）
     * @param commentPublishDTO 评论发布DTO
     * @param userId 用户ID
     * @return 评论ID
     */
    String publishComment(CommentPublishDTO commentPublishDTO, String userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(String commentId, String userId);

    /**
     * 获取文章的评论列表（包括顶级评论和回复）
     * @param articleId 文章ID
     * @param userId 当前用户ID（用于判断是否已点赞，可为null）
     * @return 评论列表
     */
    List<CommentVO> getCommentsByArticleId(String articleId, String userId);
}

