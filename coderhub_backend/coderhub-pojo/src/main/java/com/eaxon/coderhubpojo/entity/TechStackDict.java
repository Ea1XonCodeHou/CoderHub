package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技术栈字典实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechStackDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 技术栈ID */
    private Long id;
    
    /** 技术名称 */
    private String techName;
    
    /** 是否启用：0-禁用 1-启用 */
    private Integer isActive;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}

