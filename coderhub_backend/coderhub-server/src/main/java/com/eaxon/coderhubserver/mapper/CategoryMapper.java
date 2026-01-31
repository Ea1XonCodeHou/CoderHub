package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 查询所有分类
     * @return 分类列表
     */
    @Select("SELECT * FROM coder_hub.category ORDER BY sort_order ASC, create_time DESC")
    List<Category> findAll();

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    @Select("SELECT * FROM coder_hub.category WHERE id = #{id}")
    Category findById(String id);

    /**
     * 根据父ID查询子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM coder_hub.category WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<Category> findByParentId(String parentId);

    /**
     * 查询一级分类（父ID为NULL）
     * @return 一级分类列表
     */
    @Select("SELECT * FROM coder_hub.category WHERE parent_id IS NULL ORDER BY sort_order ASC")
    List<Category> findRootCategories();

    /**
     * 根据分类名称查询
     * @param categoryName 分类名称
     * @return 分类信息
     */
    @Select("SELECT * FROM coder_hub.category WHERE category_name = #{categoryName}")
    Category findByName(String categoryName);

    /**
     * 插入分类
     * @param category 分类信息
     */
    @Insert("INSERT INTO coder_hub.category(id, category_name, parent_id, sort_order, description, icon, status) " +
            "VALUES(#{id}, #{categoryName}, #{parentId}, #{sortOrder}, #{description}, #{icon}, #{status})")
    void insert(Category category);

    /**
     * 更新分类（动态更新，通过XML配置）
     * @param category 分类信息
     */
    void update(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     */
    @Delete("DELETE FROM coder_hub.category WHERE id = #{id}")
    void deleteById(String id);

    /**
     * 统计子分类数量
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM coder_hub.category WHERE parent_id = #{parentId}")
    int countByParentId(String parentId);

    /**
     * 增加文章数量
     * @param categoryId 分类ID
     */
    @Update("UPDATE coder_hub.category SET article_count = article_count + 1 WHERE id = #{categoryId}")
    void incrementArticleCount(String categoryId);

    /**
     * 减少文章数量
     * @param categoryId 分类ID
     */
    @Update("UPDATE coder_hub.category SET article_count = article_count - 1 WHERE id = #{categoryId} AND article_count > 0")
    void decrementArticleCount(String categoryId);
}

