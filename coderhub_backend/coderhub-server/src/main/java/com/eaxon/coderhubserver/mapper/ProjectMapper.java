package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper {

    /**
     * 插入项目
     */
    int insert(Project project);

    /**
     * 根据ID查询项目
     */
    Project getById(@Param("id") String id);

    /**
     * 查询个人项目列表（分页）
     */
    List<Project> getPersonalProjects(@Param("projectType") Integer projectType,
                                       @Param("categoryId") Long categoryId,
                                       @Param("isOpenSource") Integer isOpenSource,
                                       @Param("auditStatus") Integer auditStatus,
                                       @Param("keyword") String keyword,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 查询个人项目总数
     */
    Long countPersonalProjects(@Param("projectType") Integer projectType,
                                @Param("categoryId") Long categoryId,
                                @Param("isOpenSource") Integer isOpenSource,
                                @Param("auditStatus") Integer auditStatus,
                                @Param("keyword") String keyword);

    /**
     * 查询用户的项目列表
     */
    List<Project> getByUserId(@Param("userId") String userId,
                               @Param("isOpenSource") Integer isOpenSource,
                               @Param("status") Integer status);

    /**
     * 更新项目
     */
    int update(Project project);

    /**
     * 增加浏览量
     */
    int incrementViewCount(@Param("id") String id);

    /**
     * 删除项目（软删除）
     */
    int softDelete(@Param("id") String id);

    /**
     * 按技术栈搜索项目
     */
    List<Project> searchByTechStack(@Param("techIds") List<Long> techIds,
                                     @Param("projectType") Integer projectType,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);
}

