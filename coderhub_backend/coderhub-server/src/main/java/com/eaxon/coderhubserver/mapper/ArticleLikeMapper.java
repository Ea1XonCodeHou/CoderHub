package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章点赞Mapper接口
 */
@Mapper
public interface ArticleLikeMapper {

    /**
     * 插入点赞记录
     * 注意：不使用@AutoFill，因为ArticleLike没有updateTime字段
     */
    void insert(ArticleLike articleLike);

    /**
     * 删除点赞记录
     */
    void delete(@Param("userId") String userId, @Param("articleId") String articleId);

    /**
     * 查询用户是否已点赞该文章
     */
    ArticleLike getByUserIdAndArticleId(@Param("userId") String userId, @Param("articleId") String articleId);

    /**
     * 统计文章点赞数
     */
    Integer countByArticleId(String articleId);
}

