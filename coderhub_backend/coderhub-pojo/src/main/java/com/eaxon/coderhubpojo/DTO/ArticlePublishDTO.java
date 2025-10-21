package com.eaxon.coderhubpojo.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章发布DTO
 */
@Data
public class ArticlePublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String categoryId;
    private List<String> tags;
    private Integer isOriginal;
    private Integer status;
}

