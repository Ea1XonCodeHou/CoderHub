package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.CategoryDTO;
import com.eaxon.coderhubpojo.VO.CategoryVO;

import java.util.List;

public interface CategoryService {
    
    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();
    
    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    CategoryVO getCategoryById(String id);
    
    /**
     * 查询一级分类
     * @return 一级分类列表
     */
    List<CategoryVO> getRootCategories();
    
    /**
     * 根据父ID查询子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryVO> getSubCategories(String parentId);
    
    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return 新增后的分类信息
     */
    CategoryVO addCategory(CategoryDTO categoryDTO);
    
    /**
     * 更新分类
     * @param categoryDTO 分类信息
     * @return 更新后的分类信息
     */
    CategoryVO updateCategory(CategoryDTO categoryDTO);
    
    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(String id);
    
    /**
     * 修改分类状态
     * @param id 分类ID
     * @param status 状态
     */
    void updateStatus(String id, Integer status);
}

