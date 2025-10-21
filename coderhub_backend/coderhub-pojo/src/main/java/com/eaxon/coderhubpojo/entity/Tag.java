package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tagName;
    private Integer usageCount;
    private Integer referenceCount;
    private Long viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

