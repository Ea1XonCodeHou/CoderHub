package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubcommon.utils.MinioUtil;
import com.eaxon.coderhubpojo.DTO.ProjectDTO;
import com.eaxon.coderhubpojo.VO.ProjectVO;
import com.eaxon.coderhubserver.service.ProjectCacheService;
import com.eaxon.coderhubserver.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 项目管理Controller
 */
@RestController
@RequestMapping("/project")
@Tag(name = "项目管理接口")
@Slf4j
public class ProjectController {

    private static final String PROJECT_FILES_BUCKET = "project-files";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectCacheService projectCacheService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private MinioUtil minioUtil;

    // ==================== 分类和技术栈 ====================

    /**
     * 获取所有项目分类（带技术栈，从Redis缓存读取）
     */
    @GetMapping("/categories")
    @Operation(summary = "获取项目分类和技术栈")
    public Result<List<ProjectVO.CategoryItem>> getCategories() {
        log.info("获取项目分类和技术栈");
        List<ProjectVO.CategoryItem> categories = projectCacheService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 根据分类ID获取技术栈
     */
    @GetMapping("/categories/{categoryId}/tech-stacks")
    @Operation(summary = "根据分类获取技术栈")
    public Result<List<ProjectVO.TechStackItem>> getTechStacksByCategory(@PathVariable Long categoryId) {
        log.info("获取分类{}的技术栈", categoryId);
        List<ProjectVO.TechStackItem> techStacks = projectCacheService.getTechStacksByCategoryId(categoryId);
        return Result.success(techStacks);
    }

    // ==================== 文件上传 ====================

    /**
     * 上传项目图片（封面图/截图）到OSS
     */
    @PostMapping("/upload/image")
    @Operation(summary = "上传项目图片（OSS）")
    public Result<String> uploadImage(MultipartFile file) {
        log.info("上传项目图片：{}", file.getOriginalFilename());
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            
            // 生成文件路径：projects/images/2024/12/29/uuid.jpg
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = "projects/images/" + date + "/" + UUID.randomUUID() + extension;
            
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(url);
        } catch (Exception e) {
            log.error("项目图片上传失败", e);
            return Result.error("图片上传失败");
        }
    }

    /**
     * 上传README.md文件，解析内容返回
     */
    @PostMapping("/upload/readme")
    @Operation(summary = "上传README.md文件")
    public Result<Map<String, String>> uploadReadme(MultipartFile file) {
        log.info("上传README文件：{}", file.getOriginalFilename());
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // 读取文件内容
            String content = reader.lines().collect(Collectors.joining("\n"));
            
            Map<String, String> result = new HashMap<>();
            result.put("content", content);
            result.put("filename", file.getOriginalFilename());
            
            log.info("README解析成功，内容长度：{}", content.length());
            return Result.success(result);
        } catch (Exception e) {
            log.error("README解析失败", e);
            return Result.error("README解析失败");
        }
    }

    /**
     * 上传项目压缩包到MinIO
     */
    @PostMapping("/upload/file")
    @Operation(summary = "上传项目压缩包（MinIO）")
    public Result<Map<String, Object>> uploadProjectFile(MultipartFile file) {
        log.info("上传项目压缩包：{}，大小：{} MB", 
                file.getOriginalFilename(), 
                file.getSize() / 1024.0 / 1024.0);
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            
            // 生成文件路径：2024/12/29/uuid.zip
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = date + "/" + UUID.randomUUID() + extension;
            
            // 设置ContentType
            String contentType = "application/zip";
            if (extension.equalsIgnoreCase(".rar")) {
                contentType = "application/x-rar-compressed";
            } else if (extension.equalsIgnoreCase(".7z")) {
                contentType = "application/x-7z-compressed";
            }
            
            // 上传到MinIO的project-files bucket
            String url = minioUtil.uploadToBucket(
                    file.getInputStream(),
                    objectName,
                    contentType,
                    file.getSize(),
                    PROJECT_FILES_BUCKET
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("size", file.getSize());
            result.put("filename", originalName);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("项目压缩包上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    // ==================== 项目CRUD ====================

    /**
     * 创建项目
     */
    @PostMapping
    @Operation(summary = "创建项目")
    public Result<String> createProject(@RequestBody ProjectDTO.CreateRequest request) {
        String userId = BaseContext.getCurrentId();
        log.info("用户{}创建项目：{}", userId, request.getProjectName());
        
        String projectId = projectService.createProject(request, userId);
        return Result.success(projectId);
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    public Result<ProjectVO.Detail> getProjectDetail(@PathVariable String id) {
        String userId = BaseContext.getCurrentId();
        log.info("获取项目详情：{}", id);
        
        // 增加浏览量
        projectService.incrementViewCount(id);
        
        ProjectVO.Detail detail = projectService.getProjectDetail(id, userId);
        if (detail == null) {
            return Result.error("项目不存在或无权访问");
        }
        return Result.success(detail);
    }

    /**
     * 获取优秀开源项目列表（平台展示）
     */
    @GetMapping("/showcase")
    @Operation(summary = "获取优秀开源项目列表")
    public Result<ProjectVO.PageResult> getShowcaseProjects(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        
        log.info("获取优秀开源项目列表，分类：{}，关键词：{}，排序：{}", categoryId, keyword, sortBy);
        
        ProjectDTO.QueryRequest request = ProjectDTO.QueryRequest.builder()
                .projectType(1) // 优秀开源项目
                .categoryId(categoryId)
                .keyword(keyword)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        
        ProjectVO.PageResult result = projectService.getPersonalProjects(request);
        return Result.success(result);
    }

    /**
     * 获取个人项目列表（社区展示）
     */
    @GetMapping("/community")
    @Operation(summary = "获取个人项目列表（社区）")
    public Result<ProjectVO.PageResult> getCommunityProjects(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        
        log.info("获取社区项目列表，分类：{}，关键词：{}，排序：{}", categoryId, keyword, sortBy);
        
        ProjectDTO.QueryRequest request = ProjectDTO.QueryRequest.builder()
                .projectType(2) // 个人项目
                .categoryId(categoryId)
                .keyword(keyword)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();
        
        ProjectVO.PageResult result = projectService.getPersonalProjects(request);
        return Result.success(result);
    }

    /**
     * 获取我的项目列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的项目列表")
    public Result<ProjectVO.PageResult> getMyProjects(
            @RequestParam(required = false) Integer isOpenSource) {
        
        String userId = BaseContext.getCurrentId();
        log.info("获取用户{}的项目列表", userId);
        
        ProjectVO.PageResult result = projectService.getUserProjects(userId, isOpenSource);
        return Result.success(result);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    public Result<Void> deleteProject(@PathVariable String id) {
        String userId = BaseContext.getCurrentId();
        log.info("用户{}删除项目：{}", userId, id);
        
        boolean success = projectService.deleteProject(id, userId);
        if (success) {
            return Result.success(null);
        }
        return Result.error("删除失败，项目不存在或无权操作");
    }

    /**
     * 刷新分类缓存（管理员用）
     */
    @PostMapping("/cache/refresh")
    @Operation(summary = "刷新分类缓存")
    public Result<Void> refreshCache() {
        log.info("刷新项目分类缓存");
        projectCacheService.refreshCache();
        return Result.success(null);
    }
}

