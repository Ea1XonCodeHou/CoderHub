package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.ArticlePublishDTO;
import com.eaxon.coderhubpojo.VO.ArticleDetailVO;
import com.eaxon.coderhubpojo.VO.ArticleVO;

import java.util.List;

/**
 * 文章Service接口
 */
public interface ArticleService {

    /**
     * 发布文章
     */
    ArticleVO publishArticle(ArticlePublishDTO dto);

    /**
     * 获取文章详情
     */
    ArticleDetailVO getArticleDetail(String articleId);

    /**
     * 获取文章列表
     */
    List<ArticleVO> getArticleList(String categoryId, String tagId, Integer status);

    /**
     * 删除文章
     */
    void deleteArticle(String articleId);
}

