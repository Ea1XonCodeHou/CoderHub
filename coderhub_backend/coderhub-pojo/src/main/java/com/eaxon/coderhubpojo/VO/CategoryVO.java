package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类VO（用于返回给前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO implements Serializable {
    private String id;
    private String categoryName;
    private String parentId;
    private String parentName;
    private Integer sortOrder;
    private String description;
    private String icon;
    private Integer status;
    private Integer articleCount;
    private Integer referenceCount;
    private Long viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

