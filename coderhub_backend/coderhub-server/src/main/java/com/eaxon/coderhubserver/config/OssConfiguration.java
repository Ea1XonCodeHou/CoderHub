package com.eaxon.coderhubserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eaxon.coderhubcommon.properties.AliOssProperties;
import com.eaxon.coderhubcommon.properties.MinioProperties;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubcommon.utils.MinioUtil;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OssConfiguration {
    
    /**
     * 阿里云OSS工具类 - 用于小文件（图片、文档）
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("配置阿里云文件上传工具类对象：{}",aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());
    }

    /**
     * MinIO工具类 - 用于大文件（视频、大型文档）
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties minioProperties){
        log.info("配置MinIO文件上传工具类对象：{}", minioProperties);
        return new MinioUtil(
                minioProperties.getEndpoint(),
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey(),
                minioProperties.getBucketName()
        );
    }
}
