package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.ProjectCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目分类Mapper接口
 */
@Mapper
public interface ProjectCategoryMapper {

    /**
     * 查询所有启用的分类
     */
    List<ProjectCategory> getAllActive();

    /**
     * 根据ID查询分类
     */
    ProjectCategory getById(@Param("id") Long id);
}

