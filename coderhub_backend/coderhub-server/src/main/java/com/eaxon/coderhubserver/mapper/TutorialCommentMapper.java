package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubcommon.enumeration.OperationType;
import com.eaxon.coderhubpojo.entity.TutorialComment;
import com.eaxon.coderhubserver.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教程评论Mapper接口
 */
@Mapper
public interface TutorialCommentMapper {

    /**
     * 插入评论
     * 注：不使用@AutoFill，因为TutorialComment只有createTime，没有updateTime
     */
    void insert(TutorialComment comment);

    /**
     * 根据ID删除评论
     */
    void deleteById(String id);

    /**
     * 根据ID查询评论
     */
    TutorialComment getById(String id);

    /**
     * 查询教程的顶级评论列表
     */
    List<TutorialComment> getTopCommentsByTutorialId(String tutorialId);

    /**
     * 查询某条评论的回复列表
     */
    List<TutorialComment> getRepliesByCommentId(String commentId);

    /**
     * 统计教程的评论数（只统计顶级评论）
     */
    Integer countByTutorialId(String tutorialId);

    /**
     * 更新评论点赞数
     */
    void updateLikeCount(@Param("id") String id, @Param("likeCount") Integer likeCount);
}

