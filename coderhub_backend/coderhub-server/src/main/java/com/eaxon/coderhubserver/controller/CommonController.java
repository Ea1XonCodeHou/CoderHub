package com.eaxon.coderhubserver.controller;

import com.eaxon.coderhubcommon.constant.MessageConstant;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
@Api(tags = "通用工具接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传接口")
    public Result<String> upload(MultipartFile file){
        log.info("开始向阿里云上传文件：{}",file);
        try {
            String fileOriginalName= file.getOriginalFilename();
            //截取原始文件名后缀
            String extennsion=fileOriginalName.substring(fileOriginalName.lastIndexOf('.'));
            String objectName= UUID.randomUUID().toString()+extennsion;
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败：{}",e.toString());
        }
        //参数和接口定义保持一样才可以
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
