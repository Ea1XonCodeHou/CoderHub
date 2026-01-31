package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.TutorialCommentPublishDTO;
import com.eaxon.coderhubpojo.VO.TutorialCommentVO;

import java.util.List;

/**
 * 教程评论业务接口
 */
public interface TutorialCommentService {

    /**
     * 发布评论（包括顶级评论和回复）
     * @param dto 评论发布DTO
     * @param userId 用户ID
     * @return 评论ID
     */
    String publishComment(TutorialCommentPublishDTO dto, String userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(String commentId, String userId);

    /**
     * 获取教程的评论列表（包括顶级评论和回复）
     * @param tutorialId 教程ID
     * @param userId 当前用户ID（用于判断是否已点赞和是否本人，可为null）
     * @return 评论列表
     */
    List<TutorialCommentVO> getCommentsByTutorialId(String tutorialId, String userId);

    /**
     * 统计教程的评论数
     * @param tutorialId 教程ID
     * @return 评论数量
     */
    Integer countByTutorialId(String tutorialId);
}

