package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String title;
    private String summary;
    private String contentUrl;
    private String coverImage;
    private String categoryId;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer status;
    private Integer isTop;
    private Integer isOriginal;
    private Integer auditStatus;
    private String auditReason;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishTime;
}

