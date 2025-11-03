package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章点赞实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLike implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String articleId;
    private LocalDateTime createTime;
}

