package com.eaxon.coderhubpojo.VO;

import com.eaxon.coderhubpojo.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String userId;
    private String username;
    private String avatar;
    private String title;
    private String summary;
    private String contentUrl;
    private String coverImage;
    private String categoryId;
    private String categoryName;
    private List<Tag> tags;
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer isTop;
    private Integer isOriginal;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime publishTime;
}

