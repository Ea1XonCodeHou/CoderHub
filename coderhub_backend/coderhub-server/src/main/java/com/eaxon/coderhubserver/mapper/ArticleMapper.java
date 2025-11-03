package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章Mapper接口
 */
@Mapper
public interface ArticleMapper {

    /**
     * 插入文章
     */
    void insert(Article article);

    /**
     * 根据ID查询文章
     */
    Article getById(String id);

    /**
     * 根据ID删除文章
     */
    void deleteById(String id);

    /**
     * 更新文章
     */
    void update(Article article);

    /**
     * 查询文章列表
     */
    List<Article> list(Article article);

    /**
     * 根据标签ID查询文章
     */
    List<Article> getByTagId(String tagId);

    /**
     * 增加阅读量
     */
    void incrementViewCount(String id);

    /**
     * 根据用户ID查询文章
     */
    List<Article> getByUserId(String userId);

    /**
     * 查询待审核文章列表
     */
    List<Article> getPendingArticles();

    /**
     * 查询已发布文章列表（管理端）
     */
    List<Article> getPublishedArticles();

    /**
     * 更新审核状态
     */
    void updateAuditStatus(@Param("id") String id, 
                           @Param("auditStatus") Integer auditStatus, 
                           @Param("auditReason") String auditReason, 
                           @Param("status") Integer status);

    /**
     * 统计指定分类下指定状态的文章数量
     */
    int countByCategoryIdAndStatus(@Param("categoryId") String categoryId, 
                                    @Param("status") Integer status, 
                                    @Param("auditStatus") Integer auditStatus);

    /**
     * 更新文章点赞数
     */
    void updateLikeCount(@Param("id") String id, @Param("likeCount") Integer likeCount);
}

