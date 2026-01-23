package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目分类字典实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;
    
    /** 分类名称 */
    private String categoryName;
    
    /** 是否启用：0-禁用 1-启用 */
    private Integer isActive;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

