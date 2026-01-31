package com.eaxon.coderhubcommon.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * MinIO配置属性类
 * 用于大文件存储（视频、大型文档等）
 */
@Component
@ConfigurationProperties(prefix = "coder.minio")
@Data
public class MinioProperties {
    /**
     * MinIO服务器地址（API端口）
     * 例如：http://127.0.0.1:9090
     */
    private String endpoint;

    /**
     * MinIO公共访问地址（前端访问路径）
     * 生产环境建议通过 Nginx 代理，例如：/minio
     */
    private String publicEndpoint;

    /**
     * MinIO访问密钥（用户名）
     */
    private String accessKey;

    /**
     * MinIO密钥（密码）
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName;
}
