package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.Article;

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
     * 直接更新浏览量（用于Redis同步）
     */
    void updateViewCountDirect(@Param("id") String id, @Param("viewCount") Long viewCount);

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

    /**
     * 更新文章评论数
     */
    void updateCommentCount(@Param("id") String id, @Param("commentCount") Integer commentCount);

    /**
     * 统计用户发布的文章数量（已发布状态）
     */
    Integer countByUserId(String userId);
    
    /**
     * 获取点赞数最多的文章（用于预热）
     * @param limit 数量限制
     * @return 文章列表
     */
    List<Article> getTopLikedArticles(@Param("limit") Integer limit);
    
    /**
     * 根据关键词搜索文章（用于AI工具调用）
     * @param keyword 搜索关键词
     * @param limit 返回数量限制
     * @return 文章列表
     */
    List<Article> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);
    
    /**
     * 获取热门文章（按浏览量排序）
     * @param limit 返回数量限制
     * @return 文章列表
     */
    List<Article> getHotArticles(@Param("limit") Integer limit);
}

