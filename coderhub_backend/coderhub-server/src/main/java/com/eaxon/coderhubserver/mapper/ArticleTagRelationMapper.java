package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.ArticleTagRelation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章标签关联Mapper接口
 */
@Mapper
public interface ArticleTagRelationMapper {

    /**
     * 插入关联关系
     */
    void insert(ArticleTagRelation relation);

    /**
     * 根据文章ID删除关联关系
     */
    void deleteByArticleId(String articleId);

    /**
     * 根据文章ID查询关联关系
     */
    List<ArticleTagRelation> getByArticleId(String articleId);

    /**
     * 批量插入关联关系
     */
    void batchInsert(List<ArticleTagRelation> relations);
}

