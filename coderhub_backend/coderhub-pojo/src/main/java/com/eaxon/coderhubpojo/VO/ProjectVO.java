package com.eaxon.coderhubpojo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目相关VO
 */
public class ProjectVO {

    /**
     * 项目列表项VO（卡片展示用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目列表项")
    public static class ListItem {
        
        @Schema(description = "项目ID")
        private String id;
        
        @Schema(description = "项目名称")
        private String projectName;
        
        @Schema(description = "简短描述")
        private String shortDescription;
        
        @Schema(description = "封面图URL")
        private String coverImage;
        
        @Schema(description = "项目截图列表")
        private List<String> screenshots;
        
        @Schema(description = "Git仓库地址")
        private String gitUrl;
        
        @Schema(description = "在线演示地址")
        private String demoUrl;
        
        @Schema(description = "分类ID")
        private Long categoryId;
        
        @Schema(description = "分类名称")
        private String categoryName;
        
        @Schema(description = "难度等级")
        private Integer difficultyLevel;
        
        @Schema(description = "是否开源")
        private Integer isOpenSource;
        
        @Schema(description = "浏览量")
        private Integer viewCount;
        
        @Schema(description = "GitHub Stars数（优秀开源项目专用）")
        private Integer githubStars;
        
        @Schema(description = "技术栈列表")
        private List<TechStackItem> techStacks;
        
        @Schema(description = "作者信息")
        private AuthorInfo author;
        
        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }

    /**
     * 项目详情VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目详情")
    public static class Detail {
        
        @Schema(description = "项目ID")
        private String id;
        
        @Schema(description = "项目名称")
        private String projectName;
        
        @Schema(description = "简短描述")
        private String shortDescription;
        
        @Schema(description = "详细描述（Markdown格式）")
        private String detailedDescription;
        
        @Schema(description = "Git仓库地址")
        private String gitUrl;
        
        @Schema(description = "在线演示地址")
        private String demoUrl;
        
        @Schema(description = "演示视频地址")
        private String videoUrl;
        
        @Schema(description = "封面图URL")
        private String coverImage;
        
        @Schema(description = "项目截图列表")
        private List<String> screenshots;
        
        @Schema(description = "项目压缩包URL")
        private String projectFileUrl;
        
        @Schema(description = "文件大小（字节）")
        private Long fileSize;
        
        @Schema(description = "分类ID")
        private Long categoryId;
        
        @Schema(description = "分类名称")
        private String categoryName;
        
        @Schema(description = "难度等级")
        private Integer difficultyLevel;
        
        @Schema(description = "是否开源")
        private Integer isOpenSource;
        
        @Schema(description = "浏览量")
        private Integer viewCount;
        
        @Schema(description = "技术栈列表")
        private List<TechStackItem> techStacks;
        
        @Schema(description = "作者信息")
        private AuthorInfo author;
        
        @Schema(description = "是否是当前用户的项目")
        private Boolean isOwner;
        
        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
        
        @Schema(description = "更新时间")
        private LocalDateTime updatedAt;
    }

    /**
     * 技术栈项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "技术栈项")
    public static class TechStackItem {
        
        @Schema(description = "技术栈ID")
        private Long id;
        
        @Schema(description = "技术名称")
        private String techName;
    }

    /**
     * 作者信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者信息")
    public static class AuthorInfo {
        
        @Schema(description = "用户ID")
        private String userId;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "头像")
        private String avatar;
        
        @Schema(description = "角色/职位（优秀开源项目专用）")
        private String role;
    }

    /**
     * 项目分类VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目分类")
    public static class CategoryItem {
        
        @Schema(description = "分类ID")
        private Long id;
        
        @Schema(description = "分类名称")
        private String categoryName;
        
        @Schema(description = "该分类下可用的技术栈列表")
        private List<TechStackItem> techStacks;
    }

    /**
     * 分页结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页结果")
    public static class PageResult {
        
        @Schema(description = "项目列表")
        private List<ListItem> list;
        
        @Schema(description = "总数")
        private Long total;
        
        @Schema(description = "当前页")
        private Integer page;
        
        @Schema(description = "每页数量")
        private Integer size;
        
        @Schema(description = "总页数")
        private Integer totalPages;
    }
}

