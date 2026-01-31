package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程评论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tutorialId;
    private String userId;
    private String content;
    private String parentId;
    private Integer likeCount;
    private LocalDateTime createTime;
}
