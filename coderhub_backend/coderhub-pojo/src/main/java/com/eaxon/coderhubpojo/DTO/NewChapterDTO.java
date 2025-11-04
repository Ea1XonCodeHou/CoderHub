package com.eaxon.coderhubpojo.DTO;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新增章节DTO
 */
@Data
@ApiModel(description = "新增章节请求数据")
public class NewChapterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "教程ID", required = true)
    private String tutorialId;

    @ApiModelProperty(value = "章节排序（数字越小越靠前）", required = true)
    private Integer chapterOrder;

    @ApiModelProperty(value = "章节标题", required = true)
    private String chapterTitle;

    @ApiModelProperty(value = "本章时长（如：45分钟）")
    private String duration;

    @ApiModelProperty(value = "是否免费试看：0-否 1-是", required = true)
    private Integer isFree;

    @ApiModelProperty(value = "状态：0-未发布 1-已发布", required = true)
    private Integer status;
}
