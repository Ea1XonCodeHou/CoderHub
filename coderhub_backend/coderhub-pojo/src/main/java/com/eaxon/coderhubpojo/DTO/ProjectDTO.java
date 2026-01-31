package com.eaxon.coderhubpojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目相关DTO
 */
public class ProjectDTO {

    /**
     * 创建项目请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "创建项目请求")
    public static class CreateRequest {
        
        @Schema(description = "项目名称", required = true)
        private String projectName;
        
        @Schema(description = "简短描述")
        private String shortDescription;
        
        @Schema(description = "详细描述（Markdown格式，从README.md解析）")
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
        
        @Schema(description = "项目分类ID", required = true)
        private Long categoryId;
        
        @Schema(description = "难度等级：1-入门 2-初级 3-中级 4-高级 5-专家")
        private Integer difficultyLevel;
        
        @Schema(description = "是否开源：0-私有 1-开源", required = true)
        private Integer isOpenSource;
        
        @Schema(description = "技术栈ID列表", required = true)
        private List<Long> techStackIds;
    }

    /**
     * 项目列表查询请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目列表查询请求")
    public static class QueryRequest {
        
        @Schema(description = "项目类型：1-优秀开源项目 2-个人项目")
        private Integer projectType;
        
        @Schema(description = "分类ID")
        private Long categoryId;
        
        @Schema(description = "技术栈ID")
        private Long techStackId;
        
        @Schema(description = "难度等级")
        private Integer difficultyLevel;
        
        @Schema(description = "是否开源：0-私有 1-开源")
        private Integer isOpenSource;
        
        @Schema(description = "搜索关键词")
        private String keyword;
        
        @Schema(description = "排序方式：latest-最新 hot-最热")
        private String sortBy;
        
        @Schema(description = "页码", defaultValue = "1")
        private Integer page;
        
        @Schema(description = "每页数量", defaultValue = "12")
        private Integer size;
    }
}

