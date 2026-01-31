package com.eaxon.coderhubserver.service;
import com.eaxon.coderhubpojo.VO.ProjectVO;
import java.util.List;

/**
 * 项目缓存服务接口
 * 用于缓存项目分类和技术栈数据
 */
public interface ProjectCacheService {

    /**
     * 获取所有项目分类（带技术栈）
     * 首次从数据库加载，之后从Redis读取
     */
    List<ProjectVO.CategoryItem> getAllCategories();

    /**
     * 根据分类ID获取技术栈列表
     */
    List<ProjectVO.TechStackItem> getTechStacksByCategoryId(Long categoryId);

    /**
     * 刷新缓存（管理员手动触发或数据变更时调用）
     */
    void refreshCache();

    /**
     * 检查缓存是否存在
     */
    boolean isCacheExists();
}

