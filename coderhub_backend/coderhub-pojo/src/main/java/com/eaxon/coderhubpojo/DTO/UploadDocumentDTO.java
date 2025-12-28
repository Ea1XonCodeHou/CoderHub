package com.eaxon.coderhubpojo.DTO;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传文档DTO
 */
@Data
@Schema(description = "上传文档请求数据")
public class UploadDocumentDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String chapterId;
    private String documentTitle;
    private String documentUrl;
    private String documentType;
    private Long fileSize;
}
