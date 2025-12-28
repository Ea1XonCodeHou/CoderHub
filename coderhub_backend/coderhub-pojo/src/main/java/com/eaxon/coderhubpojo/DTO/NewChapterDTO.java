package com.eaxon.coderhubpojo.DTO;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增章节DTO
 */
@Data
@Schema(description = "新增章节请求数据")
public class NewChapterDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tutorialId;
    private Integer chapterOrder;
    private String chapterTitle;
    private String duration;
    private Integer isFree;
    private Integer status;
}
