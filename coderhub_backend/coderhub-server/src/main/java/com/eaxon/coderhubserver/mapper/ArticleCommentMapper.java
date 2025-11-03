package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubcommon.enumeration.OperationType;
import com.eaxon.coderhubpojo.entity.ArticleComment;
import com.eaxon.coderhubserver.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章评论Mapper接口
 */
@Mapper
public interface ArticleCommentMapper {

    /**
     * 插入评论
     */
    @AutoFill(OperationType.INSERT)
    void insert(ArticleComment comment);

    /**
     * 根据ID删除评论
     */
    void deleteById(String id);

    /**
     * 根据ID查询评论
     */
    ArticleComment getById(String id);

    /**
     * 查询文章的顶级评论列表
     */
    List<ArticleComment> getTopCommentsByArticleId(String articleId);

    /**
     * 查询某条评论的回复列表
     */
    List<ArticleComment> getRepliesByCommentId(String commentId);

    /**
     * 统计文章的评论数（只统计顶级评论）
     */
    Integer countByArticleId(String articleId);

    /**
     * 更新评论点赞数
     */
    void updateLikeCount(@Param("id") String id, @Param("likeCount") Integer likeCount);
}

