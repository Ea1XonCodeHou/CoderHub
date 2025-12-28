package com.eaxon.coderhubserver.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eaxon.coderhubcommon.constant.MessageConstant;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubcommon.utils.MinioUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/common")
@Tag(name = "通用工具接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 文件上传接口（阿里云OSS）
     * 适用于小文件：图片、PDF文档等
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传接口（OSS）")
    public Result<String> upload(MultipartFile file){
        log.info("开始向阿里云上传文件：{}",file);
        try {
            String fileOriginalName= file.getOriginalFilename();
            //截取原始文件名后缀
            String extension=fileOriginalName.substring(fileOriginalName.lastIndexOf('.'));
            String objectName= UUID.randomUUID().toString()+extension;
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败：{}",e.toString());
        }
        //参数和接口定义保持一样才可以
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

    /**
     * 文件上传接口（MinIO）
     * 适用于大文件：视频、大型文档等
     */
    @PostMapping("/upload/minio")
    @Operation(summary = "大文件上传接口（MinIO）")
    public Result<String> uploadToMinio(MultipartFile file){
        log.info("开始向MinIO上传大文件：{}, 大小：{} MB", 
                file.getOriginalFilename(), 
                file.getSize() / 1024.0 / 1024.0);
        
        try {
            String fileOriginalName = file.getOriginalFilename();
            // 截取文件扩展名
            String extension = fileOriginalName.substring(fileOriginalName.lastIndexOf('.'));
            
            // 生成文件路径：video/2024/11/07/uuid.mp4
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = "video/" + date + "/" + UUID.randomUUID().toString() + extension;
            
            // 判断文件类型
            String contentType = file.getContentType();
            if (contentType == null) {
                // 根据扩展名设置默认类型
                if (extension.equalsIgnoreCase(".mp4")) {
                    contentType = "video/mp4";
                } else if (extension.equalsIgnoreCase(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (extension.equalsIgnoreCase(".mov")) {
                    contentType = "video/quicktime";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            
            // 上传到MinIO
            String filePath = minioUtil.upload(
                    file.getInputStream(), 
                    objectName, 
                    contentType, 
                    file.getSize()
            );
            
            log.info("MinIO上传成功，URL：{}", filePath);
            return Result.success(filePath);
            
        } catch (IOException e) {
            e.printStackTrace();
            log.error("MinIO文件上传失败：{}", e.toString());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
