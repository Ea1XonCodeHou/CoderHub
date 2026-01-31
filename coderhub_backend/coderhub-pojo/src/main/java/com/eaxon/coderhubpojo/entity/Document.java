package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程文档实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String chapterId;
    private String documentTitle;
    private String documentUrl;
    private String documentType;
    private Long fileSize;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
