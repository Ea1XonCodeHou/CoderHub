package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tutorial implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String coverImage;
    private String categoryId;
    private Integer difficulty;
    private String instructorName;
    private String instructorAvatar;
    private String instructorDesc;
    private BigDecimal price;
    private Integer status;
    private Long viewCount;
    private Integer likeCount;
    private Integer recommendCount;
    private Integer studentCount;
    private BigDecimal rating;
    private Integer chapterCount;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
