package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.ProjectDTO;
import com.eaxon.coderhubpojo.VO.ProjectVO;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 创建项目
     */
    String createProject(ProjectDTO.CreateRequest request, String userId);

    /**
     * 获取项目详情
     */
    ProjectVO.Detail getProjectDetail(String projectId, String currentUserId);

    /**
     * 获取个人项目列表（分页）
     */
    ProjectVO.PageResult getPersonalProjects(ProjectDTO.QueryRequest request);

    /**
     * 获取用户的项目列表
     */
    ProjectVO.PageResult getUserProjects(String userId, Integer isOpenSource);

    /**
     * 增加浏览量
     */
    void incrementViewCount(String projectId);

    /**
     * 删除项目
     */
    boolean deleteProject(String projectId, String userId);
}

