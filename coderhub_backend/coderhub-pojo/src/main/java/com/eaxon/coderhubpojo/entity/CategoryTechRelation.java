package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目分类与技术栈关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTechRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;
    
    /** 项目分类ID */
    private Long categoryId;
    
    /** 技术栈ID */
    private Long techId;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}

