package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.ArticleCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章评论点赞Mapper接口
 */
@Mapper
public interface ArticleCommentLikeMapper {

    /**
     * 插入点赞记录
     */
    void insert(ArticleCommentLike commentLike);

    /**
     * 删除点赞记录
     */
    void delete(@Param("userId") String userId, @Param("commentId") String commentId);

    /**
     * 根据用户ID和评论ID查询点赞记录
     */
    ArticleCommentLike getByUserIdAndCommentId(@Param("userId") String userId, @Param("commentId") String commentId);

    /**
     * 统计评论点赞数
     */
    Integer countByCommentId(String commentId);
}

