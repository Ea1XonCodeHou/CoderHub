package com.eaxon.coderhubpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章标签关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagRelation implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String articleId;
    private String tagId;
    private LocalDateTime createTime;
}

