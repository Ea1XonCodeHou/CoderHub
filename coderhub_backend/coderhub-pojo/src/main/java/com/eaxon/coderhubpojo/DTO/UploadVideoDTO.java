package com.eaxon.coderhubpojo.DTO;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上传视频DTO
 */
@Data
@ApiModel(description = "上传视频请求数据")
public class UploadVideoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "章节ID", required = true)
    private String chapterId;

    @ApiModelProperty(value = "视频标题", required = true)
    private String videoTitle;

    @ApiModelProperty(value = "视频URL（OSS地址或视频平台外链）", required = true)
    private String videoUrl;

    @ApiModelProperty(value = "视频封面图URL")
    private String coverImage;

    @ApiModelProperty(value = "视频时长（如：45:30）")
    private String duration;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;
}
