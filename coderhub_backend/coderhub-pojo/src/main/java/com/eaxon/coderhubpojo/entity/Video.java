package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程视频实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String chapterId;
    private String videoTitle;
    private String videoUrl;
    private String coverImage;
    private String duration;
    private Long fileSize;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
