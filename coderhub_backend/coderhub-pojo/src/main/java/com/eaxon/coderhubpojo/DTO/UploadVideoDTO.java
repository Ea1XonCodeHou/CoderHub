package com.eaxon.coderhubpojo.DTO;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传视频DTO
 */
@Data
@Schema(description = "上传视频请求数据")
public class UploadVideoDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String chapterId;
    private String videoTitle;
    private String videoUrl;

    private String coverImage;

    private String duration;
    private Long fileSize;
}
