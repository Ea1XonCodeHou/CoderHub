package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程章节实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialChapter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tutorialId;
    private Integer chapterOrder;
    private String chapterTitle;
    private String duration;
    private Integer isFree;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
