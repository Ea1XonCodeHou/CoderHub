package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.CategoryDTO;
import com.eaxon.coderhubpojo.VO.CategoryVO;
import com.eaxon.coderhubpojo.entity.Category;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.CategoryMapper;
import com.eaxon.coderhubserver.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(String id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    public List<CategoryVO> getRootCategories() {
        List<Category> categories = categoryMapper.findRootCategories();
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getSubCategories(String parentId) {
        List<Category> categories = categoryMapper.findByParentId(parentId);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CategoryVO addCategory(CategoryDTO categoryDTO) {
        // 1. 检查分类名称是否已存在
        Category existCategory = categoryMapper.findByName(categoryDTO.getCategoryName());
        if (existCategory != null) {
            throw new RuntimeException("分类名称已存在");
        }

        // 2. 如果有父分类，检查父分类是否存在
        if (categoryDTO.getParentId() != null && !categoryDTO.getParentId().isEmpty()) {
            Category parentCategory = categoryMapper.findById(categoryDTO.getParentId());
            if (parentCategory == null) {
                throw new RuntimeException("父分类不存在");
            }
        }

        // 3. 构建分类对象
        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .categoryName(categoryDTO.getCategoryName())
                .parentId(categoryDTO.getParentId())
                .sortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0)
                .description(categoryDTO.getDescription())
                .icon(categoryDTO.getIcon())
                .status(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : 1)
                .build();

        // 4. 插入数据库
        categoryMapper.insert(category);

        // 5. 查询并返回
        return convertToVO(categoryMapper.findById(category.getId()));
    }

    @Override
    public CategoryVO updateCategory(CategoryDTO categoryDTO) {
        // 1. 检查分类是否存在
        Category existCategory = categoryMapper.findById(categoryDTO.getId());
        if (existCategory == null) {
            throw new RuntimeException("分类不存在");
        }

        // 2. 检查分类名称是否与其他分类冲突
        Category categoryByName = categoryMapper.findByName(categoryDTO.getCategoryName());
        if (categoryByName != null && !categoryByName.getId().equals(categoryDTO.getId())) {
            throw new RuntimeException("分类名称已存在");
        }

        // 3. 如果修改了父分类，检查父分类是否存在，且不能设置自己为父分类
        if (categoryDTO.getParentId() != null && !categoryDTO.getParentId().isEmpty()) {
            if (categoryDTO.getParentId().equals(categoryDTO.getId())) {
                throw new RuntimeException("不能将自己设置为父分类");
            }
            Category parentCategory = categoryMapper.findById(categoryDTO.getParentId());
            if (parentCategory == null) {
                throw new RuntimeException("父分类不存在");
            }
        }

        // 4. 构建更新对象
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setParentId(categoryDTO.getParentId());
        category.setSortOrder(categoryDTO.getSortOrder());
        category.setDescription(categoryDTO.getDescription());
        category.setIcon(categoryDTO.getIcon());
        category.setStatus(categoryDTO.getStatus());

        // 5. 更新数据库
        categoryMapper.update(category);

        // 6. 查询并返回
        return convertToVO(categoryMapper.findById(categoryDTO.getId()));
    }

    @Override
    public void deleteCategory(String id) {
        // 1. 检查分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 2. 检查是否有子分类
        int subCount = categoryMapper.countByParentId(id);
        if (subCount > 0) {
            throw new RuntimeException("该分类下存在子分类，不能删除");
        }

        // 3. 检查是否有文章（这里暂时跳过，等文章表创建后再添加）
        // if (category.getArticleCount() > 0) {
        //     throw new RuntimeException("该分类下存在文章，不能删除");
        // }

        // 4. 删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        // 1. 检查分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 2. 更新状态
        Category updateCategory = new Category();
        updateCategory.setId(id);
        updateCategory.setStatus(status);
        categoryMapper.update(updateCategory);
    }

    /**
     * 将Category转换为CategoryVO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);

        // 查询父分类名称
        if (category.getParentId() != null) {
            Category parentCategory = categoryMapper.findById(category.getParentId());
            if (parentCategory != null) {
                vo.setParentName(parentCategory.getCategoryName());
            }
        }

        // ✨ 动态计算真实的文章数量
        int realArticleCount = calculateArticleCount(category);
        vo.setArticleCount(realArticleCount);

        return vo;
    }
    
    /**
     * 计算分类的真实文章数量
     * - 子分类：统计该分类下已发布且审核通过的文章数
     * - 父分类：统计所有子分类的文章数之和
     */
    private int calculateArticleCount(Category category) {
        if (category.getParentId() != null) {
            // 子分类：直接统计
            return articleMapper.countByCategoryIdAndStatus(category.getId(), 1, 1);
        } else {
            // 父分类：统计所有子分类的文章数之和
            List<Category> subCategories = categoryMapper.findByParentId(category.getId());
            int totalCount = 0;
            for (Category subCategory : subCategories) {
                totalCount += articleMapper.countByCategoryIdAndStatus(subCategory.getId(), 1, 1);
            }
            return totalCount;
        }
    }
}

