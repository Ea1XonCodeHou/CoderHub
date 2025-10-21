package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagMapper {

    /**
     * 插入标签
     */
    void insert(Tag tag);

    /**
     * 根据名称查询标签
     */
    Tag getByName(String tagName);

    /**
     * 根据ID查询标签
     */
    Tag getById(String id);

    /**
     * 增加标签使用次数
     */
    void incrementUsageCount(String id);

    /**
     * 减少标签使用次数
     */
    void decrementUsageCount(String id);

    /**
     * 根据文章ID查询标签列表
     */
    List<Tag> getByArticleId(String articleId);

    /**
     * 根据ID列表批量查询标签
     */
    List<Tag> getByIds(List<String> ids);

    /**
     * 根据关键词搜索标签（用于自动完成）
     */
    List<Tag> searchByKeyword(String keyword);

    /**
     * 查询热门标签
     */
    List<Tag> getHotTags(Integer limit);

    /**
     * 查询所有标签
     */
    List<Tag> list();
}

