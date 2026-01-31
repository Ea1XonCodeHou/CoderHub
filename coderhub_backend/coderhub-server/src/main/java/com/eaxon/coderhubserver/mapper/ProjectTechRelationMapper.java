package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.ProjectTechRelation;
import com.eaxon.coderhubpojo.entity.TechStackDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目与技术栈关联Mapper接口
 */
@Mapper
public interface ProjectTechRelationMapper {

    /**
     * 批量插入项目技术栈关联
     */
    int batchInsert(@Param("list") List<ProjectTechRelation> list);

    /**
     * 根据项目ID查询关联的技术栈
     */
    List<TechStackDict> getTechStacksByProjectId(@Param("projectId") String projectId);

    /**
     * 根据项目ID删除关联
     */
    int deleteByProjectId(@Param("projectId") String projectId);
}

