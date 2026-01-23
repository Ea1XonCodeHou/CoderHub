package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.DTO.ProjectDTO;
import com.eaxon.coderhubpojo.VO.ProjectVO;
import com.eaxon.coderhubpojo.entity.Project;
import com.eaxon.coderhubpojo.entity.ProjectCategory;
import com.eaxon.coderhubpojo.entity.ProjectTechRelation;
import com.eaxon.coderhubpojo.entity.TechStackDict;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ProjectCategoryMapper;
import com.eaxon.coderhubserver.mapper.ProjectMapper;
import com.eaxon.coderhubserver.mapper.ProjectTechRelationMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectCategoryMapper categoryMapper;

    @Autowired
    private ProjectTechRelationMapper techRelationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public String createProject(ProjectDTO.CreateRequest request, String userId) {
        String projectId = UUID.randomUUID().toString();

        // 构建项目实体
        Project project = Project.builder()
                .id(projectId)
                .userId(userId)
                .projectName(request.getProjectName())
                .projectType(2) // 个人项目
                .shortDescription(request.getShortDescription())
                .detailedDescription(request.getDetailedDescription())
                .gitUrl(request.getGitUrl())
                .demoUrl(request.getDemoUrl())
                .videoUrl(request.getVideoUrl())
                .coverImage(request.getCoverImage())
                .screenshots(convertListToJson(request.getScreenshots()))
                .projectFileUrl(request.getProjectFileUrl())
                .fileSize(request.getFileSize())
                .categoryId(request.getCategoryId())
                .difficultyLevel(request.getDifficultyLevel())
                .isOpenSource(request.getIsOpenSource())
                .status(1) // 正常状态
                .auditStatus(1) // MVP阶段：所有项目自动审核通过（后续可添加管理员审核功能）
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存项目
        projectMapper.insert(project);
        log.info("项目创建成功，ID: {}, 名称: {}, 类型: {}, 开源: {}, 审核状态: {}", 
                projectId, request.getProjectName(), project.getProjectType(), 
                project.getIsOpenSource(), project.getAuditStatus());

        // 保存技术栈关联
        if (request.getTechStackIds() != null && !request.getTechStackIds().isEmpty()) {
            List<ProjectTechRelation> relations = request.getTechStackIds().stream()
                    .map(techId -> ProjectTechRelation.builder()
                            .projectId(projectId)
                            .techId(techId)
                            .createdAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            techRelationMapper.batchInsert(relations);
            log.info("项目技术栈关联保存成功，共{}个技术栈", relations.size());
        }

        return projectId;
    }

    @Override
    public ProjectVO.Detail getProjectDetail(String projectId, String currentUserId) {
        // 查询项目
        Project project = projectMapper.getById(projectId);
        if (project == null) {
            return null;
        }

        // 检查权限：私有项目只能本人查看
        if (project.getIsOpenSource() == 0 && !project.getUserId().equals(currentUserId)) {
            log.warn("用户{}尝试访问私有项目{}", currentUserId, projectId);
            return null;
        }

        // 查询分类
        ProjectCategory category = categoryMapper.getById(project.getCategoryId());

        // 查询技术栈
        List<TechStackDict> techStacks = techRelationMapper.getTechStacksByProjectId(projectId);

        // 查询作者信息
        User author = userMapper.getUserById(project.getUserId());

        // 构建返回对象
        return ProjectVO.Detail.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .shortDescription(project.getShortDescription())
                .detailedDescription(project.getDetailedDescription())
                .gitUrl(project.getGitUrl())
                .demoUrl(project.getDemoUrl())
                .videoUrl(project.getVideoUrl())
                .coverImage(project.getCoverImage())
                .screenshots(parseJsonToList(project.getScreenshots()))
                .projectFileUrl(project.getProjectFileUrl())
                .fileSize(project.getFileSize())
                .categoryId(project.getCategoryId())
                .categoryName(category != null ? category.getCategoryName() : null)
                .difficultyLevel(project.getDifficultyLevel())
                .isOpenSource(project.getIsOpenSource())
                .viewCount(project.getViewCount())
                .techStacks(techStacks.stream()
                        .map(ts -> ProjectVO.TechStackItem.builder()
                                .id(ts.getId())
                                .techName(ts.getTechName())
                                .build())
                        .collect(Collectors.toList()))
                .author(ProjectVO.AuthorInfo.builder()
                        .userId(author != null ? author.getId() : null)
                        .username(author != null ? author.getUsername() : "未知用户")
                        .avatar(author != null ? author.getAvatar() : null)
                        .build())
                .isOwner(project.getUserId().equals(currentUserId))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    public ProjectVO.PageResult getPersonalProjects(ProjectDTO.QueryRequest request) {
        // 默认值处理
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 12;
        int offset = (page - 1) * size;

        // 使用request中的projectType参数(1-优秀开源项目, 2-个人项目)
        Integer projectType = request.getProjectType() != null ? request.getProjectType() : 2;
        
        log.info("查询项目列表: projectType={}, categoryId={}, keyword={}, page={}, size={}", 
                projectType, request.getCategoryId(), request.getKeyword(), page, size);

        // 查询项目：根据projectType查询对应类型的项目
        List<Project> projects = projectMapper.getPersonalProjects(
                projectType, // 项目类型：1-优秀开源项目 2-个人项目
                request.getCategoryId(),
                1, // 开源
                1, // 审核通过
                request.getKeyword(),
                offset,
                size
        );

        // 查询总数
        Long total = projectMapper.countPersonalProjects(
                projectType, // 使用相同的项目类型
                request.getCategoryId(),
                1,
                1,
                request.getKeyword()
        );

        // 转换为VO
        List<ProjectVO.ListItem> items = projects.stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());

        return ProjectVO.PageResult.builder()
                .list(items)
                .total(total)
                .page(page)
                .size(size)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    @Override
    public ProjectVO.PageResult getUserProjects(String userId, Integer isOpenSource) {
        List<Project> projects = projectMapper.getByUserId(userId, isOpenSource, 1);

        List<ProjectVO.ListItem> items = projects.stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());

        return ProjectVO.PageResult.builder()
                .list(items)
                .total((long) items.size())
                .page(1)
                .size(items.size())
                .totalPages(1)
                .build();
    }

    @Override
    public void incrementViewCount(String projectId) {
        projectMapper.incrementViewCount(projectId);
    }

    @Override
    @Transactional
    public boolean deleteProject(String projectId, String userId) {
        Project project = projectMapper.getById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            return false;
        }

        // 软删除
        projectMapper.softDelete(projectId);
        // 删除技术栈关联
        techRelationMapper.deleteByProjectId(projectId);
        
        log.info("项目删除成功，ID: {}", projectId);
        return true;
    }

    /**
     * 转换为列表项VO
     */
    private ProjectVO.ListItem convertToListItem(Project project) {
        // 查询分类
        ProjectCategory category = categoryMapper.getById(project.getCategoryId());
        
        // 查询技术栈
        List<TechStackDict> techStacks = techRelationMapper.getTechStacksByProjectId(project.getId());
        
        // 构建作者信息：优秀开源项目使用original_author，个人项目使用user_id
        ProjectVO.AuthorInfo authorInfo;
        if (project.getProjectType() != null && project.getProjectType() == 1) {
            // 优秀开源项目：使用original_author字段
            authorInfo = ProjectVO.AuthorInfo.builder()
                    .userId(null)
                    .username(project.getOriginalAuthor() != null ? project.getOriginalAuthor() : "未知作者")
                    .avatar(project.getOriginalAuthorAvatar())
                    .role(project.getOriginalAuthorRole())
                    .build();
        } else {
            // 个人项目：查询用户表
            User author = userMapper.getUserById(project.getUserId());
            authorInfo = ProjectVO.AuthorInfo.builder()
                    .userId(author != null ? author.getId() : null)
                    .username(author != null ? author.getUsername() : "未知用户")
                    .avatar(author != null ? author.getAvatar() : null)
                    .build();
        }

        return ProjectVO.ListItem.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .shortDescription(project.getShortDescription())
                .coverImage(project.getCoverImage())
                .screenshots(parseJsonToList(project.getScreenshots()))
                .gitUrl(project.getGitUrl())
                .demoUrl(project.getDemoUrl())
                .categoryId(project.getCategoryId())
                .categoryName(category != null ? category.getCategoryName() : null)
                .difficultyLevel(project.getDifficultyLevel())
                .isOpenSource(project.getIsOpenSource())
                .viewCount(project.getViewCount())
                .githubStars(project.getGithubStars()) // 添加GitHub Stars
                .techStacks(techStacks.stream()
                        .map(ts -> ProjectVO.TechStackItem.builder()
                                .id(ts.getId())
                                .techName(ts.getTechName())
                                .build())
                        .collect(Collectors.toList()))
                .author(authorInfo)
                .createdAt(project.getCreatedAt())
                .build();
    }

    /**
     * List转JSON字符串
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("转换JSON失败", e);
            return "[]";
        }
    }

    /**
     * JSON字符串转List
     */
    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析JSON失败", e);
            return new ArrayList<>();
        }
    }
}

