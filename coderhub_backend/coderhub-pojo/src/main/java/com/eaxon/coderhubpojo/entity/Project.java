package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目ID (UUID) */
    private String id;
    
    /** 创建者ID */
    private String userId;
    
    /** 项目名称 */
    private String projectName;
    
    /** 项目类型：1-优秀开源项目(官方推荐) 2-个人项目(用户上传) */
    private Integer projectType;
    
    /** 简短描述 */
    private String shortDescription;
    
    /** 详细描述（Markdown格式） */
    private String detailedDescription;
    
    /** Git仓库地址 */
    private String gitUrl;
    
    /** 在线演示地址 */
    private String demoUrl;
    
    /** 演示视频地址 */
    private String videoUrl;
    
    /** 封面图URL */
    private String coverImage;
    
    /** 项目截图（JSON数组格式） */
    private String screenshots;
    
    /** 项目压缩包OSS地址 */
    private String projectFileUrl;
    
    /** 文件大小（字节） */
    private Long fileSize;
    
    /** 项目分类ID */
    private Long categoryId;
    
    /** 难度等级：1-入门 2-初级 3-中级 4-高级 5-专家 */
    private Integer difficultyLevel;
    
    /** 是否开源：0-私有 1-开源 */
    private Integer isOpenSource;
    
    /** 状态：0-已删除 1-正常 2-草稿 */
    private Integer status;
    
    /** 审核状态：0-待审核 1-审核通过 2-审核拒绝 */
    private Integer auditStatus;
    
    /** 审核备注 */
    private String auditRemark;
    
    /** 浏览量 */
    private Integer viewCount;
    
    /** 原作者名称（优秀开源项目用） */
    private String originalAuthor;
    
    /** 原作者角色（优秀开源项目用） */
    private String originalAuthorRole;
    
    /** 原作者头像（优秀开源项目用） */
    private String originalAuthorAvatar;
    
    /** GitHub Star数（优秀项目用） */
    private Integer githubStars;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

