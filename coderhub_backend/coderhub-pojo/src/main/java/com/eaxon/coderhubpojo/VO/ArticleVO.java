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
 * 文章VO（列表展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String username;
    private String avatar;
    private String title;
    private String summary;
    private String contentUrl;  // Markdown内容URL
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

    /**
     * 状态：0草稿 1已发布 2审核中 3已下架
     */
    private Integer status;
    
    /**
     * 审核状态：0待审核 1审核通过 2审核拒绝
     */
    private Integer auditStatus;
    private String auditReason;
    
    private LocalDateTime createTime;
    private LocalDateTime publishTime;
}

